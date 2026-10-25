package com.alain.epure.pdf.domain

// Side of one tile in pixels, for pages too large to render in one bitmap (DEC-0040).
const val TILE_PX = 512

// A part of a page drawn at a given width: position and size in pixels, from the page's top-left corner.
data class Tile(val left: Int, val top: Int, val width: Int, val height: Int)

// Tiles of a page that touch the visible part of it, given as pixels from the page's top-left corner.
// The visible part may extend past the page: it is clipped to the page.
fun tilesFor(
    pageWidthPx: Int,
    pageHeightPx: Int,
    visibleLeft: Int,
    visibleTop: Int,
    visibleRight: Int,
    visibleBottom: Int
): List<Tile> {
    val left = visibleLeft.coerceIn(0, pageWidthPx)
    val top = visibleTop.coerceIn(0, pageHeightPx)
    val right = visibleRight.coerceIn(0, pageWidthPx)
    val bottom = visibleBottom.coerceIn(0, pageHeightPx)
    if (right <= left || bottom <= top) return emptyList()

    val columns = left / TILE_PX..(right - 1) / TILE_PX
    val rows = top / TILE_PX..(bottom - 1) / TILE_PX
    return rows.flatMap { row ->
        columns.map { column ->
            val tileLeft = column * TILE_PX
            val tileTop = row * TILE_PX
            Tile(
                left = tileLeft,
                top = tileTop,
                width = minOf(TILE_PX, pageWidthPx - tileLeft),
                height = minOf(TILE_PX, pageHeightPx - tileTop)
            )
        }
    }
}
