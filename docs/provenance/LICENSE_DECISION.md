# LICENSE_DECISION — Gate AT1

> Gate: AT1 — Source License & Provenance Lock  
> Status: `done`  
> Last updated: 2026-05-10

---

## Purpose

This document defines the decision logic for how license status affects code transplantation from the donor codebase into Anvil.

---

## Decision Matrix

### 1. License is clean and compatible

**Condition:** Donor license is MIT, BSD, Apache-2.0, or similarly permissive. No additional restrictions. No CLA conflicts.

**Current status: ✅ APPLIES** — Donor uses MIT License.

**Actions allowed:**
- Code may be transplanted according to the Transplant Map (`TRANSPLANT_MAP.md`)
- KEEP decisions are valid for audited areas
- REWRITE decisions proceed without license concern
- Attribution must be maintained in `THIRD_PARTY_NOTICES.md`

---

### 2. License is unclear or ambiguous

**Condition:** License file is missing, contradictory, or references additional terms not captured in the main license.

**Current status: ❌ Does not apply**

**Actions if triggered:**
- No code import — only architectural ideas may be studied
- All areas default to REWRITE or DROP
- Document the ambiguity in `OGCODE_SOURCE_AUDIT.md`
- Attempt to clarify with the copyright holder before proceeding

---

### 3. Attribution is required

**Condition:** License requires copyright notice and/or permission notice in copies or substantial portions.

**Current status: ✅ APPLIES** — MIT requires both.

**Actions:**
- `THIRD_PARTY_NOTICES.md` must contain the full MIT copyright and permission notice
- Any KEEP decision must reference the attribution entry
- Attribution stays in `docs/provenance/` — never in product UI or active source

---

### 4. Dependencies have problematic licenses

**Condition:** A direct dependency of a KEEP area has a license that is copyleft (GPL, AGPL), commercially restricted, or otherwise incompatible with Anvil's intended license.

**Current status: ⚠️ Pending per-area review** — No KEEP decisions have been made yet.

**Actions if triggered:**
- Affected area moves to **REWRITE** (implement equivalent functionality without the dependency)
- Or affected area moves to **DROP** (abandon the capability)
- Document the specific dependency and license conflict in `OGCODE_RISK_MAP.md`
- Never import the problematic dependency into Anvil

---

### 5. Copyleft license discovered

**Condition:** Donor or any transitive dependency uses GPL, LGPL, AGPL, or similar copyleft license.

**Current status: ❌ Does not apply** — Donor is MIT, all observed deps are permissive.

**Actions if triggered:**
- ALL code import is blocked
- Only architectural ideas may be studied (clean-room approach)
- All areas default to REWRITE with clean-room documentation
- Kill criterion activated — escalate to human review

---

## Summary

| Scenario | Status | Result |
|----------|--------|--------|
| Clean compatible license | ✅ Active | Transplant allowed per Transplant Map |
| Unclear license | ❌ N/A | Would block all code import |
| Attribution required | ✅ Active | `THIRD_PARTY_NOTICES.md` must be maintained |
| Dependency license issue | ⚠️ Pending | Per-area review required before KEEP |
| Copyleft discovered | ❌ N/A | Would block all code import |

---

## Cross-References

- [`OGCODE_SOURCE_AUDIT.md`](OGCODE_SOURCE_AUDIT.md) — Full audit
- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) — Attribution tracking
- [`OGCODE_RISK_MAP.md`](OGCODE_RISK_MAP.md) — Risk assessment
- [`TRANSPLANT_MAP.md`](TRANSPLANT_MAP.md) — Keep / Rewrite / Drop per area
