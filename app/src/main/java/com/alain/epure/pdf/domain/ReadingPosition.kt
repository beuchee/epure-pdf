package com.alain.epure.pdf.domain

// Share of a page already scrolled past, from 0 (top) to 1 (bottom).
// It is kept as a fraction so the position survives a rotation, when page heights change (SPEC F-2.6).
fun pageFraction(scrolledPx: Int, pageHeightPx: Int): Float {
    if (pageHeightPx <= 0) return 0f
    return (scrolledPx.toFloat() / pageHeightPx).coerceIn(0f, 1f)
}
