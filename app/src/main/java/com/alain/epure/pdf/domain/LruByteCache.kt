package com.alain.epure.pdf.domain

// Least-recently-used cache limited by total size in bytes (SPEC T-6).
// Entries are measured with sizeOf; the least recently used ones are dropped first.
class LruByteCache<K : Any, V : Any>(
    private val maxBytes: Long,
    private val sizeOf: (V) -> Int
) {
    private val entries = LinkedHashMap<K, V>(16, 0.75f, true)
    private var currentBytes = 0L

    init {
        require(maxBytes >= 0) { "maxBytes must not be negative" }
    }

    val sizeInBytes: Long
        @Synchronized get() = currentBytes

    @Synchronized
    fun get(key: K): V? = entries[key]

    @Synchronized
    fun put(key: K, value: V) {
        val size = sizeOf(value).toLong()
        // An entry larger than the whole budget is not kept at all.
        if (size > maxBytes) {
            remove(key)
            return
        }
        entries.remove(key)?.let { currentBytes -= sizeOf(it) }
        entries[key] = value
        currentBytes += size
        trimToBudget()
    }

    @Synchronized
    fun clear() {
        entries.clear()
        currentBytes = 0
    }

    @Synchronized
    private fun remove(key: K) {
        entries.remove(key)?.let { currentBytes -= sizeOf(it) }
    }

    private fun trimToBudget() {
        val iterator = entries.entries.iterator()
        while (currentBytes > maxBytes && iterator.hasNext()) {
            val eldest = iterator.next()
            currentBytes -= sizeOf(eldest.value)
            iterator.remove()
        }
    }
}
