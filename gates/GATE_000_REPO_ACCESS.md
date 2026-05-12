# GATE_000 — Repo Access

**Gate:** 000  
**Name:** Repo Access Proof  
**Status:** `done`  
**Branch:** `main`  
**Stand:** 2026-05-12

---

## Purpose

No work begins without proven access to the target repository.

This gate is the first checkpoint in every ANVIL development session. It is not optional. It is not skippable.

---

## Access Verification Steps

1. Confirm the target branch exists and is reachable (git fetch or GitHub API).
2. Confirm the agent can read at least one file from the repo root (e.g., `README.md`).
3. Confirm the agent can write — either by creating a test branch or confirming push credentials exist.
4. Record the verification in a proof artifact (see format below).

No step may be substituted with an assumption or a cached result from a previous session.

---

## Stop Rules

If any of the following is true, **stop immediately**:

- The target repo is unreachable (network error, auth failure, wrong URL).
- The target branch does not exist and cannot be created.
- Push credentials are missing or expired.
- The agent is operating in an ephemeral workspace that cannot reach the remote.

**Action on stop:** Export a patch bundle of any completed local work. Do not proceed to the next gate. Report the exact error to the human.

Do not claim "access confirmed" without executing the verification steps.

---

## Proof Format

```json
{
  "gate": "000",
  "verified_at": "ISO 8601 timestamp",
  "repo": "owner/name",
  "branch": "branch-name",
  "readable": true,
  "writable": true,
  "method": "git-fetch | github-api | direct-read",
  "proof_file": "path/to/artifact (e.g. REPO_ACCESS_PROOF.md)"
}
```

---

## Existing Proof Artifact

Gate A1 (root `REPO_ACCESS_PROOF.md`) serves as the Gate 000 proof for the initial ANVIL repo setup.

Cross-reference: [`REPO_ACCESS_PROOF.md`](../REPO_ACCESS_PROOF.md)

---

## Rules

1. Gate 000 runs before every gate session, not just once.
2. A stale proof (> 24 hours) must be refreshed before any write operation.
3. "I can see files from context" is not proof. Execution must confirm live access.
4. If the file picker or upload UI is grayed out, this gate has failed — see `docs/ANVIL_KNOWNBUGS.md`.
