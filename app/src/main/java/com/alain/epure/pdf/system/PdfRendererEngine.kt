package com.alain.epure.pdf.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import androidx.core.graphics.createBitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.alain.epure.pdf.domain.pageHeightPx
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Engine based on the platform PdfRenderer (DEC-0015).
// PdfRenderer accepts one open page at a time, so every call goes through one lock.
class PdfRendererEngine : PdfEngine {
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

            try {
                OpenResult.Opened(RendererDocument(PdfRenderer(descriptor), descriptor))
            } catch (e: SecurityException) {
                // Without a password, PdfRenderer refuses an encrypted file this way.
                descriptor.close()
                OpenResult.Failed(OpenError.PasswordRequired)
            } catch (e: Exception) {
                descriptor.close()
                OpenResult.Failed(OpenError.Unreadable)
            }
        }
}

private class RendererDocument(
    private val renderer: PdfRenderer,
    private val descriptor: ParcelFileDescriptor
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
        }
    }
}

private const val A4_WIDTH = 595f
private const val A4_HEIGHT = 842f
