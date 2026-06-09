# 🚪 GATES — ANVIL

> Statusklassen: `done` · `prototype` · `docs-only` · `partial` · `blocked` · `superseded`  
> Letzte Reconciliation: 2026-05-09 (Gate AX) · Letzte Aktualisierung: 2026-05-20 (Gate B8)
> Vollständige Analyse: [`docs/GATE_RECONCILIATION.md`](docs/GATE_RECONCILIATION.md)

---

## Abgeschlossene Gates

| Gate | Name | Status | Dateien |
|------|------|--------|---------|
| A1 | Repo Access Proof | `done` | `REPO_ACCESS_PROOF.md`, `CURRENT_TREE.md` |
| A2 | Naming & Concept Contract | `done` | `docs/ANVIL_CONCEPT_CONTRACT.md` |
| A3 | Shell & Surface | `done` | `app/index.html`, `app/style.css`, `app/app.js` |
| A4 | Workspace Model | `done` | `docs/WORKSPACE_MODEL.md`, `app/data.js` |
| A5 | Module Slot Contract | `done` | `docs/MODULE_CONTRACT.md`, `modules/*/module.json` |
| A6 | The Forge — Module Launchpad | `done` | `app/app.js` (Forge-Overlay) |
| A11 | Governance Files | `done` | `AGENTS.md`, `CLAUDE.md`, `principles/` |
| A12 | Prompt Pack Builder | `done` | `modules/prompt-pack-builder/` |
| A14 | Provider Registry | `done` | `app/data.js` (PROVIDER_REGISTRY) |
| A15 | Nvidia Build Models | `done` | `app/data.js` (NVIDIA_MODELS) |
| A16 | HuggingFace Launcher | `done` | `app/data.js` (HF_TOP_MODELS) |
| A17 | Platform Abstraction | `done` | `app/platform.js` |
| A19 | Workspace Sync | `done` | `app/sync.js` |

## Prototype Gates

| Gate | Name | Status | Dateien | Lücke |
|------|------|--------|---------|-------|
| A10 | Local Preview / Run Surface | `prototype` | `app/app.js` (Preview) | Preview ja, Run/Execution nein |
| A13 | Token Management | `prototype` | `modules/token-manager/` | Key im Klartext, keine Encryption |

## Partial Gates

| Gate | Name | Status | Dateien | Lücke |
|------|------|--------|---------|-------|
| A7 | Artifact Output Layer | `partial` | `docs/ARTIFACT_OUTPUT_LAYER.md`, `app/data.js` | Kein Manifest-Generator, leere Registry |
| A8 | Blueprint Export | `partial` | `modules/prompt-pack-builder/`, `docs/AGENT_HANDOFF_FORMAT.md` | Kein Export-Flow, keine A7-Integration |

## Docs-Only Gates

| Gate | Name | Status | Dateien | Was fehlt |
|------|------|--------|---------|----------|
| A9 | Tasker/App-Factory Blueprint | `docs-only` | `docs/ANDROID_BLUEPRINT_TRACK.md` | Kein Modul-Verzeichnis, kein Scaffolding-Code |
| A18 | Pake Desktop Shell | `docs-only` | `pake.config.json`, `docs/PAKE_DESKTOP_SHELL.md` | Kein Build, keine Icons, Name falsch ("Anvil Bellows") |
| A20 | OmniRoute Gateway Bridge | `docs-only` | `docs/OMNIROUTE_BRIDGE.md` | Kein Bridge-Code, nur Provider-Eintrag |

## Meta-Gate

| Gate | Name | Status | Dateien |
|------|------|--------|---------|
| AX | Repo Reality Lock | `done` | `docs/REPO_REALITY_LOCK.md`, `docs/GATE_RECONCILIATION.md`, `docs/KNOWN_DRIFT_RISKS.md` |

---

## 🧬 Native Donor-Codebase Transplant Preparation

