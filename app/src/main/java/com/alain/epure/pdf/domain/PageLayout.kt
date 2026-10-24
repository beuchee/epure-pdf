package com.alain.epure.pdf.domain

import kotlin.math.roundToInt

// Height in pixels of a page drawn at the given width, keeping its proportions (SPEC F-2.3).
fun pageHeightPx(widthPx: Int, pageWidth: Float, pageHeight: Float): Int {
    require(pageWidth > 0f) { "page width must be positive" }
    return (widthPx * pageHeight / pageWidth).roundToInt().coerceAtLeast(1)
}
