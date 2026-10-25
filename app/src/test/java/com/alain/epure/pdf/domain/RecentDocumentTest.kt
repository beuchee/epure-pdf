package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentDocumentTest {
    private val saved = RecentDocument(
        uri = "content://provider/1",
        name = "article.pdf",
        pageCount = 48,
        page = 11,
        fraction = 0.5f
    )

    @Test
    fun sameNameIsTheSameFile() {
        assertTrue(isSameFile(saved, name = "article.pdf"))
    }

    @Test
    fun differentNameIsAnotherFile() {
        assertFalse(isSameFile(saved, name = "other.pdf"))
    }

    @Test
    fun savedPageIsKeptWhenItExists() {
        assertEquals(11, resumePage(savedPage = 11, pageCount = 48))
        assertEquals(47, resumePage(savedPage = 47, pageCount = 48))
    }

    @Test
    fun pageBeyondTheEndStartsAtTheFirstPage() {
        // The file was replaced by a shorter one (SPEC F-5.5).
        assertEquals(0, resumePage(savedPage = 60, pageCount = 48))
    }

    @Test
    fun negativePageStartsAtTheFirstPage() {
        assertEquals(0, resumePage(savedPage = -1, pageCount = 48))
    }
}
