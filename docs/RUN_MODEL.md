# RUN_MODEL — Gate AT4

> Gate: AT4 — Anvil Execution Core Skeleton  
> Status: `done`  
> Last updated: 2026-05-10

---

## Purpose

Defines the data model for a Run in Anvil.  
A Run is an isolated execution session for a single Task within a Plan.

---

## Run Schema

```json
{
  "run_id": "",
  "workspace_id": "",
  "plan_id": "",
  "task_id": "",
  "status": "stable | adapting | act_now | failed",
  "started_at": "",
  "finished_at": "",
  "artifacts": [],
  "logs": [],
  "failure_state": "",
  "recovery_step": "",
  "commands_run": [],
  "changed_files": [],
  "tests_run": [],
  "human_review_required": true
}
```

---

## Field Definitions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `run_id` | string | yes | Unique identifier for this run |
| `workspace_id` | string | yes | The Workspace this run operates in |
| `plan_id` | string | yes | The Plan this run belongs to |
| `task_id` | string | yes | The Task this run executes |
| `status` | string | yes | Current run status (see below) |
| `started_at` | string (ISO 8601) | yes | When the run started |
| `finished_at` | string (ISO 8601) | no | When the run finished (empty if still running) |
| `artifacts` | object[] | no | Artifacts produced by this run |
| `logs` | string[] | no | Execution log entries |
| `failure_state` | string | no | Description of failure (if status is `failed`) |
| `recovery_step` | string | no | Suggested recovery action (if status is `failed`) |
| `commands_run` | object[] | no | Commands executed via Command Guard |
| `changed_files` | object[] | no | Files modified during this run (with diffs) |
| `tests_run` | object[] | no | Tests executed and their results |
| `human_review_required` | boolean | yes | Whether human review is required. Default: `true`. |

---

## Run Status Values

| Status | Description |
|--------|-------------|
| `stable` | Run completed successfully, all checks passed |
| `adapting` | Run is in progress, actively executing |
| `act_now` | Run needs immediate attention (permission request, blocking error) |
| `failed` | Run failed — see `failure_state` and `recovery_step` |

---

## Run Lifecycle

```text
created → adapting → stable
                   → act_now → adapting (after resolution)
                   → failed
```

---

## Command Log Entry

```json
{
  "command": "",
  "working_dir": "",
  "exit_code": 0,
  "stdout": "",
  "stderr": "",
  "timestamp": "",
  "allowed_by": ""
}
```

`allowed_by` references the Command Guard rule that permitted this command.

---

## Changed File Entry

```json
{
  "path": "",
  "action": "create | modify | delete",
  "diff": "",
  "timestamp": "",
  "rollback_available": true
}
```

---

## Test Entry

```json
{
  "test_name": "",
  "status": "pass | fail | skip | error",
  "output": "",
  "timestamp": ""
}
```

---

## Rules

1. Every Run must be linked to exactly one Task and one Plan
2. `human_review_required` defaults to `true`
3. A Run in `failed` state must have `failure_state` populated
4. All commands must be logged in `commands_run`
5. All file mutations must be logged in `changed_files` with diffs
6. A Run cannot modify files outside the Workspace scope
7. A Run cannot execute commands not permitted by Command Guard

---

## Cross-References

- [`EXECUTION_CORE_ARCHITECTURE.md`](EXECUTION_CORE_ARCHITECTURE.md) — Architecture overview
- [`INTERNAL_EXECUTION_PLAN.md`](INTERNAL_EXECUTION_PLAN.md) — Plan model
- [`TASK_GRAPH_MODEL.md`](TASK_GRAPH_MODEL.md) — Task model
