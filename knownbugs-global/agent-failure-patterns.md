# Agent Failure Patterns

Recurring AI-agent failure patterns observed across projects.

## Current Patterns

- scope creep when gate prompts are vague
- repeated local build-environment blockers despite CI verifier
- push/PR failure due to missing credentials
- stale PR titles after branch reuse
- continuing to next gate after current gate is not actually remote/merged
- treating synthetic/agent-generated data as gold data

## Countermeasures

- one gate per run
- CI-first policy
- push failure means patch export and stop
- PR title must match highest gate implemented
- no future-gate work
- explicit handoff after every gate
- knownbugs are append-only memory, not shame
