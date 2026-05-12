# ANVIL_ARTIFACT_STORE.md

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

Everything Anvil produces must be findable. Everything findable must have an ID, a lifecycle state, and a manifest.

Base definition: [`docs/ARTIFACT_OUTPUT_LAYER.md`](ARTIFACT_OUTPUT_LAYER.md) (Gate A7).  
This document adds lifecycle states, extended artifact types, and naming conventions for logs and patches.

---

## Artifact Types

| Type | Extension | MIME-artig | Beschreibung |
|---|---|---|---|
| Markdown | `.md` | `text/markdown` | Docs, reports, prompts |
| JSON | `.json` | `application/json` | Manifests, configs, schemas |
| ZIP | `.zip` | `application/zip` | Export packages, patch bundles |
| Patch | `.patch` | `text/x-diff` | Code changes (git format-patch) |
| Prompt | `.prompt.md` | `text/markdown` | Agent prompts, handoff packs |
| Config | `.config.json` | `application/json` | Module configs |
| Log | `.log` | `text/plain` | Run execution logs |
| Report | `.report.md` | `text/markdown` | Gate reports, audit outputs |

---

## Lifecycle States

Every artifact is in exactly one lifecycle state:

| State | Meaning | May Be Deleted? |
|---|---|---|
| `raw` | Produced by a run, not yet reviewed | No — preserve for recovery |
| `final` | Reviewed, accepted, may be shared | No — permanent record |
| `broken` | Produced by a failed run, not usable | No — preserve for debugging |
| `discarded` | Deliberately rejected after review | Logged deletion only |

Lifecycle transitions:
```
raw → final      (human or gate review passes)
raw → broken     (run failed after artifact was partially written)
raw → discarded  (human explicitly rejects)
broken → discarded (human cleans up after investigation)
```

No artifact moves from `final` back to any other state. Final is permanent.

---

## Naming Convention

### Outputs
```
OUT_{YYYYMMDD}_{HHMMSS}_{NNN}
```
Example: `OUT_20260512_143000_001`

### Logs
```
LOG_{YYYYMMDD}_{HHMMSS}_{NNN}.log
```

### Patches
```
PATCH_{YYYYMMDD}_{HHMMSS}_{NNN}.patch
```

### Reports
```
REPORT_{YYYYMMDD}_{HHMMSS}_{NNN}.report.md
```

All names are sortable by creation time. All names are unique within a session.

---

## Storage Layout

```
outputs/
├── {output_id}/
│   ├── manifest.json    ← required for every artifact
│   └── {filename}.{ext} ← the actual artifact
├── latest/              ← symlink to most recent final artifact
└── registry.json        ← index of all artifacts with id, type, lifecycle
```

An artifact without a manifest is not an artifact. It is an orphan. Orphans must be investigated before deletion.

---

## Manifest Schema

Reference: [`docs/ARTIFACT_OUTPUT_LAYER.md`](ARTIFACT_OUTPUT_LAYER.md) for the full manifest format.

Required additions for Gate 001:

```json
{
  "lifecycle": "raw | final | broken | discarded",
  "run_id": "RUN_{YYYYMMDD}_{HHMMSS}_{NNN}",
  "gate": "gate-id (e.g. 001)"
}
```

---

## Rules

1. No output without an ID.
2. No ID without a manifest.
3. Manifests are written before the artifact is considered complete.
4. `broken` artifacts are never deleted without documented investigation.
5. `discarded` artifacts get a deletion log entry in `registry.json` before removal.
6. `registry.json` is append-only for `final` and `broken`. Discarded entries are marked, not removed.

---

## Cross-References

- [`docs/ARTIFACT_OUTPUT_LAYER.md`](ARTIFACT_OUTPUT_LAYER.md) — base definition (Gate A7)
- [`outputs/registry.json`](../outputs/registry.json) — live registry
- [`src/core/artifacts/README.md`](../src/core/artifacts/README.md) — implementation home
- [`src/core/types/artifact.schema.json`](../src/core/types/artifact.schema.json) — JSON schema
