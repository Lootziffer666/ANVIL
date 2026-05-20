# Gate Reconciliation — A1 bis A20

**Gate:** AX — Repo Reality Lock  
**Stand:** 2026-05-09 (rev. 2026-05-20)  
**Agent:** Viktor (getviktor.com)  
**Methode:** Jede Gate gegen tatsächlich vorhandenen Code, Dateien und Funktionalität geprüft.

---

## Statusklassen

| Klasse | Bedeutung |
|--------|-----------|
| `done` | Gate-Ziel vollständig erreicht, Code + Docs vorhanden |
| `prototype` | Funktionaler Code vorhanden, aber nicht produktionsreif |
| `docs-only` | Nur Dokumentation, kein funktionaler Code |
| `partial` | Teilweise implementiert, wesentliche Teile fehlen |
| `blocked` | Durch externe Abhängigkeit blockiert |
| `superseded` | Durch spätere Entscheidung ersetzt |

---

## Gate A1 — Repo Access Proof

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `REPO_ACCESS_PROOF.md`, `CURRENT_TREE.md` |
| **Beweis** | Repo erreichbar, Clone erfolgreich, Dateibaum dokumentiert |
| **Lücken** | Keine |

---

## Gate A2 — Naming & Concept Contract

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `docs/ANVIL_CONCEPT_CONTRACT.md` |
| **Beweis** | Kanonische Begriffe definiert (Anvil, The Forge, Micro-App, Module Slot, Blueprint, Artifact, Build Target). Verbotene Begriffe dokumentiert. Abgrenzung Anvil ≠ IIG. State Surface Grammar. |
| **Lücken** | Keine |

---

## Gate A3 — Shell & Surface

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `app/index.html` (369 LOC), `app/style.css` (661 LOC), `app/app.js` (587 LOC) |
| **Beweis** | Werkbank-UI mit Status-Zonen: Workspace-Zone, Module-Zone, Output-Zone, Status-Timeline. State Surface Design umgesetzt (Zustand zuerst, nicht Menüs). Anti-Dashboard-Prinzip eingehalten. |
| **Lücken** | Keine |

---

## Gate A4 — Workspace Model

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `docs/WORKSPACE_MODEL.md`, `app/data.js` (`EXAMPLE_WORKSPACE`), `examples/workspace.sample.json` |
| **Beweis** | Workspace-Datenstruktur definiert (name, description, modules, inputs, outputs, buildTarget, status). Zustandsübergänge dokumentiert. Beispiel-Workspace in data.js implementiert. |
| **Lücken** | Keine |

---

## Gate A5 — Module Slot Contract

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `docs/MODULE_CONTRACT.md`, `modules/*/module.json` (3 Module) |
| **Beweis** | Contract-Spec mit 8 Pflichtfeldern. Alle 3 Module (example-module, prompt-pack-builder, token-manager) haben konforme `module.json`. Erlaubte Permissions dokumentiert. |
| **Lücken** | `token-manager/module.json` nutzt `storage.local` — nicht in erlaubter Permission-Liste. → Drift. |

---

## Gate A6 — The Forge — Module Launchpad

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `app/app.js` (renderForge, openForge, closeForge), `app/data.js` (MODULE_REGISTRY) |
| **Beweis** | Forge-Overlay zeigt alle registrierten Module mit Status, Purpose, Permissions. Öffnet per Button, schließt per Escape/Overlay-Click. |
| **Lücken** | Keine |

---

## Gate A7 — Artifact Output Layer

| Feld | Wert |
|------|------|
| **Status** | `partial` |
| **Dateien** | `docs/ARTIFACT_OUTPUT_LAYER.md`, `app/data.js` (OUTPUT_TYPES), `outputs/registry.json`, `outputs/agent-pack-example.json` |
| **Beweis** | Spec vollständig: Output-Manifest-Schema, Naming, Registry-Konzept. OUTPUT_TYPES im Code definiert. |
| **Lücken** | `outputs/registry.json` ist leer (`{"outputs":[], "version":1}`). Kein automatisierter Artifact-Pipeline-Code. Kein Manifest-Generator. `outputs/agent-pack-example.json` ist manuell erstellt, nicht vom System erzeugt. Es gibt keine Funktion, die tatsächlich ein Manifest erzeugt und in registry.json einträgt. |
| **Fehlende Bestandteile** | Manifest-Generator-Funktion, Registry-Update-Logik, Output-Verzeichnisstruktur (`{output_id}/manifest.json`) |

---

## Gate A8 — Blueprint Export: Agent-Ready Prompt Packs

| Feld | Wert |
|------|------|
| **Status** | `partial` |
| **Dateien** | `docs/AGENT_HANDOFF_FORMAT.md`, `modules/prompt-pack-builder/src/index.js`, `outputs/agent-pack-example.md` |
| **Beweis** | Format-Spec vollständig. `buildPromptPack()` erzeugt Markdown + JSON-Manifest. UI-Button in app.js vorhanden. |
| **Lücken** | Export geht nicht in die Artifact Output Layer (A7). Kein automatischer Registry-Eintrag. `outputs/agent-pack-example.md` ist statisch, nicht vom Builder erzeugt. Kein Download/Export-Flow im UI. |

