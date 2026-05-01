package io.github.kotlinmania.threadlocal.internal

import io.github.kotlinmania.threadlocal.Thread

/**
 * Per-OS-thread storage for the current [Thread] record. Each
 * platform actual stores and retrieves a value visible only to the
 * calling OS thread. This follows Kotlin coroutines' internal
 * `CommonThreadLocal` shape, narrowed to [Thread] so the Native
 * actual does not need unchecked casts.
 */
internal expect class CommonThreadLocal(name: Symbol) {
    fun get(): Thread?
    fun set(value: Thread?)
}

internal expect fun commonThreadLocal(name: Symbol): CommonThreadLocal
