package com.alain.epure.pdf.domain

// Part of one page that is on screen, in pixels.
data class VisiblePage(val index: Int, val visiblePx: Int)

// Page shown in the indicator: the one that takes most of the screen height (SPEC F-7.1).
// A tie goes to the earlier page. Null when no page is on screen.
fun currentPage(visible: List<VisiblePage>): Int? =
    visible.filter { it.visiblePx > 0 }
        .minWithOrNull(compareBy<VisiblePage> { -it.visiblePx }.thenBy { it.index })
        ?.index

// Number typed in « Aller à la page »: from 1 to the page count, or null when it is not valid (SPEC F-7.2).
fun parsePageNumber(text: String, pageCount: Int): Int? =
    text.trim().toIntOrNull()?.takeIf { it in 1..pageCount }
