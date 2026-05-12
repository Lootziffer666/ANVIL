# Gates — Gate Execution Engine

> Status: Skeleton (Gate 001)  
> No implementation yet.

This directory owns the **gate execution engine** — the runtime that reads gate specs, checks acceptance criteria, evaluates kill criteria, and records gate outcomes.

## Responsibilities

- Parse gate spec files from `gates/GATE_{NNN}_{NAME}.md`
- Validate that acceptance criteria are met (file existence, test results, artifact presence)
- Evaluate kill criteria and halt execution if triggered
- Record gate outcomes as artifacts

## What This Is Not

- Not the gate spec files themselves (those live in `gates/` at repo root)
- Not CATALON-GUARD (which enforces scope at run time) — this validates gate-level outcomes

## Gate Status Flow

```
pending → in_progress → done
                      → blocked (kill criteria triggered)
                      → failed (acceptance not met)
```

## v0 Status

Gate tracking is currently manual (GATES.md + git log).  
This engine will automate gate status verification once the Execution Core is active.

Schema: [`src/core/types/gate.schema.json`](../types/gate.schema.json)  
Gate specs: [`gates/`](../../../gates/)  
Master index: [`GATES.md`](../../../GATES.md)

## Rules

- A gate may not auto-advance to `done` — human confirmation required
- Kill criteria trigger an immediate halt, not a warning
- Gate outcomes are stored as artifacts in `outputs/`
