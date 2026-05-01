// port-lint: source lib.rs
package io.github.kotlinmania.threadlocal

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Mirrors the Rust crate's `different_thread`, `iter`, and
 * `miri_iter_soundness_check` inline tests for the Android host
 * target, whose actual storage is backed by `java.lang.ThreadLocal`.
 */
class CrossThreadTest {
    private fun makeCreate(): () -> Int {
        val count = atomic(0)
        return { count.getAndIncrement() }
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @Test
    fun differentThread() = runBlocking {
        val create = makeCreate()
        val local = ThreadLocal<Int>()
        assertNull(local.get())
        assertEquals(0, local.getOr { create() })
        assertEquals(0, local.get())

        val other = newSingleThreadContext("threadlocal-android-different-thread")
        try {
            withContext(other) {
                assertNull(local.get())
                assertEquals(1, local.getOr { create() })
                assertEquals(1, local.get())
            }
        } finally {
            other.close()
        }

        assertEquals(0, local.get())
        assertEquals(0, local.getOr { create() })
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @Test
    fun iter() = runBlocking {
        val local = ThreadLocal<Int>()
        local.getOr { 1 }

        val outer = newSingleThreadContext("threadlocal-android-iter-outer")
        val inner = newSingleThreadContext("threadlocal-android-iter-inner")
        try {
            withContext(outer) {
                local.getOr { 2 }
                withContext(inner) {
                    local.getOr { 3 }
                }
            }
        } finally {
            outer.close()
            inner.close()
        }

        val sortedFromIter = local.iter().asSequence().toMutableList().also { it.sort() }
        assertEquals(listOf(1, 2, 3), sortedFromIter)

        val sortedFromIterMut = local.iterMut().asSequence().toMutableList().also { it.sort() }
        assertEquals(listOf(1, 2, 3), sortedFromIterMut)

        val sortedFromIntoIter = local.intoIter().asSequence().toMutableList().also { it.sort() }
        assertEquals(listOf(1, 2, 3), sortedFromIntoIter)
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @Test
    fun miriIterSoundnessCheck() = runBlocking {
        val local = ThreadLocal<Int>()
        local.getOr { 1 }

        val other = newSingleThreadContext("threadlocal-android-miri-soundness")
        try {
            val job = launch(other) {
                local.getOr { 2 }
                for (item in local.iter()) {
                    println(item)
                }
            }

            for (item in local.iter()) {
                println(item)
            }

            job.join()
        } finally {
            other.close()
        }
    }
}
