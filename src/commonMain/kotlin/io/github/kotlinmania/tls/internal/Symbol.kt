package io.github.kotlinmania.tls.internal

/**
 * A symbol class that is used to define unique constants that are
 * self-explanatory in a debugger. Mirrors the
 * `kotlinx.coroutines.internal.Symbol` debug helper used in
 * kotlinx.coroutines' own thread-local substrate.
 */
internal class Symbol(val name: String) {
    override fun toString(): String = "<$name>"
}
