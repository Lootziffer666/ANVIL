# TRANSPLANT_MAP — Gate AT3

> Gate: AT3 — Transplant Map: Keep / Rewrite / Drop  
> Status: `done`  
> Last updated: 2026-05-10

---

## Governing Rule

```
No donor file may be copied into active Anvil paths before it appears in this map.
No donor structure may become active Anvil runtime before its target path and status are defined.
```

---

## Decision Values

| Decision | Meaning |
|----------|---------|
| **KEEP** | Structure may be transplanted (after audit, with attribution). Must appear in `THIRD_PARTY_NOTICES.md`. |
| **REWRITE** | Architectural pattern is useful but implementation must be written fresh for Anvil. |
| **DROP** | Not useful, too risky, or incompatible. Do not transplant. |
| **DEFER** | Cannot decide yet. Requires further analysis or depends on future architecture decisions. |
| **UNKNOWN** | Not enough information to classify. Requires investigation. |

---

## Transplant Map

| Donor Area / Path | Function | Decision | Target Anvil Area | Risk | Rationale | Required Action |
|---|---|---|---|---|---|---|
| `internal/plan/plan.go` | Plan model (struct, statuses) | REWRITE | `core/planning/` | Low | Clean model but needs Anvil terminology (Plan → Plan, locked → locked). Struct is small enough to rewrite cleanly. | Define Anvil Plan model natively |
| `internal/plan/store.go` | Plan persistence (SQLite CRUD) | REWRITE | `core/planning/` | Low | DB schema must align with Anvil workspace model, not donor schema | Design Anvil-native storage |
| `internal/plan/util.go` | Plan utilities | REWRITE | `core/planning/` | Low | Trivial helpers, rewrite fresh | — |
| `internal/task/task.go` | Task model (struct, statuses, deps) | REWRITE | `core/tasks/` | Low | Good model but must use Anvil terms. Dependency chain pattern is sound. | Define Anvil Task model natively |
| `internal/task/store.go` | Task persistence | REWRITE | `core/tasks/` | Low | Same as plan store — needs Anvil-native DB | Design Anvil-native storage |
| `internal/task/util.go` | Task utilities | REWRITE | `core/tasks/` | Low | Trivial | — |
| `internal/agent/agent.go` | Agent definitions (Build/Plan/Breakdown) | REWRITE | `core/planning/` | Medium | System prompts and tool sets are architecture decisions, not transplantable code | Define Anvil agent patterns natively |
| `internal/agent/breakdown.go` | Task breakdown from plan | REWRITE | `core/tasks/` | Medium | Breakdown logic is useful pattern but tightly coupled to donor message format | Implement Anvil-native breakdown |
| `internal/agent/loop.go` | Agent execution loop (~1,400 lines) | REWRITE | `core/runs/` | High | Most complex file. Tightly coupled to donor session, tools, git, permissions. Must be decomposed for Anvil. | Full REWRITE, decompose into smaller modules |
| `internal/agent/agentmd.go` | Markdown generation | DROP | none | Low | Donor-specific formatting, trivial to implement fresh if needed | — |
| `internal/session/message.go` | Session + message models | REWRITE | `core/runs/` | Medium | Session model needs to map to Anvil Run/Workspace model | Define Anvil Run model natively |
| `internal/session/store.go` | Session persistence | REWRITE | `core/runs/` | Low | Needs Anvil-native DB | — |
| `internal/session/model_store.go` | Model preferences | REWRITE | `core/providers/` | Low | Must integrate with Anvil's existing Provider Registry | — |
| `internal/session/provider_store.go` | Provider credentials (plaintext) | REWRITE | `core/providers/` | High | Plaintext keys — needs encryption. Aligns with Token Manager fix. | Encrypted credential storage |
| `internal/session/memory_store.go` | Memory config | DEFER | — | Low | Depends on whether Anvil adopts agentic memory | Evaluate after core is running |
| `internal/session/schema.go` | ID type aliases | DROP | none | Low | Trivial, implement fresh | — |
| `internal/tool/tool.go` | Tool interface + registry | REWRITE | `core/execution/` | Low | Clean interface pattern. Registry is useful. Must use Anvil terms. | Define Anvil tool interface |
| `internal/tool/bash.go` | Shell execution (unrestricted) | REWRITE | `core/execution/commands/` | **Critical** | No allowlist, no sandbox. Must become Command Guard with strict safety. | Command Guard with allowlist + audit log |
| `internal/tool/read.go` | File reading | REWRITE | `core/repo/` | Low | Trivial, but should integrate with Repo Context Engine | — |
| `internal/tool/write.go` | File writing | REWRITE | `core/execution/file-mutations/` | Medium | No diff tracking, no rollback. Must add mutation log. | File Mutation Engine with rollback |
| `internal/tool/edit.go` | File editing (search-replace) | REWRITE | `core/execution/file-mutations/` | Medium | Same as write — needs diff tracking | File Mutation Engine with rollback |
| `internal/tool/glob.go` | File discovery | REWRITE | `core/repo/` | Low | Missing `.gitignore` awareness. Integrate with Repo Context Engine. | — |
| `internal/tool/grep.go` | Content search | REWRITE | `core/repo/` | Low | Same as glob | — |
| `internal/tool/memory_recall.go` | Memory recall tool | DEFER | — | Low | Depends on agentic memory decision | — |
| `internal/tool/breakdown.go` | Breakdown submission tool | REWRITE | `core/tasks/` | Low | Tool-specific, needs Anvil-native implementation | — |
| `internal/provider/provider.go` | Provider interface + registry | REWRITE | `core/providers/` | Medium | Clean abstraction. Must align with Anvil's existing Provider Registry. | Merge with Anvil Provider Registry |
| `internal/provider/anthropic.go` | Anthropic provider | REWRITE | `core/providers/` | Medium | API client implementation. Must use Anvil credential management. | Implement Anvil Anthropic provider |
| `internal/provider/openai.go` | OpenAI/OpenRouter/Ollama provider | REWRITE | `core/providers/` | Medium | Multi-provider via OpenAI-compatible API. Must use Anvil credential management. | Implement Anvil OpenAI-compat provider |
| `internal/permission/permission.go` | Permission system (Allow/Deny/Ask) | REWRITE | `core/safety/` | Medium | Good pattern but glob matching is too simple. Needs path traversal protection. | Implement Anvil safety with stricter matching |
| `internal/memory/memory.go` | Agentic memory lifecycle | DEFER | — | Medium | Advanced feature. Evaluate after core is stable. | — |
| `internal/memory/store.go` | Knowledge graph store | DEFER | — | Medium | Same as above | — |
| `internal/memory/graph.go` | Knowledge graph operations | DEFER | — | Medium | Same as above | — |
| `internal/db/db.go` | SQLite + migrations | REWRITE | `core/runs/` | Low | Migration pattern is useful. Schema must be Anvil-native. | Design Anvil-native schema |
| `internal/db/*.sql` | Migration files | REWRITE | `core/runs/` | Low | Schema is donor-specific. Study for patterns, rewrite for Anvil. | — |
| `internal/bus/bus.go` | Event bus | DEFER | — | Low | Evaluate if Anvil needs internal event bus | — |
| `internal/cli/` | Cobra CLI | DROP | none | Low | Anvil is not a CLI tool | — |
| `internal/id/` | ULID generation | DROP | none | Low | Trivial utility, implement fresh if needed | — |
| `internal/version/` | Version string | DROP | none | Low | Trivial | — |
| `internal/mcp/` | MCP protocol client | DEFER | — | Medium | Protocol coupling. Evaluate if Anvil needs MCP support. | — |
| `internal/server/` | HTTP server + REST API | REWRITE | — | Medium | API patterns may be useful. Server framework is donor-specific. | Evaluate API patterns for Anvil |
| `internal/git/` | Git operations | UNKNOWN | `core/branching/` | Medium | Minimal content observed. Investigate further when branching is scoped. | — |
| `web/src/` | React/Vite SPA | DROP | none | Medium | Anvil has its own UI. Different stack, different design language. | — |
| `web/src/api/` | API client + SSE | DROP | none | Low | Tied to donor server API | — |
| `web/src/components/` | React components | DROP | none | Low | Donor UI components | — |
| `web/src/context/` | React contexts | DROP | none | Low | Donor state management | — |
| `web/src/pages/` | React pages | DROP | none | Low | Donor page components | — |
| `assets/` | Demo images/GIFs | DROP | none | Low | Donor marketing material | — |
| `docs/` | Donor documentation | DROP | none | Low | Donor-specific docs | — |
| `Makefile` | Build targets | DROP | none | Low | Different build system | — |
| `Dockerfile` | Container build | DROP | none | Low | Different deployment | — |
| `.goreleaser.yaml` | Release config | DROP | none | Low | Different release process | — |
| `install.sh` / `install.ps1` | Install scripts | DROP | none | Low | Not applicable | — |
| `.github/workflows/` | CI/CD | DROP | none | Low | Different CI/CD | — |
| `web/package.json` | npm config | DROP | none | Low | Anvil has no npm deps | — |

