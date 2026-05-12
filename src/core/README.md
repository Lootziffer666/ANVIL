# Anvil Execution Core

> Status: Skeleton (Gate AT4 + Gate 001)  
> No active execution code exists yet.

This is the root of Anvil's native Execution Core.  
See [`docs/EXECUTION_CORE_ARCHITECTURE.md`](../../docs/EXECUTION_CORE_ARCHITECTURE.md) for the full architecture.

## Structure

```text
core/
├── planning/          ← Plan Engine (CATALON)
├── tasks/             ← Task Engine (CATALON)
├── runs/              ← Run Engine (CATALON)
├── run-state/         ← Run State Persistence → maps to runs/ (Gate 001)
├── repo/              ← Repo Context Engine
├── execution/
│   ├── file-mutations/ ← File Mutation Engine
│   └── commands/       ← Command Guard (CATALON-GUARD)
├── providers/         ← Provider Core (OPENDORK implementation)
├── opendork/          ← OPENDORK named entry point → maps to providers/ (Gate 001)
├── catalon/           ← CATALON named entry point → maps to planning/tasks/runs/ (Gate 001)
├── catalon-guard/     ← CATALON-GUARD named entry point → maps to safety/ (Gate 001)
├── deafpiper/         ← DEAFPIPER: structured handoffs & command channel (Gate 001)
├── modules/           ← Module Slot Registry (Gate 001)
├── registry/          ← Project & Workspace Registry (Gate 001)
├── gates/             ← Gate Execution Engine (Gate 001)
├── artifacts/         ← Artifact Engine
├── safety/            ← Safety Engine (CATALON-GUARD implementation)
├── branching/         ← Branch / PR Engine
└── types/             ← JSON Schema types (Gate 001)
```

## Named System Mapping

| Named System | Entry Point | Implementation |
|---|---|---|
| OPENDORK | `opendork/` | `providers/` |
| CATALON | `catalon/` | `planning/`, `tasks/`, `runs/` |
| CATALON-GUARD | `catalon-guard/` | `safety/` |
| DEAFPIPER | `deafpiper/` | (no existing skeleton — new) |
| Run State | `run-state/` | `runs/` |

Named entry points hold contracts and routing. Implementation directories hold code.

## Rules

- No donor code in this directory. Only Anvil-native implementations.
- All code must use Anvil terminology exclusively.
- See `docs/CODEBASE_TRANSPLANT_RULES.md` for transplant governance.
- No execution code until a gate explicitly authorizes it.