| Gate | Name | Status | Dateien |
|------|------|--------|---------|
| AT1 | Source License & Provenance Lock | `done` | `docs/provenance/OGCODE_SOURCE_AUDIT.md`, `docs/provenance/LICENSE_DECISION.md`, `docs/provenance/THIRD_PARTY_NOTICES.md` |
| AT2 | Donor Codebase Inventory | `done` | `docs/provenance/OGCODE_CODEBASE_INVENTORY.md`, `docs/provenance/OGCODE_FEATURE_MAP.md`, `docs/provenance/OGCODE_RISK_MAP.md` |
| AT3 | Transplant Map: Keep / Rewrite / Drop | `done` | `docs/provenance/TRANSPLANT_MAP.md`, `docs/CODEBASE_TRANSPLANT_RULES.md` |
| AT4 | Execution Core Skeleton | `done` | `src/core/*/README.md`, `docs/EXECUTION_CORE_ARCHITECTURE.md`, `docs/INTERNAL_EXECUTION_PLAN.md`, `docs/RUN_MODEL.md`, `docs/TASK_GRAPH_MODEL.md` |

### Gate AT1 — Source License & Provenance Lock
- **Ziel:** Lizenz, Copyright, Attribution der Donor-Codebase dokumentieren
- **Entscheidung:** MIT-Lizenz — kompatibel. Kein Code-Import vor AT1–AT3 abgeschlossen.
- **Kill:** Lizenz inkompatibel, Attribution unmöglich

### Gate AT2 — Donor Codebase Inventory
- **Ziel:** Donor-Repo vollständig inventarisieren — Ordner, Dateien, Capabilities, Risiken
- **Ergebnis:** 22 Abschnitte, ~12.850 Go-Zeilen + ~8.570 Web-Zeilen dokumentiert
- **Kill:** Repo nicht zugreifbar

### Gate AT3 — Transplant Map: Keep / Rewrite / Drop
- **Ziel:** Für jeden Donor-Bereich entscheiden: KEEP / REWRITE / DROP / DEFER / UNKNOWN
- **Ergebnis:** 0 KEEP, 27 REWRITE, 18 DROP, 5 DEFER, 1 UNKNOWN
- **Kill:** Donor-Code ohne Map-Eintrag in aktive Pfade kopiert

### Gate AT4 — Execution Core Skeleton
- **Ziel:** Native Zielarchitektur für spätere Transplantation anlegen (nur Skeleton, keine Execution)
- **Ergebnis:** `src/core/` mit 11 Unterverzeichnissen + README.md je Verzeichnis + 4 Architektur-Docs
- **Kill:** Execution-Code implementiert, Donor-Code importiert

---

## 🏗️ B-Gates — Execution Core Foundation (KMP)

> Diese Gates implementieren den Execution Core in `anvil-kmp/`.  
> Abhängigkeitskette: B1 → B2 → B3 → B4 → ...

| Gate | Name | Status | Dateien | Abhängig von |
|------|------|--------|---------|-------------|
| B1 | Safety Policy | `done` | `docs/SAFETY_POLICY.md` | — |
| B2 | KMP Core Contracts | `done` | `anvil-kmp/core/contracts/`, `anvil-kmp/core/quality/` | B1 |
| B3 | KMP Core Domain | `done` | `anvil-kmp/core/domain/` (Workspace, Run, Artifact, Snapshot, MemoryEntry) | B2 |
| B4 | KMP Core Pipeline | `done` | `anvil-kmp/core/pipeline/` (RunStep, RunResult, StepRecord) | B3 |
| B5 | KMP Bellows (LLM-Routing) | `done` | `anvil-kmp/modules/bellows/` (ProviderAdapter, BellowsRouter, BellowsLegacyClient, AnvilBellowsBridgeAdapter) | B2 |
| B6 | KMP Knight (Datei-I/O) | `done` | `anvil-kmp/modules/forge/knight/` (Knight, FileDiff, UnifiedDiff, module.json, README) | B3 |
| B7 | KMP Knight (Contract) | `done` | `anvil-kmp/modules/forge/knight/` (KnightContract, FileContent, WriteResult, UnifiedDiff, PatchResult, DiffEngine, PatchApplier) | B6 |
| B8 | Compose Commander Shell | `done` | `anvil-kmp/surfaces/commander/` (CommanderState, CommanderEvent, CommanderViewModel, CommanderApp, WorkspaceBrowser, DiffViewer, RunLog, QualityBadge) | B7 |
| B9 | Bellows Gateway (Produktionsreife) | `done` | `anvil-kmp/modules/bellows/` (OpenAiCompatibleAdapter, BellowsRouter, BellowsConfig, ProviderFactory, wire/OpenAiDto), `anvil-kmp/app/bellows-gateway/` (Ktor-Server, CLI, JvmCredentialVault), `docs/BELLOWS_GATEWAY.md` | B5 |

