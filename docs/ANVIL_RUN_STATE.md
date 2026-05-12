# ANVIL_RUN_STATE.md

**Gate:** 001 — Core Skeleton  
**Stand:** 2026-05-12  
**Status:** Verbindlich (Spec)

---

> Default to absence. Do not add visible UI unless the current state requires it. Empty space is an active design state, not unused space.
>
> Every visible element must justify its existence by reducing user burden right now.
>
> Before adding anything, remove. Before proposing UI, justify absence. Before creating controls, name the state that makes them necessary. If no state requires the element, do not render it.

---

## Purpose

Every agent action in Anvil runs inside a traceable Run. A Run is not a log — it is a recoverable state. If a Run fails, the next human or agent action has enough information to continue safely.

Full schema reference: [`docs/RUN_MODEL.md`](RUN_MODEL.md) (Gate AT4).  
This document defines the required fields and naming convention from the mission perspective.

---

## Required Fields

Every Run must carry these fields. No field may be omitted:

| Field | Type | Description |
|---|---|---|
| `run_id` | string | Unique run identifier — format: `RUN_{YYYYMMDD}_{HHMMSS}_{NNN}` |
| `started_at` | ISO 8601 | When this run started |
| `agent` | string | Which agent or tool executed this run |
| `tool` | string | Tool name (e.g. `claude-code`, `viktor-ai`, `bash`) |
| `input` | string or object | What was given to the run (prompt, task description, or structured input) |
| `goal` | string | What the run was trying to achieve — one sentence |
| `changed_files` | object[] | Every file modified, with action (`create/modify/delete`) and diff |
| `tests_run` | object[] | Tests executed with their pass/fail status |
| `failure_state` | string | If status is `failed`: exact error text |
| `recovery_step` | string | If status is `failed`: next safe action for human or agent |
| `status` | string | `stable | adapting | act_now | failed` |

---

## Run ID Naming

```
RUN_{YYYYMMDD}_{HHMMSS}_{NNN}
```

- `YYYYMMDD` — date of run start (UTC)
- `HHMMSS` — time of run start (UTC)
- `NNN` — three-digit sequence number within that second (000–999)

Example: `RUN_20260512_143000_001`

Runs are sortable by ID. No two runs within the same session may share an ID.

---

## State Machine

```
created → adapting → stable
                   → act_now → adapting (after human resolution)
                   → failed
```

A Run in `failed` state:
- Must have `failure_state` populated with the exact error.
- Must have `recovery_step` populated with a concrete next action.
- Must not be silently restarted. A new Run must be created with a new ID.

---

## Changed File Entry

```json
{
  "path": "relative/path/from/repo/root",
  "action": "create | modify | delete",
  "diff": "unified diff string or null if create/delete",
  "timestamp": "ISO 8601",
  "rollback_available": true
}
```

---

## Recovery Principle

A Run's job is not just to execute — it is to leave the system in a state where the next actor (human or agent) can continue safely.

If a Run cannot finish:
1. Write `failure_state` with the exact error.
2. Write `recovery_step` with the concrete action.
3. Do not delete partial artifacts.
4. Set status to `failed`.
5. Stop.

The recovery_step is actionable: "Run `git reset --hard HEAD~1` to undo the last commit" — not "try again".

---

## Rules

1. Every Run links to exactly one Task and one Plan.
2. `human_review_required` defaults to `true`. It may only be `false` if the gate spec explicitly permits autonomous execution.
3. All file mutations are logged in `changed_files`.
4. A Run cannot modify files outside the Workspace scope.
5. A Run cannot execute commands not permitted by CATALON-GUARD's Command Guard.

---

## Cross-References

- [`docs/RUN_MODEL.md`](RUN_MODEL.md) — full schema (Gate AT4)
- [`docs/EXECUTION_CORE_ARCHITECTURE.md`](EXECUTION_CORE_ARCHITECTURE.md) — where Runs fit in the architecture
- [`src/core/run-state/README.md`](../src/core/run-state/README.md) — implementation home
- [`src/core/types/agent-run.schema.json`](../src/core/types/agent-run.schema.json) — JSON schema