---

## Decision Rules Applied

| Rule | Enforcement |
|------|-------------|
| UI from donor → DROP or REWRITE | ✅ All `web/src/` → DROP |
| Product names from donor → DROP | ✅ No donor product names in any KEEP/REWRITE target |
| Shell/Command execution → REWRITE with Command Guard | ✅ `bash.go` → REWRITE to `core/execution/commands/` |
| Secret handling → REWRITE | ✅ `provider_store.go` → REWRITE with encryption |
| Provider logic → at most REWRITE | ✅ All provider files → REWRITE |
| Branch/PR logic → at most REWRITE until Safety Policy | ✅ Branch logic → REWRITE or UNKNOWN |
| Plan/Task/Run → KEEP or REWRITE after audit | ✅ All audited → REWRITE (structures are small enough to rewrite cleanly) |
| Unclear areas → UNKNOWN or DEFER, not KEEP | ✅ MCP, memory, bus, git → DEFER or UNKNOWN |

---

## Summary

| Decision | Count |
|----------|-------|
| KEEP | 0 |
| REWRITE | 27 |
| DROP | 18 |
| DEFER | 5 |
| UNKNOWN | 1 |

> **Note:** Zero KEEP decisions. All transplantable structures are small enough and different enough from Anvil's stack that native reimplementation (REWRITE) is the correct path. This avoids dependency chain contamination, naming drift, and platform mismatch. The donor codebase serves as an architecture reference, not a code source.

---

## Cross-References

- [`OGCODE_CODEBASE_INVENTORY.md`](OGCODE_CODEBASE_INVENTORY.md) — What each area contains
- [`OGCODE_FEATURE_MAP.md`](OGCODE_FEATURE_MAP.md) — Capability mapping
- [`OGCODE_RISK_MAP.md`](OGCODE_RISK_MAP.md) — Risk per area
- [`../CODEBASE_TRANSPLANT_RULES.md`](../CODEBASE_TRANSPLANT_RULES.md) — Governing rules
- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) — Attribution (empty — no KEEP decisions)
