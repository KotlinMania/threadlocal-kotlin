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
        class Dropped(
            val counter: Counter,
        ) : AutoCloseable {
            override fun close() {
                counter.value.incrementAndGet()
            }

            fun drop() {
                close()
            }
        }

        val local = ThreadLocal<Dropped>()
        val dropped = Counter()
        local.getOr { Dropped(dropped) }
        assertEquals(0, dropped.value.value)

        local.drop()
        assertEquals(1, dropped.value.value)
        assertNull(local.get())
    }

    @Test
    fun testEarlyreturnBuckets() {
        class Dropped(
            val counter: Counter,
        ) : AutoCloseable {
            override fun close() {
                counter.value.incrementAndGet()
            }

            fun drop() {
                close()
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

        local.drop()
        assertEquals(1, dropped.value.value)
        assertNull(local.get())
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
    fun newAndDefaultConstructors() {
        val fromNew = ThreadLocal.new<Int>()
        assertNull(fromNew.get())
        assertEquals(10, fromNew.getOr { 10 })
        assertEquals(10, fromNew.get())

        val fromDefault = ThreadLocal.default<String>()
        assertNull(fromDefault.get())
        assertEquals("default", fromDefault.getOr { "default" })
        assertEquals("default", fromDefault.get())
    }

    @Test
    fun isSync() {
        fun <T : Any> foo() {}
        foo<ThreadLocal<String>>()
        val local: ThreadLocal<String> = ThreadLocal()
        assertNull(local.get())
    }
}
