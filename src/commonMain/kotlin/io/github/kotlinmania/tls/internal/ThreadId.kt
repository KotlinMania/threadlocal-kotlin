package io.github.kotlinmania.tls.internal

import io.github.kotlinmania.tls.Thread
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Thread ID manager which allocates thread IDs. It attempts to
 * aggressively reuse thread IDs where possible to avoid cases where a
 * `ThreadLocal` grows indefinitely when it is used by many short-lived
 * threads.
 */
internal class ThreadIdManager : SynchronizedObject() {
    private var freeFrom: Int = 0
    private var freeList: MinHeap? = null

    fun alloc(): Int = synchronized(this) {
        val recycled = freeList?.pop()
        if (recycled != null) {
            recycled
        } else {
            // `freeFrom` can't overflow as each thread takes up at
            // least 2 bytes of memory and thus we can't even have
            // `Int.MAX_VALUE / 2 + 1` threads.
            val id = freeFrom
            freeFrom += 1
            id
        }
    }

    fun free(id: Int): Unit = synchronized(this) {
        val list = freeList ?: MinHeap().also { freeList = it }
        list.push(id)
    }
}

internal val THREAD_ID_MANAGER: ThreadIdManager = ThreadIdManager()

private val THREAD: CommonThreadLocal<Thread> = commonThreadLocal(Symbol("Thread"))

/**
 * Returns a thread record for the current OS thread, allocating a new
 * thread ID if one has not been assigned yet. The record is cached in
 * a per-thread slot so subsequent calls on the same OS thread are
 * cheap.
 *
 * Note on cleanup: Rust's upstream `thread_local` crate uses a TLS
 * destructor (`ThreadGuard`) to release the thread ID back to the
 * manager when an OS thread exits. Pure Kotlin/Multiplatform has no
 * portable thread-exit hook, so this port does not recycle IDs on
 * thread death — IDs grow monotonically with the number of distinct
 * threads observed.
 */
internal fun currentThread(): Thread {
    val cached = THREAD.get()
    if (cached != null) return cached
    return getSlow()
}

private fun getSlow(): Thread {
    val new = Thread.new(THREAD_ID_MANAGER.alloc())
    THREAD.set(new)
    return new
}
