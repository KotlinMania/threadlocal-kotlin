// port-lint: source lib.rs
/**
 * Per-object thread-local storage.
 *
 * This module provides the [ThreadLocal] type which allows a separate
 * copy of an object to be used for each thread. This allows for
 * per-object thread-local storage, unlike Kotlin's `@ThreadLocal`
 * annotation which only allows static thread-local storage.
 *
 * Per-thread objects are not destroyed when a thread exits. Instead,
 * objects are only destroyed when the [ThreadLocal] containing them
 * is itself dropped (cleared).
 *
 * You can also iterate over the thread-local values of all threads in
 * a [ThreadLocal] object using [ThreadLocal.iter] and friends. Since
 * Kotlin is GC-managed, iteration over a live [ThreadLocal] returns
 * read-only references; the consuming variant ([ThreadLocal.intoIter])
 * removes entries from the [ThreadLocal] as it yields them.
 *
 * Note that since thread IDs are recycled when a thread exits, it is
 * possible for one thread to retrieve the object of another thread.
 * Since this can only occur after a thread has exited this does not
 * lead to any race conditions.
 *
 * # Examples
 *
 * Basic usage of [ThreadLocal]:
 *
 * ```
 * val local: ThreadLocal<Int> = ThreadLocal()
 * assertEquals(null, local.get())
 * assertEquals(5, local.getOr { 5 })
 * assertEquals(5, local.get())
 * ```
 */
package io.github.kotlinmania.threadlocal

import io.github.kotlinmania.threadlocal.internal.currentThread
import kotlinx.atomicfu.AtomicArray
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls

internal class Entry<T : Any> {
    val present: AtomicBoolean = atomic(false)
    val value: AtomicRef<T?> = atomic(null)
}

/**
 * Thread-local variable wrapper. See the module-level documentation
 * for details.
 */
public class ThreadLocal<T : Any> {
    /**
     * The buckets in the thread local. The nth bucket contains `2^n`
     * elements. Each bucket is lazily allocated.
     */
    internal val buckets: AtomicArray<Array<Entry<T>>?> = atomicArrayOfNulls(BUCKETS)

    /**
     * The number of values in the thread local. This can be less than
     * the real number of values, but is never more.
     */
    internal val values: AtomicInt = atomic(0)

    /** Creates a new empty [ThreadLocal]. */
    public constructor()

    /**
     * Creates a new [ThreadLocal] with an initial capacity. If less
     * than the capacity threads access the thread local it will never
     * reallocate. The capacity may be rounded up to the nearest power
     * of two.
     */
    public constructor(capacity: Int) {
        val allocatedBuckets = POINTER_WIDTH - capacity.countLeadingZeroBits()
        for (i in 0 until allocatedBuckets) {
            buckets[i].value = allocateBucket(1 shl i)
        }
    }

    /** Returns the element for the current thread, if it exists. */
    public fun get(): T? = getInner(currentThread())

    /**
     * Returns the element for the current thread, or creates it if it
     * doesn't exist.
     */
    public fun getOr(create: () -> T): T {
        val thread = currentThread()
        val existing = getInner(thread)
        if (existing != null) return existing
        return insert(thread, create())
    }

    /**
     * Returns the element for the current thread, or creates it if it
     * doesn't exist. If [create] fails, that error is returned and no
     * element is added.
     */
    public fun getOrTry(create: () -> Result<T>): Result<T> {
        val thread = currentThread()
        val existing = getInner(thread)
        if (existing != null) return Result.success(existing)
        return create().map { data -> insert(thread, data) }
    }

    private fun getInner(thread: Thread): T? {
        val bucket = buckets[thread.bucket].value ?: return null
        val entry = bucket[thread.index]
        return if (entry.present.value) entry.value.value else null
    }