### Gate B1 — Safety Policy
- **Ziel:** Bindende Regeln für alle Execution-Code-Implementierungen
- **Ergebnis:** `docs/SAFETY_POLICY.md` — Command Guard Allowlist, Scope-Beschränkung, Credential-Policy, Privacy-Mode, Transplant-Checklist
- **Kill:** Execution-Code vor Existenz dieser Policy

### Gate B2 — KMP Core Contracts
- **Ziel:** Erste Kotlin-Quellen — Interfaces und Contracts ohne Seiteneffekte
- **Ergebnis:** `ModuleSlotContract`, `BellowsContract`, `CredentialVaultContract`, `QualityState`, `QualityGuard`, `QualityReport`
- **Besonders:** `CredentialVaultContract` adressiert Risk 5 (Token Manager Plaintext) für alle künftigen Implementierungen
- **Kill:** Donor-Code importiert, Execution-Code geschrieben (nur Interfaces!)

### Gate B3 — KMP Core Domain
- **Ziel:** Serialisierbare Datenmodelle für den Execution Core — kein Verhalten, nur Typen
- **Ergebnis:**
  - `Workspace` (id, name, description, rootPath, buildTarget, status, moduleIds)
  - `Run` (runId, workspaceId, planId, taskId, status, artifacts, logs, humanReviewRequired=true)
  - `Artifact` (id, runId, kind, path, sizeBytes, producedAt)
  - `Snapshot` (id, workspaceId, runId?, takenAt, checkpoint: CheckpointData)
  - `MemoryEntry` (id, workspaceId, runId?, content, kind, timestamp)
- **Besonders:** `Workspace.rootPath` ist die Scope-Grenze für alle Datei-Mutationen (SAFETY_POLICY.md)
- **Kill:** Donor-Code importiert, Verhalten implementiert (nur Datenmodelle!)

### Gate B4 — KMP Core Pipeline
- **Ziel:** Sealed-Hierarchien für Pipeline-Primitives — kein Verhalten, nur Typen
- **Ergebnis:**
  - `RunStep` sealed: ReadFile, WriteFile, PromptLlm, RunCommand, SaveCheckpoint
  - `RunResult` sealed: FileRead, FileWritten, LlmResponse, CommandExecuted, CheckpointSaved, Failure
  - `StepRecord` (step, result, durationMs, timestamp) — Audit-Log-Eintrag
- **Besonders:** `RunStep.RunCommand` referenziert Command Guard Allowlist (SAFETY_POLICY.md). `RunStep.PromptLlm` nutzt `ModelRequest` aus `:core:contracts`.
- **Kill:** Donor-Code importiert, Execution-Logik implementiert (nur sealed types!)

### Gate B5 — KMP Bellows (LLM-Routing)
- **Ziel:** Erste Bellows-Implementierung als Bridge-Adapter zu ANVIL-BELLOWS (Android)
- **Ergebnis:**
  - `commonMain`: `ProviderAdapter` (pluggable adapter interface), `BellowsRouter` (implements `BellowsContract`)
  - `androidMain`: `BellowsLegacyClient` (fun interface), `AnvilBellowsBridgeAdapter` (delegates to ANVIL-BELLOWS)
