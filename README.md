# threadlocal-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fthreadlocal--kotlin-blue.svg)](https://github.com/KotlinMania/threadlocal-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/threadlocal-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/threadlocal-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/threadlocal-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/threadlocal-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`Amanieu/thread_local-rs`](https://github.com/Amanieu/thread_local-rs).

**Original Project:** This port is based on [`Amanieu/thread_local-rs`](https://github.com/Amanieu/thread_local-rs). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `Amanieu/thread_local-rs`

> The text below is reproduced and lightly edited from [`https://github.com/Amanieu/thread_local-rs`](https://github.com/Amanieu/thread_local-rs). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## thread_local


[![Build Status](https://github.com/Amanieu/thread_local-rs/actions/workflows/ci.yml/badge.svg)](https://github.com/Amanieu/thread_local-rs/actions) [![crates.io](https://img.shields.io/crates/v/thread_local.svg)](https://crates.io/crates/thread_local)

This library provides the `ThreadLocal` type which allow a separate copy of an
object to be used for each thread. This allows for per-object thread-local
storage, unlike the standard library's `thread_local!` macro which only allows
static thread-local storage.

[Documentation](https://docs.rs/thread_local/)

## Usage

Add this to your `Cargo.toml`:

```toml
[dependencies]
thread_local = "1.1"
```

## Minimum Rust version

This crate's minimum supported Rust version (MSRV) is 1.63.0.

## License

Licensed under either of

 * Apache License, Version 2.0, ([LICENSE-APACHE](https://github.com/Amanieu/thread_local-rs/blob/HEAD/LICENSE-APACHE) or http://www.apache.org/licenses/LICENSE-2.0)
 * MIT license ([LICENSE-MIT](https://github.com/Amanieu/thread_local-rs/blob/HEAD/LICENSE-MIT) or http://opensource.org/licenses/MIT)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be dual licensed as above, without any
additional terms or conditions.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:threadlocal-kotlin:0.2.1")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same Apache-2.0 license as the upstream [`Amanieu/thread_local-rs`](https://github.com/Amanieu/thread_local-rs). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the thread_local-rs authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`Amanieu/thread_local-rs`](https://github.com/Amanieu/thread_local-rs) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
