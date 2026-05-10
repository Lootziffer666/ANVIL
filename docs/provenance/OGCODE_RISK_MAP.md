# OGCODE_RISK_MAP — Gate AT2

> Gate: AT2 — Donor Codebase Inventory  
> Status: `done`  
> Last updated: 2026-05-10

---

## Purpose

Systematic risk assessment for transplanting donor codebase structures into Anvil.

---

## Risk Areas

### 1. License Risk

| Factor | Assessment |
|--------|-----------|
| Donor License | MIT — permissive, compatible |
| Risk Level | **Low** |
| Copyright Holder | Prasenjeet Symon |
| Attribution Required | Yes — copyright + permission notice in substantial copies |
| Copyleft Risk | None |
| Patent Risk | None (MIT has no patent clause, but no restrictions either) |
| Mitigation | Maintain `THIRD_PARTY_NOTICES.md` for all KEEP decisions |

---

### 2. Security Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **Critical** |
| Primary Concern | Unrestricted shell execution (`sh -c`) in `internal/tool/bash.go` |
| Secondary Concern | No API authentication on REST server |
| Tertiary Concern | No file write sandboxing |
| Impact if Unmitigated | Arbitrary code execution, data loss, system compromise |
| Mitigation | REWRITE all execution paths with Command Guard. No KEEP for bash tool. |

---

### 3. Secret Handling Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **High** |
| Primary Concern | API keys stored in plaintext in SQLite |
| Secondary Concern | API keys read from env vars without scoping |
| Tertiary Concern | No key rotation mechanism |
| Anvil Context | Token Manager is already `prototype` with known issue (keys in localStorage) |
| Impact if Unmitigated | Key leakage, unauthorized API access |
| Mitigation | REWRITE. Implement encrypted storage. Do not import donor secret handling. |

---

### 4. Shell Execution Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **Critical** |
| Primary Concern | `internal/tool/bash.go` runs arbitrary commands via `sh -c` |
| No Allowlist | Confirmed — any command can be executed |
| No Sandbox | Confirmed — runs in session directory but no containment |
| Permission Gate | Exists (`Ask` action) but relies on user approval |
| Impact if Unmitigated | System compromise, data exfiltration, destructive operations |
| Mitigation | REWRITE with Command Guard: allowlist, blocklist, scope restriction, audit log |

---

### 5. Repo Damage Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **High** |
| Primary Concern | File write/edit tools can modify any file in session directory |
| Secondary Concern | No rollback mechanism for mutations |
| Tertiary Concern | Branch/PR automation without safety gates |
| Auto-merge | Not observed, but PR creation is automatic |
| Impact if Unmitigated | Unintended file mutations, broken branches, unwanted PRs |
| Mitigation | REWRITE File Mutation Engine with diff tracking, rollback, mutation log. REWRITE Branch/PR Engine with Review Gate. |

---

### 6. Naming Drift Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **High** |
| Primary Concern | Donor uses its own product terms throughout codebase |
| Donor Terms | ogcode, Striker, ogden (MCP command), session (vs. Anvil's Workspace/Run) |
| Anvil Terms | Anvil, The Forge, Workspace, Artifact, Plan, Task, Run, Execution Core |
| Impact if Unmitigated | Product identity confusion, mixed terminology in codebase |
| Mitigation | `CODEBASE_TRANSPLANT_RULES.md` enforces naming rules. All KEEP/REWRITE must use Anvil terms. |

---

### 7. UI Identity Drift Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **Medium** |
| Primary Concern | Donor has a full React/Vite UI with its own design language |
| Anvil Context | Anvil has its own HTML/CSS/JS UI (`app/`) |
| Impact if Unmitigated | Two competing UI stacks, inconsistent user experience |
| Mitigation | DROP all donor UI. Anvil's existing UI is the only UI. |

---

### 8. Dependency Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **Medium** |
| Primary Concern | Donor has Go module dependencies that could leak into Anvil |
| Key Dependencies | chi, cobra, sqlite, goose, mcp-go |
| Anvil Context | Anvil has no Go backend — different stack entirely |
| Impact if Unmitigated | Unexpected dependencies, build complexity, maintenance burden |
| Mitigation | No KEEP decision imports donor dependencies. All KEEP areas must be reimplemented without donor dep chain. |

---

### 9. Platform Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **Low** |
| Primary Concern | Donor is a Go CLI + local server. Anvil is browser-based IDE. |
| Platform Gap | Different runtime model (server vs. client-side) |
| Impact if Unmitigated | Architecture mismatch, features that don't translate |
| Mitigation | Only transplant architectural patterns, not platform-specific implementations. All REWRITE targets Anvil's platform. |

---

### 10. Maintenance Risk

| Factor | Assessment |
|--------|-----------|
| Risk Level | **Medium** |
| Primary Concern | KEEP areas may diverge from donor upstream over time |
| Secondary Concern | Donor may change license or direction |
| Tertiary Concern | Transplanted code without original context may rot |
| Impact if Unmitigated | Stale code, unpatched vulnerabilities, lost context |
| Mitigation | Transplant Map documents rationale. KEEP areas must be understood, not just copied. All code becomes Anvil-maintained after transplant. No upstream sync. |

---

## Risk Summary

| Risk Area | Level | Primary Mitigation |
|-----------|-------|-------------------|
| License | Low | `THIRD_PARTY_NOTICES.md` |
| Security | Critical | REWRITE with Command Guard |
| Secret Handling | High | REWRITE with encryption |
| Shell Execution | Critical | REWRITE with Command Guard |
| Repo Damage | High | REWRITE with rollback + Review Gate |
| Naming Drift | High | `CODEBASE_TRANSPLANT_RULES.md` |
| UI Identity Drift | Medium | DROP all donor UI |
| Dependency | Medium | No dependency import |
| Platform | Low | Pattern transplant only |
| Maintenance | Medium | Anvil-owned after transplant |

---

## Cross-References

- [`OGCODE_SOURCE_AUDIT.md`](OGCODE_SOURCE_AUDIT.md) — License audit
- [`OGCODE_CODEBASE_INVENTORY.md`](OGCODE_CODEBASE_INVENTORY.md) — Structural inventory
- [`TRANSPLANT_MAP.md`](TRANSPLANT_MAP.md) — Keep / Rewrite / Drop decisions
- [`../CODEBASE_TRANSPLANT_RULES.md`](../CODEBASE_TRANSPLANT_RULES.md) — Repo-wide rules
