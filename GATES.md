# 🚪 GATES — ANVIL

> Statusklassen: `done` · `prototype` · `docs-only` · `partial` · `blocked` · `superseded`  
> Letzte Reconciliation: 2026-05-09 (Gate AX)  
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

## 🔜 Nächste Gates

### Gate A21: Android-APK-Builder
- **Branch:** `gate/a21-apk-builder`
- **To-Dos:**
  - [ ] Workspace → APK Export Pipeline
  - [ ] Module als Feature-Module im APK
  - [ ] Signierung mit Keystore
  - [ ] Build-Status Dashboard
- **Akzeptanz:** Funktionierendes APK aus Workspace
- **Kill:** Broken APK

### Gate A22: Plugin-Marketplace
- **Branch:** `gate/a22-marketplace`
- **To-Dos:**
  - [ ] Module Registry (JSON-basiert)
  - [ ] Install/Remove per CLI
  - [ ] Versions-Management
  - [ ] Dependency Resolution
- **Akzeptanz:** Module installierbar und auffindbar
- **Kill:** Zentrale Server-Abhängigkeit

### Gate A23: Agent Prompt Pack v2
- **Branch:** `gate/a23-prompt-pack-v2`
- **To-Dos:**
  - [ ] Strukturiertes Prompt-Format
  - [ ] Context-Window-Management
  - [ ] Multi-Agent-Handoff
  - [ ] Prompt-Versionierung
- **Akzeptanz:** Agent kann Workspace lesen und modifizieren
- **Kill:** Prompt > 100K Tokens

### Gate A24: Collaborative Workspaces
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

Gate-Reihenfolge wird nicht nachträglich geändert.
