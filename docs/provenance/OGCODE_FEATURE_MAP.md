# OGCODE_FEATURE_MAP — Gate AT2

> Gate: AT2 — Donor Codebase Inventory  
> Status: `done`  
> Last updated: 2026-05-10

---

## Purpose

Maps donor capabilities to observed evidence, possible Anvil target areas, and current status.

---

## Capability Map

| Donor Capability | Observed? | Description | Possible Anvil Target | Status | Notes |
|---|---|---|---|---|---|
| Plan creation | yes | `internal/plan/plan.go` — Plan struct with open/locked lifecycle | `core/planning/` | observed | Clean model, MIT license |
| Plan locking | yes | Plan status transitions open → locked, triggers breakdown | `core/planning/` | observed | Gate mechanism for plan finalization |
| Task generation | yes | `internal/agent/breakdown.go` — BreakdownAgent parses locked plan into TaskDefinitions | `core/tasks/` | observed | JSON-based task extraction with validation |
| Dependency handling | yes | `internal/task/task.go` — Dependencies as task ID list, linear chain constraint | `core/tasks/` | observed | Intentionally limited to linear chains (no fan-in) |
| Run/session management | yes | `internal/session/` — Session model with directory scope, message history, compaction | `core/runs/` | observed | Session ≈ Run context; ~1,400 line agent loop |
| Repository context reading | yes | `internal/tool/glob.go`, `grep.go`, `read.go` — file discovery and content search | `core/repo/` | observed | Tool-based, no dedicated repo model |
| File editing | yes | `internal/tool/edit.go` — search-and-replace, unique match required | `core/execution/file-mutations/` | observed | No diff tracking, no rollback |
| Diff generation | no | Not observed — edits are applied directly without generating diffs | `core/execution/file-mutations/` | not-found | Anvil should add this |
| Command execution | yes | `internal/tool/bash.go` — unrestricted `sh -c` with timeout | `core/execution/commands/` | observed | HIGH RISK — no allowlist, no sandbox |
| Provider routing | yes | `internal/provider/provider.go` — Registry with model resolution, custom model support | `core/providers/` | observed | Supports Anthropic, OpenAI, OpenRouter, Ollama |
| Branch creation | yes | In `internal/agent/loop.go` — git worktree per task, branch per task | `core/branching/` | observed | Tightly coupled to agent loop |
| Pull request preparation | yes | In `internal/agent/loop.go` — auto-PR on task completion, PR error tracking | `core/branching/` | observed | No safety policy, no review gate |
| Archive/log storage | yes | `.ogcode/archives/` for completed plans, SQLite for sessions/messages | `core/artifacts/` | observed | Markdown archives + SQLite |
| UI run monitoring | yes | `web/src/pages/task-execution.tsx`, SSE streaming | — | observed | React UI — DROP for Anvil |
| Permission system | yes | `internal/permission/permission.go` — Allow/Deny/Ask rules, glob-based | `core/safety/` | observed | Simple but extensible pattern |
| Agentic memory | yes | `internal/memory/` — knowledge graph with topics, concepts, facts, embeddings | `core/artifacts/` (future) | observed | Advanced feature, uses separate SQLite |
| MCP protocol support | yes | `internal/mcp/` — MCP client for external tool servers | — | observed | Protocol coupling risk |
| Token usage tracking | yes | `internal/provider/provider.go` — TokenUsage struct | `core/providers/` | observed | Input, output, reasoning, cache tokens |
| Model preferences | yes | `internal/session/model_store.go` — per-installation model config | `core/providers/` | observed | DB-stored preferences |
| Theme system | yes | `internal/server/theme_routes.go`, DB table | — | observed | UI-specific — DROP |
| Compaction / context window | yes | Session compaction summary for long conversations | `core/runs/` | observed | Token optimization strategy |

---

## Status Legend

| Status | Meaning |
|--------|---------|
| `observed` | Capability exists and was verified in source code |
| `expected` | Capability is expected based on docs/README but not yet verified in code |
| `unknown` | Capability status could not be determined |
| `not-found` | Capability was looked for but not found |
| `blocked` | Capability exists but cannot be used (license, risk, etc.) |

---

## Cross-References

- [`OGCODE_CODEBASE_INVENTORY.md`](OGCODE_CODEBASE_INVENTORY.md) — Detailed structural inventory
- [`OGCODE_RISK_MAP.md`](OGCODE_RISK_MAP.md) — Risk assessment per area
- [`TRANSPLANT_MAP.md`](TRANSPLANT_MAP.md) — Keep / Rewrite / Drop decisions
