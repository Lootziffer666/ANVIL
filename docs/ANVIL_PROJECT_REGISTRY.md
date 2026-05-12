# ANVIL_PROJECT_REGISTRY.md

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

## Purpose

Anvil knows its projects. Every project gets an entry before work begins. No project is worked on without a registry entry. No entry is silently removed.

---

## Registry Entry Schema

```json
{
  "id": "short-kebab-id",
  "name": "Display Name",
  "repo": "owner/repo-name or null if not yet on GitHub",
  "status": "active | paused | archived | planned",
  "gate_phase": "current gate or milestone description",
  "notes": "one-sentence context or open conflict"
}
```

---

## Start Projects

| ID | Name | Repo | Status | Gate Phase | Notes |
|---|---|---|---|---|---|
| `catchit` | CatchIt | TBD | `active` | Planning | State-surface UX + mobility trust tool |
| `flow` | FLOW | TBD | `active` | Planning | Language toolchain — part of FLOW family |
| `spin` | SPIN | TBD | `active` | Planning | Language toolchain — part of FLOW family |
| `loom` | LOOM | TBD | `active` | Planning | Language toolchain — part of FLOW family |
| `smash` | SMASH | TBD | `active` | Planning | Language toolchain — part of FLOW family |
| `borderline` | Borderline | TBD | `active` | Planning | Android context reduction / Zen-OS layer |
| `zenos` | ZenOS | TBD | `planned` | Concept | Minimal Android OS layer; scope TBD |
| `grid` | GRID | TBD | `planned` | Concept | Purpose TBD — awaiting gate definition |
| `kids-launcher` | Kids Launcher | TBD | `planned` | Concept | Android launcher for children |
| `tabula` | Tabula | TBD | `active` | Planning | Workspace cleanup utility |
| `pdf-splitter` | PDF Splitter | TBD | `planned` | Concept | PDF processing tool |
| `iig` | Ink & Iron Glow | TBD | `active` | Separate repo track | Tattoo-studio brand — NOT an Anvil module |

---

## Notes on Specific Entries

### Ink & Iron Glow (iig)
IIG is listed here only for tracking purposes. It is **not** an Anvil subsystem. It has its own design system, repos, and governance track. No IIG branding enters the Anvil codebase. See [`docs/ANVIL_CONCEPT_CONTRACT.md`](ANVIL_CONCEPT_CONTRACT.md) for the canonical Anvil ≠ IIG boundary.

### FLOW Family (flow, spin, loom, smash)
These four share a language-toolchain concept. They are tracked separately because they may end up in separate repos. The family relationship is documented in [`projects/flow-family.md`](../projects/flow-family.md).

### ZenOS / GRID
These are placeholder entries. They exist in the registry so work does not begin without a gate. If no gate is defined within two milestone reviews, consider archiving.

---

## Rules

1. A project must have a registry entry before any implementation work begins.
2. `status` transitions are documented in commit messages.
3. No entry is silently deleted. Archive instead of delete. If deletion is necessary, add a `removed_at` timestamp and `removed_reason`.
4. `notes` field must be updated when a conflict or blocker is discovered.
5. `repo` field is updated as soon as the project has a remote.

---

## Registry File

The machine-readable registry lives at:

```
src/core/registry/projects.json
```

This file is maintained by the `registry` subsystem (v0: skeleton). In v0, updates are manual.

---

## Cross-References

- [`projects/`](../projects/) — extended project descriptions
- [`src/core/registry/README.md`](../src/core/registry/README.md) — implementation home
- [`src/core/types/project.schema.json`](../src/core/types/project.schema.json) — JSON schema
