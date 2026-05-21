// Translated from upstream thread_local-1.1.9/src/cached.rs
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

    /** Returns the element for the current thread, if it exists. */
    public fun get(): T? = inner.get()

    /**
     * Returns the element for the current thread, or creates it if it
     * doesn't exist.
     */
    public fun getOr(create: () -> T): T = inner.getOr(create)

    /**
     * Returns the element for the current thread, or creates it if it
     * doesn't exist. If [create] fails, that error is returned and no
     * element is added.
     */
    public fun getOrTry(create: () -> Result<T>): Result<T> = inner.getOrTry(create)

    /**
     * Returns a mutable iterator over the local values of all threads.
     *
     * Since this call borrows the [CachedThreadLocal] mutably, this
     * operation can be done safely---the mutable borrow statically
     * guarantees no other threads are currently accessing their
     * associated values.
     */
    public fun iterMut(): CachedIterMut<T> = CachedIterMut(inner.iterMut())

    /**
     * Removes all thread-specific values from the [CachedThreadLocal],
     * effectively resetting it to its original state.
     *
     * Since this call borrows the [CachedThreadLocal] mutably, this
     * operation can be done safely---the mutable borrow statically
     * guarantees no other threads are currently accessing their
     * associated values.
     */
    public fun clear() {
        inner.clear()
    }

    /** Returns an iterator that moves out of the [CachedThreadLocal]. */
    public fun intoIter(): CachedIntoIter<T> = CachedIntoIter(inner.intoIter())

    /**
     * Returns the element for the current thread, or creates a default
     * one if it doesn't exist.
     */
    public fun getOrDefault(default: () -> T): T = getOr(default)

    override fun toString(): String = "ThreadLocal { local_data: ${get()} }"
}

/**
 * Mutable iterator over the contents of a [CachedThreadLocal].
 *
 * Deprecated since the upstream 1.1.0 release. Use [IterMut] instead.
 */
public class CachedIterMut<T : Any> internal constructor(
    private val inner: IterMut<T>,
) : Iterator<T> {
    override fun hasNext(): Boolean = inner.hasNext()

    override fun next(): T = inner.next()

    public fun sizeHint(): Pair<Int, Int?> = inner.sizeHint()
}

/**
 * An iterator that moves out of a [CachedThreadLocal].
 *
 * Deprecated since the upstream 1.1.0 release. Use [IntoIter] instead.
 */
public class CachedIntoIter<T : Any> internal constructor(
    private val inner: IntoIter<T>,
) : Iterator<T> {
    override fun hasNext(): Boolean = inner.hasNext()

    override fun next(): T = inner.next()

    public fun sizeHint(): Pair<Int, Int?> = inner.sizeHint()
}
