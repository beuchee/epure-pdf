package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoomTest {
    @Test
    fun zoomIsClampedBetweenWidthAndMaximum() {
        assertEquals(1f, clampZoom(0.4f), 0.0001f)
        assertEquals(5f, clampZoom(7f), 0.0001f)
        assertEquals(2.5f, clampZoom(2.5f), 0.0001f)
    }

    @Test
    fun doubleTapCycleGoesWidthHalfWidth() {
        assertEquals(2f, doubleTapTarget(1f), 0.0001f)
        assertEquals(1f, doubleTapTarget(2f), 0.0001f)
    }

    @Test
    fun doubleTapAfterPinchReturnsToWidth() {
        assertEquals(1f, doubleTapTarget(3.4f), 0.0001f)
        assertFalse(isAtWidth(3.4f))
        assertTrue(isAtWidth(1f))
    }

    @Test
    fun offsetStaysWithinThePage() {
        // At x2 on a 1080 px screen, the page can move between 0 and -1080.
        assertEquals(0f, clampOffsetX(50f, 2f, 1080f), 0.0001f)
        assertEquals(-1080f, clampOffsetX(-2000f, 2f, 1080f), 0.0001f)
        assertEquals(-300f, clampOffsetX(-300f, 2f, 1080f), 0.0001f)
    }

    @Test
    fun focusStaysUnderTheFingersWhenZooming() {
        // Point at x = 400 on screen; zoom from 1 to 2 keeps it at the same place.
        val newOffset = offsetXForFocus(offsetX = 0f, focusX = 400f, oldScale = 1f, newScale = 2f)
        // Content point under the finger before: 400. After: 400 * 2 + newOffset must equal 400.
        assertEquals(400f, 400f * 2f + newOffset, 0.0001f)
    }

    @Test
    fun leftHalfShowsLeftColumnAndRightHalfShowsRightColumn() {
        assertEquals(0f, halfPageOffsetX(touchX = 200f, viewportWidthPx = 1080f, scale = 2f), 0.0001f)
        assertEquals(-1080f, halfPageOffsetX(touchX = 800f, viewportWidthPx = 1080f, scale = 2f), 0.0001f)
    }

    @Test
    fun scrollKeepsFocusedPointStill() {
        // Focus at y = 500, page top at y = 100: the point is 400 px into the page.
        // Doubling the zoom makes it 800 px into the page, so the top must move up by 400 px.
        assertEquals(300, scrollOffsetForFocus(focusY = 500f, itemOffsetPx = 100f, factor = 2f))
    }

    @Test
    fun renderWidthFollowsZoomWhenWithinBudget() {
        // A4 page (ratio about 1.414): width 1080 px at x2 is 2160 px, 2160 x 3055 is about 6.6 Mpx.
        // That is above the budget, so the width is capped to about 1682 px.
        assertEquals(1080, renderWidthPx(viewportWidthPx = 1080, scale = 1f, pageHeightOverWidth = 1.414f))
        assertEquals(1682, renderWidthPx(viewportWidthPx = 1080, scale = 2f, pageHeightOverWidth = 1.414f))
    }

    @Test
    fun renderWidthNeverGoesBelowTheViewport() {
        assertEquals(1080, renderWidthPx(viewportWidthPx = 1080, scale = 1f, pageHeightOverWidth = 10f))
    }
}
