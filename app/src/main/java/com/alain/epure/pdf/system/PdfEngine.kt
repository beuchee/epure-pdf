package com.alain.epure.pdf.system

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

// The rest of the app only talks to these types, never to PdfRenderer directly (SPEC T-12).
// The engine was chosen in DEC-0015.
interface PdfEngine {
    // False when the engine cannot take a password: encrypted files then cannot be opened (SPEC F-14.1).
    val passwordSupported: Boolean

    // The password is used for this call only; it is never stored (SPEC F-14.1).
    suspend fun open(context: Context, uri: Uri, password: String? = null): OpenResult
}

sealed interface OpenResult {
    class Opened(val document: PdfDocument) : OpenResult
    class Failed(val error: OpenError) : OpenResult
}

enum class OpenError {
    NotFound,
    AccessRevoked,
    PasswordRequired,
    WrongPassword,
    Unreadable
}

// Page size in points (1/72 inch).
data class PageSize(val width: Float, val height: Float)

// A rectangle as fractions of the page width and height, from 0 to 1, top-left origin.
data class PageRect(val left: Float, val top: Float, val right: Float, val bottom: Float)

interface PdfDocument {
    val pageCount: Int

    // Text search needs Android 16 and later (DEC-0015); false elsewhere.
    val searchable: Boolean

    suspend fun pageSize(index: Int): PageSize

    // Returns null when the page cannot be rendered (SPEC F-14.2).
    suspend fun render(index: Int, widthPx: Int): Bitmap?

    // One list of rectangles per occurrence of the text on the page. Null when the page cannot be searched.
    suspend fun searchPage(index: Int, query: String): List<List<PageRect>>?

    // True when the page has a text layer. A scanned page has none (SPEC F-11.6).
    suspend fun hasText(index: Int): Boolean

    // Safe to call from any thread; waits for a render in progress.
    fun close()
}
