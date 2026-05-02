# threadlocal-kotlin — Maintainer Notes

This project is a Kotlin Multiplatform port of the Rust `thread_local`
crate ([Amanieu/thread_local-rs v1.1.9](https://github.com/Amanieu/thread_local-rs)).
The Kotlin API mirrors the upstream's per-object thread-local storage,
lock-free read fast-path, and cross-thread iteration.

The port has reached its functional gate; new work is incremental
maintenance, not bulk transliteration.

## Source Reference

Upstream crate: `thread_local` 1.1.9 — https://github.com/Amanieu/thread_local-rs

When you need to consult the upstream, fetch it locally however is
convenient (`git clone`, `cargo download`, etc.). It is a behavior
reference, not a tracked input.

## Maintenance Contract

- **Behavioral parity** with the Rust crate is the gate. The runtime
  test suite under `src/commonTest/` and `src/nativeTest/` exercises
  every behavior the upstream's inline `#[test]` blocks did.
- **Kotlin source starts in `commonMain`**; platform-specific code goes
  in `nativeMain` / `jvmMain` / etc. only when a primitive isn't
  available in common.
- **Tests belong in `commonTest`** (or the corresponding `(platform)Test`
  when the test exercises platform-specific code).
- **No stubs, no placeholder implementations, no `@Suppress`** to silence
  warnings. Warnings are errors; fix the cause.
- **No JVM-only APIs in common source.** Android/JVM-adjacent targets may
  be supported through platform source sets, but `commonMain` stays
  Kotlin-only.
- **Prefer Kotlin idioms** when they preserve the observable behavior.
  Rust idioms with no Kotlin analog (`Drop`, `Box<T>`, `Cell<T>`,
  `MaybeUninit<T>`, `unsafe` blocks, `Sync`/`Send` markers) collapse
  under the GC and atomicfu — see [`README.md`](./README.md#building-from-source)
  for the full list of port-time decisions.

## Build Commands

```bash
./gradlew compileKotlinMacosArm64
./gradlew macosArm64Test
./gradlew build
```

CI (`.github/workflows/ci.yml`) runs all native targets on every PR.

## File-level provenance

Each Kotlin source file under `src/commonMain/kotlin/io/github/kotlinmania/threadlocal/`
starts with a one-line attribution comment pointing at the upstream `.rs`
file it was translated from, e.g.

```kotlin
// Translated from upstream thread_local-1.1.9/src/lib.rs
package io.github.kotlinmania.threadlocal
```

This is documentation for human readers, not a build-time check. The
`NOTICE` file at the project root carries the long-form attribution and
license declaration.

## Intentionally not ported: `cached.rs`

Upstream `cached.rs` defines `CachedThreadLocal`, `CachedIterMut`, and
`CachedIntoIter`, all marked
`#[deprecated(since = "1.1.0", note = "Use \`ThreadLocal\` instead")]`.
The upstream crate keeps them only because removing public symbols
would break crates.io v1.0 consumers; the doc comment is explicit
that the wrapper has been obsoleted by performance improvements to
`ThreadLocal` itself.

The Kotlin port has no published v1.0 API to preserve, so the entire
deprecated module is elided. The corresponding re-export at upstream
`lib.rs:74-75` (`#[allow(deprecated)] pub use cached::{...}`) is
likewise dropped. The faithful translation lives in this repo's git
history if a deprecated-compat surface is ever needed.

This is the only place the port intentionally diverges from upstream
on API surface; everything else in `lib.rs`, `thread_id.rs`, and
`unreachable.rs` translates 1:1.

## Notes

The Kotlin API may grow a more idiomatic coroutine / execution-local
surface alongside the Rust-compatible thread-bound one. Either layer
should preserve the observable behavior the upstream defines; the
test suite is the contract.
