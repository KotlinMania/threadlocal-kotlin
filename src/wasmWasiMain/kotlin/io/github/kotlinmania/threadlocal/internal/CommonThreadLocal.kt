package io.github.kotlinmania.threadlocal.internal

import io.github.kotlinmania.threadlocal.Thread

/**
 * The wasmWasi target is single-threaded. Per-thread storage therefore
 * collapses to a plain process-global map, keyed by [Symbol] to keep
 * the same call surface as the multithreaded actuals.
 */
internal actual class CommonThreadLocal actual constructor(private val name: Symbol) {
    actual fun get(): Thread? = storage[name]

    actual fun set(value: Thread?) {
        if (value == null) {
            storage.remove(name)
        } else {
            storage[name] = value
        }
    }
}

internal actual fun commonThreadLocal(name: Symbol): CommonThreadLocal =
    CommonThreadLocal(name)

private val storage: MutableMap<Symbol, Thread> = mutableMapOf()
