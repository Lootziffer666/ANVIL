# EXECUTION_CORE_ARCHITECTURE — Gate AT4

> Gate: AT4 — Anvil Execution Core Skeleton  
> Status: `done`  
> Last updated: 2026-05-10

---

## Purpose of the Execution Core

The Execution Core is the native runtime engine that will power Anvil's ability to plan, decompose, execute, and track work against a codebase.

It is **not** a plugin, not an external module, and not a donor codebase wrapper.  
It is the internal engine that makes Anvil a workbench that can act, not just display.

---

## What the Execution Core Does

- Accepts a **Plan** and decomposes it into **Tasks**
- Manages **Runs** — isolated execution contexts for each Task
- Provides controlled **file mutation** capabilities (read, write, edit with diff tracking)
- Provides controlled **command execution** via Command Guard (allowlisted, scoped, audited)
- Routes LLM requests through **Provider Core** (Anvil's own provider abstraction)
- Produces **Artifacts** — the outputs of work (files, diffs, logs, reports)
- Enforces **safety** at every layer (Review Gate, Command Guard, scope restrictions)
- Manages **branches and PRs** through Branch / PR Engine (with safety policy)

---

## What the Execution Core Does NOT Do

- Does not provide a UI — the UI is `app/` (existing Anvil HTML/CSS/JS)
- Does not manage workspace metadata — that is the Workspace Model (`docs/WORKSPACE_MODEL.md`)
- Does not handle module discovery or launching — that is The Forge
- Does not replace existing Anvil features — it extends them with execution capability
- Does not directly execute arbitrary shell commands — all execution goes through Command Guard
- Does not store secrets in plaintext — all credential management uses encrypted storage
- Does not auto-merge PRs — all PRs require human review

---

## Why It Is Not a Module

Modules in Anvil (see `docs/MODULE_CONTRACT.md`) are pluggable features loaded via The Forge.  
The Execution Core is **infrastructure** — it sits below modules and powers their execution capabilities.

A module might use the Execution Core to run a task.  
The Execution Core is not a module itself.

This distinction matters because:
1. Modules can be installed/removed. The Execution Core cannot.
2. Modules have a `module.json` manifest. The Execution Core has architecture docs.
3. Modules live in `modules/`. The Execution Core lives in `src/core/`.

---

## Relationship to Workspace / The Forge / Artifacts

```text
┌─────────────────────────────────────┐
│  The Forge (Launchpad)              │
│  ┌───────────┐  ┌───────────┐      │
│  │ Module A  │  │ Module B  │ ...  │
│  └─────┬─────┘  └─────┬─────┘      │
│        │               │            │
│  ┌─────▼───────────────▼─────┐      │
│  │     Workspace Model       │      │
│  │  (data.js, sync.js)       │      │
│  └─────────────┬─────────────┘      │
│                │                    │
│  ┌─────────────▼─────────────┐      │
│  │   Execution Core          │      │
│  │   (src/core/)             │      │
│  │                           │      │
│  │  Plan → Task → Run        │      │
│  │  → Artifacts              │      │
│  └───────────────────────────┘      │
└─────────────────────────────────────┘
```

- **Workspace** provides context (which files, which project, current state)
- **The Forge** launches modules and (future) triggers execution
- **Execution Core** performs the work
- **Artifacts** are the output, stored via the Artifact Output Layer (`docs/ARTIFACT_OUTPUT_LAYER.md`)

---

## Relationship to Plan / Task / Run

```text
Plan (goal + constraints)
  │
  ├── Task 1 (unit of work, depends on nothing)
  │     └── Run 1.1 (execution session)
  │           ├── File mutations
  │           ├── Commands (via Command Guard)
  │           ├── Provider calls
  │           └── Artifacts produced
  │
  ├── Task 2 (depends on Task 1)
  │     └── Run 2.1
  │
  └── Task 3 (depends on Task 2)
        └── Run 3.1
```

- A **Plan** defines the goal and constraints
- **Tasks** are the atomic units of work, with dependencies
- **Runs** are the execution sessions for each task
- Each Run produces Artifacts and logs

See also:
- [`INTERNAL_EXECUTION_PLAN.md`](INTERNAL_EXECUTION_PLAN.md) — Plan schema
- [`TASK_GRAPH_MODEL.md`](TASK_GRAPH_MODEL.md) — Task schema
- [`RUN_MODEL.md`](RUN_MODEL.md) — Run schema

---

## Relationship to Provider Core

Provider Core is the Anvil-native LLM abstraction layer.

- Execution Core uses Provider Core to make LLM requests
- Provider Core handles model selection, streaming, token tracking
- Provider Core manages credentials via encrypted storage (not plaintext)
- Provider Core replaces and extends Anvil's existing Provider Registry (`app/data.js`)

Provider Core is **part of** the Execution Core (`src/core/providers/`), not separate.

---

## Relationship to File Mutation Engine

File Mutation Engine handles all file operations during a Run:

- Read files (with scope validation)
- Write files (with diff tracking and mutation log)
- Edit files (search-and-replace with diff tracking)
- Rollback capability for all mutations
- Mutation log as part of Run artifacts

Lives at `src/core/execution/file-mutations/`.

---

## Relationship to Command Guard

Command Guard is the safety layer for shell command execution:

- Maintains an **allowlist** of permitted commands
- Maintains a **blocklist** of forbidden commands
- Validates command scope (working directory)
- Logs all commands and outputs
- Requires explicit safety policy before activation
- No `sh -c` with arbitrary input — ever

Lives at `src/core/execution/commands/`.

---

## Relationship to Branch / PR Engine

Branch / PR Engine manages git automation with safety:

- Creates branches with validated naming conventions
- Manages worktrees for parallel task execution (future)
- Prepares PRs with structured descriptions
- Does **not** auto-merge — all PRs require human review via Review Gate
- Logs all git operations

Lives at `src/core/branching/`.

---

## Why Donor Code Must Not Be Scattered in Root

```text
❌ Wrong:
  /app/ogcode-plan.js        ← donor code in product UI
  /modules/ogcode-runner/    ← donor as module
  /ogcode/                   ← donor code at root level

✅ Correct:
  /src/core/planning/        ← Anvil-native plan engine
  /src/core/tasks/           ← Anvil-native task engine
  /src/core/runs/            ← Anvil-native run engine
  /docs/provenance/          ← donor audit trail
```

All Execution Core code lives under `src/core/`.  
All provenance documentation lives under `docs/provenance/`.  
Nothing in between.

---

## Why Donor Code Is Not Allowed as Runtime Dependency

1. **Identity:** Anvil is Anvil. It does not run another product inside itself.
2. **Safety:** Donor code has not been audited for Anvil's safety requirements.
3. **Maintenance:** A runtime dependency creates an ongoing sync obligation.
4. **Architecture:** Donor is Go CLI + local server. Anvil is browser-based. Incompatible runtimes.
5. **Naming:** Runtime dependency would leak donor terms into Anvil's active codebase.
6. **License:** Even though MIT is permissive, runtime coupling is an unnecessary risk vector.

The correct path is REWRITE: study the pattern, implement it natively, own the result.

---

## Directory Structure

```text
src/core/
├── planning/          ← Plan Engine (plan lifecycle, lock, breakdown trigger)
├── tasks/             ← Task Engine (task model, dependencies, status)
├── runs/              ← Run Engine (execution sessions, message tracking)
├── repo/              ← Repo Context Engine (file discovery, content search)
├── execution/
│   ├── file-mutations/ ← File Mutation Engine (read, write, edit, diff, rollback)
│   └── commands/       ← Command Guard (allowlisted shell execution)
├── providers/         ← Provider Core (LLM abstraction, model routing, credentials)
├── artifacts/         ← Artifact Engine (output storage, manifest, registry)
├── safety/            ← Safety Engine (permissions, Review Gate, scope validation)
└── branching/         ← Branch / PR Engine (branch management, PR preparation)
```

Each directory contains a `README.md` explaining its purpose and current status.

---

## Cross-References

- [`INTERNAL_EXECUTION_PLAN.md`](INTERNAL_EXECUTION_PLAN.md) — Plan data model
- [`TASK_GRAPH_MODEL.md`](TASK_GRAPH_MODEL.md) — Task data model
- [`RUN_MODEL.md`](RUN_MODEL.md) — Run data model
- [`CODEBASE_TRANSPLANT_RULES.md`](CODEBASE_TRANSPLANT_RULES.md) — Transplant rules
- [`provenance/TRANSPLANT_MAP.md`](provenance/TRANSPLANT_MAP.md) — What to transplant
- [`WORKSPACE_MODEL.md`](WORKSPACE_MODEL.md) — Workspace context
- [`ARTIFACT_OUTPUT_LAYER.md`](ARTIFACT_OUTPUT_LAYER.md) — Artifact system
- [`MODULE_CONTRACT.md`](MODULE_CONTRACT.md) — Module system (Execution Core is NOT a module)
