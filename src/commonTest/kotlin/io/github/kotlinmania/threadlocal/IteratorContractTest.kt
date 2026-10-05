package io.github.kotlinmania.threadlocal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IteratorContractTest {
    private fun localWithGaps(): ThreadLocal<Int> {
        val local = ThreadLocal<Int>()
        local.buckets[1].ref.value =
            Array(2) { index ->
                Entry<Int>().also { entry ->
                    if (index == 1) {
                        entry.value.value = 11
                        entry.present.value = true
                    }
                }
            }
        local.buckets[3].ref.value =
            Array(8) { index ->
                Entry<Int>().also { entry ->
                    if (index == 2) {
                        entry.value.value = 23
                        entry.present.value = true
                    }
                }
            }
        local.values.value = 2
        return local
    }

    @Test
    fun repeatedPeeksPreserveValuesAndSizeHint() {
        val iterator = Iter(localWithGaps())
        repeat(3) { assertTrue(iterator.hasNext()) }
        assertEquals(2, iterator.sizeHint().first)
        assertEquals(11, iterator.next())
        repeat(3) { assertTrue(iterator.hasNext()) }
        assertEquals(1, iterator.sizeHint().first)
        assertEquals(23, iterator.next())
        repeat(3) { assertFalse(iterator.hasNext()) }
        assertEquals(0, iterator.sizeHint().first)
        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun nextWithoutPeekPreservesValuesAndExhaustion() {
        val iterator = localWithGaps().iter()
        assertEquals(11, iterator.next())
        assertEquals(23, iterator.next())
        assertFailsWith<NoSuchElementException> { iterator.next() }
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun emptyIteratorIsExhausted() {
        val iterator = ThreadLocal<Int>().iter()
        repeat(3) { assertFalse(iterator.hasNext()) }
        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun mutableIteratorWithGapsCountsPendingValues() {
        val iterator = IterMut(localWithGaps())
        for (value in listOf(11, 23)) {
            val remaining = if (value == 11) 2 else 1
            repeat(3) {
                assertTrue(iterator.hasNext())
                assertEquals(Pair(remaining, remaining), iterator.sizeHint())
            }
            assertEquals(value, iterator.next())
            assertEquals(Pair(remaining - 1, remaining - 1), iterator.sizeHint())
        }
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }
        assertEquals(Pair(0, 0), iterator.sizeHint())
    }

    @Test
    fun consumingIteratorWithGapsCountsPendingValues() {
        val local = localWithGaps()
        val iterator = IntoIter(local)
        assertEquals(0, local.values.value)
        local.insert(Thread.new(1234), 99)
        for (value in listOf(11, 23)) {
            val remaining = if (value == 11) 2 else 1
            repeat(3) {
                assertTrue(iterator.hasNext())
                assertEquals(Pair(remaining, remaining), iterator.sizeHint())
            }
            assertEquals(value, iterator.next())
            assertEquals(Pair(remaining - 1, remaining - 1), iterator.sizeHint())
        }
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }
        assertEquals(Pair(0, 0), iterator.sizeHint())
        assertEquals(listOf(99), local.intoIter().asSequence().toList())
    }
}
