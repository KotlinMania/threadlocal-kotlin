package io.github.kotlinmania.tls.internal

/**
 * Per-OS-thread storage keyed by [Symbol]. Mirrors
 * `kotlinx.coroutines.internal.CommonThreadLocal`: each platform
 * actual maintains a per-thread map keyed by symbol; reads and writes
 * are unsynchronized and only visible to the calling OS thread.
 */
internal expect class CommonThreadLocal<T>(name: Symbol) {
    fun get(): T?
    fun set(value: T?)
}

internal expect fun <T> commonThreadLocal(name: Symbol): CommonThreadLocal<T>
