package io.github.kotlinmania.threadlocal.internal

import io.github.kotlinmania.threadlocal.Thread
import kotlin.native.concurrent.ThreadLocal

/**
 * On Kotlin/Native, per-OS-thread storage is supplied by the
 * compiler via `@kotlin.native.concurrent.ThreadLocal` on a top-level
 * object. Every thread observes its own `Storage` map; this class
 * routes per-symbol reads and writes through that map. This is the
 * same storage shape used by `kotlinx.coroutines.internal`
 * `CommonThreadLocal`; this port stores only [Thread] records, so the
 * map is typed and needs no unchecked cast.
 */
internal actual class CommonThreadLocal actual constructor(private val name: Symbol) {
    actual fun get(): Thread? = Storage[name]

    actual fun set(value: Thread?) {
        if (value == null) {
            Storage.remove(name)
        } else {
            Storage[name] = value
        }
    }
}

internal actual fun commonThreadLocal(name: Symbol): CommonThreadLocal =
    CommonThreadLocal(name)

@ThreadLocal
private object Storage : MutableMap<Symbol, Thread> by mutableMapOf()
