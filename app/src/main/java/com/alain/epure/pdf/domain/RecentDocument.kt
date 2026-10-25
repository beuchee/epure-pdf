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

// Same file under another URI: the display name is the only identification (DEC-0023).
fun isSameFile(saved: RecentDocument, name: String): Boolean = saved.name == name

// Page to open at: the saved page, or page 1 when the document is now shorter (SPEC F-5.5).
fun resumePage(savedPage: Int, pageCount: Int): Int =
    if (savedPage in 0 until pageCount) savedPage else 0
