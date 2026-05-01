// port-lint: source thread_id.rs
package io.github.kotlinmania.threadlocal

import kotlin.test.Test
import kotlin.test.assertEquals

class ThreadTest {
    @Test
    fun testThread() {
        var thread = Thread.new(0)
        assertEquals(0, thread.id)
        assertEquals(0, thread.bucket)
        assertEquals(1, thread.bucketSize)
        assertEquals(0, thread.index)

        thread = Thread.new(1)
        assertEquals(1, thread.id)
        assertEquals(1, thread.bucket)
        assertEquals(2, thread.bucketSize)
        assertEquals(0, thread.index)

        thread = Thread.new(2)
        assertEquals(2, thread.id)
        assertEquals(1, thread.bucket)
        assertEquals(2, thread.bucketSize)
        assertEquals(1, thread.index)

        thread = Thread.new(3)
        assertEquals(3, thread.id)
        assertEquals(2, thread.bucket)
        assertEquals(4, thread.bucketSize)
        assertEquals(0, thread.index)

        thread = Thread.new(19)
        assertEquals(19, thread.id)
        assertEquals(4, thread.bucket)
        assertEquals(16, thread.bucketSize)
        assertEquals(4, thread.index)
    }
}
