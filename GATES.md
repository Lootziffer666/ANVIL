# 🚪 GATES — ANVIL

> Statusklassen: `done` · `prototype` · `docs-only` · `partial` · `blocked` · `superseded`  
> Letzte Reconciliation: 2026-05-09 (Gate AX) · Letzte Aktualisierung: 2026-05-20 (Gates B1–B2)  
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
|------|------|--------|---------|-----------|
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
| B3 | KMP Core Domain | `done` | `anvil-kmp/core/domain/` | B2 |
| B4 | KMP Core Pipeline | `done` | `anvil-kmp/core/pipeline/` | B3 |
| B5 | KMP Bellows (LLM-Routing) | `done` | `anvil-kmp/modules/bellows/` | B2 |
| B6 | KMP Knight (Datei-I/O) | `done` | `anvil-kmp/modules/forge/knight/` | B3 |
| B7 | kotlinx-datetime Timestamp | `done` | `gradle/libs.versions.toml`, `ScopeGuard.kt` | B6 |
| B8 | Plan + Task Domain Models | `done` | `anvil-kmp/core/domain/` | B3 |
| B9 | Warden (CommandGuard) | `done` | `anvil-kmp/core/quality/` | B1 |
| B10 | RunContext + RunEngine Interface | `done` | `anvil-kmp/core/pipeline/` | B4, B8 |
| B11 | ForgeRunner (RunEngine-Impl) | `done` | `anvil-kmp/modules/forge/runner/` | B9, B10 |

### Gate B1 — Safety Policy
- **Ziel:** Bindende Regeln für alle Execution-Code-Implementierungen
- **Ergebnis:** `docs/SAFETY_POLICY.md` — Command Guard Allowlist, Scope-Beschränkung, Credential-Policy, Privacy-Mode, Transplant-Checklist
- **Kill:** Execution-Code vor Existenz dieser Policy

### Gate B2 — KMP Core Contracts
- **Ziel:** Erste Kotlin-Quellen — Interfaces und Contracts ohne Seiteneffekte
- **Ergebnis:** `ModuleSlotContract`, `BellowsContract`, `CredentialVaultContract`, `QualityState`, `QualityGuard`, `QualityReport`
- **Besonders:** `CredentialVaultContract` adressiert Risk 5 (Token Manager Plaintext) für alle künftigen Implementierungen
- **Kill:** Donor-Code importiert, Execution-Code geschrieben (nur Interfaces!)

### Gate B5 — KMP Bellows (LLM-Routing)
- **Ziel:** `BellowsContract`-Implementierung mit `ProviderAdapter`-Interface für alle Provider
- **Ergebnis:** `BellowsRouter` + `ProviderAdapter` — LOCAL_ONLY-Enforcement, `BellowsExhaustedException`
- **Besonders:** Kein Ktor — HTTP-Engine kommt erst mit konkreten Cloud-Adaptern (Anti-Scope-Creep)
- **Kill:** Cloud-Adapter ohne explizite Gate; Ktor ohne konkreten Adapter-Bedarf

### Gate B6 — KMP Knight (Datei-I/O)
- **Ziel:** Datei-I/O-Facade mit Scope-Guard, Diff-Tracking, `ChangedFile`-Rückgabe
- **Ergebnis:** `Knight` + `KnightReader` + `KnightWriter` + `KnightDiff` + `ScopeGuard`
- **Besonders:** `requireInScope()` erzwingt `Workspace.rootPath`-Beschränkung (Safety Policy §2); pure-Kotlin Diff ohne externe Deps
- **Kill:** Datei-Mutation außerhalb `rootPath`; externes diff-Vendor in commonMain

### Gate B7 — kotlinx-datetime Timestamp
- **Ziel:** `currentTimestamp()` Stub durch echten ISO-8601-Timestamp ersetzen
- **Ergebnis:** `kotlinx-datetime 0.6.0` in Catalog + `Clock.System.now().toString()` in `ScopeGuard.kt`
- **Kill:** Nicht-KMP-kompatibler Timestamp (JVM-only API in commonMain)

