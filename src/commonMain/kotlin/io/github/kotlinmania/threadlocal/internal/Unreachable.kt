package io.github.kotlinmania.threadlocal.internal

/**
 * Mirrors `thread_local-1.1.9/src/unreachable.rs`, which provides
 * `UncheckedOptionExt::unchecked_unwrap` and friends. Those traits
 * exist purely to give the Rust optimizer an
 * `std::hint::unreachable_unchecked()` hint after the caller has
 * externally proven the wrapped value is present; the optimizer then
 * elides the panic branch.
 *
 * Kotlin exposes no equivalent intrinsic. Where the upstream Rust
 * source uses `.unchecked_unwrap_ok()` to turn a known-infallible
 * `Result<T, !>` into a `T`, the Kotlin port restructures the call
 * site to never produce the wrapping `Result` at all (see
 * `ThreadLocal.getOr`). No Kotlin file mirrors the trait API because
 * there are no Kotlin call sites that need it.
 */
