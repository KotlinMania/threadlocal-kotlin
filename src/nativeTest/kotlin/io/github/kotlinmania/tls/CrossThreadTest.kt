package io.github.kotlinmania.tls

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Mirrors the Rust crate's `different_thread`, `iter`, and
 * `miri_iter_soundness_check` inline tests. These exercise the
 * cross-thread storage contract — that two distinct OS threads see
 * distinct slots — which is only meaningful on multithreaded targets.
 * They live in `nativeTest` rather than `commonTest` because JS and
 * wasmJs are single-threaded by construction; on those targets every
 * "spawned" coroutine shares the same thread ID and the assertions
 * here trivially fail by design.
 */
class CrossThreadTest {
    private fun makeCreate(): () -> Int {
        val count = atomic(0)
        return { count.getAndIncrement() }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun differentThread() = runBlocking {
        val create = makeCreate()
        val tls = ThreadLocal<Int>()
        assertNull(tls.get())
        assertEquals(0, tls.getOr { create() })
        assertEquals(0, tls.get())

        val other = newSingleThreadContext("tls-different-thread")
        try {
            withContext(other) {
                assertNull(tls.get())
                assertEquals(1, tls.getOr { create() })
                assertEquals(1, tls.get())
            }
        } finally {
            other.close()
        }

        assertEquals(0, tls.get())
        assertEquals(0, tls.getOr { create() })
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun iter() = runBlocking {
        val tls = ThreadLocal<Int>()
        tls.getOr { 1 }

        val outer = newSingleThreadContext("tls-iter-outer")
        val inner = newSingleThreadContext("tls-iter-inner")
        try {
            withContext(outer) {
                tls.getOr { 2 }
                withContext(inner) {
                    tls.getOr { 3 }
                }
            }
        } finally {
            outer.close()
            inner.close()
        }

        val sortedFromIter = tls.iter().asSequence().toMutableList().also { it.sort() }
        assertEquals(listOf(1, 2, 3), sortedFromIter)

        val sortedFromIterMut = tls.iterMut().asSequence().toMutableList().also { it.sort() }
        assertEquals(listOf(1, 2, 3), sortedFromIterMut)

        val sortedFromIntoIter = tls.intoIter().asSequence().toMutableList().also { it.sort() }
        assertEquals(listOf(1, 2, 3), sortedFromIntoIter)
    }

    @Test
    fun miriIterSoundnessCheck() = runBlocking {
        val tls = ThreadLocal<Int>()
        tls.getOr { 1 }

        val deferred = async(Dispatchers.Default) {
            tls.getOr { 2 }
            for (item in tls.iter()) {
                println(item)
            }
        }

        for (item in tls.iter()) {
            println(item)
        }

        deferred.await()
    }
}
