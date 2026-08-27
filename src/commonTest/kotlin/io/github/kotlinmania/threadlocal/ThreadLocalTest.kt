// port-lint: tests lib.rs
package io.github.kotlinmania.threadlocal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class Counter {
    val value: kotlinx.atomicfu.AtomicInt = kotlinx.atomicfu.atomic(0)
}

class ThreadLocalTest {
    private fun makeCreate(): () -> Int {
        val count = Counter()
        return { count.value.getAndIncrement() }
    }

    @Test
    fun sameThread() {
        val create = makeCreate()
        val local = ThreadLocal<Int>()
        assertNull(local.get())
        assertEquals("ThreadLocal { local_data: null }", local.toString())
        assertEquals(0, local.getOr { create() })
        assertEquals(0, local.get())
        assertEquals(0, local.getOr { create() })
        assertEquals(0, local.get())
        assertEquals(0, local.getOr { create() })
        assertEquals(0, local.get())
        assertEquals("ThreadLocal { local_data: 0 }", local.toString())
        local.clear()
        assertNull(local.get())
    }

    @Test
    fun testDrop() {
        // Kotlin is GC-managed and does not have deterministic drop, so the
        // closest faithful assertion is that draining via intoIter visits every
        // entry exactly once and removes it from the ThreadLocal.
        class Dropped(
            val counter: Counter,
        ) {
            fun observe() {
                counter.value.incrementAndGet()
            }
        }

        val local = ThreadLocal<Dropped>()
        val dropped = Counter()
        local.getOr { Dropped(dropped) }
        assertEquals(0, dropped.value.value)

        var visited = 0
        for (entry in local.intoIter()) {
            entry.observe()
            visited += 1
        }
        assertEquals(1, visited)
        assertEquals(1, dropped.value.value)
        assertNull(local.get())
    }

    @Test
    fun testEarlyreturnBuckets() {
        class Dropped(
            val counter: Counter,
        ) {
            fun observe() {
                counter.value.incrementAndGet()
            }
        }

        // Use a high `id` here to guarantee that a lazily allocated
        // bucket somewhere in the middle is used. Neither iteration
        // nor `clear()` must early-return on `null` buckets that are
        // used for lower bucket indices.
        val thread = Thread.new(1234)
        assertTrue(thread.bucket > 1)

        val dropped = Counter()
        val local = ThreadLocal<Dropped>()
        local.insert(thread, Dropped(dropped))

        val first = local.iter().asSequence().first()
        assertEquals(0, first.counter.value.value)

        val firstMut = local.iterMut().asSequence().first()
        assertEquals(0, firstMut.counter.value.value)

        for (entry in local.intoIter()) {
            entry.observe()
        }
        assertEquals(1, dropped.value.value)
    }

    @Test
    fun intoIterLeavesLocalEmpty() {
        val local = ThreadLocal<Int>()
        local.insert(Thread.new(1234), 1)

        assertEquals(listOf(1), local.intoIter().asSequence().toList())
        assertNull(local.get())
        assertEquals(emptyList(), local.intoIter().asSequence().toList())
    }

    @Test
    fun isSync() {
        // Compile-time check that ThreadLocal<String> can be instantiated and used.
        val local: ThreadLocal<String> = ThreadLocal()
        assertNull(local.get())
    }
}
