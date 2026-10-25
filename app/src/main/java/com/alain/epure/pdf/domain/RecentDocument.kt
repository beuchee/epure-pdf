package com.alain.epure.pdf.domain

// The last document opened, with its reading position (SPEC F-4, F-5, DEC-0024, DEC-0026).
// Only the URI, the name, the page count and the position are kept (SPEC T-2).
data class RecentDocument(
    val uri: String,
    val name: String,
    val pageCount: Int,
    val page: Int,
    val fraction: Float
)

// Documents kept when the optional list is on (DEC-0024); without it, only the last one.
const val RECENT_LIMIT = 10
const val SINGLE_RECENT = 1

fun limitFor(keepList: Boolean): Int = if (keepList) RECENT_LIMIT else SINGLE_RECENT

// Same file under another URI: the display name is the only identification (DEC-0023).
fun isSameFile(saved: RecentDocument, name: String): Boolean = saved.name == name

// Puts the document first, replacing its earlier entry (same URI or same name), then keeps the limit.
fun withRecent(recents: List<RecentDocument>, document: RecentDocument, keepList: Boolean): List<RecentDocument> {
    val others = recents.filterNot { it.uri == document.uri || isSameFile(it, document.name) }
    return (listOf(document) + others).take(limitFor(keepList))
}

// Page to open at: the saved page, or page 1 when the document is now shorter (SPEC F-5.5).
fun resumePage(savedPage: Int, pageCount: Int): Int =
    if (savedPage in 0 until pageCount) savedPage else 0
