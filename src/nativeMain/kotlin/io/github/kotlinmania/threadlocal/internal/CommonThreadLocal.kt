package io.github.kotlinmania.threadlocal.internal

import kotlin.native.concurrent.ThreadLocal

/**
 * On Kotlin/Native, per-OS-thread storage is supplied by the
 * compiler via `@kotlin.native.concurrent.ThreadLocal` on a top-level
 * object. Every thread observes its own `Storage` map; this class
 * routes per-symbol reads and writes through that map.
 */
internal actual class CommonThreadLocal<T> actual constructor(private val name: Symbol) {
    actual fun get(): T? {
        @Suppress("UNCHECKED_CAST")
        return Storage[name] as T?
    }

    actual fun set(value: T?) {
        if (value == null) {
            Storage.remove(name)
        } else {
            Storage[name] = value
        }
    }
}

internal actual fun <T> commonThreadLocal(name: Symbol): CommonThreadLocal<T> =
    CommonThreadLocal(name)

@ThreadLocal
private object Storage : MutableMap<Symbol, Any?> by mutableMapOf()
