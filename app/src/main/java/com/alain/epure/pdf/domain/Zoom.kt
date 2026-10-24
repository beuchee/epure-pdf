package com.alain.epure.pdf.domain

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

// Zoom rules (SPEC F-3.1 to F-3.3, DEC-0017 and DEC-0018).
// 1f is the page width. Pixels are used for offsets and sizes.

const val MIN_ZOOM = 1f
const val MAX_ZOOM = 5f

// Double tap goes to half a page (x2) and back to the width.
const val HALF_PAGE_ZOOM = 2f

// Upper bound for one page bitmap: about 16 MB in ARGB_8888 (SPEC T-6, provisional).
const val MAX_BITMAP_PIXELS = 4_000_000L

fun clampZoom(scale: Float): Float = scale.coerceIn(MIN_ZOOM, MAX_ZOOM)

fun isAtWidth(scale: Float): Boolean = abs(scale - MIN_ZOOM) < 0.01f

// From the width, a double tap goes to half a page; from any other zoom, back to the width.
fun doubleTapTarget(scale: Float): Float = if (isAtWidth(scale)) HALF_PAGE_ZOOM else MIN_ZOOM

// Horizontal offset is 0 at the left edge and -(scale - 1) * viewportWidth at the right edge.
fun clampOffsetX(offsetX: Float, scale: Float, viewportWidthPx: Float): Float =
    offsetX.coerceIn(-(scale - MIN_ZOOM) * viewportWidthPx, 0f)

// Keeps the point under the fingers still under them when the zoom changes.
fun offsetXForFocus(offsetX: Float, focusX: Float, oldScale: Float, newScale: Float): Float =
    focusX - (focusX - offsetX) * (newScale / oldScale)

// Double tap on the left half shows the left column, on the right half the right column.
fun halfPageOffsetX(touchX: Float, viewportWidthPx: Float, scale: Float): Float =
    if (touchX < viewportWidthPx / 2f) 0f else -(scale - MIN_ZOOM) * viewportWidthPx

// Vertical scroll that keeps the point at focusY on the same spot of the page after a zoom change.
// itemOffsetPx is the top of the page on screen, factor is newScale / oldScale.
fun scrollOffsetForFocus(focusY: Float, itemOffsetPx: Float, factor: Float): Int =
    ((focusY - itemOffsetPx) * factor - focusY).roundToInt()

// Width at which a page is rendered for a given zoom: sharp when zoomed, within the bitmap budget.
// pageHeightOverWidth is the page proportion (height / width).
fun renderWidthPx(viewportWidthPx: Int, scale: Float, pageHeightOverWidth: Float): Int {
    require(pageHeightOverWidth > 0f) { "page proportion must be positive" }
    val wanted = viewportWidthPx * scale
    val budgetWidth = sqrt(MAX_BITMAP_PIXELS / pageHeightOverWidth.toDouble()).toFloat()
    return minOf(wanted, budgetWidth).roundToInt().coerceAtLeast(viewportWidthPx)
}
