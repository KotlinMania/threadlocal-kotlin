package io.github.kotlinmania.threadlocal.internal

import io.github.kotlinmania.threadlocal.Thread

/**
 * On JVM-bytecode targets (`jvm`, `android`), per-OS-thread storage is
 * supplied by the JDK `java.lang.ThreadLocal` primitive — Kotlin's
 * unqualified `ThreadLocal` resolves through default imports. Each
 * [CommonThreadLocal] wraps a single platform thread-local; reads and
 * writes only ever touch the calling thread's slot.
 *
 * This file lives in the shared `jvmAndAndroidMain` source set so the
 * implementation is not duplicated between the two JVM-bytecode
 * artifacts.
 */
internal actual class CommonThreadLocal actual constructor(name: Symbol) {
    private val backing: ThreadLocal<Thread> = ThreadLocal()

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
