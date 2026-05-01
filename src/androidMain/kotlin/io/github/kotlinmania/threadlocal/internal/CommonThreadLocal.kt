package io.github.kotlinmania.threadlocal.internal

import io.github.kotlinmania.threadlocal.Thread

/**
 * On the Android (JVM) target, per-OS-thread storage is supplied by
 * `java.lang.ThreadLocal`. Each [CommonThreadLocal] wraps a single
 * Java thread-local; reads and writes only ever touch the calling
 * thread's slot.
 */
internal actual class CommonThreadLocal actual constructor(name: Symbol) {
    private val backing: java.lang.ThreadLocal<Thread> = java.lang.ThreadLocal()

    actual fun get(): Thread? = backing.get()

    actual fun set(value: Thread?) {
        if (value == null) {
            backing.remove()
        } else {
            backing.set(value)
        }
    }
}

internal actual fun commonThreadLocal(name: Symbol): CommonThreadLocal =
    CommonThreadLocal(name)
