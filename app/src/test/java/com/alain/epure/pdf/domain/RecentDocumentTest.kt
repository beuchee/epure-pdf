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

    private fun document(index: Int) = RecentDocument(
        uri = "content://provider/$index",
        name = "file$index.pdf",
        pageCount = 10,
        page = 0,
        fraction = 0f
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

    @Test
    fun newestDocumentComesFirst() {
        val list = withRecent(listOf(document(1)), document(2), keepList = true)
        assertEquals(listOf("file2.pdf", "file1.pdf"), list.map { it.name })
    }

    @Test
    fun sameNameReplacesTheOlderEntry() {
        val moved = document(1).copy(uri = "content://attachment/1")
        val list = withRecent(listOf(document(1), document(2)), moved, keepList = true)
        assertEquals(listOf("content://attachment/1", "content://provider/2"), list.map { it.uri })
    }

    @Test
    fun withoutTheListOnlyTheLastDocumentIsKept() {
        val list = withRecent(listOf(document(1)), document(2), keepList = false)
        assertEquals(listOf("file2.pdf"), list.map { it.name })
    }

    @Test
    fun listIsCappedAtTenDocuments() {
        val list = (1..12).fold(emptyList<RecentDocument>()) { acc, index ->
            withRecent(acc, document(index), keepList = true)
        }
        assertEquals(10, list.size)
        assertEquals("file12.pdf", list.first().name)
        assertEquals("file3.pdf", list.last().name)
    }
}
