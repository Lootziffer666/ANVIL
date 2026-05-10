# OGCODE_CODEBASE_INVENTORY — Gate AT2

> Gate: AT2 — Donor Codebase Inventory  
> Status: `done`  
> Last updated: 2026-05-10

---

## 1. Audit Boundary

This inventory covers the donor repository at commit HEAD as of 2026-05-10.  
Only the source tree is inventoried — no build artifacts, no vendored dependencies, no `.git` internals.

**What is inventoried:**
- All Go source files under `internal/` and `main.go`
- All TypeScript/React source files under `web/src/`
- Configuration and build files (`go.mod`, `Makefile`, `Dockerfile`, web build config)
- Documentation files at repo root

**What is NOT inventoried:**
- Compiled build output (`web/build/`)
- Git history
- GitHub Actions workflows (noted but not audited in detail)

---

## 2. Donor Repository Snapshot

| Field | Value |
|-------|-------|
| Repository | `https://github.com/prasenjeet-symon/ogcode` |
| Snapshot Date | 2026-05-10 |
| Primary Language | Go 1.26.1 |
| Secondary Language | TypeScript (React, Vite) |
| Go Source Lines | ~12,850 |
| Web Source Lines | ~8,570 |
| License | MIT (Prasenjeet Symon, 2026) |
| Build System | Go toolchain (backend), Vite (frontend) |
| Database | SQLite (via `modernc.org/sqlite`, pure Go) |
| CLI Framework | Cobra (`spf13/cobra`) |

---

## 3. Folder Inventory

| Path | Type | Description |
|------|------|-------------|
| `main.go` | Entry | CLI bootstrap — calls `cli.Execute()` |
| `internal/agent/` | Core | Agent definitions (Build, Plan, Breakdown) and agent loop |
| `internal/bus/` | Core | Event bus for internal message passing |
| `internal/cli/` | Core | Cobra CLI commands (root, version, check_updates) |
| `internal/db/` | Core | SQLite database layer with goose migrations (14 migrations) |
| `internal/git/` | Core | Git operations (empty or minimal — directory exists) |
| `internal/id/` | Util | ULID-based ID generation |
| `internal/mcp/` | Integration | MCP (Model Context Protocol) client and config |
| `internal/memory/` | Core | Agentic memory system — knowledge graph with topics, concepts, facts |
| `internal/permission/` | Core | Permission system with Allow/Deny/Ask rules |
| `internal/plan/` | Core | Plan model, store, and utilities |
| `internal/provider/` | Core | LLM provider abstraction (Anthropic, OpenAI, OpenRouter, Ollama) |
| `internal/server/` | Core | HTTP server with chi router — REST API + SSE + static file serving |
| `internal/session/` | Core | Session model, message model, stores (session, message, model, provider) |
| `internal/task/` | Core | Task model, store, and utilities |
| `internal/tool/` | Core | Tool definitions: bash, read, write, edit, glob, grep, memory_recall |
| `internal/version/` | Util | Version string management |
| `web/src/` | UI | React/TypeScript SPA — session UI, plan UI, task board, settings |
| `web/src/api/` | UI | API client and SSE event stream |
| `web/src/components/` | UI | React components (message list, prompt input, task board, etc.) |
| `web/src/context/` | UI | React context providers (notification, plan, server, session, theme) |
| `web/src/pages/` | UI | Page components (home, session, plan-list, plan-detail, plan-tasks, task-execution, settings) |
| `web/src/lib/` | UI | Utility libraries (providers list, scroll memory) |
| `assets/` | Static | Demo GIFs and images |
| `docs/` | Docs | Additional documentation |
| `.github/workflows/` | CI | GitHub Actions (not audited in detail) |

---

## 4. Entry Points

| Entry Point | File | Description |
|-------------|------|-------------|
| CLI | `main.go` → `internal/cli/root.go` | Cobra root command — starts HTTP server |
| HTTP Server | `internal/server/server.go` | chi router, serves API + embedded SPA |
| Agent Loop | `internal/agent/loop.go` | Core execution loop for Build/Plan/Breakdown agents |
| Database | `internal/db/db.go` | SQLite open + goose migration runner |

---

## 5. UI Layer

