# CODEBASE_TRANSPLANT_RULES — Gate AT3

> Canonical rules for donor codebase transplantation into Anvil.  
> Gate: AT3 — Transplant Map: Keep / Rewrite / Drop  
> Status: `done`  
> Last updated: 2026-05-10

---

## 1. Donor Code Definition

A **donor codebase** is any external repository whose architecture, patterns, or code may inform Anvil's native implementation.

The donor codebase is **not** a fork, plugin, adapter, external dependency, submodule, or runtime module of Anvil.

Anvil may study donor structures and absorb useful patterns into its own native Execution Core. After transplantation, all active product code uses Anvil terminology and architecture exclusively.

**Current registered donors:**

| Donor | Repository | License | Status |
|-------|-----------|---------|--------|
| ogcode | `https://github.com/prasenjeet-symon/ogcode` | MIT | Audited (Gate AT1) |

---

## 2. Allowed Locations for Donor References

Donor codebase names, terms, and references may appear **only** in:

```text
docs/provenance/          — All provenance, audit, and transplant documentation
THIRD_PARTY_NOTICES.md    — Attribution tracking (if KEEP decisions exist)
GATES.md                  — Gate descriptions referencing transplant work
docs/GATES.md             — Same
```

Donor references must **never** appear in:

```text
app/                      — Product UI
src/                      — Product source code
modules/                  — Product modules
outputs/                  — Product outputs
README.md                 — Product README (root)
Any user-facing surface
```

---

## 3. Forbidden Integration Forms

The following integration forms are **permanently forbidden**:

| Form | Rule |
|------|------|
| Fork | Anvil is not a fork of any donor |
| Submodule | No donor repo as git submodule |
| npm/Go dependency | No donor package in any dependency file |
| Plugin/adapter | No donor code loaded as plugin at runtime |
| External runtime | No donor process invoked by Anvil |
| Direct file copy | No donor file copied into active Anvil paths without TRANSPLANT_MAP entry |
| Symlink | No symlinks to donor code |

---

## 4. Provenance Requirements

Before any code pattern is transplanted:

1. Donor must be audited (`OGCODE_SOURCE_AUDIT.md`)
2. License must be evaluated (`LICENSE_DECISION.md`)
3. Area must appear in `TRANSPLANT_MAP.md` with a decision
4. If decision is KEEP: entry in `THIRD_PARTY_NOTICES.md` required
5. If decision is REWRITE: no attribution required, but source inspiration may be noted in provenance docs

---

## 5. Keep / Rewrite / Drop Rules

### KEEP Rules
- Only for code that is directly useful AND small AND has no security risk
- Must not bring donor dependencies into Anvil
- Must be renamed to Anvil terminology
- Must have entry in `THIRD_PARTY_NOTICES.md`
- Must be reviewed before merge
- Currently: **zero KEEP decisions** (all areas are REWRITE)

### REWRITE Rules
- Architectural pattern is studied from donor
- Implementation is written fresh for Anvil
- No donor code is copied — only the idea/pattern is used
- Must use Anvil terminology exclusively
- No attribution required in `THIRD_PARTY_NOTICES.md`
- Must meet Anvil's safety standards (Command Guard, encryption, etc.)

### DROP Rules
- Area is not useful, too risky, or incompatible
- No code, no pattern, no reference in active codebase
- May be mentioned in provenance docs as "evaluated and dropped"

### DEFER Rules
- Cannot decide yet
- Must not be treated as KEEP
- Must be re-evaluated when the dependent architecture decision is made
- No code import while in DEFER status

### UNKNOWN Rules
- Not enough information
- Must not be treated as KEEP
- Requires investigation before any action
- No code import while in UNKNOWN status

---

## 6. Naming Rules

### Forbidden Visible Terms (in product/UI/core files)

```text
ogcode module
ogcode fork
ogcode adapter
external coding agent
Striker adapter
plugin marketplace for ogcode
run ogcode
use ogcode
ogcode integration
```

