package com.alain.epure.pdf

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import com.alain.epure.pdf.domain.LruByteCache
import com.alain.epure.pdf.system.PdfRendererEngine

// Builds the app's dependencies by hand: no dependency injection framework (DEC-0004).
class AppContainer(val context: Context) {
    val engine = PdfRendererEngine(temporaryDir = File(context.cacheDir, TEMPORARY_DIR))

    // Page bitmaps, bounded in bytes. The budget is a provisional eighth of the app's
    // memory (SPEC T-6, to be measured in session 5a).
    val pageCache = LruByteCache<PageKey, Bitmap>(
        maxBytes = Runtime.getRuntime().maxMemory() / 8,
        sizeOf = { it.allocationByteCount }
    )
}

data class PageKey(val page: Int, val widthPx: Int)

private const val TEMPORARY_DIR = "opened"
