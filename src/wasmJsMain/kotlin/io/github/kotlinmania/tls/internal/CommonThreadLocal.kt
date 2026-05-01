package io.github.kotlinmania.tls.internal

/**
 * The wasmJs target is single-threaded — there is exactly one
 * execution thread, so per-thread storage collapses to a plain
 * process-global map. [CommonThreadLocal] still routes through it by
 * [Symbol] so the call surface stays consistent with the
 * multithreaded actuals.
 */
internal actual class CommonThreadLocal<T> actual constructor(private val name: Symbol) {
    actual fun get(): T? {
        @Suppress("UNCHECKED_CAST")
        return storage[name] as T?
    }

    actual fun set(value: T?) {
        if (value == null) {
            storage.remove(name)
        } else {
            storage[name] = value
        }
    }
}

internal actual fun <T> commonThreadLocal(name: Symbol): CommonThreadLocal<T> =
    CommonThreadLocal(name)

private val storage: MutableMap<Symbol, Any?> = mutableMapOf()
