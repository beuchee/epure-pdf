package com.alain.epure.pdf.system

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

// The rest of the app only talks to these types, never to PdfRenderer directly (SPEC T-12).
// The engine was chosen in DEC-0015.
interface PdfEngine {
    suspend fun open(context: Context, uri: Uri): OpenResult
}

sealed interface OpenResult {
    class Opened(val document: PdfDocument) : OpenResult
    class Failed(val error: OpenError) : OpenResult
}

enum class OpenError {
    NotFound,
    AccessRevoked,
    PasswordRequired,
    Unreadable
}

// Page size in points (1/72 inch).
data class PageSize(val width: Float, val height: Float)

interface PdfDocument {
    val pageCount: Int

    suspend fun pageSize(index: Int): PageSize

    // Returns null when the page cannot be rendered (SPEC F-14.2).
    suspend fun render(index: Int, widthPx: Int): Bitmap?

    // Safe to call from any thread; waits for a render in progress.
    fun close()
}
