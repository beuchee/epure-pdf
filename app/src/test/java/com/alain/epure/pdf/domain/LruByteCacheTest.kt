package com.alain.epure.pdf.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LruByteCacheTest {
    // Each value is a byte array whose size is its length.
    private fun cache(maxBytes: Long) = LruByteCache<String, ByteArray>(maxBytes) { it.size }

    @Test
    fun keepsEntriesWithinBudget() {
        val cache = cache(maxBytes = 10)
        cache.put("a", ByteArray(4))
        cache.put("b", ByteArray(4))
        assertEquals(8L, cache.sizeInBytes)
        assertEquals(4, cache.get("a")!!.size)
    }

    @Test
    fun evictsLeastRecentlyUsedFirst() {
        val cache = cache(maxBytes = 10)
        cache.put("a", ByteArray(4))
        cache.put("b", ByteArray(4))
        cache.get("a") // "a" becomes the most recently used
        cache.put("c", ByteArray(4)) // 12 bytes: "b" must go
        assertNull(cache.get("b"))
        assertEquals(4, cache.get("a")!!.size)
        assertEquals(8L, cache.sizeInBytes)
    }

    @Test
    fun replacingAKeyDoesNotCountItTwice() {
        val cache = cache(maxBytes = 10)
        cache.put("a", ByteArray(6))
        cache.put("a", ByteArray(3))
        assertEquals(3L, cache.sizeInBytes)
    }

    @Test
    fun valueLargerThanBudgetIsNotKept() {
        val cache = cache(maxBytes = 10)
        cache.put("a", ByteArray(4))
        cache.put("big", ByteArray(11))
        assertNull(cache.get("big"))
        assertEquals(4, cache.get("a")!!.size)
        assertEquals(4L, cache.sizeInBytes)
    }

    @Test
    fun clearEmptiesTheCache() {
        val cache = cache(maxBytes = 10)
        cache.put("a", ByteArray(4))
        cache.clear()
        assertNull(cache.get("a"))
        assertEquals(0L, cache.sizeInBytes)
    }
}
