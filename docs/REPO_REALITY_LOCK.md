# Repo Reality Lock

**Gate:** AX — Repo Reality Lock & Gate Reconciliation  
**Stand:** 2026-05-09  
**Agent:** Viktor (getviktor.com)  
**HEAD:** `994decd` (main)  
**Methode:** Vollständiger Abgleich Repo-Zustand vs. behauptete Gates

---

## 1. Repo-Fakten

| Prüfpunkt | Ergebnis |
|------------|----------|
| Repo | `github.com/Lootziffer666/ANVIL` |
| Default-Branch | `main` |
| HEAD-Commit | `994decd` |
| Branches (remote) | 1 (nur `main`, alle Gate-Branches gemerged) |
| Merged PRs | 11 (davon 4 Gate-PRs: A1–A6, A7–A12, A13–A16, A17–A20) |
| Offene PRs | 0 |
| Open Issues | 0 |
| CI/CD | ❌ Keine GitHub Actions konfiguriert |
| package.json | ❌ Nicht vorhanden |
| Tests | ❌ Keine Tests |
| Build-System | ❌ Keines (plain HTML/CSS/JS) |

---

## 2. Tatsächliche App-Struktur

### App-Kern (Browser-basiert, ~2.390 LOC)

```
app/
├── index.html     (369 LOC)  — Werkbank-UI mit State Surface Zones
├── style.css      (661 LOC)  — Dark Theme, State-Farben, Forge-Overlay
├── app.js         (587 LOC)  — Werkbank-Logik, Forge, Preview, Token/Provider UI
├── data.js        (469 LOC)  — Workspace Model, Module Registry, Provider Registry, Nvidia/HF Models
├── platform.js    (114 LOC)  — Platform Abstraction Layer (Gate A17)
└── sync.js        (190 LOC)  — Workspace Sync Protocol (Gate A19)
```

**Laufzeitumgebung:** Reiner Browser (kein Build, kein Bundler, kein Node).  
**Datenhaltung:** Alles in `localStorage`.  
**Netzwerk:** Keine API-Calls implementiert.

### Module (3 Stück)

```
modules/
├── example-module/         — Dummy (Echo-Input)
├── prompt-pack-builder/    — Prompt Pack Generator (funktional)
└── token-manager/          — Token CRUD (Prototype, Klartext-Keys)
```

### Docs (17 Dateien)

```
docs/
├── AGENT_HANDOFF_FORMAT.md
├── ANDROID_BLUEPRINT_TRACK.md
├── ANVIL_CONCEPT_CONTRACT.md
├── ARTIFACT_OUTPUT_LAYER.md
├── GATE_RECONCILIATION.md        ← NEU (Gate AX)
├── GATES.md
├── HUGGINGFACE_LAUNCHER.md
├── KNOWN_DRIFT_RISKS.md
├── LOCALAI_INTEGRATION.md
├── MODULE_CONTRACT.md
├── NVIDIA_BUILD_MODELS.md
├── OMNIROUTE_BRIDGE.md
├── PAKE_DESKTOP_SHELL.md
├── PLATFORM_ABSTRACTION.md
├── PROVIDER_REGISTRY.md
├── REPO_REALITY_LOCK.md          ← NEU (Gate AX)
├── TOKEN_MANAGEMENT.md
├── WORKSPACE_MODEL.md
└── WORKSPACE_SYNC.md
```

### Templates (umfangreich, 3 Varianten)

```
templates/
├── anvil-app/          — Python/YAML Anvil-Template (Flask-artig)
├── core-gated/         — Minimales Gate-Template
├── gated-project/      — Erweitertes Gate-Template mit Config
└── modules/            — Modul-Templates (4 Stück)
```

### Sonstige

```
principles/     — 6 Design-Principles
projects/       — 6 Projekt-Referenzen
sources/        — Template-Extraction-Pläne
knownbugs-global/ — Bug-Tracking
outputs/        — Fast leer (1 statisches Beispiel)
examples/       — 1 Workspace-Sample
```

---

## 3. Was funktioniert (tatsächlich)

