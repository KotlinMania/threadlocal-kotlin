package io.github.kotlinmania.threadlocal.internal

import io.github.kotlinmania.threadlocal.Thread

/**
 * The JS target is single-threaded — there is exactly one execution
 * thread, so per-thread storage collapses to a plain process-global
 * map. [CommonThreadLocal] still routes through it by [Symbol] so the
 * call surface stays consistent with the multithreaded actuals.
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