| Component | File | Purpose |
|-----------|------|---------|
| App Router | `web/src/app.tsx` | React Router — top-level routing |
| Home | `web/src/pages/home.tsx` | Landing / session list |
| Session View | `web/src/pages/session.tsx` | Chat interface for Build mode |
| Plan List | `web/src/pages/plan-list.tsx` | List of plans |
| Plan Detail | `web/src/pages/plan-detail.tsx` | Single plan conversation |
| Plan Tasks | `web/src/pages/plan-tasks.tsx` | Task breakdown view |
| Task Execution | `web/src/pages/task-execution.tsx` | Live task execution view |
| Settings | `web/src/pages/settings/` | General, Models, About pages |
| Message List | `web/src/components/message-list.tsx` | Chat message rendering |
| Task Board | `web/src/components/task-board.tsx` | Kanban-style task view |
| Task Card | `web/src/components/task-card.tsx` | Individual task card |
| Model Selector | `web/src/components/model-selector.tsx` | LLM model picker |
| Prompt Input | `web/src/components/prompt-input.tsx` | User input component |

**Assessment:** UI is tightly coupled to donor product identity and React/Vite stack.  
**Recommendation:** DROP or full REWRITE. Anvil has its own HTML/CSS/JS UI.

---

## 6. Planning / Plan Mode Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/plan/plan.go` | 35 | Plan struct with statuses (open/locked), breakdown status tracking |
| `internal/plan/store.go` | ~150 | SQLite CRUD for plans |
| `internal/plan/util.go` | ~30 | Utilities |
| `internal/agent/agent.go` | ~180 | PlanAgent definition — read-only, system prompt for planning |
| `internal/agent/breakdown.go` | ~180 | BreakdownAgent — converts locked plan to structured task definitions |
| `internal/server/plan_routes.go` | ~200 | REST API for plan operations |

**Key concepts:**
- Plan = conversation between user and PlanAgent
- Plan can be `open` (editable) or `locked` (finalized)
- Locked plan triggers BreakdownAgent → produces TaskDefinitions
- Breakdown has states: none → in_progress → completed/failed
- Archive system: completed plans stored as markdown in `.ogcode/archives/`

**Assessment:** Architecture is solid. Plan/Lock/Breakdown pattern is adaptable.

---

## 7. Task Breakdown Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/task/task.go` | 50 | Task struct with status, effort, complexity, dependencies, branch, PR |
| `internal/task/store.go` | ~200 | SQLite CRUD for tasks |
| `internal/task/util.go` | ~30 | Utilities |
| `internal/agent/breakdown.go` | ~180 | Task extraction from plan — JSON parsing, validation, defaults |
| `internal/server/task_routes.go` | ~150 | REST API for task operations |

**Key concepts:**
- Task has status: pending → in_progress → completed/failed
- Each task gets its own git branch and worktree
- Tasks have dependencies (linear chain only, no fan-in)
- Effort levels: S, M, L, XL
- Complexity: low, medium, high
- Auto-PR creation on completion

**Assessment:** Task model is clean. Dependency chain constraint is intentional and sound.

---

## 8. Run / Session Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/session/message.go` | ~80 | Session struct, MessageInfo, MessageWithParts, Part types |
| `internal/session/store.go` | ~100 | Session CRUD |
| `internal/session/model_store.go` | ~80 | Model preference persistence |
| `internal/session/provider_store.go` | ~80 | Provider config persistence |
| `internal/session/memory_store.go` | ~60 | Memory config persistence |
| `internal/session/schema.go` | ~20 | ID type aliases |
| `internal/agent/loop.go` | ~1,400 | Agent execution loop — message streaming, tool execution, permission handling |

**Key concepts:**
- Session = isolated conversation context with a directory scope
- Sessions have types: chat (Build mode) or plan (Plan mode)
- Messages have parts: text, tool calls, reasoning, files
- Agent loop streams LLM responses, executes tool calls, handles permissions
- Compaction summary for long sessions (memory optimization)

**Assessment:** Session model is thorough. Loop.go is the most complex file (~1,400 lines). Needs careful REWRITE for Anvil context.

---

## 9. Repository Context Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/git/` | ~0 | Directory exists but minimal/empty content observed |
| `internal/tool/glob.go` | ~80 | File discovery via glob patterns |
| `internal/tool/grep.go` | ~100 | Content search via regex |
| `internal/tool/read.go` | ~60 | File reading |

**Key concepts:**
- Repo context is read via tools (glob, grep, read) rather than a dedicated engine
- No explicit repo model or context graph
- Session directory serves as the working directory scope
- No `.gitignore` awareness in glob/grep (but skips hidden dirs and node_modules)

**Assessment:** Minimal dedicated repo context. Anvil could build a richer Repo Context Engine.

---

## 10. File Mutation Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/tool/write.go` | ~50 | Full file write (create or overwrite) |
| `internal/tool/edit.go` | ~60 | Search-and-replace edit (unique match required) |

**Key concepts:**
- Write: creates parent dirs, writes full content
- Edit: exact string match, single occurrence only, replaces in place
- No diff generation
- No rollback mechanism
- No file mutation log

**Assessment:** Basic but functional. Anvil's File Mutation Engine should add diff tracking, rollback, and mutation log.

