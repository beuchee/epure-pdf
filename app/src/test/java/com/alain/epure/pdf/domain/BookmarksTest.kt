package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BookmarksTest {
    @Test
    fun marksAPageAndKeepsThePagesSorted() {
        assertEquals(listOf(2, 5, 9), toggledBookmarks(listOf(2, 9), 5))
    }

    @Test
    fun markingAPageTwiceRemovesIt() {
        assertEquals(listOf(2, 9), toggledBookmarks(toggledBookmarks(listOf(2, 9), 5), 5))
    }

    @Test
    fun removesTheMarkedPageOnly() {
        assertEquals(listOf(9), toggledBookmarks(listOf(2, 9), 2))
    }

    @Test
    fun firstBookmarkOfADocument() {
        assertEquals(listOf(0), toggledBookmarks(emptyList(), 0))
    }
}