- **Wiring in `:app:android`:** `BellowsRouter(listOf(AnvilBellowsBridgeAdapter(legacyClient)))`
- **Privacy-Mode:** `LOCAL_ONLY` wirft `BellowsExhaustedException` — kein Cloud-Fallback
- **Nächste Gate (Bellows-KMP-Migration):** ANVIL-BELLOWS Internals nach KMP portieren, Bridge entfernt
- **Kill:** Donor-Code importiert, Credentials im Klartext, `LOCAL_ONLY` mit Cloud-Fallback

### Gate B6 — KMP Knight (Datei-I/O)
- **Ziel:** File-I/O- und Diff-Layer — `ModuleSlotContract` + `CheckpointCapable`-Implementierung
- **Ergebnis:**
  - `Knight(fileSystem, workspace)` — read/write/delete via injiziertem Okio `FileSystem`
  - `write()` gibt `FileDiff(path, unified)` zurück — Diff old→new als Teil des Rückgabewerts
  - `diff(original, modified)` — reiner Kotlin LCS Unified-Diff (kein diff-match-patch)
  - `checkpoint()` / `restore()` serialisieren `KnightState` (workspaceId, rootPath, qualityState)
  - `KnightScopeViolation` + `QualityState.FAILED` bei Pfad außerhalb `workspace.rootPath`
- **Besonders:** `FileSystem` wird injiziert — `FileSystem.SYSTEM` in Produktion, `FakeFileSystem` in Tests
- **Kill:** Donor-Code importiert, Mutations ohne Scope-Prüfung, Silent-Fail auf Scope-Verletzung

### Gate B7 — KMP Knight (Contract)
- **Ziel:** Knight-Modul formalisieren: typisierter `KnightContract` + `applyPatch()`
- **Ergebnis:**
  - `KnightContract` — Interface mit readFile / writeFile / diff / applyPatch / qualityState
  - `FileContent(path, content)` — typisierter Rückgabewert für readFile
  - `WriteResult(path, diff)` — typisierter Rückgabewert für writeFile
  - `UnifiedDiff(text)` — typisiertes Diff-Objekt (data class, ersetzt raw String)
  - `PatchResult(path, success, appliedHunks, rejectedHunks)` — Ergebnis von applyPatch
  - `diff/DiffEngine` — LCS-Algorithmus (umbenannt von UnifiedDiff-Object); gibt `UnifiedDiff` zurück
  - `diff/PatchApplier` — pure-Kotlin Unified-Diff Patch-Applicator mit radialer Kontext-Suche
- **Besonders:** `applyPatch` lokalisiert Hunks via kontextbasierter Suche (radiale Expansion um Hint). Partial-Apply möglich: `PatchResult.rejectedHunks > 0` → `QualityState.DEGRADED`.
- **diff-match-patch:** Java-only, nicht KMP-kompatibel. Reiner Kotlin LCS + Patch-Applicator deckt Unified-Diff vollständig ab.
- **Kill:** Donor-Code importiert, applyPatch ohne Scope-Prüfung, Silent-Fail auf Rejection

### Gate B8 — Compose Commander Shell
- **Ziel:** State-First Compose UI — drei Panels: Workspace-Browser, Diff-Viewer, Run-Log
- **Ergebnis:**
  - `CommanderState` (workspace, files, selectedFile, activeDiff, runLog, knightQuality, bellowsQuality, isLoading, error) — Single Source of Truth
  - `CommanderEvent` sealed: OpenWorkspace, LoadFiles, SelectFile, WriteFile, ApplyPatch, DismissError
  - `CommanderViewModel` — coroutine-basierter State-Holder, `StateFlow<CommanderState>`, delegiert I/O an `KnightContract`
  - `CommanderApp` — Composable: `Row(WorkspaceBrowser 25% | DiffViewer 50% | QualityBadge+RunLog 25%)`
  - `WorkspaceBrowser` — Workspace-Name + `LazyColumn` der Dateinamen, klickbar
  - `DiffViewer` — Diff-Text (Priorität) oder Dateiinhalt in Monospace + Scroll
  - `RunLog` — `LazyColumn` der Log-Einträge
  - `QualityBadge` — farbige Surface-Chips für Knight + Bellows (4 Zustände)
