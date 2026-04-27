# AGENTS.md — Gated Project Agent Instructions

This project uses ANVIL gated implementation.

Agents must work conservatively, document their work, run available checks, and stop at the selected gate boundary.

The repository files are the source of truth. Do not rely on chat memory.

---

## Project Summary

Replace this section with the project-specific summary.

Include:

- what the project is
- what pain it solves
- what it is not
- current platform / runtime
- safety boundaries

---

## Required Reading Before Any Change

Before modifying files, read:

- `instructions.md`
- `README.md`
- `claude.md`
- `knownbugs.md`
- latest relevant `handoff/GATE_X_HANDOFF.md`
- current project tree
- recent commits

---

## Runtime Profile

Default runtime profile:

```text
interactive
```

Allowed profiles:

- `interactive` — user is present; ask only when necessary
- `batch` — bounded autonomous task; stop on ambiguity
- `overnight_safe` — unattended conservative work; no destructive actions
- `overnight_aggressive` — generation/analysis only; never for secrets, writes, deletion, or production actions

---

## CI-First Gate Policy

CI is the authoritative verifier when local environments are incomplete.

Local missing SDKs, caches, or platform tools are environment limitations, not product bugs, if CI is configured to verify the missing layer.

Do not repeatedly create knownbugs for the same local environment limitation.

If CI fails, CI output becomes the source of truth.

---

## Gate Rules

- Implement exactly one gate per task unless explicitly instructed otherwise.
- Do not skip gates.
- Do not merge multiple gates into one commit.
- Do not invent features.
- Do not perform broad cleanup.
- Do not rewrite working code for style.
- Do not introduce unrelated dependencies.
- Do not silently skip checks.
- Do not claim success without evidence.
- Do not commit secrets, tokens, passwords, API keys, or local config files.

---

## Broad Implementation Request Safety Rule

If the user asks to replace all fake/stub implementations, make everything productive, wire everything end-to-end, remove all stubs, or "just finish it", do **not** start implementation immediately.

First create:

1. Fake/stub inventory
2. Risk classification
3. Safe gate split
4. Recommended first gate
5. Explicit non-goals

Then stop.

Implementation may begin only after the user explicitly selects one gate.

Never collapse auth, token storage, HTTP clients, file/SAF access, UI navigation, ViewModels/state machines, write execution, automatic retries, and conflict/merge behavior into one unattended run.

Short form:

```text
Everything productive = inventory first, gate second.
```

---

## Push / PR Failure Rule

If push or PR creation fails because credentials are unavailable:

1. stop implementation
2. export a patch
3. document completed work
4. do not start the next gate

---

## Handoff Rules

Every attempted gate must create or update:

```text
handoff/GATE_X_HANDOFF.md
```

Use `handoff/GATE_X_HANDOFF_TEMPLATE.md` as the base.

---

## knownbugs.md Rules

Maintain `knownbugs.md` as a permanent structured risk and bug log.

Never delete old entries.

When something is fixed, mark it `FIXED`.

When a risk is intentionally accepted, mark it `ACCEPTED` and explain why.