### Gate B8 — Plan + Task Domain Models
- **Ziel:** Vollständiges Domain-Modell in `:core:domain` — Plan-Lifecycle + Task-Graph
- **Ergebnis:** `Plan` (PlanLifecycle: DRAFT→KILLED, killCriteria, forbiddenFiles) + `Task` (TaskStatus, RiskLevel, dependsOn-DAG)
- **Quellen:** `INTERNAL_EXECUTION_PLAN.md`, `TASK_GRAPH_MODEL.md`
- **Kill:** Donor-Schema ohne Umbenennung übernommen

### Gate B9 — Warden (CommandGuard)
- **Ziel:** Safety Policy §1 implementieren — Allowlist/Blocklist für Shell-Kommandos
- **Ergebnis:** `CommandPolicy` (Allowlist + Blocklist) + `CommandGuard` (validate/require/qualityState)
- **Besonders:** `qualityState()` gibt `FAILED` zurück, nie Exception suppressed; kein Silent-Fail
- **Kill:** `rm`, `curl`, `sh -c` ohne Blocklist-Eintrag passierbar

### Gate B10 — RunContext + RunEngine Interface
- **Ziel:** Ausführungskontext und Engine-Schnittstelle in `:core:pipeline` — ohne Modul-Abhängigkeiten
- **Ergebnis:** `RunContext` (WorkspaceId+PlanId+TaskId+RunId+rootPath) + `RunEngine` Interface (step/run)
- **Besonders:** Konkrete Impl kommt in `:modules:forge:runner` (B11) — kein Scope Creep in `:core:*`
- **Kill:** Modul-Imports (Knight, Bellows) in `:core:pipeline`

### Gate B11 — ForgeRunner (RunEngine-Implementierung)
- **Ziel:** Erste lauffähige RunEngine, die Plan/Execute/Review/Finish-Schritte verarbeitet
- **Ergebnis:** `ForgeRunner(bellows: BellowsContract)` in `:modules:forge:runner`
- **Besonders:** Execute → CommandGuard.require() (Safety §1); Plan → bellows.route(); kein `:modules:*`-Import (CLAUDE.md §3)
- **Kill:** Modul-Abhängigkeit auf `:modules:bellows` oder `:modules:forge:knight` direkt

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
| 2026-05-10 | Gates A21–A24 als "deferred until Execution Core exists" markiert |
| 2026-05-20 | Drift-Bereinigung: Risk 7 (Status-Inflation) ✅, Risk 9 (Permission-Drift) ✅; storage.local in MODULE_CONTRACT.md ergänzt; ANDROID_BLUEPRINT_TRACK Status auf docs-only gesetzt; GATE_RECONCILIATION A18 Pake-Name-Befund aktualisiert; ogcode-Compliance verifiziert |
| 2026-05-20 | Gate B1: Safety Policy (docs/SAFETY_POLICY.md) — Risk 14 behoben |
| 2026-05-20 | Gate B2: KMP Core Contracts (core/contracts + core/quality) — Risk 10 teilweise, Risk 5 adressiert |
| 2026-05-20 | Gate B3: KMP Core Domain (Workspace, Run, Artifact, Snapshot, MemoryEntry + alle IDs) |
| 2026-05-20 | Gate B4: KMP Core Pipeline (RunStep sealed, RunResult sealed, StepRecord) |
| 2026-05-20 | Gate B5: KMP Bellows (BellowsRouter + ProviderAdapter — LLM-Routing mit LOCAL_ONLY-Enforcement) |
| 2026-05-20 | Gate B6: KMP Knight (KnightReader, KnightWriter, KnightDiff, Knight-Facade, ScopeGuard — Datei-I/O) |
| 2026-05-20 | Gate B7: kotlinx-datetime — currentTimestamp() Stub ersetzt (Clock.System.now()) |
| 2026-05-20 | Gate B8: Plan + Task Domain Models (PlanLifecycle, TaskStatus, RiskLevel, dependsOn-DAG) |
| 2026-05-20 | Gate B9: Warden — CommandGuard + CommandPolicy (Safety Policy §1) |
| 2026-05-20 | Gate B10: RunContext + RunEngine Interface in :core:pipeline |
| 2026-05-20 | Gate B11: ForgeRunner — konkrete RunEngine-Impl in :modules:forge:runner |

Gate-Reihenfolge wird nicht nachträglich geändert.