---

## 11. Command Execution Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/tool/bash.go` | ~60 | Shell command execution via `sh -c` |

**Key concepts:**
- Unrestricted shell access via `sh -c`
- Timeout support (default 120s)
- Runs in session directory
- Combined stdout + stderr output
- No command allowlist/blocklist
- No sandboxing
- Permission system can require user approval (`Ask` action)

**Assessment:** HIGH RISK. Must be REWRITE with Command Guard. No direct shell access without allowlist.

---

## 12. Provider / Model Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/provider/provider.go` | ~200 | Provider interface, Registry, StreamRequest, model resolution |
| `internal/provider/anthropic.go` | ~300 | Anthropic Claude provider |
| `internal/provider/openai.go` | ~350 | OpenAI provider (also used for OpenRouter, Ollama) |

**Key concepts:**
- Provider interface: `ID()`, `Models()`, `StreamChat()`
- Optional interfaces: `Embedder`, `ModelRefresher`
- Registry with model resolution (custom models → built-in → fallback)
- Streaming via channels (`<-chan StreamEvent`)
- Supports: Anthropic, OpenAI, OpenRouter, Ollama
- API keys via env vars or DB config
- Token usage tracking (input, output, reasoning, cache)

**Assessment:** Clean abstraction. Provider Core for Anvil should REWRITE to match Anvil's existing Provider Registry pattern and add safety checks.

---

## 13. Branch / PR Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/task/task.go` | — | Task struct has `BranchName`, `WorktreePath`, `PRURL`, `PRNumber`, `PRError` |
| `internal/agent/loop.go` | — | Contains git worktree setup, branch creation, PR creation logic |

**Key concepts:**
- Each task gets a dedicated git branch
- Git worktrees used for parallel task isolation
- Auto-PR creation on task completion
- PR errors stored on task (`PRError` field)
- Branch naming convention based on task

**Assessment:** Needs REWRITE. Branch/PR automation must have safety policy before activation in Anvil.

---

## 14. Storage / Archive Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/db/db.go` | ~30 | SQLite with WAL mode, foreign keys, goose migrations |
| `internal/db/*.sql` | ~200 | 14 migration files (sessions, messages, parts, permissions, plans, tasks, themes, model prefs, provider config, memory config) |
| `internal/memory/store.go` | ~300 | Knowledge graph SQLite store (topics, concepts, facts, edges, embeddings) |

**Key concepts:**
- Single SQLite database per installation
- WAL mode for concurrent access
- Goose for migration management
- Separate memory SQLite for knowledge graph
- Archive system for completed plans (markdown in `.ogcode/archives/`)

**Assessment:** Storage layer is straightforward SQLite. Anvil may adopt similar pattern or integrate with existing workspace model.

---

## 15. Config / Environment Logic

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/mcp/config.go` | ~30 | MCP server config from env vars |
| `internal/server/config_routes.go` | ~100 | REST API for runtime config |
| `internal/session/provider_store.go` | ~80 | Provider credentials in DB |
| `internal/session/model_store.go` | ~80 | Model preferences in DB |

**Key concepts:**
- Environment variables for initial config (`OGCODE_*`)
- DB-stored provider credentials (API keys, base URLs)
- DB-stored model preferences
- DB-stored memory config
- MCP server config from env

**Assessment:** Config via env + DB is standard. Provider credentials in DB needs security review (relates to Secret Handling).

---

## 16. Auth / Secrets Handling

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/session/provider_store.go` | ~80 | Stores API keys in SQLite |
| `internal/provider/anthropic.go` | — | Reads `ANTHROPIC_API_KEY` from env |
| `internal/provider/openai.go` | — | Reads `OPENAI_API_KEY` / `OPENROUTER_API_KEY` / `OLLAMA_API_KEY` from env |

**Key concepts:**
- API keys from environment variables (primary)
- API keys storable in SQLite DB (secondary, for UI-configured providers)
- No encryption at rest
- No key rotation
- No vault integration
- No access token scoping

**Assessment:** HIGH RISK. Secret handling is plaintext. Must be REWRITE for Anvil — aligns with existing Token Manager `prototype` status and known issue (keys in localStorage).

---

## 17. Tests

| File | Lines (approx.) | Function |
|------|-----------------|----------|
| `internal/agent/agentmd_test.go` | ~30 | Tests for markdown generation |
| `internal/version/version_test.go` | ~20 | Version parsing tests |
| `internal/server/version_routes_test.go` | ~20 | Version API tests |

**Assessment:** Minimal test coverage. ~70 lines of tests for ~12,850 lines of Go code. Test infrastructure exists but coverage is negligible.

---

## 18. Build System

