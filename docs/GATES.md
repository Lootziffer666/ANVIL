# Gates — Detailansicht

> **Kanonische Quelle:** [`/GATES.md`](../GATES.md) (Root)  
> **Reconciliation:** [`/docs/GATE_RECONCILIATION.md`](GATE_RECONCILIATION.md) (Gate AX)  
> **Letzte Aktualisierung:** 2026-05-09

---

## Gate-System

Gates sind sequenziell, jede hat:
- **Ziel** — Was soll erreicht werden?
- **Aufgaben** — Konkrete Schritte
- **Definition of Done** — Wann ist die Gate erledigt?
- **Kill-Kriterien** — Wann abbrechen?

## Statusklassen (seit Gate AX)

| Klasse | Bedeutung |
|--------|-----------|
| `done` | Gate-Ziel vollständig erreicht, Code + Docs vorhanden |
| `prototype` | Funktionaler Code vorhanden, aber nicht produktionsreif |
| `docs-only` | Nur Dokumentation, kein funktionaler Code |
| `partial` | Teilweise implementiert, wesentliche Teile fehlen |
| `blocked` | Durch externe Abhängigkeit blockiert |
| `planned` | Roadmap-Gate ist benannt, aber noch nicht implementiert |
| `superseded` | Durch spätere Entscheidung ersetzt |

## Übersicht

| Gate | Name | Status |
|------|------|--------|
| A1 | Repo Access Proof | `done` |
| A2 | Naming & Concept Contract | `done` |
| A3 | Shell & Surface | `done` |
| A4 | Workspace Model | `done` |
| A5 | Module Slot Contract | `done` |
| A6 | The Forge — Module Launchpad | `done` |
| A7 | Artifact Output Layer | `prototype` |
| A8 | Blueprint Export: Agent-Ready Prompt Packs | `prototype` |
| A9 | Tasker/App-Factory Blueprint Track | `docs-only` |
| A10 | Local Preview / Run Surface | `prototype` |
| A11 | Governance Files | `done` |
| A12 | First Real Module: Prompt Pack Builder | `done` |
| A13 | Token Management | `prototype` |
| A14 | Provider Registry + Multi-Provider | `done` |
| A15 | Nvidia Build Models | `done` |
| A16 | HuggingFace Launcher Surface | `done` |
| A17 | Platform Abstraction Layer | `done` |
| A18 | Pake Desktop Shell | `prototype` |
| A19 | Workspace Sync Protocol | `done` |
| A20 | OmniRoute Gateway Bridge | `docs-only` |
| AX | Repo Reality Lock | `done` |


## KMP-/Studio-Gates (B-Serie)

| Gate | Name | Status | Leitdokument |
|------|------|--------|--------------|
| B10 | Studio Roadmap / Contract Registry | `docs-only` | [`ANVIL_STUDIO_ROADMAP.md`](ANVIL_STUDIO_ROADMAP.md) |
| B11 | Gameplay Compiler Contracts | `prototype` | `GameplayPlan/v1`, `InteractionDefinition/v1`, `StatePatch/v1` |
| B12 | Scene Compiler / 3D-RE-GEN Contracts | `prototype` | `SceneBundle/v1`, `NavigationGraph/v1`, `CameraPlan/v1` |
| B13 | Target Adapter MVP | `prototype` | `TargetAdapterContract/v1`, `RunnableBuild/v1` |
| B14 | Interface Compiler MVP | `prototype` | `InputActionMap/v1`, `HUDState/v1` |
| B15 | Acoustic Runtime Slice | `prototype` | `AudioCueGraph/v1`, `AudioProof/v1` |
| B16 | Artifact Output Layer MVP | `prototype` | `ArtifactManifest/v1`, `ArtifactRegistry/v1` |
| B17 | Run Surface MVP | `prototype` | `RunPlan/v1`, `RunSummary/v1` |
| B18 | Handoff Export MVP | `prototype` | `HandoffExportRequest/v1`, `HandoffPackage/v1` |

B-Serie-Gates folgen der Roadmap-Regel: erst Contract Registry / Wahrheitsbesitzer,
dann Implementierung. Keine Engine-, Plugin- oder Modulabhängigkeit darf vor dem
zugehörigen Contract-Gate eingeführt werden.

## Gate-Details

Für vollständige Details pro Gate siehe:
- **Reconciliation mit Beweis:** [`GATE_RECONCILIATION.md`](GATE_RECONCILIATION.md)
- **Drift-Risiken:** [`KNOWN_DRIFT_RISKS.md`](KNOWN_DRIFT_RISKS.md)
- **Repo-Fakten:** [`REPO_REALITY_LOCK.md`](REPO_REALITY_LOCK.md)

## Gate A17 — Platform Abstraction Layer
- **Dateien:** `app/platform.js`, `docs/PLATFORM_ABSTRACTION.md`
- **Zweck:** OS-Erkennung, portable Pfade, Feature Flags, Platform Indicator
- **Status:** `done`

## Gate A18 — Pake Desktop Shell
- **Dateien:** `pake.config.json`, `docs/PAKE_DESKTOP_SHELL.md`
- **Zweck:** Anvil als native Desktop-App via Pake/Tauri
- **Status:** `docs-only` — Kein Build, keine Icons, Name-Korrektur nötig

