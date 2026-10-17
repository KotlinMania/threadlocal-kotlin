# Immediate Actions - Porting and Symbol Parity Report

Based on AST analysis and symbol parity audit between `tmp/thread_local-1.1.9/src` and `src/`:

## Summary

- **Production definitions:** 66/66 matched (100.0%)
- **Test definitions:** 14/14 matched (100.0%)
- **Functions:** 2/2 matched (100.0%) (`allocate_bucket` -> `allocateBucket`, `deallocate_bucket` -> `deallocateBucket`)
- **Structs:** 11/11 matched (100.0%) (`CachedThreadLocal`, `CachedIterMut`, `CachedIntoIter`, `ThreadIdManager`, `Thread`, `ThreadLocal`, `Entry`, `RawIter`, `Iter`, `IterMut`, `IntoIter`)
- **Traits:** 2/2 matched (100.0%) (`UncheckedOptionExt`, `UncheckedResultExt`)
- **Impl Methods:** 51/51 matched (100.0%) (including `drop`, `next_mut` -> `nextMut`, `new`, `default`, `with_capacity`, `get`, `get_or`, `get_or_try`, `clear`, `into_iter`, `size_hint`, `size_hint_frozen`, etc.)
- **Constants:** 4/4 matched (100.0%) (`POINTER_WIDTH`, `BUCKETS`)
- **Test Parity:** 14/14 test definitions matched (100.0%) (including `same_thread`, `different_thread`, `iter`, `miri_iter_soundness_check`, `test_drop`, `test_earlyreturn_buckets`, `is_sync`, `test_thread`, `make_create`, `foo`, `Dropped`)
- **Module-root headers:** Clean. Module-root port-lint headers pointing to `lib.rs` removed in compliance with AGENTS.md §3 to prevent `ast_distance` stub zeroing.

## Detailed Work Completed

1. **Drop & Deallocation Parity:**
   - Implemented `Entry.drop()` to release values and invoke `AutoCloseable.close()` when supported.
   - Implemented `deallocateBucket(bucket, size)` in `ThreadLocal.kt` to mirror `deallocate_bucket` from `lib.rs:520`.
   - Implemented `ThreadLocal.drop()` to release all buckets upon explicit drop or `clear()`.

2. **Iterator Parity:**
   - Added `nextMut(threadLocal)` to `RawIter` with camelCase parity for `next_mut`.
   - Wired `IterMut` to call `nextMut` directly.

3. **Test Suite Parity:**
   - Updated `testDrop` and `testEarlyreturnBuckets` in `ThreadLocalTest.kt` to test deterministic drop semantics and bucket draining.
   - Added `foo` generic witness check to `isSync`.
   - Replicated `CrossThreadTest` in `src/jvmTest/` for multi-threaded validation on JVM alongside existing native and Android host tests.

4. **Build & Gate Verification:**
   - `./gradlew test` passes 100% across JVM, JS, Wasm-JS, Wasm-WASI, and native targets including Swift Export SPM harness tests.
   - `./gradlew build` completes successfully across all retained KotlinMania targets.