    internal fun insert(thread: Thread, data: T): T {
        val bucketAtomic = buckets[thread.bucket]
        var bucketArr = bucketAtomic.value

        // If the bucket doesn't already exist, we need to allocate it.
        if (bucketArr == null) {
            val newBucket = allocateBucket<T>(thread.bucketSize)
            bucketArr = if (bucketAtomic.compareAndSet(null, newBucket)) {
                newBucket
            } else {
                // If the bucket value changed (from null), that means
                // another thread stored a new bucket before we could,
                // and we can drop our bucket and use that one instead.
                bucketAtomic.value!!
            }
        }

        // Insert the new element into the bucket.
        val entry = bucketArr[thread.index]
        entry.value.value = data
        entry.present.value = true

        values.incrementAndGet()

        return data
    }

    /**
     * Returns the element for the current thread, or creates a default
     * one if it doesn't exist.
     *
     * Rust's signature requires `T: Default` and calls `Default::default`
     * to construct the missing value. Kotlin has no `Default` marker
     * trait, so the caller supplies the default factory; semantically
     * this is identical to [getOr] and exists for upstream API parity.
     */
    public fun getOrDefault(default: () -> T): T = getOr(default)

    /**
     * Returns an iterator over the local values of all threads in
     * unspecified order.
     */
    public fun iter(): Iter<T> = Iter(this)

    /**
     * Returns a mutable iterator over the local values of all threads
     * in unspecified order. Since Kotlin's reference types are
     * inherently mutable, this is equivalent to [iter] and is
     * provided for API symmetry with the upstream Rust crate.
     */
    public fun iterMut(): IterMut<T> = IterMut(this)

    /**
     * Returns an iterator that drains the local values of all threads
     * in unspecified order. Rust moves the [ThreadLocal] into its
     * `IntoIter`; Kotlin has no move, so this method detaches the
     * buckets immediately and leaves this [ThreadLocal] empty.
     */
    public fun intoIter(): IntoIter<T> = IntoIter(this)

    /**
     * Removes all thread-specific values from the [ThreadLocal],
     * effectively resetting it to its original state.
     */
    public fun clear() {
        for (i in 0 until BUCKETS) {
            buckets[i].value = null
        }
        values.value = 0
    }

    override fun toString(): String = "ThreadLocal { local_data: ${get()} }"

    public companion object {
        /**
         * Creates a new [ThreadLocal] with an initial capacity. If
         * less than the capacity threads access the thread local it
         * will never reallocate. The capacity may be rounded up to
         * the nearest power of two.
         */
        public fun <T : Any> withCapacity(capacity: Int): ThreadLocal<T> = ThreadLocal(capacity)
    }
}

/**
 * Allocate a bucket of the given size. Each entry starts with
 * `present = false` and a null value slot.
 */
private fun <T : Any> allocateBucket(size: Int): Array<Entry<T>> =
    Array(size) { Entry() }

/**
 * Shared per-iteration state used by [Iter], [IterMut], and
 * [IntoIter].
 */
internal class RawIter {
    var yielded: Int = 0
    var bucket: Int = 0
    var bucketSize: Int = 1
    var index: Int = 0

    fun <T : Any> next(threadLocal: ThreadLocal<T>): T? {
        while (bucket < BUCKETS) {
            val bucketArr = threadLocal.buckets[bucket].value
            if (bucketArr != null) {
                while (index < bucketSize) {
                    val entry = bucketArr[index]
                    index += 1
                    if (entry.present.value) {
                        yielded += 1
                        return entry.value.value
                    }
                }
            }
            nextBucket()
        }
        return null
    }

    fun <T : Any> nextEntry(threadLocal: ThreadLocal<T>): Entry<T>? {
        if (threadLocal.values.value == yielded) return null
        while (bucket < BUCKETS) {
            val bucketArr = threadLocal.buckets[bucket].value
            if (bucketArr != null) {
                while (index < bucketSize) {
                    val entry = bucketArr[index]
                    index += 1
                    if (entry.present.value) {
                        yielded += 1
                        return entry
                    }
                }
            }
            nextBucket()
        }
        return null
    }

    private fun nextBucket() {
        bucketSize = bucketSize shl 1
        bucket += 1
        index = 0
    }

