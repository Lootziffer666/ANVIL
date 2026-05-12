# ANVIL_CORE_MANIFEST.md

**Gate:** 001 — Core Skeleton  
**Stand:** 2026-05-12  
**Status:** Verbindlich

---

> Default to absence. Do not add visible UI unless the current state requires it. Empty space is an active design state, not unused space.
>
> Every visible element must justify its existence by reducing user burden right now.
>
> Before adding anything, remove. Before proposing UI, justify absence. Before creating controls, name the state that makes them necessary. If no state requires the element, do not render it.

---

## What Anvil Is

Anvil is a **local workbench for agentic work**.

It is the machine on which work gets done: repos get orchestrated, providers get routed, gates get executed, artifacts get tracked, and projects get governed.

Anvil is a KI-native IDE. It provides a structured environment where human-agent collaboration happens in auditable, gate-controlled sessions. Every action leaves a trace. Every output has an ID. Every run has a recoverable state.

---

## What Anvil Is Not

| Not this | Why |
|---|---|
| A dashboard | No info-dump. State surface only. |
| An app store | Anvil builds tools; it does not sell them. |
| A marketing surface | No branding surface. |
| Ink & Iron Glow | IIG is a tattoo-studio brand. Anvil is a dev workbench. |
| Anvil-Bellows | Bellows is an IIG project. Do not reference it here. |
| A framework | Anvil is a workbench, not a library others import. |
| A generic "shared" namespace | Every component has a named owner. |

---

## Why Anvil Exists

Christian needs a single, stable machine for:

1. **Multi-repo work** — switching between 12+ active projects without losing state.
2. **Agent orchestration** — running AI agents with controlled scope, logged actions, and safe recovery.
3. **Provider routing** — choosing and switching between LLM providers without rewriting workflows.
4. **Gate-controlled quality** — no feature ships without a passing gate.
5. **Artifact ownership** — every output is named, timestamped, and traceable.

No existing tool provides all five with the control and auditability Anvil requires.

---

## Problems Anvil Solves

| Problem | Anvil's Answer |
|---|---|
| Agent claims "done" with no proof | Run State + Artifact Store — nothing is done without an artifact |
| Provider lock-in | OPENDORK — multi-provider routing, no hard binding |
| Scope creep in agent runs | CATALON-GUARD — gate enforcement, kill criteria |
| Lost context between sessions | DEAFPIPER — structured handoffs |
| Unknown project state | Project Registry — known projects, tracked status |
| Untraceable outputs | Artifact Store — every output has an ID and manifest |

---

## Systems That Live Inside Anvil

| System | Function | Implementation Home |
|---|---|---|
| **OPENDORK** | Provider/API routing, model selection, fallbacks | `src/core/opendork/` → `src/core/providers/` |
| **CATALON** | Agent/workflow orchestration, plan-task-run model | `src/core/catalon/` → `src/core/planning/`, `tasks/`, `runs/` |
| **CATALON-GUARD** | Guardrails, gate execution, quality control | `src/core/catalon-guard/` → `src/core/safety/` |
| **DEAFPIPER** | Structured handoffs, agent-to-agent command channel | `src/core/deafpiper/` |
| **Repo Governance** | Branches, gate logs, audit trails | `src/core/gates/` |
| **Artifact Store** | Outputs, reports, patches, logs, manifests | `src/core/artifacts/`, `outputs/` |
| **Run State** | Traceable agent runs with recovery | `src/core/run-state/` → `src/core/runs/` |
| **Project Registry** | Known projects and repos | `src/core/registry/` |

---

## v0 Limits

This is the skeleton. The following are intentionally absent in v0:

- No execution code in any subsystem
- No running provider routing
- No live gate execution engine
- No agent orchestration runtime
- No artifact manifest generator
- No project registry service

v0 delivers: docs, contracts, schemas, and directory structure. The skeleton is the commitment. Implementation fills it gate by gate.

---

## Canonical Terminology

See [`docs/ANVIL_CONCEPT_CONTRACT.md`](ANVIL_CONCEPT_CONTRACT.md) for all binding term definitions.

State surface grammar, module contract, and workspace model are defined in their own contract files. This manifest does not redefine them.

---

## Related Contracts

- [`docs/ANVIL_MODULE_MAP.md`](ANVIL_MODULE_MAP.md) — module matrix
- [`docs/ANVIL_GOVERNANCE.md`](ANVIL_GOVERNANCE.md) — gate principle and stop rules
- [`docs/MODULE_CONTRACT.md`](MODULE_CONTRACT.md) — module slot contract
- [`gates/GATE_001_CORE_SKELETON.md`](../gates/GATE_001_CORE_SKELETON.md) — this gate's spec