- **Besonders:** `LoadFiles` Event entkoppelt Verzeichnis-Listing vom Surface — App-Layer (B9) fütter die Dateiliste. `bellowsQuality` initial STABLE; B9 koppelt BellowsRouter ein.
- **Kill:** Donor-Code importiert, Seiteneffekte in Composables, State nicht durch ViewModel zentralisiert

### Gate B9 — Bellows Gateway (Produktionsreife)
- **Ziel:** Aus dem Bellows-Bridge-Skeleton (B5) einen tatsächlich nutzbaren, OpenAI-kompatiblen
  LLM-Gateway machen — lokal auf Windows/Desktop lauffähig, für OpenCode & lokale Modelle (Hermes).
  Erfüllt den in B5 angekündigten nächsten Schritt: „ANVIL-BELLOWS Internals nach KMP portieren, Bridge entfernt."
- **Ergebnis:**
  - `:core:contracts`: `ModelRequest`/`ModelResponse` angereichert (`ChatMessage`, `TokenUsage`) — OpenAI-tauglich.
  - `:modules:bellows` (commonMain/KMP): `OpenAiCompatibleAdapter` (Ktor-Client; deckt OpenAI, OpenRouter,
    Nvidia, LM Studio, Ollama, llama.cpp, vLLM ab), produktionsreifer `BellowsRouter`
    (Privacy → Modell-Match → Health → Fallback-Kette), `BellowsConfig`/`ProviderConfig`,
    `ProviderFactory`, kanonische `wire/OpenAiDto`. Android-Bridge entfernt.
  - `:app:bellows-gateway` (JVM): Ktor-Server (`POST /v1/chat/completions`, `GET /v1/models`, `/health`),
    SSE-Streaming, optionaler Bearer-Token, CLI (`serve|config|key|models`),
    `JvmCredentialVault` (JCEKS — kein Klartext-Key, SAFETY_POLICY §3).
  - Build JVM-fähig: Android-Target opt-in (`-Panvil.android`), Gradle-Wrapper ergänzt,
    Ktor-Server im Version-Catalog, Root auf `compilerOptions`-DSL migriert.
- **Verifiziert:** `:modules:bellows:jvmTest` + `:app:bellows-gateway:test` grün; Live-Smoke
  (echter `bellows serve` gegen Upstream liefert OpenAI-konforme Antwort); `installDist` erzeugt
  `bellows`/`bellows.bat`.
- **Privacy:** `LOCAL_ONLY` (Header `X-Anvil-Privacy: local_only`) routet nur lokal — kein Cloud-Fallback,
  sonst `503 bellows_exhausted`.
- **Kill:** Donor-Code importiert, Credentials im Klartext (Config/Log/Store), `LOCAL_ONLY` mit Cloud-Fallback.
- **Offen (dokumentiert in `docs/BELLOWS_GATEWAY.md` §10):** inkrementelles Passthrough-Streaming,
  Cost-Cap-/Rate-Limit-Enforcement.

---

## 🔜 Deferred Gates (warten auf Execution Core)

> A21–A24 können nicht sinnvoll implementiert werden, bevor der Execution Core existiert.
> Sie bleiben als Roadmap dokumentiert, werden aber nicht priorisiert.

### Gate A21: Android-APK-Builder *(deferred until Execution Core exists)*
- **Branch:** `gate/a21-apk-builder`
- **To-Dos:**
  - [ ] Workspace → APK Export Pipeline
  - [ ] Module als Feature-Module im APK
  - [ ] Signierung mit Keystore
  - [ ] Build-Status Dashboard
- **Akzeptanz:** Funktionierendes APK aus Workspace
- **Kill:** Broken APK

### Gate A22: Plugin-Marketplace *(deferred until Execution Core exists)*
- **Branch:** `gate/a22-marketplace`
- **To-Dos:**
  - [ ] Module Registry (JSON-basiert)
  - [ ] Install/Remove per CLI
  - [ ] Versions-Management
  - [ ] Dependency Resolution
