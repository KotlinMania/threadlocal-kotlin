package io.github.kotlinmania.threadlocal

import kotlinx.atomicfu.atomic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ThreadLocalTest {
    private fun makeCreate(): () -> Int {
        val count = atomic(0)
        return { count.getAndIncrement() }
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
        // Rust's `test_drop` exercises that dropping the `ThreadLocal`
        // runs the `Drop` impl on each entry. Kotlin is GC-managed and
        // does not have deterministic drop, so the closest faithful
        // assertion is that draining via `intoIter` visits every
        // entry exactly once and removes it from the [ThreadLocal].
        val local = ThreadLocal<Dropped>()
        val dropped = atomic(0)
        local.getOr { Dropped(dropped) }
        assertEquals(0, dropped.value)

        var visited = 0
        for (entry in local.intoIter()) {
            entry.observe()
            visited += 1
        }
        assertEquals(1, visited)
        assertEquals(1, dropped.value)
        assertNull(local.get())
    }

    @Test
    fun testEarlyReturnBuckets() {
        // Use a high `id` here to guarantee that a lazily allocated
        // bucket somewhere in the middle is used. Neither iteration
        // nor `clear()` must early-return on `null` buckets that are
        // used for lower bucket indices.
        val thread = Thread.new(1234)
        assertTrue(thread.bucket > 1)

        val dropped = atomic(0)
        val local = ThreadLocal<Dropped>()
        local.insert(thread, Dropped(dropped))

        val first = local.iter().asSequence().first()
        assertEquals(0, first.dropped.value)

        val firstMut = local.iterMut().asSequence().first()
        assertEquals(0, firstMut.dropped.value)

        for (entry in local.intoIter()) {
            entry.observe()
        }
        assertEquals(1, dropped.value)
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
        // Rust's `is_sync` is a compile-time check that
        // `ThreadLocal<String>` implements the `Sync` trait. Kotlin
        // has no equivalent marker trait — values shared between
        // threads are managed by the GC and the lock-free bucket
        // structure built on top of atomicfu provides the same
        // guarantees Rust's `Sync` would. The runtime equivalent of
        // this test is simply that the type exists and can be
        // instantiated.
        val local: ThreadLocal<String> = ThreadLocal()
        assertNull(local.get())
    }
}

private class Dropped(val dropped: kotlinx.atomicfu.AtomicInt) {
    fun observe() {
        dropped.incrementAndGet()
    }
}
