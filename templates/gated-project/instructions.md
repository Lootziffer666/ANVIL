# instructions.md — Project Source of Truth

This file defines the product, gates, scope, and safety boundaries.

Agents must treat this file as the primary source of truth.

---

## Product Summary

- Name:
- Purpose:
- Platform:
- Primary user:
- Pain solved:
- Non-goals:

---

## MVP Core

Define the smallest coherent product core.

```text
MVP Core = ...
```

---

## Gate Plan

### Gate 0 — Project Foundation

Scope:

- repo skeleton
- build system
- root hygiene files
- CI
- minimal app shell / runtime shell

Non-goals:

- production features
- real external writes

### Gate 1 — Domain Core

Scope:

- core models
- validation
- deterministic planning logic
- pure tests

### Gate 2 — Input / Intake Layer

Scope:

- user input model
- source adapters or boundaries
- safe normalization

### Gate 3 — Target / Context Layer

Scope:

- destination / context model
- selection or configuration
- non-secret persistence if needed

### Gate 4 — MVP Core Complete

Scope:

- integrate Gates 1–3
- document MVP core status
- verify build

### Gate 5+ — Project-Specific Execution Gates

Add gates only as needed.

---

## Safety Boundaries

- No hardcoded secrets.
- No token logging.
- No destructive action without explicit confirmation.
- No automatic writes from preview/sample state.
- No future-gate work.
- No silent overwrite.

---

## Definition of Done Per Gate

A gate is complete only when:

- scope is implemented
- tests/checks ran or unavailable checks are justified
- handoff exists
- README updated if status changed
- knownbugs updated for real risks
- one coherent commit exists
- CI is green or CI limitation is documented
