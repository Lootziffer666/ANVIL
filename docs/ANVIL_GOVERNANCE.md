# ANVIL_GOVERNANCE.md

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

## Gate Principle

One gate per session. One commit per gate. One branch per gate.

A gate is the unit of verified progress. A gate is not done until its acceptance criteria are met and a verifiable output (commit, artifact, test result) exists.

**No gate skipping.** If Gate N is not done, Gate N+1 cannot start.

**No scope creep.** Each gate has a defined set of files and behaviors. Work outside that set belongs to a future gate.

**No silent completion.** "I think it's done" is not done. Done means: artifact exists, test passes, commit is on remote.

Cross-reference: [`GATES.md`](../GATES.md), [`docs/EXECUTION_CORE_ARCHITECTURE.md`](EXECUTION_CORE_ARCHITECTURE.md)

---

## Branch-Safe Execution

Every gate runs on a dedicated branch:

```
gate/{gate-id}-{short-name}
```

Example: `gate/001-core-skeleton`

Rules:
- No direct commits to `main`.
- Branch is created from `main` at gate start.
- Branch is merged only after gate acceptance criteria are verified.
- If a branch contains work from more than one gate, the PR title reflects the highest gate.
- A stale branch (gate abandoned) must be documented in `docs/ANVIL_KNOWNBUGS.md` before deletion.

---

## First Gate: Repo Access Proof

Before any session begins:

1. Confirm live read access to the target repo and branch.
2. Confirm write/push credentials are active.
3. Record the proof.

See [`gates/GATE_000_REPO_ACCESS.md`](../gates/GATE_000_REPO_ACCESS.md) for the full protocol.

**If access cannot be confirmed:** stop. Do not build. Export a patch bundle of any local work. Report the exact error.

---

## Stop Rules

Stop immediately and report when:

| Condition | Action |
|---|---|
| Repo unreachable | Export patch, stop, report error |
| Push credentials missing | Export patch, stop, report error |
| Gate acceptance criteria cannot be met | Report blockers, do not claim done |
| Kill criteria triggered | Stop, do not proceed, report which criterion |
| Scope creep detected | Stop, document the out-of-scope work as a future gate |
| "Done" without verifiable output | Not done. Keep working or stop and report. |

---

## Patch / Transplant Bundle Policy

If an agent cannot push to the remote:

1. Create a patch bundle: `git format-patch origin/main`.
2. Store it in `outputs/` with a manifest.
3. Report the patch location to the human.
4. Do not continue to the next gate.

Transplant decisions (what to keep/rewrite/drop from donor code) are governed by [`docs/CODEBASE_TRANSPLANT_RULES.md`](CODEBASE_TRANSPLANT_RULES.md).

---

## No "Done" Without Proof

The following are not proof:

- "I believe I've implemented this."
- "The code should work."
- A summary of what was written.
- A previous session's output.

Proof is one or more of:

- A commit SHA on the remote branch.
- A passing test run with visible output.
- An artifact with a manifest in `outputs/`.
- A gate acceptance checklist with all items checked.

---

## Agent Permissions

See [`AGENTS.md`](../AGENTS.md) for role-specific permissions.

- Agents may not define new gates.
- Agents may not change gate acceptance criteria.
- Agents may not merge PRs without human approval.
- Agents may not modify `CLAUDE.md` or `AGENTS.md`.

---

## Audit Trail

Every gate leaves a trace:

| Trace | Where |
|---|---|
| Gate spec | `gates/GATE_{NNN}_{NAME}.md` |
| Commit | Git log with `gate(NNN):` prefix |
| Artifacts | `outputs/` with manifest |
| Run state | `src/core/run-state/` (when implemented) |
| Known issues | `docs/ANVIL_KNOWNBUGS.md` |