---

## Gate A9 — Tasker/App-Factory Blueprint Track

| Feld | Wert |
|------|------|
| **Status** | `docs-only` |
| **Dateien** | `docs/ANDROID_BLUEPRINT_TRACK.md`, `app/data.js` (android-blueprint Modul in MODULE_REGISTRY) |
| **Beweis** | Spec vorhanden. Modul-Slot-Definition in MODULE_REGISTRY. |
| **Lücken** | Kein Verzeichnis `modules/android-blueprint/`. Kein `module.json`. Kein Scaffolding-Code. Kein Tasker-Export. Kein Gradle-Generator. Das Modul existiert nur als Eintrag in `data.js`, nicht als implementiertes Modul. |
| **Einordnung** | War als Konzept-Gate gedacht (Machbarkeits-Analyse), aber die DoD "Build Target aktiv" ist nicht erfüllt. |

---

## Gate A10 — Local Preview / Run Surface

| Feld | Wert |
|------|------|
| **Status** | `prototype` |
| **Dateien** | `app/app.js` (showPreview, showPreviewError, getRecoveryHint), `app/index.html` (Preview-Zone) |
| **Beweis** | Preview-Zone im UI. Kann Markdown, JSON, HTML, Text anzeigen. Error-Recovery mit Hints. Status-Badge für Preview-Zustand. |
| **Lücken** | Kein echter "Run" — keine Modul-Execution. Preview zeigt nur statische Inhalte. Keine Build-Pipeline-Integration. Kein Workspace-Run-Button. |
| **Einordnung** | Preview-Anzeige funktioniert, aber "Run Surface" impliziert Execution-Fähigkeit, die nicht existiert. |

---

## Gate A11 — Governance Files

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `AGENTS.md`, `CLAUDE.md`, `principles/` (6 Dateien), `docs/KNOWN_DRIFT_RISKS.md`, `knownbugs-global/` |
| **Beweis** | Agenten-Rollen definiert. Principles dokumentiert (anti-dashboard, frictionless-design, state-surface-design, ci-first, shared-code-policy, broad-implementation-request-safety). Known Bugs und Drift Risks vorhanden. |
| **Lücken** | Keine |

---

## Gate A12 — First Real Module: Prompt Pack Builder

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `modules/prompt-pack-builder/module.json`, `modules/prompt-pack-builder/src/index.js`, `modules/prompt-pack-builder/README.md` |
| **Beweis** | `buildPromptPack()` erzeugt strukturiertes Markdown + JSON-Manifest. Pflichtfeld-Validierung. UI-Button "Build Prompt Pack" in app.js. module.json konform. |
| **Lücken** | Keine für diese Gate. (Integration mit A7 Artifact Layer fehlt, aber das war nicht A12-Scope.) |

---

## Gate A13 — Token Management

| Feld | Wert |
|------|------|
| **Status** | `prototype` ⚠️ |
| **Dateien** | `modules/token-manager/module.json`, `modules/token-manager/src/index.js`, `docs/TOKEN_MANAGEMENT.md` |
| **Beweis** | `TokenManager` IIFE mit create(), list(), remove(), getKey(), rotate(), getForProvider(). UI: Token-Liste, Erstellen, Löschen. Provider-Select. |
| **Lücken** | **Kritisch:** `_key` wird im Klartext in localStorage gespeichert. Kommentar sagt "stored encrypted in real impl", aber es gibt keine Encryption. key_preview funktioniert, aber der echte Key ist trivial auslesbar. |
| **Empfehlung** | Als `prototype` markieren. Für Produktion: Web Crypto API oder IndexedDB mit Encryption. |

---

## Gate A14 — Provider Registry + Multi-Provider

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `app/data.js` (PROVIDER_REGISTRY), `docs/PROVIDER_REGISTRY.md` |
| **Beweis** | 11 Provider definiert (nvidia, huggingface, openai, anthropic, google, groq, mistral, together, omniroute, localai, ollama). `addCustomProvider()`, `loadCustomProviders()`. `renderProviders()` UI. Status-System (needs-token, offline, etc.). |
| **Lücken** | Kein tatsächlicher API-Call-Code. Provider sind konfiguriert, aber es gibt keine `callProvider()` oder `chat()` Funktion. Rein deklarativ. |
| **Einordnung** | Gate-Ziel war "Registry + Multi-Provider", nicht "API Integration". Done für Registry-Scope. |

---

