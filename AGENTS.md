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

## Trait default methods with `where` clauses → method-level Kotlin generic bounds

Rust traits routinely declare a default method whose body only typechecks
when the type parameter satisfies a stricter bound:

```rust
pub trait RangeBounds<T> {
    fn start_bound(&self) -> Bound<&T>;
    fn end_bound(&self) -> Bound<&T>;

    fn is_empty(&self) -> bool
    where T: PartialOrd,
    { /* default body uses < */ }
}
```

The trait stays unconstrained; the *method* picks up the bound via its
own `where` clause. Kotlin has no per-method `where` on an interface
member. Three obvious mappings fail:

1. **Tighten the interface to `<T : Comparable<T>>`.** Breaks every
   caller that holds the unbounded interface type.
2. **Make the method abstract on the interface.** Forces every concrete
   impl to invent a body and pile on `override` boilerplate, even when
   the Rust counterpart inherits the default unchanged.
3. **Runtime cast helper** — `if (left is Comparable<*> ...) ... else throw IllegalStateException(...)`.
   Compile-time bounds become runtime crashes; the cheat detector flags
   this and zeros the file's score.

### The faithful pattern

Translate the default to a Kotlin **extension function whose own type
parameter carries the bound**:

```kotlin
interface RangeBounds<T> {
    fun startBound(): Bound<T>
    fun endBound(): Bound<T>
}

fun <T : Comparable<T>> RangeBounds<T>.isEmpty(): Boolean { /* default body */ }
```

Concrete impls that want to specialise the default supply a same-named
**member function**. Kotlin resolves `range.isEmpty()` to the member
when the static receiver type is the concrete class and to the
extension when it is the interface — exactly mirroring Rust's
"default method, per-impl override". No `override` keyword on the
member; there is nothing on the interface to override.

Recipe:

1. Interface keeps only the methods declared without where-clauses.
2. Each default-method-with-where-clause becomes a Kotlin extension
   whose own type-parameter bound mirrors the where-clause.
3. Concrete subtypes specialise by declaring a same-named member.
4. Callers holding the unbounded interface type cannot invoke the
   comparison-using methods — correct, Rust would reject the same
   call without the bound.

### Pair with the dual-overload pattern when both paths are needed

When a function has to work in both the comparator-aware and natural-order
paths, expose two overloads — the unbounded one takes the comparator
explicitly, the bounded one is sugar:

```kotlin
internal fun <Q> Tree.search(key: Q, compare: (Stored, Q) -> Int): Hit { /* heavy */ }

internal fun <Q : Comparable<Q>> Tree.search(key: Q): Hit
    where Stored : Comparable<Q> =
    search(key) { stored, query -> stored.compareTo(query) }
```

Heavy lifting in the comparator overload; natural-order overload is a
one-line delegation. The canonical implementation lives in
[`btree-kotlin`](../btree-kotlin/) `Search.kt::searchTree` /
`searchNode` / `findLowerBoundEdge` / `findUpperBoundEdge` and
`Navigate.kt::searchTreeForBifurcation` / `lowerBound` / `upperBound`.

### Why this is faithful, not engineering

- Interface mirrors Rust's trait declaration shape exactly.
- Extension's bound mirrors Rust's `where` clause exactly.
- Concrete-class members shadow the extension exactly the way Rust
  inherent-impl methods override a trait default.
- "Unbounded callers can't use these methods" mirrors Rust's
  compile-time rejection without the bound.
- No runtime casts, no `IllegalStateException`, no `is Comparable<*>`.

### When you cannot apply this

When the bound is on a *class* type parameter (e.g. `impl<K: Ord> Map<K, V>`),
Kotlin has no method-level analog — class type parameters bind for the
whole class. Use the `Comparator<in K>` field pattern with a
`compareKeys(a, b)` dispatch helper that prefers the supplied
comparator and falls back to a `Comparable<K>`-based path. The fallback
is the design contract, not a translation hack.

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
