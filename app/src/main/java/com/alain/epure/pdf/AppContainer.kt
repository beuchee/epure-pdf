package com.alain.epure.pdf

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import com.alain.epure.pdf.data.BookmarkStore
import com.alain.epure.pdf.data.RecentStore
import com.alain.epure.pdf.data.SettingsStore
import com.alain.epure.pdf.domain.LruByteCache
import com.alain.epure.pdf.domain.Tile
import com.alain.epure.pdf.system.PdfRendererEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Builds the app's dependencies by hand: no dependency injection framework (DEC-0004).
class AppContainer(val context: Context) {
    val engine = PdfRendererEngine(temporaryDir = File(context.cacheDir, TEMPORARY_DIR))

    val recents = RecentStore(context)

    val bookmarks = BookmarkStore(context)

    val settings = SettingsStore(context)

    // Lives as long as the process, so a last position is written even after the screen is gone.
    // One writer at a time, in the order the writes were requested: the latest position always wins.
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    // Page bitmaps, bounded in bytes. The budget is a provisional eighth of the app's
    // memory (SPEC T-6, to be measured in session 5a).
    val pageCache = LruByteCache<PageKey, Bitmap>(
        maxBytes = Runtime.getRuntime().maxMemory() / 8,
        sizeOf = { it.allocationByteCount }
    )
}

// A whole page (tile = null) or one tile of it, at a given width (SPEC T-6, DEC-0040).
data class PageKey(val page: Int, val widthPx: Int, val tile: Tile? = null)

private const val TEMPORARY_DIR = "opened"