| Feature | Status | Nachweis |
|---------|--------|----------|
| Werkbank-UI öffnen | ✅ | `app/index.html` im Browser |
| Workspace laden (Demo) | ✅ | Button → EXAMPLE_WORKSPACE |
| Module-Liste anzeigen | ✅ | MODULE_REGISTRY → Forge |
| The Forge (Overlay) | ✅ | Modul-Katalog mit Status |
| Preview (Text/JSON/HTML) | ✅ | showPreview() |
| Token erstellen/löschen | ✅ | TokenManager CRUD |
| Provider-Liste anzeigen | ✅ | PROVIDER_REGISTRY → UI |
| Nvidia-Modelle filtern | ✅ | NVIDIA_MODELS + Filter-Buttons |
| HuggingFace-Modelle suchen | ✅ | HF_TOP_MODELS + Search |
| Platform erkennen | ✅ | AnvilPlatform.detect() |
| Sync-Bundle exportieren | ✅ | AnvilSync.downloadBundle() |
| Sync-Bundle importieren | ✅ | AnvilSync.importBundle() |
| Prompt Pack erzeugen | ✅ | buildPromptPack() |

## 4. Was NICHT funktioniert / existiert

| Feature | Status | Details |
|---------|--------|---------|
| Module ausführen | ❌ | Kein Execution Core |
| API-Calls an Provider | ❌ | Kein fetch/XHR Code |
| Artifact-Pipeline | ❌ | Kein Auto-Manifest, leere Registry |
| Pake-Build | ❌ | Kein Build, keine Icons |
| Android-APK-Build | ❌ | Nur Docs |
| OmniRoute-Bridge | ❌ | Nur Provider-Eintrag, kein Code |
| Token-Encryption | ❌ | Klartext in localStorage |
| Tests | ❌ | Keine |
| CI/CD | ❌ | Keine |
| Workspace persistieren | ❌ | Nur Demo-Workspace, kein Save |

---

## 5. Pake-Name-Korrektur

**Problem:** `pake.config.json` und `docs/PAKE_DESKTOP_SHELL.md` verwenden `"Anvil Bellows"`.

**Laut ANVIL_CONCEPT_CONTRACT.md (Gate A2):**
> Anvil-Bellows ist ein eigenständiges Projekt unter dem IIG-Dach.
> Der Name „Bellows" referenziert die Schmiede-Metapher, gehört aber zum IIG-Ökosystem, nicht zu Anvil-IDE.

**Korrektur:** Alle Referenzen müssen von `"Anvil Bellows"` zu `"Anvil"` geändert werden:
- `pake.config.json` → `"name": "Anvil"`
- `docs/PAKE_DESKTOP_SHELL.md` → `--name "Anvil"` (3 Stellen)

---

## 6. Token Manager — Prototype-Markierung

**Problem:** Token Manager wird als `done` (Gate A13 ✅) geführt.

**Realität:**
```javascript
// modules/token-manager/src/index.js, Zeile 30:
_key: key,  // stored encrypted in real impl
```

Der Key wird im Klartext gespeichert. Der Kommentar verspricht Encryption, die nicht existiert.

**Empfehlung:** Gate A13 als `prototype` markieren. Für Produktion mindestens:
- Web Crypto API (`crypto.subtle.encrypt`)
- Oder IndexedDB statt localStorage
- Key-Derivation aus User-Passphrase

---

## 7. Execution Core — Nicht vorhanden

**Problem:** Kein Code führt Module tatsächlich aus.

**Was fehlt:**
1. Eine `executeModule(moduleId, input)` Funktion
2. Eine Pipeline: Module → Artifact → Output Layer → Registry
3. Ein "Run"-Button im UI, der eine Workspace-Execution startet
4. Output-Manifest-Generator
5. Registry-Update nach jedem Run

**Einordnung:** Das ist die größte Lücke. Anvil hat eine Werkbank-UI, Modul-Definitionen und Docs — aber keine Execution. Die Werkbank kann anzeigen, aber nicht schmieden.
