package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TilesTest {
    @Test
    fun nothingVisibleGivesNoTile() {
        assertTrue(tilesFor(2000, 3000, 0, 3000, 2000, 3500).isEmpty())
        assertTrue(tilesFor(2000, 3000, 100, 100, 100, 200).isEmpty())
    }

    @Test
    fun smallVisiblePartGivesOneTile() {
        val tiles = tilesFor(2000, 3000, 10, 20, 300, 400)
        assertEquals(listOf(Tile(0, 0, TILE_PX, TILE_PX)), tiles)
    }

    @Test
    fun edgeTilesAreCutToThePage() {
        // 1200 x 700 page: the last column is 176 px wide, the last row 188 px high.
        val tiles = tilesFor(1200, 700, 0, 0, 1200, 700)
        assertEquals(3 * 2, tiles.size)
        assertEquals(Tile(1024, 512, 176, 188), tiles.last())
    }

    @Test
    fun onlyTheVisibleBandIsCovered() {
        // Rows 1 and 2 only (pixels 512 to 1200), one column (0 to 400).
        val tiles = tilesFor(2000, 3000, 0, 600, 400, 1100)
        assertEquals(listOf(Tile(0, 512, TILE_PX, TILE_PX), Tile(0, 1024, TILE_PX, TILE_PX)), tiles)
    }

    @Test
    fun visibleAreaPastThePageIsClipped() {
        val tiles = tilesFor(600, 600, -500, -500, 5000, 5000)
        assertEquals(4, tiles.size)
    }
}
