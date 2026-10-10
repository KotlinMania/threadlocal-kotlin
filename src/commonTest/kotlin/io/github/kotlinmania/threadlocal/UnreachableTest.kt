// port-lint: tests unreachable.rs
package io.github.kotlinmania.threadlocal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnreachableTest {
    @Test
    fun testUncheckedOption() {
        val some: Int? = 42
        assertEquals(42, some.uncheckedUnwrap())
        val none: Int? = null
        none.uncheckedUnwrapNone()

        assertFailsWith<IllegalStateException> {
            none.uncheckedUnwrap()
        }
        assertFailsWith<IllegalStateException> {
            some.uncheckedUnwrapNone()
        }
    }

    @Test
    fun testUncheckedResult() {
        val ok = TryResult(42, null)
        assertEquals(42, ok.uncheckedUnwrapOk())

        val err = TryResult<Int>(null, "failed")
        assertEquals("failed", err.uncheckedUnwrapErr())

        assertFailsWith<IllegalStateException> {
            err.uncheckedUnwrapOk()
        }
        assertFailsWith<IllegalStateException> {
            ok.uncheckedUnwrapErr()
        }
    }
}
