package io.github.kotlinmania.tls.internal

/**
 * A small binary min-heap of [Int] values. Mirrors the
 * `BinaryHeap<Reverse<usize>>` that Rust's `thread_local` crate uses
 * to recycle freed thread IDs in a deterministic, lowest-first order.
 *
 * Not thread-safe — callers synchronize externally.
 */
internal class MinHeap {
    private val data: MutableList<Int> = mutableListOf()

    val isEmpty: Boolean get() = data.isEmpty()

    fun push(value: Int) {
        data.add(value)
        siftUp(data.size - 1)
    }

    fun pop(): Int? {
        if (data.isEmpty()) return null
        val top = data[0]
        val last = data.removeAt(data.size - 1)
        if (data.isNotEmpty()) {
            data[0] = last
            siftDown(0)
        }
        return top
    }

    private fun siftUp(start: Int) {
        var i = start
        while (i > 0) {
            val parent = (i - 1) / 2
            if (data[i] < data[parent]) {
                swap(i, parent)
                i = parent
            } else {
                break
            }
        }
    }

    private fun siftDown(start: Int) {
        var i = start
        val n = data.size
        while (true) {
            val left = 2 * i + 1
            val right = 2 * i + 2
            var smallest = i
            if (left < n && data[left] < data[smallest]) smallest = left
            if (right < n && data[right] < data[smallest]) smallest = right
            if (smallest == i) break
            swap(i, smallest)
            i = smallest
        }
    }

    private fun swap(a: Int, b: Int) {
        val tmp = data[a]
        data[a] = data[b]
        data[b] = tmp
    }
}