| File | Purpose |
|------|---------|
| `go.mod` / `go.sum` | Go module definition |
| `Makefile` | Build targets (not audited in detail) |
| `Dockerfile` | Container build |
| `.goreleaser.yaml` | Release automation |
| `install.sh` / `install.ps1` | Install scripts |
| `web/package.json` | Frontend dependencies (npm) |
| `web/vite.config.ts` | Vite bundler config |
| `web/tsconfig.json` | TypeScript config |
| `web/embed.go` | Embeds built web assets into Go binary |
| `.github/workflows/` | CI/CD (not audited in detail) |

**Assessment:** Go + Vite build pipeline. Not relevant for Anvil (different stack). DROP.

---

## 19. Security Risks

| Risk | Severity | Location | Description |
|------|----------|----------|-------------|
| Unrestricted shell execution | **Critical** | `internal/tool/bash.go` | `sh -c` with no allowlist — arbitrary command execution |
| Plaintext API keys in DB | **High** | `internal/session/provider_store.go` | No encryption at rest |
| Plaintext API keys in env | **Medium** | `internal/provider/*.go` | Standard practice but no rotation/scoping |
| No file write sandboxing | **High** | `internal/tool/write.go` | Can write anywhere in session dir |
| No edit validation | **Medium** | `internal/tool/edit.go` | Edit replaces content without semantic validation |
| Permission bypass potential | **Medium** | `internal/permission/permission.go` | Simple glob matching, no path traversal protection |
| No HTTPS enforcement | **Low** | `internal/server/server.go` | Local-only server, but no TLS |
| No auth on API | **Medium** | `internal/server/routes.go` | No authentication on REST API |

---

## 20. Candidate Extraction Areas

Areas with potential value for Anvil's Execution Core:

| Area | Donor Location | Anvil Target | Rationale |
|------|---------------|--------------|-----------|
| Plan model + lifecycle | `internal/plan/` | `core/planning/` | Clean plan/lock/breakdown pattern |
| Task model + dependencies | `internal/task/` | `core/tasks/` | Structured task graph with effort/complexity |
| Session/Run model | `internal/session/` | `core/runs/` | Session as execution context |
| Provider abstraction | `internal/provider/provider.go` | `core/providers/` | Clean interface, registry, model resolution |
| Tool interface | `internal/tool/tool.go` | `core/execution/` | Tool registry pattern |
| Permission model | `internal/permission/` | `core/safety/` | Allow/Deny/Ask pattern |
| Knowledge graph | `internal/memory/` | `core/artifacts/` (future) | Semantic memory for context |

---

## 21. Areas Requiring Rewrite

| Area | Donor Location | Reason |
|------|---------------|--------|
| Shell execution | `internal/tool/bash.go` | No allowlist, no sandboxing — needs Command Guard |
| File write/edit | `internal/tool/write.go`, `edit.go` | No mutation tracking, no rollback |
| Secret handling | Provider stores, env | Plaintext keys — needs encryption |
| Agent loop | `internal/agent/loop.go` | 1,400 lines, tightly coupled to donor session model |
| Provider implementations | `internal/provider/anthropic.go`, `openai.go` | Must match Anvil's existing Provider Registry |
| Branch/PR automation | In `loop.go` | Needs safety policy, no auto-merge |
| DB schema | `internal/db/*.sql` | Must align with Anvil's workspace model |
| MCP integration | `internal/mcp/` | Protocol coupling, needs evaluation |

---

## 22. Areas Likely To Drop

| Area | Donor Location | Reason |
|------|---------------|--------|
| Web UI (React/Vite) | `web/src/` | Anvil has its own HTML/CSS/JS UI |
| CLI framework | `internal/cli/` | Anvil is not a CLI tool |
| Dockerfile | `Dockerfile` | Different deployment model |
| GoReleaser config | `.goreleaser.yaml` | Different release process |
| Install scripts | `install.sh`, `install.ps1` | Not applicable |
| GitHub Actions | `.github/workflows/` | Different CI/CD |
| Demo assets | `assets/` | Donor marketing material |
| Event bus | `internal/bus/` | Architecture-specific, needs evaluation |
| Version management | `internal/version/` | Trivial, implement fresh |
| ID generation | `internal/id/` | Trivial, implement fresh |

---

## Cross-References

- [`OGCODE_FEATURE_MAP.md`](OGCODE_FEATURE_MAP.md) — Capability-level mapping
- [`OGCODE_RISK_MAP.md`](OGCODE_RISK_MAP.md) — Risk assessment
- [`TRANSPLANT_MAP.md`](TRANSPLANT_MAP.md) — Keep / Rewrite / Drop decisions
- [`OGCODE_SOURCE_AUDIT.md`](OGCODE_SOURCE_AUDIT.md) — License audit
