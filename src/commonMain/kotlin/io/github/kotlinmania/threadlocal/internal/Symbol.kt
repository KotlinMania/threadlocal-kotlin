package io.github.kotlinmania.threadlocal.internal

/**
 * A symbol class that is used to define unique constants that are
 * self-explanatory in a debugger.
 */
internal class Symbol(val name: String) {
    override fun toString(): String = "<$name>"
}
