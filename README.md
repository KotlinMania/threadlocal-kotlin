# threadlocal-kotlin

Kotlin Multiplatform thread-local and execution-local storage, starting as a
port of Rust's `thread_local` crate.

This repository is Kotlin Native first, then the rest of KMP. Android
and JVM-adjacent targets are allowed as targets, but the common source
stays Kotlin-only and does not use JVM APIs.

## Status

The project is scaffolded. The Kotlin API shape is intentionally not
frozen yet.

## Build

```bash
./gradlew compileKotlinMacosArm64
./gradlew macosArm64Test
```

The build template follows the KotlinMania KMP targets: macOS, Linux,
MinGW, iOS, JS, WASM, and Android library.

## Coordinates

Planned Maven coordinates:

```kotlin
implementation("io.github.kotlinmania:threadlocal-kotlin:0.1.1")
```

## License

Dual-licensed Apache-2.0 OR MIT, matching the upstream Rust crate.