    fun <T : Any> sizeHint(threadLocal: ThreadLocal<T>): Pair<Int, Int?> {
        val total = threadLocal.values.value
        return Pair(total - yielded, null)
    }

    fun <T : Any> sizeHintFrozen(threadLocal: ThreadLocal<T>): Pair<Int, Int?> {
        val total = threadLocal.values.value
        val remaining = total - yielded
        return Pair(remaining, remaining)
    }

    override fun toString(): String =
        "RawIter(yielded=$yielded, bucket=$bucket, bucketSize=$bucketSize, index=$index)"
}

/** Iterator over the contents of a [ThreadLocal]. */
public class Iter<T : Any> internal constructor(
    private val threadLocal: ThreadLocal<T>,
) : Iterator<T> {
    private val raw: RawIter = RawIter()
    private var pending: T? = null

    override fun hasNext(): Boolean {
        if (pending != null) return true
        pending = raw.next(threadLocal)
        return pending != null
    }

    override fun next(): T {
        val v = pending ?: raw.next(threadLocal) ?: throw NoSuchElementException()
        pending = null
        return v
    }

    public fun sizeHint(): Pair<Int, Int?> = raw.sizeHint(threadLocal)

    override fun toString(): String =
        "Iter(threadLocal=$threadLocal, raw=$raw)"
}

/** Mutable iterator over the contents of a [ThreadLocal]. */
public class IterMut<T : Any> internal constructor(
    private val threadLocal: ThreadLocal<T>,
) : Iterator<T> {
    private val raw: RawIter = RawIter()
    private var pending: T? = null

    override fun hasNext(): Boolean {
        if (pending != null) return true
        val entry = raw.nextEntry(threadLocal) ?: return false
        pending = entry.value.value
        return true
    }

    override fun next(): T {
        if (pending == null) {
            val entry = raw.nextEntry(threadLocal) ?: throw NoSuchElementException()
            pending = entry.value.value
        }
        val v = pending!!
        pending = null
        return v
    }

    public fun sizeHint(): Pair<Int, Int?> = raw.sizeHintFrozen(threadLocal)

    // Manual toString so we don't call `toString` on the [ThreadLocal], as
    // doing so would create a reference to this thread's value that
    // potentially aliases with a mutable reference we have given out.
    override fun toString(): String = "IterMut(raw=$raw)"
}

/** An iterator that moves out of a [ThreadLocal]. */
public class IntoIter<T : Any> internal constructor(
    threadLocal: ThreadLocal<T>,
) : Iterator<T> {
    private val buckets: Array<Array<Entry<T>>?>
    private val total: Int
    private var yielded: Int = 0
    private var bucket: Int = 0
    private var bucketSize: Int = 1
    private var index: Int = 0
    private var pending: T? = null

    init {
        total = threadLocal.values.value
        buckets = Array(BUCKETS) { i ->
            val bucketArr = threadLocal.buckets[i].value
            threadLocal.buckets[i].value = null
            bucketArr
        }
        threadLocal.values.value = 0
    }

    override fun hasNext(): Boolean {
        if (pending != null) return true
        pending = nextValue() ?: return false
        return true
    }

    override fun next(): T {
        val v = pending ?: nextValue() ?: throw NoSuchElementException()
        pending = null
        return v
    }

    private fun nextValue(): T? {
        if (yielded == total) return null
        while (bucket < BUCKETS) {
            val bucketArr = buckets[bucket]
            if (bucketArr != null) {
                while (index < bucketSize) {
                    val entry = bucketArr[index]
                    index += 1
                    if (entry.present.value) {
                        yielded += 1
                        entry.present.value = false
                        val value = entry.value.value
                        entry.value.value = null
                        return value
                    }
                }
            }
            bucketSize = bucketSize shl 1
            bucket += 1
            index = 0
        }
        return null
    }

    public fun sizeHint(): Pair<Int, Int?> {
        val remaining = total - yielded
        return Pair(remaining, remaining)
    }
}
