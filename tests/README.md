# Tests

> Gate 001 — Core Skeleton

## What Tests Exist

| File | What It Checks | How to Run |
|---|---|---|
| `schema-check.js` | Required fields present and correctly typed for all 6 core schemas | `node tests/schema-check.js` |

## Design

Tests in this directory check **structure**, not behavior. No execution code is tested because none exists yet.

Each test validates that a fixture object satisfies the required fields of its schema. No external dependencies required — the validator is self-contained.

## Running Tests

```bash
node tests/schema-check.js
```

Exit code 0 = all checks pass.  
Exit code 1 = one or more checks failed (output shows which).

## When Tests Grow

As implementation gates add code, tests here should grow to cover:
- Module contract validation (can a module.json be loaded and validated?)
- Run state round-trips (can a run be written and read back?)
- Provider routing (does fallback logic behave correctly?)

For now: structural schema checks only.
