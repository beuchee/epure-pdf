package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTest {
    @Test
    fun nextMovesForwardAndWrapsToTheFirst() {
        assertEquals(1, nextOccurrence(0, 3))
        assertEquals(2, nextOccurrence(1, 3))
        assertEquals(0, nextOccurrence(2, 3))
    }

    @Test
    fun previousMovesBackAndWrapsToTheLast() {
        assertEquals(2, previousOccurrence(0, 3))
        assertEquals(0, previousOccurrence(1, 3))
        assertEquals(1, previousOccurrence(2, 3))
    }

    @Test
    fun noOccurrenceKeepsTheFirstIndex() {
        assertEquals(0, nextOccurrence(0, 0))
        assertEquals(0, previousOccurrence(0, 0))
    }
}
