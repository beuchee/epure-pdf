package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReaderCommandsTest {
    @Test
    fun currentPageIsTheOneWithMostHeightOnScreen() {
        val visible = listOf(VisiblePage(0, 100), VisiblePage(1, 900), VisiblePage(2, 50))
        assertEquals(1, currentPage(visible))
    }

    @Test
    fun tieGoesToTheEarlierPage() {
        val visible = listOf(VisiblePage(3, 400), VisiblePage(4, 400))
        assertEquals(3, currentPage(visible))
    }

    @Test
    fun noPageOnScreenGivesNoCurrentPage() {
        assertNull(currentPage(emptyList()))
        assertNull(currentPage(listOf(VisiblePage(0, 0))))
    }

    @Test
    fun validPageNumberIsAccepted() {
        assertEquals(12, parsePageNumber("12", pageCount = 48))
        assertEquals(1, parsePageNumber(" 1 ", pageCount = 48))
        assertEquals(48, parsePageNumber("48", pageCount = 48))
    }

    @Test
    fun invalidPageNumberIsRefused() {
        assertNull(parsePageNumber("0", pageCount = 48))
        assertNull(parsePageNumber("49", pageCount = 48))
        assertNull(parsePageNumber("abc", pageCount = 48))
        assertNull(parsePageNumber("", pageCount = 48))
    }
}
