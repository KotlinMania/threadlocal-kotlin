// port-lint: source cached.rs
package io.github.kotlinmania.threadlocal

/**
 * Wrapper around [ThreadLocal].
 *
 * This used to add a fast path for a single thread, however that has
 * been obsoleted by performance improvements to [ThreadLocal] itself.
 *
 * Deprecated since the upstream 1.1.0 release. Use [ThreadLocal] instead.
 */
public class CachedThreadLocal<T : Any> {
    private val inner: ThreadLocal<T> = ThreadLocal()

    /** Creates a new empty [CachedThreadLocal]. */
    public constructor()

    public companion object {
        /** Creates a new empty [CachedThreadLocal]. */
        public fun <T : Any> new(): CachedThreadLocal<T> = CachedThreadLocal()

        /** Creates a new empty [CachedThreadLocal]. */
        public fun <T : Any> default(): CachedThreadLocal<T> = new()
    }

    /** Returns the element for the current thread, if it exists. */
    public fun get(): T? = inner.get()

    /**
     * Returns the element for the current thread, or creates it if it
     * doesn't exist.
     */
    public fun getOr(create: ValueFactory<T>): T = inner.getOr(create)

    /**
     * Returns the element for the current thread, or creates it if it
     * doesn't exist. If [create] fails, that error is returned and no
     * element is added.
     */
    public fun getOrTry(create: TryFactory<T>): TryResult<T> = inner.getOrTry(create)

    /**
     * Returns a mutable iterator over the local values of all threads.
     *
     * Callers must ensure no other threads access their associated
     * values during iteration; Kotlin cannot enforce an exclusive borrow.
     */
    public fun iterMut(): CachedIterMut<T> = CachedIterMut(IterMut(inner))

    /** Returns a mutable iterator over the local values of all threads. */
    public operator fun iterator(): CachedIterMut<T> = iterMut()

    /**
     * Removes all thread-specific values from the [CachedThreadLocal],
     * effectively resetting it to its original state.
     *
     * Callers must ensure no other threads access their associated
     * values while clearing; Kotlin cannot enforce an exclusive borrow.
     */
    public fun clear() {
        inner.clear()
    }

    /** Detaches all values into an iterator and leaves this container empty. */
    public fun intoIter(): CachedIntoIter<T> = CachedIntoIter(IntoIter(inner))

    /**
     * Returns the element for the current thread, or creates a default
     * one if it doesn't exist.
     */
    public fun getOrDefault(default: ValueFactory<T>): T = getOr(default)

    override fun toString(): String = "ThreadLocal { local_data: ${get()} }"
}

/** Exact lower and upper bounds on the number of remaining iterator elements. */
public class CachedIteratorSizeHint internal constructor(
    public val lower: Int,
    public val upper: Int,
)

/** Mutable iterator over the contents of a [CachedThreadLocal]. */
public class CachedIterMut<T : Any> internal constructor(
    private val inner: IterMut<T>,
) : Iterator<T> {
    override fun hasNext(): Boolean = inner.hasNext()

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        return inner.next()
    }

    /** Returns exact lower and upper bounds on the remaining element count. */
    public fun sizeHint(): CachedIteratorSizeHint {
        val remaining = len()
        return CachedIteratorSizeHint(remaining, remaining)
    }

    /** Returns the exact number of remaining elements. */
    public fun len(): Int = inner.sizeHint().first
}

/** An iterator that moves out of a [CachedThreadLocal]. */
public class CachedIntoIter<T : Any> internal constructor(
    private val inner: IntoIter<T>,
) : Iterator<T> {
    override fun hasNext(): Boolean = inner.hasNext()

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        return inner.next()
    }

    /** Returns exact lower and upper bounds on the remaining element count. */
    public fun sizeHint(): CachedIteratorSizeHint {
        val remaining = len()
        return CachedIteratorSizeHint(remaining, remaining)
    }

    /** Returns the exact number of remaining elements. */
    public fun len(): Int = inner.sizeHint().first
}
