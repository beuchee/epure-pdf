package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingPositionTest {
    @Test
    fun fractionIsTheShareOfThePageScrolledPast() {
        assertEquals(0f, pageFraction(scrolledPx = 0, pageHeightPx = 1500), 0.0001f)
        assertEquals(0.5f, pageFraction(scrolledPx = 750, pageHeightPx = 1500), 0.0001f)
    }

    @Test
    fun fractionStaysWithinThePage() {
        assertEquals(1f, pageFraction(scrolledPx = 2000, pageHeightPx = 1500), 0.0001f)
        assertEquals(0f, pageFraction(scrolledPx = -40, pageHeightPx = 1500), 0.0001f)
    }

    @Test
    fun emptyPageGivesTheTop() {
        assertEquals(0f, pageFraction(scrolledPx = 10, pageHeightPx = 0), 0.0001f)
    }

    @Test
    fun fractionRestoresTheSamePlaceAfterRotation() {
        // Half of a portrait page (1500 px tall) is half of the same page once it is 760 px tall.
        val fraction = pageFraction(scrolledPx = 750, pageHeightPx = 1500)
        assertEquals(380, (fraction * 760).toInt())
    }
}
