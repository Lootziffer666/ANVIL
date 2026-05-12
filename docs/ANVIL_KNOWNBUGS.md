# ANVIL_KNOWNBUGS.md

**Gate:** 001 — Core Skeleton  
**Stand:** 2026-05-12  
**Status:** Append-only memory

---

## Format

```
## ABUG-YYYYMMDD-NNN

Status: OPEN | FIXED | ACCEPTED | BLOCKED | SUPERSEDED
Severity: LOW | MEDIUM | HIGH | CRITICAL
Pattern: one-line description of the recurring problem

Evidence:
- concrete observed instance

Action:
- what to do when this pattern is encountered
```

Entries are append-only. Do not delete. Mark `FIXED` or `SUPERSEDED` when resolved.

---

## ABUG-20260512-001

Status: OPEN  
Severity: HIGH  
Pattern: Agent claims repo access without executing a live verification step.

Evidence:
- Agent reports "I can see the files" based on context window content, not a live `git fetch` or API call.
- Work proceeds into a branch that does not exist on the remote.
- Commits are created locally but never reach the target repo.

Action:
- Enforce Gate 000 (GATE_000_REPO_ACCESS.md) at session start.
- Require a live read of at least one file before any write operation.
- If verification fails: stop, export patch, report error.

---

## ABUG-20260512-002

Status: OPEN  
Severity: HIGH  
Pattern: Agent builds in an ephemeral parallel workspace instead of the target repo.

Evidence:
- Agent creates files in a sandbox or temp directory.
- Pushes to a fork or wrong branch instead of the designated target branch.
- Work is "done" in the agent's context but invisible to the human.

Action:
- Confirm target repo + branch at session start (Gate 000).
- Verify that the push remote matches the intended repo before committing.
- If workspace mapping is unclear: stop and ask the human before writing any file.

---

## ABUG-20260512-003

Status: OPEN  
Severity: MEDIUM  
Pattern: GitHub auth or workspace mapping is unclear — agent cannot determine if it has write access.

Evidence:
- Agent proceeds past Gate 000 without confirming push credentials.
- Git push fails late in the session after significant work is done.
- Credentials expire mid-session without clear error surfacing.

Action:
- Test push credentials before starting gate implementation.
- On push failure: export a patch bundle immediately, do not retry indefinitely.
- Patch bundle location must be reported to the human before stopping.

---

## ABUG-20260512-004

Status: OPEN  
Severity: MEDIUM  
Pattern: File picker or upload UI is grayed out / unavailable in the cloud coding environment.

Evidence:
- Agent cannot upload files via the UI in mobile or restricted browser contexts.
- File creation via tool calls works, but manual file upload is blocked.
- This blocks patch bundle delivery when push credentials are missing.

Action:
- Do not rely on file picker UI for patch delivery.
- Use git format-patch and export to `outputs/` directory.
- If outputs/ is not reachable: encode the patch in a text artifact and report it inline.

---

## ABUG-20260512-005

Status: OPEN  
Severity: MEDIUM  
Pattern: Screenshot / preview capabilities break away mid-session.

Evidence:
- Agent starts with preview enabled (e.g. Gate A10 prototype works).
- After a context reset or environment change, preview stops rendering.
- Agent continues reporting "preview is working" based on prior session state.

Action:
- Re-verify preview capability at session start if preview is a gate requirement.
- Do not claim preview is functional based on a previous session's output.
- If preview breaks: document in this file and update the gate status to `prototype`.

---

## ABUG-20260512-006

Status: OPEN  
Severity: CRITICAL  
Pattern: Agent reports "done" without a visible commit, passing test, or artifact.

Evidence:
- Agent summarizes completed work but no commit SHA exists on the remote.
- Test results are described but not shown.
- Artifact is mentioned but not in `outputs/` with a manifest.

Action:
- "Done" requires at least one of: remote commit SHA, test output, artifact with manifest.
- If none of these exist: the gate is not done. Keep working or stop and report.
- Never accept an agent's verbal summary as proof of completion.

---

## ABUG-20260512-007

Status: ACCEPTED  
Severity: MEDIUM  
Pattern: PR title becomes stale when branch is reused across multiple gates.

Evidence:
- PR title references Gate N while later commits include Gate N+1 work.
- Reviewer sees inconsistent scope.

Action:
- PR title must match the highest gate implemented before merge.
- If reuse happens: update PR title before merge request.
- This is a known workflow friction (GBUG-20260426-003) — ANVIL-specific instance documented here.

---

## Cross-References

- [`knownbugs-global/knownbugs.md`](../knownbugs-global/knownbugs.md) — cross-project bugs
- [`knownbugs-global/agent-failure-patterns.md`](../knownbugs-global/agent-failure-patterns.md) — recurring AI failure patterns
- [`knownbugs-global/github-friction.md`](../knownbugs-global/github-friction.md) — GitHub workflow friction
- [`gates/GATE_000_REPO_ACCESS.md`](../gates/GATE_000_REPO_ACCESS.md) — stop rules for access failures
