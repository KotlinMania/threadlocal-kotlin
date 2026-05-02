# threadlocal-kotlin

> Kotlin Multiplatform port of the Rust [`thread_local`](https://github.com/Amanieu/thread_local-rs) crate — per-object thread-local storage with O(1) access, lock-free buckets, and `iter` / `iterMut` / `intoIter` over every thread's slot.

[![CI](https://img.shields.io/github/actions/workflow/status/KotlinMania/threadlocal-kotlin/ci.yml?branch=master&label=CI&logo=github)](https://github.com/KotlinMania/threadlocal-kotlin/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/threadlocal-kotlin?label=Maven%20Central&logo=apachemaven)](https://central.sonatype.com/artifact/io.github.kotlinmania/threadlocal-kotlin)
[![License](https://img.shields.io/badge/license-Apache--2.0%20OR%20MIT-blue)](#license)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![KMP](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![GitHub stars](https://img.shields.io/github/stars/KotlinMania/threadlocal-kotlin?style=social)](https://github.com/KotlinMania/threadlocal-kotlin/stargazers)
[![Discord](https://img.shields.io/badge/Discord-Solace%20Project-5865F2?logo=discord&logoColor=white)](https://discord.gg/rJqVeSmx4)

---

## What this is

A line-by-line transliteration of [`Amanieu/thread_local-rs`](https://github.com/Amanieu/thread_local-rs) (v1.1.9) into Kotlin Multiplatform, preserving the upstream's:

- **Per-object thread-local storage.** Each `ThreadLocal<T>` is its own slab — unlike `@ThreadLocal` annotations which only work on `static` storage, you can have many independent `ThreadLocal` instances.
- **Lock-free fast path.** A read of an existing slot takes one atomic load and one bit check. Bucket allocation is the only synchronization point, and it's CAS-based.
- **Cross-thread iteration.** `iter()`, `iterMut()`, and `intoIter()` walk every thread's slot — useful for collecting per-thread counters at the end of a workload.

If you've used the Rust crate, the Kotlin API will feel identical (modulo `snake_case` → `camelCase`).

## Why a port

Kotlin's stdlib has no per-object thread-local primitive in `commonMain`. The only options today are:

1. JVM `java.lang.ThreadLocal` — JVM-only, not multiplatform.
2. Coroutine `ThreadLocal.asContextElement()` — JVM-only, requires a coroutine context.
3. Roll your own atomics + bucket math.

This library is option 4: a faithful port of a battle-tested Rust implementation, available on every Kotlin Native target.

## Install

```kotlin
// Gradle Kotlin DSL
dependencies {
    implementation("io.github.kotlinmania:threadlocal-kotlin:0.2.0")
}
```

```toml
# Version catalog (libs.versions.toml)
[libraries]
threadlocal-kotlin = { module = "io.github.kotlinmania:threadlocal-kotlin", version = "0.2.0" }
```

## Quick start

```kotlin
import io.github.kotlinmania.threadlocal.ThreadLocal

val local: ThreadLocal<Int> = ThreadLocal()

// First access in this thread — `create` runs, value cached.
val v = local.getOr { 42 }      // 42

// Subsequent access in the same thread — no allocation.
val w = local.get()             // 42

// Walk every thread's slot.
for (perThread in local.iter()) {
    println(perThread)
}

// Drain — leaves the ThreadLocal empty.
val all: List<Int> = local.intoIter().asSequence().toList()
```

See [`src/commonTest/`](src/commonTest/kotlin/io/github/kotlinmania/threadlocal/) for the full upstream test suite ported to Kotlin.

## Targets

| Tier | Targets |
|---|---|
| Native (Apple) | `macosArm64`, `macosX64`, `iosArm64`, `iosX64`, `iosSimulatorArm64` |
| Native (Linux) | `linuxX64` |
| Native (Windows) | `mingwX64` |

CI verifies all native targets on every PR.

## Building from source

```bash
git clone https://github.com/KotlinMania/threadlocal-kotlin.git
cd threadlocal-kotlin

./gradlew compileKotlinMacosArm64
./gradlew macosArm64Test
```

If you want to consult the upstream while working on the port, clone [`Amanieu/thread_local-rs`](https://github.com/Amanieu/thread_local-rs) into `tmp/thread_local-1.1.9/` (gitignored). Every Kotlin file carries a one-line `// Translated from upstream …` header pointing at the upstream `.rs` it mirrors — that header is the human-readable provenance, not a build-time check.

Translation rules are documented in [`AGENTS.md`](./AGENTS.md). Notable port-time decisions:

- **`cached.rs` is intentionally not ported** — it's `#![allow(deprecated)]` end-to-end upstream and exists only for crates.io v1.0 ABI compatibility. See [`AGENTS.md`](./AGENTS.md#intentionally-not-ported-cachedrs).
- **`Drop` impls collapse under GC** — `Entry::drop`, `ThreadLocal::drop`, `ThreadGuard::drop` all become no-ops because Kotlin's GC runs the equivalent cleanup.
- **No thread-exit ID recycling.** Rust uses TLS destructors via `ThreadGuard` to recycle thread IDs back into a min-heap. Kotlin/Multiplatform has no portable thread-exit hook, so IDs grow monotonically with the count of distinct OS threads observed. Documented at [`internal/ThreadId.kt`](src/commonMain/kotlin/io/github/kotlinmania/threadlocal/internal/ThreadId.kt).

## Attribution

This is a derivative work of [`Amanieu/thread_local-rs`](https://github.com/Amanieu/thread_local-rs), copyright **Amanieu d'Antras**, dual-licensed Apache-2.0 OR MIT. All algorithmic and structural design credit — the bucket-of-power-of-two layout, the `BinaryHeap<Reverse<usize>>` ID recycler, the lock-free `Entry`/`ThreadLocal` invariants, the `RawIter` state machine — belongs to the upstream author.

The Kotlin port translates that work line-by-line and re-publishes it under the same dual license. See [`NOTICE`](./NOTICE) for the long-form attribution.

| | |
|---|---|
| Upstream repo | https://github.com/Amanieu/thread_local-rs |
| Upstream version | 1.1.9 (2025-06-12) |
| Upstream author | [Amanieu d'Antras](https://github.com/Amanieu) |
| Upstream license | Apache-2.0 OR MIT |

## Maintainer

**Sydney Renee** &mdash; [@sydneyrenee](https://github.com/sydneyrenee) &mdash; <sydney@solace.ofharmony.ai>

**The Solace Project** &mdash; published under the [KotlinMania](https://github.com/KotlinMania) GitHub organization.

Discussion, issues, and KMP porting talk: **[Solace Project Discord](https://discord.gg/rJqVeSmx4)**.

If this library helps you, a ⭐ on the repo is the easiest way to say so.

## License

Dual-licensed under either of:

- Apache License, Version 2.0 ([LICENSE-APACHE](./LICENSE-APACHE) or http://www.apache.org/licenses/LICENSE-2.0)
- MIT license ([LICENSE-MIT](./LICENSE-MIT) or http://opensource.org/licenses/MIT)

at your option. Mirrors the upstream Rust crate.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in this work shall be dual-licensed as above, without any additional terms or conditions.
