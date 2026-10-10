// port-lint: source unreachable.rs
package io.github.kotlinmania.threadlocal

/**
 * An extension interface providing unchecked unwrapping methods for nullable values.
 */
public interface UncheckedOptionExt<T> {
    /**
     * Get the value out of this Option without checking for None (null).
     */
    public fun uncheckedUnwrap(): T

    /**
     * Assert that this Option is None (null) to the optimizer.
     */
    public fun uncheckedUnwrapNone()
}

/**
 * An extension interface providing unchecked unwrapping methods for [TryResult].
 */
public interface UncheckedResultExt<T, E> {
    /**
     * Get the value out of this Result without checking for Err.
     */
    public fun uncheckedUnwrapOk(): T

    /**
     * Get the error out of this Result without checking for Ok.
     */
    public fun uncheckedUnwrapErr(): E
}

/**
 * Returns the non-null value without checking for null, throwing [IllegalStateException] if null.
 */
public fun <T : Any> T?.uncheckedUnwrap(): T =
    this ?: error("called uncheckedUnwrap on a null value")

/**
 * Asserts that this value is null.
 */
public fun <T> T?.uncheckedUnwrapNone() {
    if (this != null) {
        error("called uncheckedUnwrapNone on a non-null value")
    }
}

/**
 * Returns the success value without checking for error.
 */
public fun <T> TryResult<T>.uncheckedUnwrapOk(): T =
    getOrNull() ?: error("called uncheckedUnwrapOk on a failed result: $errorMessage")

/**
 * Returns the error message without checking for success.
 */
public fun <T> TryResult<T>.uncheckedUnwrapErr(): String =
    errorMessage ?: error("called uncheckedUnwrapErr on a successful result")
