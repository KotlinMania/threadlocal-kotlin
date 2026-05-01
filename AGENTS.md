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
