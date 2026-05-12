# GATE_001 — ANVIL Core Skeleton

**Gate:** 001  
**Name:** Core Skeleton  
**Status:** `done`  
**Branch:** `claude/anvil-core-skeleton-F4YXT`  
**Stand:** 2026-05-12  
**Commit:** `gate(001): establish anvil core skeleton`

---

## Purpose

Establish the minimal, stable ANVIL structure that later receives OPENDORK, CATALON, CATALON-GUARD, DEAFPIPER, and all governance systems.

This gate is docs-first and schema-first. No runtime code is written.

---

## Acceptance Criteria

- [ ] `gates/GATE_000_REPO_ACCESS.md` exists
- [ ] `gates/GATE_001_CORE_SKELETON.md` exists
- [ ] `docs/ANVIL_CORE_MANIFEST.md` exists with Design Canon block
- [ ] `docs/ANVIL_MODULE_MAP.md` exists with module matrix
- [ ] `docs/ANVIL_GOVERNANCE.md` exists with gate principle and stop rules
- [ ] `docs/ANVIL_PROVIDER_ROUTING.md` exists describing OPENDORK
- [ ] `docs/ANVIL_RUN_STATE.md` exists with all required fields
- [ ] `docs/ANVIL_ARTIFACT_STORE.md` exists with lifecycle states
- [ ] `docs/ANVIL_PROJECT_REGISTRY.md` exists with all 12 start projects
- [ ] `docs/ANVIL_KNOWNBUGS.md` exists with at least 6 ABUG entries
- [ ] `src/core/modules/README.md` exists
- [ ] `src/core/opendork/README.md` exists
- [ ] `src/core/catalon/README.md` exists
- [ ] `src/core/catalon-guard/README.md` exists
- [ ] `src/core/deafpiper/README.md` exists
- [ ] `src/core/registry/README.md` exists
- [ ] `src/core/gates/README.md` exists
- [ ] `src/core/run-state/README.md` exists
- [ ] `src/core/types/provider.schema.json` exists and is valid JSON
- [ ] `src/core/types/agent-run.schema.json` exists and is valid JSON
- [ ] `src/core/types/gate.schema.json` exists and is valid JSON
- [ ] `src/core/types/artifact.schema.json` exists and is valid JSON
- [ ] `src/core/types/project.schema.json` exists and is valid JSON
- [ ] `src/core/types/module-contract.schema.json` exists and is valid JSON
- [ ] `tests/schema-check.js` exits 0 when run with `node`
- [ ] No execution code added to any existing `src/core/` directory
- [ ] No UI changes to `app/`
- [ ] No changes to existing `modules/`

---

## Kill Criteria

This gate must not proceed if any of the following is true:

- Execution code (not schemas/docs) is written into any `src/core/` subdirectory
- The UI (`app/`) is modified
- Any existing module is modified
- A "shared" namespace or generic helper is introduced
- Anvil-Bellows or Mjölnir are referenced
- Any subsystem (OPENDORK, CATALON, etc.) is partially implemented rather than declared

---

## Files Changed

### New directories
- `gates/`
- `src/core/modules/`
- `src/core/opendork/`
- `src/core/catalon/`
- `src/core/catalon-guard/`
- `src/core/deafpiper/`
- `src/core/registry/`
- `src/core/gates/`
- `src/core/run-state/`
- `src/core/types/`
- `tests/`

### New files (26)
See acceptance checklist above plus `tests/README.md`.

### Modified files
- `src/core/README.md` — structure map updated

---

## What Was NOT Built

- No implementation code for any named system
- No runtime for OPENDORK, CATALON, CATALON-GUARD, or DEAFPIPER
- No working provider routing logic
- No artifact manifest generator
- No gate execution engine
- No project registry service

---

## Open Conflicts

| Conflict | Resolution |
|---|---|
| `src/core/providers/` vs `src/core/opendork/` | opendork/ README maps to providers/ as implementation home |
| `src/core/runs/` vs `src/core/run-state/` | run-state/ README maps to runs/ |
| `src/core/safety/` vs `src/core/catalon-guard/` | catalon-guard/ README maps to safety/ |
| `docs/RUN_MODEL.md` vs `docs/ANVIL_RUN_STATE.md` | new file references existing, does not replace |
| `docs/ARTIFACT_OUTPUT_LAYER.md` vs `docs/ANVIL_ARTIFACT_STORE.md` | new file extends existing |

---

## Next Gate

**Gate 002** (to be defined): First executable subsystem.  
Prerequisite: Gate 001 merged, all acceptance criteria verified.

Cross-references:
- [`docs/ANVIL_CORE_MANIFEST.md`](../docs/ANVIL_CORE_MANIFEST.md)
- [`docs/ANVIL_GOVERNANCE.md`](../docs/ANVIL_GOVERNANCE.md)
- [`docs/ANVIL_MODULE_MAP.md`](../docs/ANVIL_MODULE_MAP.md)