- **Akzeptanz:** Module installierbar und auffindbar
- **Kill:** Zentrale Server-Abhängigkeit

### Gate A23: Agent Prompt Pack v2 *(deferred until Execution Core exists)*
- **Branch:** `gate/a23-prompt-pack-v2`
- **To-Dos:**
  - [ ] Strukturiertes Prompt-Format
  - [ ] Context-Window-Management
  - [ ] Multi-Agent-Handoff
  - [ ] Prompt-Versionierung
- **Akzeptanz:** Agent kann Workspace lesen und modifizieren
- **Kill:** Prompt > 100K Tokens

### Gate A24: Collaborative Workspaces *(deferred until Execution Core exists)*
- **Branch:** `gate/a24-collab`
- **To-Dos:**
  - [ ] Workspace-Export/Import (ZIP)
  - [ ] Diff-Viewer für Workspace-Stände
  - [ ] Merge-Strategie
  - [ ] Conflict Resolution UI
- **Akzeptanz:** Workspace transferierbar
- **Kill:** Echtzeit-Sync (zu komplex für jetzt)

---

## Änderungsprotokoll

| Datum | Änderung |
|-------|----------|
| 2026-05-08 | Gates A1–A20 initial erstellt und gepusht |
| 2026-05-09 | Gate AX: Reconciliation — Status korrigiert, Statusklassen eingeführt |
| 2026-05-10 | Gates AT1–AT4: Donor-Codebase Transplant Preparation angelegt |
| 2026-05-10 | Gates A21–A24 als „deferred until Execution Core exists“ markiert |
| 2026-05-20 | Drift-Bereinigung: Risk 7 (Status-Inflation) ✅, Risk 9 (Permission-Drift) ✅; storage.local in MODULE_CONTRACT.md ergänzt; ANDROID_BLUEPRINT_TRACK Status auf docs-only gesetzt; GATE_RECONCILIATION A18 Pake-Name-Befund aktualisiert; ogcode-Compliance verifiziert |
| 2026-05-20 | Gate B1: Safety Policy (docs/SAFETY_POLICY.md) — Risk 14 behoben |
| 2026-05-20 | Gate B2: KMP Core Contracts (core/contracts + core/quality) — Risk 10 teilweise, Risk 5 adressiert |
| 2026-05-20 | Gate B5: KMP Bellows — BellowsRouter + AnvilBellowsBridgeAdapter (Bridge zu ANVIL-BELLOWS) |
| 2026-05-20 | Gate B3: KMP Core Domain — Workspace, Run, Artifact, Snapshot, MemoryEntry (Recovery-Push) |
| 2026-05-20 | Gate B4: KMP Core Pipeline — RunStep sealed, RunResult sealed, StepRecord (Recovery-Push) |
| 2026-05-20 | Gate B6: KMP Knight — Knight (read/write/delete/diff), FileDiff, UnifiedDiff (LCS), CheckpointCapable (Recovery-Push) |
| 2026-05-20 | Gate B7: KMP Knight Contract — KnightContract, FileContent, WriteResult, UnifiedDiff (data class), PatchResult, DiffEngine, PatchApplier (applyPatch) |
| 2026-05-20 | Gate B8: Compose Commander Shell — CommanderState, CommanderEvent, CommanderViewModel, CommanderApp, WorkspaceBrowser, DiffViewer, RunLog, QualityBadge |
| 2026-06-09 | Gate B9: Bellows Gateway (Produktionsreife) — OpenAI-kompatibler Router/Gateway (JVM/Windows), OpenAiCompatibleAdapter, BellowsRouter (Fallback + LOCAL_ONLY), Ktor-Server + CLI + JCEKS-Vault, Bridge entfernt. Build JVM-fähig (Android opt-in, Gradle-Wrapper). Verifiziert per Tests + Live-Smoke. |

Gate-Reihenfolge wird nicht nachträglich geändert.
