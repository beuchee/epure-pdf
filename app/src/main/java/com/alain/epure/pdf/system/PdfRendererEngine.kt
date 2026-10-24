package com.alain.epure.pdf.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import androidx.core.graphics.createBitmap
import com.alain.epure.pdf.domain.pageHeightPx
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Engine based on the platform PdfRenderer (DEC-0015).
// PdfRenderer accepts one open page at a time, so every call goes through one lock.
class PdfRendererEngine(private val temporaryDir: File) : PdfEngine {
    override suspend fun open(context: Context, uri: Uri): OpenResult =
        withContext(Dispatchers.IO) {
            val descriptor = try {
                context.contentResolver.openFileDescriptor(uri, "r")
            } catch (e: FileNotFoundException) {
                null
            } catch (e: SecurityException) {
                return@withContext OpenResult.Failed(OpenError.AccessRevoked)
            }
            if (descriptor == null) return@withContext OpenResult.Failed(OpenError.NotFound)

            // PdfRenderer needs to seek. Some sources (attachments, downloads in progress)
            // give a stream that cannot seek: copy it to a temporary file (SPEC F-1.4, DEC-0022).
            val temporary: File? = if (isSeekable(descriptor)) {
                null
            } else {
                try {
                    copyToTemporaryFile(descriptor)
                } catch (e: IOException) {
                    return@withContext OpenResult.Failed(OpenError.Unreadable)
                }
            }
            val readable = if (temporary == null) descriptor else ParcelFileDescriptor.open(temporary, ParcelFileDescriptor.MODE_READ_ONLY)

            try {
                OpenResult.Opened(RendererDocument(PdfRenderer(readable), readable, temporary))
            } catch (e: SecurityException) {
                // Without a password, PdfRenderer refuses an encrypted file this way.
                readable.close()
                temporary?.delete()
                OpenResult.Failed(OpenError.PasswordRequired)
            } catch (e: Exception) {
                readable.close()
                temporary?.delete()
                OpenResult.Failed(OpenError.Unreadable)
            }
        }

    // Removes copies left by an earlier run. Called once when the app starts.
    fun clearTemporaryCopies() {
        temporaryDir.deleteRecursively()
    }

    private fun isSeekable(descriptor: ParcelFileDescriptor): Boolean = try {
        Os.lseek(descriptor.fileDescriptor, 0, OsConstants.SEEK_CUR)
        true
    } catch (e: ErrnoException) {
        false
    }

    private fun copyToTemporaryFile(descriptor: ParcelFileDescriptor): File {
        temporaryDir.mkdirs()
        val file = File(temporaryDir, "${UUID.randomUUID()}.pdf")
        // AutoCloseInputStream closes the descriptor once the copy is done.
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }
}

private class RendererDocument(
    private val renderer: PdfRenderer,
    private val descriptor: ParcelFileDescriptor,
    private val temporaryFile: File?
) : PdfDocument {
    private val lock = Any()
    private var closed = false

    override val pageCount: Int = renderer.pageCount

    // A broken page reports A4 here; its render then fails and shows "Page illisible".
    override suspend fun pageSize(index: Int): PageSize = withContext(Dispatchers.IO) {
        synchronized(lock) {
            try {
                renderer.openPage(index).use { page -> PageSize(page.width.toFloat(), page.height.toFloat()) }
            } catch (e: Exception) {
                PageSize(A4_WIDTH, A4_HEIGHT)
            }
        }
    }

    override suspend fun render(index: Int, widthPx: Int): Bitmap? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            if (closed) return@withContext null
            try {
                renderer.openPage(index).use { page ->
                    val heightPx = pageHeightPx(widthPx, page.width.toFloat(), page.height.toFloat())
                    val bitmap = createBitmap(widthPx, heightPx)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            renderer.close()
            descriptor.close()
            // The temporary copy only lives as long as the document is open.
            temporaryFile?.delete()
        }
    }
}

private const val A4_WIDTH = 595f
private const val A4_HEIGHT = 842f