### Required Anvil Terms

All transplanted structures must use Anvil-native terminology:

| Concept | Anvil Term |
|---------|-----------|
| IDE / Workbench | Anvil |
| Launchpad | The Forge |
| Project context | Workspace |
| Output / result | Artifact |
| Implementation plan | Plan |
| Unit of work | Task |
| Execution session | Run |
| Agent runtime | Execution Core |
| Plan logic | Plan Engine |
| Task logic | Task Engine |
| Session logic | Run Engine |
| Code understanding | Repo Context Engine |
| LLM abstraction | Provider Core |
| File operations | File Mutation Engine |
| Shell safety | Command Guard |
| Git automation | Branch / PR Engine |
| Safety gate | Review Gate |
| Origin tracking | Provenance |
| Decision document | Transplant Map |

---

## 7. Safety Rules

| Rule | Enforcement |
|------|-------------|
| No unrestricted shell access | Command Guard with allowlist required |
| No plaintext secret storage | Encrypted credential storage required |
| No auto-merge | PR Engine must not auto-merge without human review |
| No file mutation without tracking | File Mutation Engine must log all changes with rollback capability |
| No branch creation without safety | Branch Engine must validate branch names and enforce naming conventions |
| No provider calls without credential check | Provider Core must validate credentials before API calls |
| No execution without scope | Run Engine must enforce file/directory scope restrictions |

---

## 8. Review Requirements

| Action | Review Required |
|--------|----------------|
| KEEP decision | Human review of code + license + security |
| REWRITE implementation | Standard code review |
| DROP decision | No review needed |
| DEFER → any decision | Human review when re-evaluated |
| UNKNOWN → any decision | Human review after investigation |
| Any change to this document | Human review |
| Any change to `TRANSPLANT_MAP.md` | Human review |

---

## 9. Kill Criteria

Transplantation must be **immediately stopped** if:

1. ❌ Donor license changes to incompatible terms
2. ❌ A KEEP area introduces a dependency with incompatible license
3. ❌ Donor product terms appear in active Anvil code outside `docs/provenance/`
4. ❌ A runtime dependency on any donor is introduced
5. ❌ Donor files are copied into active Anvil paths without Transplant Map entry
6. ❌ Security-critical code (shell execution, secret handling) is transplanted without REWRITE
7. ❌ Transplanted code introduces a regression in existing Anvil functionality
8. ❌ Transplant Map or Provenance docs are falsified or omit material facts

**On kill:** No further transplantation. Write failure report to `docs/provenance/TRANSPLANT_PREP_FAILURE.md`.

---

## 10. Promotion Path

How transplanted patterns become active Anvil code:

```text
1. Area appears in TRANSPLANT_MAP.md with decision (REWRITE)
2. Anvil-native implementation is written in target path (src/core/*)
3. Implementation uses only Anvil terminology
4. Implementation passes code review
5. Implementation has tests (when test infrastructure exists)
6. Implementation is merged via gated PR
7. Gate status is updated in GATES.md
8. Transplant Map entry is updated with "transplanted" note
```

No shortcut. No skip.

---

## Cross-References

- [`docs/provenance/TRANSPLANT_MAP.md`](provenance/TRANSPLANT_MAP.md) — Per-area decisions
- [`docs/provenance/OGCODE_SOURCE_AUDIT.md`](provenance/OGCODE_SOURCE_AUDIT.md) — License audit
- [`docs/provenance/THIRD_PARTY_NOTICES.md`](provenance/THIRD_PARTY_NOTICES.md) — Attribution
- [`docs/provenance/OGCODE_RISK_MAP.md`](provenance/OGCODE_RISK_MAP.md) — Risk assessment
- [`docs/EXECUTION_CORE_ARCHITECTURE.md`](EXECUTION_CORE_ARCHITECTURE.md) — Target architecture
