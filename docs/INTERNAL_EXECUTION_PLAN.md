# INTERNAL_EXECUTION_PLAN — Gate AT4

> Gate: AT4 — Anvil Execution Core Skeleton  
> Status: `done`  
> Last updated: 2026-05-10

---

## Purpose

Defines the data model for an Execution Plan in Anvil.  
A Plan is the top-level container that captures a goal, constraints, and the expected shape of work.

---

## Plan Schema

```json
{
  "plan_id": "",
  "workspace_id": "",
  "goal": "",
  "current_state": "",
  "allowed_files": [],
  "forbidden_files": [],
  "tasks": [],
  "dependencies": [],
  "expected_outputs": [],
  "kill_criteria": [],
  "tests_required": [],
  "human_review_required": true
}
```

---

## Field Definitions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `plan_id` | string | yes | Unique identifier for this plan |
| `workspace_id` | string | yes | The Workspace this plan operates in |
| `goal` | string | yes | What this plan aims to achieve |
| `current_state` | string | yes | Description of the current state before execution |
| `allowed_files` | string[] | no | Files/patterns that may be modified. Empty = no restriction (dangerous). |
| `forbidden_files` | string[] | no | Files/patterns that must not be modified. Takes precedence over `allowed_files`. |
| `tasks` | string[] | no | Task IDs belonging to this plan (populated after breakdown) |
| `dependencies` | object[] | no | External dependencies or prerequisites |
| `expected_outputs` | string[] | no | What artifacts the plan should produce |
| `kill_criteria` | string[] | yes | Conditions that must abort the plan immediately |
| `tests_required` | string[] | no | Tests that must pass before the plan is considered complete |
| `human_review_required` | boolean | yes | Whether human review is required before plan execution. Default: `true`. |

---

## Plan Lifecycle

```text
draft → open → locked → executing → completed
                                   → failed
                                   → killed
```

| State | Description |
|-------|-------------|
| `draft` | Plan is being written, not yet finalized |
| `open` | Plan is open for collaboration/review |
| `locked` | Plan is finalized, ready for task breakdown |
| `executing` | Tasks are being executed |
| `completed` | All tasks completed successfully |
| `failed` | One or more tasks failed |
| `killed` | Plan was aborted due to kill criteria |

---

## Rules

1. A Plan must have at least one kill criterion
2. `human_review_required` defaults to `true` and must be explicitly set to `false`
3. A Plan cannot move to `locked` without a goal and current_state
4. A Plan cannot move to `executing` without at least one Task
5. `forbidden_files` always overrides `allowed_files`
6. A Plan in `killed` state cannot be restarted — create a new Plan

---

## Example

```json
{
  "plan_id": "plan-001",
  "workspace_id": "ws-anvil-main",
  "goal": "Add encrypted credential storage to Token Manager",
  "current_state": "Token Manager is prototype status. Keys stored in plaintext in localStorage.",
  "allowed_files": [
    "modules/token-manager/**",
    "app/data.js"
  ],
  "forbidden_files": [
    "app/index.html",
    "GATES.md"
  ],
  "tasks": [],
  "dependencies": [],
  "expected_outputs": [
    "Encrypted key storage implementation",
    "Migration path from plaintext to encrypted"
  ],
  "kill_criteria": [
    "Keys are still readable in plaintext after implementation",
    "Existing workspace functionality breaks"
  ],
  "tests_required": [
    "Encrypted keys cannot be read without unlock",
    "Existing keys are migrated on first run"
  ],
  "human_review_required": true
}
```

---

## Cross-References

- [`EXECUTION_CORE_ARCHITECTURE.md`](EXECUTION_CORE_ARCHITECTURE.md) — Architecture overview
- [`TASK_GRAPH_MODEL.md`](TASK_GRAPH_MODEL.md) — Task model
- [`RUN_MODEL.md`](RUN_MODEL.md) — Run model
