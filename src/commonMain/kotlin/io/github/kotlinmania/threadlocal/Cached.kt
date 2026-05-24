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
     * Since this call borrows the [CachedThreadLocal] mutably, this
     * operation can be done safely---the mutable borrow statically
     * guarantees no other threads are currently accessing their
     * associated values.
     */
    public fun iterMut(): Iterator<T> = inner.iterMut()

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
    public fun intoIter(): Iterator<T> = inner.intoIter()

    /**
     * Returns the element for the current thread, or creates a default
     * one if it doesn't exist.
     */
    public fun getOrDefault(default: ValueFactory<T>): T = getOr(default)

    override fun toString(): String = "ThreadLocal { local_data: ${get()} }"
}
