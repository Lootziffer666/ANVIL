# OGCODE_SOURCE_AUDIT — Gate AT1

> Gate: AT1 — Source License & Provenance Lock  
> Status: `done`  
> Last updated: 2026-05-10

---

## Donor Repository

| Field | Value |
|-------|-------|
| Repo | `https://github.com/prasenjeet-symon/ogcode` |
| Primary Language | Go (backend), TypeScript/React (frontend) |
| Go Lines (approx.) | ~12,850 |
| Web/UI Lines (approx.) | ~8,570 |
| License File | `LICENSE` (root) |

## Audit Metadata

| Field | Value |
|-------|-------|
| Audit Date | 2026-05-10 |
| Audit Agent | Viktor AI (Gate AT1) |
| Audit Scope | License, copyright, attribution, dependency chain, structural risk |
| Audit Depth | Full file-level scan of donor root, all Go packages, web source, build config |

---

## License Status

| Field | Value |
|-------|-------|
| License Type | MIT License |
| Copyright Holder | Prasenjeet Symon |
| Copyright Year | 2026 |
| SPDX Identifier | MIT |
| License Location | `LICENSE` (repo root) |
| Per-File Headers | None observed |

### MIT License — Key Terms

- Permission to use, copy, modify, merge, publish, distribute, sublicense, sell — **granted**.
- Condition: copyright notice and permission notice must be included in all copies or substantial portions.
- No warranty. No liability.

---

## Copyright / Attribution Obligations

| Requirement | Status |
|-------------|--------|
| Include original copyright notice | **Required** — must appear in `THIRD_PARTY_NOTICES.md` |
| Include MIT permission notice | **Required** — must appear in `THIRD_PARTY_NOTICES.md` |
| Attribute author by name | Required by MIT terms |
| Preserve LICENSE file in transplanted code | Not required in Anvil product (code is rewritten), but provenance docs must reference it |
| Display in UI or product surface | **Not required** — attribution stays in docs/provenance only |

---

## Allowed Usage Form

```text
- Study and analyze code structure, architecture, patterns
- Extract architectural ideas and adapt them for native Anvil implementation
- Rewrite selected structures natively in Anvil's own codebase (REWRITE)
- Keep selected structures with clear provenance chain (KEEP — after Transplant Map review)
- Reference in provenance, license, audit, and transplant documentation
```

## Forbidden Usage Form

```text
- Fork or mirror ogcode as an Anvil product
- Use ogcode as a plugin, adapter, submodule, or runtime dependency
- Import ogcode packages or Go modules into Anvil's dependency tree
- Reference "ogcode" in product UI, user-facing docs, or core source files
- Ship ogcode binaries or compiled artifacts as part of Anvil
- Present ogcode functionality as Anvil-native without rewrite
- Use ogcode's web/UI components directly (must be DROP or REWRITE)
```

---

## Third-Party Dependencies (Donor)

The donor codebase declares dependencies in `go.mod`. These require separate evaluation before any KEEP decision.

| Dependency | Purpose | License | Risk |
|-----------|---------|---------|------|
| `go-chi/chi` | HTTP router | MIT | Low |
| `mark3labs/mcp-go` | MCP protocol client | **pending audit** | Medium — protocol coupling |
| `oklog/ulid` | ID generation | Apache-2.0 | Low |
| `pressly/goose` | DB migrations | MIT | Low |
| `spf13/cobra` | CLI framework | Apache-2.0 | Low |
| `modernc.org/sqlite` | Pure-Go SQLite | BSD-3-Clause | Low |
| `joho/godotenv` | Env file loading | MIT | Low |
| `google/jsonschema-go` | JSON Schema | Apache-2.0 | Low |
| `google/uuid` | UUID generation | BSD-3-Clause | Low |

> **Note:** These are the donor's dependencies. Anvil does NOT inherit them.  
> If a KEEP decision is made for any donor area, that area's dependencies must be independently evaluated.  
> Any dependency with unclear or incompatible license moves the affected area to REWRITE or DROP.

---

## Risk Analysis

| Risk | Level | Description |
|------|-------|-------------|
| License incompatibility | **Low** | MIT is permissive, compatible with any Anvil license choice |
| Attribution failure | **Medium** | Must ensure `THIRD_PARTY_NOTICES.md` is maintained |
| Naming drift | **High** | ogcode terms (Striker, ogden, etc.) must never leak into Anvil product surface |
| Dependency chain contamination | **Medium** | Donor deps must not silently enter Anvil; each KEEP requires dep review |
| Security surface import | **High** | Donor has shell execution, permission system — must be REWRITE with Command Guard |
| Secret handling import | **High** | Donor stores API keys; Anvil's Token Manager is already `prototype` with known issues |
| Identity confusion | **Medium** | Users must never see ogcode references in Anvil product |

---

## Kill Criteria

Any of the following blocks ALL code import:

1. ❌ License changes to a restrictive or incompatible license before transplant
2. ❌ Copyright holder objects to derivative use (unlikely under MIT, but monitored)
3. ❌ A dependency in the KEEP path has an incompatible license
4. ❌ Transplant Map is not completed (Gate AT3 must be done first)
5. ❌ ogcode product terms appear in active Anvil code outside `docs/provenance/`
6. ❌ Any direct runtime dependency on ogcode is introduced

---

## Decision

```text
✅ License is MIT — permissive and compatible.
✅ Attribution requirements are clear and manageable.
✅ No code import before AT1–AT3 are complete.
✅ No visible ogcode product identity outside docs/provenance/.
✅ No runtime dependency to ogcode — ever.
✅ All transplanted code must appear in TRANSPLANT_MAP.md first.
✅ THIRD_PARTY_NOTICES.md must be maintained for any KEEP decision.
```

---

## Cross-References

- [`LICENSE_DECISION.md`](LICENSE_DECISION.md) — Decision logic for license-based actions
- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) — Attribution tracking
- [`TRANSPLANT_MAP.md`](TRANSPLANT_MAP.md) — Keep / Rewrite / Drop decisions (Gate AT3)
- [`OGCODE_CODEBASE_INVENTORY.md`](OGCODE_CODEBASE_INVENTORY.md) — Structural inventory (Gate AT2)
