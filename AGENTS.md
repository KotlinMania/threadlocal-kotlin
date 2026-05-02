# threadlocal-kotlin — Agent Guidelines

This project is a Kotlin Multiplatform port of the Rust `thread_local`
crate. The goal is to provide a reusable thread-local / execution-local library
for Kotlin Native first, with the same care as the other KotlinMania
ports.

## Source Reference

The upstream Rust crate is `thread_local` 1.1.9
(<https://crates.io/crates/thread_local>). It is a behavior reference,
not a tracked input — fetch it locally however is convenient when you
need to consult it.

## Porting Contract

- Behavioral parity with the Rust crate is the gate.
- Kotlin source starts in `commonMain`.
- Tests belong in `commonTest`.
- No stubs, no placeholder implementations, no porter-invented
  typealiases.
- No JVM-only APIs in common source. Android/JVM-adjacent targets may
  be supported through platform source sets, but common code stays
  Kotlin-only.
- Prefer Kotlin idioms when they preserve the observable behavior.

## Build Commands

```bash
./gradlew compileKotlinMacosArm64
./gradlew macosArm64Test
./gradlew build
```

## Notes

The first implementation decision is still open by design: this
project may expose both a Rust-compatible thread-bound surface and a
more Kotlin-idiomatic coroutine / execution-local surface. Do not
commit to that API shape without reading the upstream crate and the
local Kotlin coroutine sources first.

## Intentionally not ported: `cached.rs`

Upstream `tmp/thread_local-1.1.9/src/cached.rs` defines `CachedThreadLocal`,
`CachedIterMut`, and `CachedIntoIter`, all marked
`#[deprecated(since = "1.1.0", note = "Use \`ThreadLocal\` instead")]`.
The upstream crate keeps them only because removing public symbols
would break crates.io v1.0 consumers; the doc comment is explicit
that the wrapper has been obsoleted by performance improvements to
`ThreadLocal` itself.

The Kotlin port has no published v1.0 API to preserve, so the entire
deprecated module is elided. The corresponding re-export at
`lib.rs:74-75` (`#[allow(deprecated)] pub use cached::{...}`) is
likewise dropped. If the port is ever published as `0.x` and later
needs a deprecated-compat surface, the file can be reintroduced — the
faithful translation lives in the git history of this repo.

This is the only place the port intentionally diverges from upstream
on API surface; everything else in `lib.rs`, `thread_id.rs`, and
`unreachable.rs` translates 1:1.
