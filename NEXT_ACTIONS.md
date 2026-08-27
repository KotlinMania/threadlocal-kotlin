# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/4 (75.0%)
- **Function parity:** 30/50 matched (target 57) — 60.0%
- **Class/type parity:** 9/17 matched (target 14) — 52.9%
- **Combined symbol parity:** 39/67 matched (target 71) — 58.2%
- **Average inline-code cosine:** 0.40 (function body across 3 matched files)
- **Average documentation cosine:** 0.85 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 3 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. lib

- **Target:** `threadlocal.ThreadLocal`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 113806.4
- **Functions:** 20/30 matched (target 44)
- **Missing functions:** `drop`, `default`, `new`, `fmt`, `next_mut`, `deallocate_bucket`, `make_create`, `different_thread`, `miri_iter_soundness_check`, `foo`
- **Types:** 7/8 matched (target 11)
- **Missing types:** `Item`
- **Tests:** 4/8 matched

### 2. cached

- **Target:** `threadlocal.Cached`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 91705.9
- **Functions:** 7/12 matched (target 8)
- **Missing functions:** `default`, `new`, `fmt`, `next`, `size_hint`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `Item`, `IntoIter`, `CachedIterMut`, `CachedIntoIter`

### 3. thread_id

- **Target:** `internal.ThreadId`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 20605.9
- **Functions:** 3/4 matched (target 5)
- **Missing functions:** `new`
- **Types:** 1/2 matched
- **Missing types:** `Thread`
- **Tests:** 1/1 matched

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

