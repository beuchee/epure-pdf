package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentDocumentTest {
    private val saved = RecentDocument(
        uri = "content://provider/1",
        name = "article.pdf",
        sizeBytes = 4_200_000L,
        pageCount = 48,
        page = 11,
        fraction = 0.5f
    )

    @Test
    fun sameNameAndSizeIsTheSameFile() {
        assertTrue(isSameFile(saved, name = "article.pdf", sizeBytes = 4_200_000L))
    }

    @Test
    fun differentNameOrSizeIsAnotherFile() {
        assertFalse(isSameFile(saved, name = "other.pdf", sizeBytes = 4_200_000L))
        assertFalse(isSameFile(saved, name = "article.pdf", sizeBytes = 4_200_001L))
    }

    @Test
    fun unknownSizeNeverMatches() {
        assertFalse(isSameFile(saved, name = "article.pdf", sizeBytes = -1L))
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
