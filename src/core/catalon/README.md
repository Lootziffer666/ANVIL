# CATALON — Agent / Workflow Orchestration

> Status: Skeleton (Gate 001)  
> Named entry point. Implementation spreads across `src/core/planning/`, `tasks/`, `runs/`.

CATALON is Anvil's orchestration layer. It manages the Plan → Task → Run model: decomposing goals into tasks, isolating execution in runs, and tracking progress.

## Named System → Implementation Mapping

| Named System | Implementation Home |
|---|---|
| CATALON | `src/core/catalon/` (this dir, entry point) |
| Planning Engine | `src/core/planning/` |
| Task Engine | `src/core/tasks/` |
| Run Engine | `src/core/runs/` |

The `catalon/` directory holds the orchestration contract. The three engine directories hold the implementations.

## Responsibilities

- Decompose a goal into a Plan
- Decompose a Plan into Tasks
- Execute Tasks as isolated Runs
- Track run state and hand off to DEAFPIPER for inter-agent transitions

## v0 Status

- Schema: [`src/core/types/agent-run.schema.json`](../types/agent-run.schema.json)
- Run model: [`docs/RUN_MODEL.md`](../../../docs/RUN_MODEL.md) (Gate AT4)
- Spec: [`docs/ANVIL_RUN_STATE.md`](../../../docs/ANVIL_RUN_STATE.md)
- Architecture: [`docs/EXECUTION_CORE_ARCHITECTURE.md`](../../../docs/EXECUTION_CORE_ARCHITECTURE.md) (Gate AT4)

## Rules

- CATALON does not modify files directly — it creates Runs that use File Mutation Engine
- CATALON does not call providers directly — all model calls go through OPENDORK
- A Run cannot start without a valid Task, and a Task cannot start without a valid Plan
