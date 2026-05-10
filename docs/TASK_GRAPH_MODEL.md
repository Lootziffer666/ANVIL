# TASK_GRAPH_MODEL — Gate AT4

> Gate: AT4 — Anvil Execution Core Skeleton  
> Status: `done`  
> Last updated: 2026-05-10

---

## Purpose

Defines the data model for a Task in Anvil.  
A Task is the atomic unit of work within a Plan, executed via a Run.

---

## Task Schema

```json
{
  "task_id": "",
  "plan_id": "",
  "title": "",
  "purpose": "",
  "depends_on": [],
  "allowed_files": [],
  "forbidden_files": [],
  "expected_artifacts": [],
  "status": "queued | ready | running | blocked | done | failed | needs_review",
  "risk_level": "low | medium | high | critical",
  "human_review_required": true
}
```

---

## Field Definitions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `task_id` | string | yes | Unique identifier for this task |
| `plan_id` | string | yes | The Plan this task belongs to |
| `title` | string | yes | Concise imperative title (e.g., "Add encrypted storage") |
| `purpose` | string | yes | Why this task exists and what it achieves |
| `depends_on` | string[] | no | Task IDs this task depends on (must complete first) |
| `allowed_files` | string[] | no | Files/patterns this task may modify |
| `forbidden_files` | string[] | no | Files/patterns this task must not modify |
| `expected_artifacts` | string[] | no | What this task should produce |
| `status` | string | yes | Current task status (see below) |
| `risk_level` | string | yes | Risk assessment for this task |
| `human_review_required` | boolean | yes | Whether human review is required. Default: `true`. |

---

## Task Status Values

| Status | Description |
|--------|-------------|
| `queued` | Task is registered but dependencies are not met |
| `ready` | All dependencies are met, task can be started |
| `running` | Task has an active Run |
| `blocked` | Task cannot proceed (permission, error, external dependency) |
| `done` | Task completed successfully |
| `failed` | Task failed — see associated Run for details |
| `needs_review` | Task completed but requires human review before promotion |

---

## Task Lifecycle

```text
queued → ready → running → done
                         → failed
                         → needs_review → done
                                        → failed (if review rejects)
       → blocked → ready (after resolution)
```

---

## Risk Levels

| Level | Description | Review Requirement |
|-------|-------------|--------------------|
| `low` | Routine change, well-understood area | Standard review |
| `medium` | Non-trivial change, some uncertainty | Careful review |
| `high` | Significant change, security-relevant, or cross-cutting | Thorough review + safety check |
| `critical` | Security-critical, data-destructive, or irreversible | Mandatory human review before execution |

---

## Dependency Rules

1. Dependencies form a DAG (directed acyclic graph) — no circular dependencies
2. A task cannot move to `ready` until all `depends_on` tasks are `done`
3. If a dependency fails, dependent tasks move to `blocked`
4. `forbidden_files` from the Plan override task-level `allowed_files`

---

## Example

```json
{
  "task_id": "task-001",
  "plan_id": "plan-001",
  "title": "Implement AES-256 key encryption module",
  "purpose": "Replace plaintext key storage in Token Manager with encrypted storage using AES-256-GCM",
  "depends_on": [],
  "allowed_files": [
    "modules/token-manager/src/**"
  ],
  "forbidden_files": [
    "app/index.html"
  ],
  "expected_artifacts": [
    "modules/token-manager/src/crypto.js"
  ],
  "status": "queued",
  "risk_level": "high",
  "human_review_required": true
}
```

---

## Cross-References

- [`EXECUTION_CORE_ARCHITECTURE.md`](EXECUTION_CORE_ARCHITECTURE.md) — Architecture overview
- [`INTERNAL_EXECUTION_PLAN.md`](INTERNAL_EXECUTION_PLAN.md) — Plan model
- [`RUN_MODEL.md`](RUN_MODEL.md) — Run model
