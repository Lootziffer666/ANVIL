# Global knownbugs

Structured cross-project bug/risk memory.

Entries are append-only. Do not delete old entries. Mark entries `FIXED`, `ACCEPTED`, or `SUPERSEDED` when appropriate.

## Entry format

```md
## GBUG-YYYYMMDD-NNN

Status: OPEN | FIXED | ACCEPTED | BLOCKED | SUPERSEDED
Projects: <project list>
Severity: LOW | MEDIUM | HIGH
Pattern: <one line>

Evidence:
- ...

Action:
- ...
```

---

## GBUG-20260426-001

Status: ACCEPTED
Projects: PAINKILLER, future gated projects
Severity: MEDIUM
Pattern: Agents may repeatedly escalate local Android SDK absence as a blocker even when CI is the intended verifier.

Evidence:
- PAINKILLER Gate 0–4 repeatedly surfaced local `SDK location not found` as a bug before CI-first policy was added.

Action:
- Add CI-first gate policy to `AGENTS.md` and `claude.md` in Android projects.
- Do not create repeated knownbugs for local Android SDK absence when GitHub Actions verifies Android builds.

---

## GBUG-20260426-002

Status: ACCEPTED
Projects: PAINKILLER, future Codex/Claude Code projects
Severity: HIGH
Pattern: If an agent loses push/PR credentials, completed work can remain stranded in an ephemeral branch/workspace.

Evidence:
- PAINKILLER Gate 5 work was likely created in Codex but did not reach `main` due to missing push credentials / unavailable PR creation.

Action:
- If push or PR creation fails, stop implementation and export a patch.
- Do not start the next gate until the current gate is on remote or safely exported.

---

## GBUG-20260426-003

Status: ACCEPTED
Projects: PAINKILLER, future gated projects
Severity: MEDIUM
Pattern: PR or branch names may become stale when an agent continues work on the same branch after a previous gate.

Evidence:
- PAINKILLER had a PR whose title reflected Gate 5 while later commits included Gate 8 work.

Action:
- PR title must match the highest gate implemented in the branch before merge.
- Handoff files and commit messages are more authoritative than stale PR titles.