## Gate A19 — Workspace Sync Protocol
- **Dateien:** `app/sync.js`, `docs/WORKSPACE_SYNC.md`
- **Zweck:** Cross-Device Sync (Android ↔ Windows), Export/Import Bundles
- **Status:** `done`

## Gate A20 — OmniRoute Gateway Bridge
- **Dateien:** `docs/OMNIROUTE_BRIDGE.md`
- **Zweck:** Ein Endpoint für 160+ Provider
- **Status:** `docs-only` — Nur Docs + Provider-Eintrag, kein Bridge-Code

## Gate AX — Repo Reality Lock
- **Dateien:** `docs/REPO_REALITY_LOCK.md`, `docs/GATE_RECONCILIATION.md`, `docs/KNOWN_DRIFT_RISKS.md`
- **Zweck:** Tatsächlichen Repo-Zustand gegen behauptete Gates abgleichen
- **Status:** `done`

---

## 🧬 Native Donor-Codebase Transplant Preparation

> Transplant-Gates vorbereiten die spätere native Übernahme von Architekturmustern  
> aus einer Donor-Codebase in den Anvil Execution Core.  
> Kein Code-Import. Keine Dependency. Keine Runtime-Kopplung.  
> Siehe: [`docs/CODEBASE_TRANSPLANT_RULES.md`](CODEBASE_TRANSPLANT_RULES.md)

| Gate | Name | Status |
|------|------|--------|
| AT1 | Source License & Provenance Lock | `done` |
| AT2 | Donor Codebase Inventory | `done` |
| AT3 | Transplant Map: Keep / Rewrite / Drop | `done` |
| AT4 | Execution Core Skeleton | `done` |

### Transplant-Dokumente

| Datei | Zweck |
|-------|-------|
| [`provenance/OGCODE_SOURCE_AUDIT.md`](provenance/OGCODE_SOURCE_AUDIT.md) | Lizenz-Audit der Donor-Codebase |
| [`provenance/LICENSE_DECISION.md`](provenance/LICENSE_DECISION.md) | Entscheidungslogik basierend auf Lizenzstatus |
| [`provenance/THIRD_PARTY_NOTICES.md`](provenance/THIRD_PARTY_NOTICES.md) | Attribution-Tracking (pending — keine KEEP-Entscheidungen) |
| [`provenance/OGCODE_CODEBASE_INVENTORY.md`](provenance/OGCODE_CODEBASE_INVENTORY.md) | Strukturelles Inventar der Donor-Codebase |
| [`provenance/OGCODE_FEATURE_MAP.md`](provenance/OGCODE_FEATURE_MAP.md) | Capability-Mapping Donor → Anvil |
| [`provenance/OGCODE_RISK_MAP.md`](provenance/OGCODE_RISK_MAP.md) | Risikoanalyse pro Bereich |
| [`provenance/TRANSPLANT_MAP.md`](provenance/TRANSPLANT_MAP.md) | Keep / Rewrite / Drop pro Donor-Bereich |
| [`CODEBASE_TRANSPLANT_RULES.md`](CODEBASE_TRANSPLANT_RULES.md) | Repo-weite Transplant-Regeln |
| [`EXECUTION_CORE_ARCHITECTURE.md`](EXECUTION_CORE_ARCHITECTURE.md) | Zielarchitektur Execution Core |
| [`INTERNAL_EXECUTION_PLAN.md`](INTERNAL_EXECUTION_PLAN.md) | Plan-Datenmodell |
| [`RUN_MODEL.md`](RUN_MODEL.md) | Run-Datenmodell |
| [`TASK_GRAPH_MODEL.md`](TASK_GRAPH_MODEL.md) | Task-Datenmodell |

---

## Änderungsprotokoll

Jede neue Gate wird hier eingetragen. Gate-Reihenfolge wird nicht nachträglich geändert.

| Datum | Gate | Änderung |
|-------|------|----------|
| 2026-05-08 | A1–A20 | Initial erstellt |
| 2026-05-09 | AX | Reconciliation, Statusklassen eingeführt, Status korrigiert |
| 2026-05-10 | AT1–AT4 | Donor-Codebase Transplant Preparation angelegt |
| 2026-07-11 | B10 | Studio Roadmap / fehlende Wahrheitsbesitzer dokumentiert |
| 2026-07-11 | B11 | Gameplay Compiler Contract-MVP begonnen |
| 2026-07-11 | B12 | Scene Compiler / 3D-RE-GEN Contract-MVP begonnen |
| 2026-07-11 | B13 | Target Adapter Contract-MVP begonnen |
| 2026-07-11 | B14 | Interface Compiler Contract-MVP begonnen |
| 2026-07-11 | B15 | Acoustic Runtime Contract-MVP begonnen |
| 2026-07-11 | B16/A7 | Artifact Output Layer Manifest-/Registry-MVP begonnen |
| 2026-07-11 | B17/A10 | Run Surface Module-Execution-MVP begonnen |
| 2026-07-11 | A13 | Legacy Token Manager deaktiviert |
| 2026-07-11 | A18 | Pake Icon-Asset und Build-Script ergänzt |
| 2026-07-11 | B18/A8 | Handoff Export über Artifact Layer als KMP-MVP begonnen |
