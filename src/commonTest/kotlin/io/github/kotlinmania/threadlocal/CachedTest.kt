// port-lint: tests cached.rs
package io.github.kotlinmania.threadlocal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CachedTest {
    private class Cell(
        var value: Int,
    )

    private fun assertSize(iterator: CachedIterMut<*>, remaining: Int) {
        assertEquals(remaining, iterator.len())
        assertEquals(remaining, iterator.sizeHint().lower)
        assertEquals(remaining, iterator.sizeHint().upper)
    }

    private fun assertSize(iterator: CachedIntoIter<*>, remaining: Int) {
        assertEquals(remaining, iterator.len())
        assertEquals(remaining, iterator.sizeHint().lower)
        assertEquals(remaining, iterator.sizeHint().upper)
    }

    @Test
    fun constructorsCreateIndependentEmptyContainers() {
        val locals = listOf(CachedThreadLocal<Int>(), CachedThreadLocal.new<Int>(), CachedThreadLocal.default<Int>())
        for (local in locals) {
            assertNull(local.get())
            assertEquals("ThreadLocal { local_data: null }", local.toString())
            assertSize(local.iterMut(), 0)
            assertSize(local.intoIter(), 0)
        }
        locals[0].getOr { 7 }
        assertNull(locals[1].get())
        assertNull(locals[2].get())
    }

    @Test
    fun factoriesAreLazyAndFailuresLeaveNoValue() {
        val local = CachedThreadLocal<Cell>()
        assertTrue(local.getOrTry { TryResult(null, "failure") }.isFailure())
        assertNull(local.get())
        val cell = Cell(5)
        assertSame(cell, local.getOrTry { TryResult(cell, null) }.getOrThrow())
        assertSame(cell, local.getOr { error("existing value must be reused") })
        assertSame(cell, local.getOrTry { error("existing value must be reused") }.getOrThrow())
        assertSame(cell, local.getOrDefault { error("existing value must be reused") })
        local.clear()
        assertNull(local.get())
        assertSame(cell, local.getOrDefault { cell })
    }

    @Test
    fun mutableIteratorPreservesSizeDuringPeeksAndMutatesStoredValue() {
        val local = CachedThreadLocal<Cell>()
        val cell = local.getOr { Cell(5) }
        val iterator: CachedIterMut<Cell> = local.iterMut()
        assertSize(iterator, 1)
        repeat(3) {
            assertTrue(iterator.hasNext())
            assertSize(iterator, 1)
        }
        assertSame(cell, iterator.next().also { it.value = 9 })
        assertEquals(9, local.get()?.value)
        assertSize(iterator, 0)
        repeat(3) { assertFalse(iterator.hasNext()) }
        assertFailsWith<NoSuchElementException> { iterator.next() }
        assertSize(iterator, 0)
        for (value in local) value.value += 1
        assertEquals(10, cell.value)
    }

    @Test
    fun consumingIteratorDetachesValuesAndPreservesSizeDuringPeeks() {
        val local = CachedThreadLocal<Cell>()
        val cell = local.getOr { Cell(5) }
        val iterator: CachedIntoIter<Cell> = local.intoIter()
        assertNull(local.get())
        local.getOr { Cell(9) }
        local.clear()
        assertSize(iterator, 1)
        repeat(3) {
            assertTrue(iterator.hasNext())
            assertSize(iterator, 1)
        }
        assertSame(cell, iterator.next())
        assertSize(iterator, 0)
        repeat(3) { assertFalse(iterator.hasNext()) }
        assertFailsWith<NoSuchElementException> { iterator.next() }
        assertSize(iterator, 0)
    }

    @Test
    fun nextWithoutPeekAndEmptyIteratorsHaveExactSizes() {
        val local = CachedThreadLocal<Int>()
        val emptyMutable = local.iterMut()
        val emptyConsuming = local.intoIter()
        repeat(3) {
            assertFalse(emptyMutable.hasNext())
            assertFalse(emptyConsuming.hasNext())
        }
        assertFailsWith<NoSuchElementException> { emptyMutable.next() }
        assertFailsWith<NoSuchElementException> { emptyConsuming.next() }
        assertSize(emptyMutable, 0)
        assertSize(emptyConsuming, 0)

        local.getOr { 7 }
        val mutable = local.iterator()
        assertEquals(7, mutable.next())
        assertSize(mutable, 0)
        val consuming = local.intoIter()
        assertEquals(7, consuming.next())
        assertSize(consuming, 0)
    }

    @Test
    fun dedicatedIteratorsCountValuesAcrossSparseBuckets() {
        val local = ThreadLocal<Cell>()
        local.insert(Thread.new(3), Cell(11))
        local.insert(Thread.new(1234), Cell(23))
        val mutable = CachedIterMut(IterMut(local))
        for (remaining in 2 downTo 1) {
            repeat(3) {
                assertTrue(mutable.hasNext())
                assertSize(mutable, remaining)
            }
            mutable.next().value += 1
            assertSize(mutable, remaining - 1)
        }
        assertFalse(mutable.hasNext())
        assertFailsWith<NoSuchElementException> { mutable.next() }

        val consuming = CachedIntoIter(IntoIter(local))
        val values = mutableListOf<Int>()
        for (remaining in 2 downTo 1) {
            repeat(3) {
                assertTrue(consuming.hasNext())
                assertSize(consuming, remaining)
            }
            values.add(consuming.next().value)
            assertSize(consuming, remaining - 1)
        }
        assertEquals(listOf(12, 24), values.sorted())
        assertFalse(consuming.hasNext())
        assertFailsWith<NoSuchElementException> { consuming.next() }
    }
}
