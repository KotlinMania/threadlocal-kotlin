package io.github.kotlinmania.tls

/**
 * The width, in bits, of the integer used for thread IDs and bucket
 * arithmetic. Rust's upstream `thread_local` crate selects this from
 * `target_pointer_width` (16 / 32 / 64); the Kotlin port pins it to 32
 * because thread IDs are stored as [Int].
 */
internal const val POINTER_WIDTH: Int = 32

/**
 * The total number of buckets stored in each thread local. All buckets
 * combined can hold up to `Int.MAX_VALUE - 1` entries.
 */
internal const val BUCKETS: Int = POINTER_WIDTH - 1
