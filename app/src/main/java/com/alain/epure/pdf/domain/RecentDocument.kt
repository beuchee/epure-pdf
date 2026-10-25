package com.alain.epure.pdf.domain

// The last document opened, with its reading position (SPEC F-4, F-5, DEC-0024, DEC-0026).
data class RecentDocument(
    val uri: String,
    val name: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val page: Int,
    val fraction: Float
)

// Same file under another URI: name and size are the only identification (DEC-0023).
// An unknown size (-1) never matches, so an unidentified file starts from page 1.
fun isSameFile(saved: RecentDocument, name: String, sizeBytes: Long): Boolean =
    sizeBytes >= 0 && saved.name == name && saved.sizeBytes == sizeBytes

// Page to open at: the saved page, or page 1 when the document is now shorter (SPEC F-5.5).
fun resumePage(savedPage: Int, pageCount: Int): Int =
    if (savedPage in 0 until pageCount) savedPage else 0
