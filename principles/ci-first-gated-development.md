# CI-First Gated Development

ANVIL projects are built one gate at a time.

Agents may write code. CI judges whether the project still stands.

## Core Rule

```text
Agent builds what the gate allows. CI verifies the result.
```

## Gate Rules

- one gate per run
- no future-gate work
- no broad refactors inside a gate
- tests for pure logic
- handoff after every gate
- knownbugs updated for real issues
- commit once per coherent gate

## CI-First Rule

A missing local Android SDK or missing local build environment is not a product bug when GitHub Actions is the intended verifier.

Do not keep reopening the same local environment blocker if CI is green.

## Push Failure Rule

If push or PR creation fails because credentials are unavailable:

1. stop implementation
2. export a patch
3. document what was completed
4. do not start the next gate
