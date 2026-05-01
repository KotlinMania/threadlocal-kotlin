@file:Suppress("DEPRECATION")

package io.github.kotlinmania.tls

/**
 * Wrapper around [ThreadLocal].
 *
 * This used to add a fast path for a single thread, however that has
 * been obsoleted by performance improvements to [ThreadLocal] itself.
 */
@Deprecated(
    message = "Use ThreadLocal instead",
    replaceWith = ReplaceWith("ThreadLocal<T>"),
    level = DeprecationLevel.WARNING,
)
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
     * Returns a mutable iterator over the local values of all
     * threads.
     */
    public fun iterMut(): CachedIterMut<T> = CachedIterMut(inner.iterMut())

    /**
     * Returns an iterator that drains the local values of all
     * threads.
     */
    public fun intoIter(): CachedIntoIter<T> = CachedIntoIter(inner.intoIter())

    /**
     * Removes all thread-specific values from the [ThreadLocal],
     * effectively resetting it to its original state.
     */
    public fun clear(): Unit = inner.clear()

    override fun toString(): String = "ThreadLocal { local_data: ${get()} }"
}

/** Mutable iterator over the contents of a [CachedThreadLocal]. */
@Deprecated(
    message = "Use IterMut instead",
    replaceWith = ReplaceWith("IterMut<T>"),
    level = DeprecationLevel.WARNING,
)
public class CachedIterMut<T : Any> internal constructor(
    private val inner: IterMut<T>,
) : Iterator<T> {
    override fun hasNext(): Boolean = inner.hasNext()
    override fun next(): T = inner.next()
}

/** An iterator that moves out of a [CachedThreadLocal]. */
@Deprecated(
    message = "Use IntoIter instead",
    replaceWith = ReplaceWith("IntoIter<T>"),
    level = DeprecationLevel.WARNING,
)
public class CachedIntoIter<T : Any> internal constructor(
    private val inner: IntoIter<T>,
) : Iterator<T> {
    override fun hasNext(): Boolean = inner.hasNext()
    override fun next(): T = inner.next()
}
