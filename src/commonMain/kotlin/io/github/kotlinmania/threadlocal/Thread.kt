// Translated from upstream thread_local-1.1.9/src/thread_id.rs
package io.github.kotlinmania.threadlocal

/**
 * Data which is unique to the current thread while it is running.
 * A thread ID may be reused after a thread exits.
 */
internal data class Thread(
    /** The thread ID obtained from the thread ID manager. */
    val id: Int,
    /** The bucket this thread's local storage will be in. */
    val bucket: Int,
    /** The size of the bucket this thread's local storage will be in. */
    val bucketSize: Int,
    /** The index into the bucket this thread's local storage is in. */
    val index: Int,
) {
    companion object {
        fun new(id: Int): Thread {
            val bucket = POINTER_WIDTH - (id + 1).countLeadingZeroBits() - 1
            val bucketSize = 1 shl bucket
            val index = id - (bucketSize - 1)

            return Thread(
                id = id,
                bucket = bucket,
                bucketSize = bucketSize,
                index = index,
            )
        }
    }
}
