# Run State — Run State Persistence

> Status: Skeleton (Gate 001)  
> Named entry point. Implementation lives in `src/core/runs/`.

This directory owns the **run state persistence layer** — reading and writing the state of active and completed runs so that any actor (human or agent) can resume safely after a failure.

## Named System → Implementation Mapping

| Named System | Implementation Home |
|---|---|
| Run State (persistence) | `src/core/run-state/` (this dir, entry point) |
| Run Engine | `src/core/runs/` (implementation) |

The `run-state/` directory holds the persistence contract. The `runs/` directory holds the run execution engine.

## Responsibilities

- Write run state to disk at each transition
- Read and restore run state for recovery scenarios
- Expose the last known safe state to DEAFPIPER for handoff package creation
- Maintain a run index (which runs exist, their status, their artifacts)

## v0 Status

- Schema: [`src/core/types/agent-run.schema.json`](../types/agent-run.schema.json)
- Full run model: [`docs/RUN_MODEL.md`](../../../docs/RUN_MODEL.md) (Gate AT4)
- Run state spec: [`docs/ANVIL_RUN_STATE.md`](../../../docs/ANVIL_RUN_STATE.md)

## Recovery Principle

A run in `failed` state must have:
1. `failure_state` — exact error text
2. `recovery_step` — concrete next action

This data must be written before the run terminates. No silent failures.

## Rules

- Run state is written before any file mutation begins (pre-mutation snapshot)
- Run state is updated after each significant step (not only at completion)
- A failed run's state is never deleted — it is preserved for investigation
