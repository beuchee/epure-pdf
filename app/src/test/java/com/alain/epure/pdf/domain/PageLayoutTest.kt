package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PageLayoutTest {
    @Test
    fun portraitPageKeepsItsProportions() {
        // A4 portrait: 595 x 842 pt, drawn 1080 px wide.
        assertEquals(1528, pageHeightPx(1080, 595f, 842f))
    }

    @Test
    fun landscapePageKeepsItsProportions() {
        assertEquals(763, pageHeightPx(1080, 842f, 595f))
    }

    @Test
    fun squarePageStaysSquare() {
        assertEquals(1080, pageHeightPx(1080, 500f, 500f))
    }
}
