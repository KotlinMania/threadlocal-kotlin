package io.github.kotlinmania.tls.internal

/**
 * On the Android (JVM) target, per-OS-thread storage is supplied by
 * `java.lang.ThreadLocal`. Each [CommonThreadLocal] wraps a single
 * Java thread-local; reads and writes only ever touch the calling
 * thread's slot.
 */
internal actual class CommonThreadLocal<T> actual constructor(name: Symbol) {
    private val backing: java.lang.ThreadLocal<Any?> = java.lang.ThreadLocal()

    actual fun get(): T? {
        @Suppress("UNCHECKED_CAST")
        return backing.get() as T?
    }

    actual fun set(value: T?) {
        if (value == null) {
            backing.remove()
        } else {
            backing.set(value)
        }
    }
}

internal actual fun <T> commonThreadLocal(name: Symbol): CommonThreadLocal<T> =
    CommonThreadLocal(name)