## Gate A15 — Nvidia Build Models

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `app/data.js` (NVIDIA_MODELS), `docs/NVIDIA_BUILD_MODELS.md` |
| **Beweis** | 34 Modelle in NVIDIA_MODELS[] (Chat, Code, Vision, Embedding, Reranking, Image Gen, Speech). Filter-UI mit Typ-Buttons. In PROVIDER_REGISTRY.nvidia.models verdrahtet. |
| **Lücken** | Keine für diese Gate. (Kein API-Call, aber das war nicht Scope.) |

---

## Gate A16 — HuggingFace Launcher Surface

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `app/data.js` (HF_TOP_MODELS, HF_TASK_ICONS, getLocalRunCommands), `docs/HUGGINGFACE_LAUNCHER.md` |
| **Beweis** | 18 Top-Modelle. Such-UI, Filter per Task, Detail-View mit Model Card. Local-Run-Commands (Ollama, llama.cpp). Task-Icons. In PROVIDER_REGISTRY.huggingface verdrahtet. |
| **Lücken** | Keine für diese Gate. |

---

## Gate A17 — Platform Abstraction Layer

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `app/platform.js` (114 LOC), `docs/PLATFORM_ABSTRACTION.md` |
| **Beweis** | `AnvilPlatform` mit detect() (6 Plattformen), toNative()/toPosix() Pfad-Konvertierung, Feature Flags (canUsePake, canUseLocalAI, canUseFileSystem, isDesktop, isMobile), Storage-Abstraktion (saveLocal/loadLocal/removeLocal), info() für UI-Indicator. |
| **Lücken** | Keine |

---

## Gate A18 — Pake Desktop Shell

| Feld | Wert |
|------|------|
| **Status** | `docs-only` ⚠️ |
| **Dateien** | `pake.config.json`, `docs/PAKE_DESKTOP_SHELL.md` |
| **Beweis** | Config-Datei vorhanden. Docs beschreiben Build-Befehle für Windows/macOS/Linux. |
| **Lücken** | **Kein tatsächlicher Build.** Kein `assets/`-Ordner mit Icons. Keine CI/CD Pipeline. Pake-CLI nicht integriert. Config referenziert `assets/anvil-icon` — Datei existiert nicht. |
| **Pake-Name** | ✅ Behoben — war `"Anvil Bellows"`, korrigiert zu `"Anvil"` in `pake.config.json` und `docs/PAKE_DESKTOP_SHELL.md` (2026-05-10, Drift-Risk 8). |

---

## Gate A19 — Workspace Sync Protocol

| Feld | Wert |
|------|------|
| **Status** | `done` |
| **Dateien** | `app/sync.js` (190 LOC), `docs/WORKSPACE_SYNC.md` |
| **Beweis** | `AnvilSync` mit exportBundle(), importBundle(), downloadBundle(), getDeviceId(), loadSyncManifest(). Token-Keys werden NICHT exportiert (nur Metadaten). UI-Integration: Export-Button, Import via File-Picker, Sync-History. Merge-Strategie implementiert. |
| **Lücken** | Kein tatsächlicher Cross-Device-Test möglich (localStorage-basiert). Git-Based Sync nur als Anleitung, nicht automatisiert. |

---

## Gate A20 — OmniRoute Gateway Bridge

| Feld | Wert |
|------|------|
| **Status** | `docs-only` ⚠️ |
| **Dateien** | `docs/OMNIROUTE_BRIDGE.md`, `app/data.js` (omniroute-Eintrag in PROVIDER_REGISTRY) |
| **Beweis** | Docs beschreiben Konzept umfassend. Provider-Eintrag mit apiBase `http://localhost:8090/v1`. |
| **Lücken** | **Kein Bridge-Code.** Kein Routing, kein Fallback, kein Health-Check. Nur ein statischer Eintrag in der Provider Registry mit Status "offline". Kein Code, der tatsächlich OmniRoute anspricht. |
| **Einordnung** | Referenz-Gate. OmniRoute ist ein externes Tool — Anvil dokumentiert die Integration, implementiert sie aber nicht. |

---

## Zusammenfassung

```
done:       A1, A2, A3, A4, A5, A6, A11, A12, A14, A15, A16, A17, A19  (13/20)
prototype:  A10, A13                                                      (2/20)
partial:    A7, A8                                                        (2/20)
docs-only:  A9, A18, A20                                                  (3/20)
blocked:    —                                                             (0/20)
superseded: —                                                             (0/20)
```

### Kritische Befunde

1. **Pake-Name:** ✅ Behoben — korrigiert zu `"Anvil"` (2026-05-10).
2. **Token Manager:** `_key` im Klartext. Muss als Prototype markiert sein, nicht als "done".
3. **Execution Core:** Existiert nicht. Kein Code, der Module tatsächlich ausführt, Outputs erzeugt oder eine Build-Pipeline anstößt. Das ist die größte Lücke zwischen Behauptung und Realität.
4. **"A1–A20 done":** Die pauschale Behauptung in Root-GATES.md ist falsch. 5 von 20 Gates sind nicht `done`.
