<h1 align="center">⚒ ANVIL — Werkbank</h1>

<p align="center">
  <strong>KI-native agentische IDE für Android & Desktop</strong><br>
  Kotlin Multiplatform · Compose Multiplatform · Gate-getriebene Entwicklung
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Stack-Kotlin%20Multiplatform-7F52FF?style=flat-square&logo=kotlin" />
  <img src="https://img.shields.io/badge/Platform-Android%20|%20JVM%20Desktop-green?style=flat-square" />
  <img src="https://img.shields.io/badge/Phase-KMP%20Bootstrap-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/Typ-Werkbank-blue?style=flat-square" />
</p>

---

## 🔨 Was Anvil ist / nicht ist

| ✅ Ist | ❌ Ist nicht |
|--------|-------------|
| Eine **Werkbank** — Workspaces mit Modulen | Kein App Store |
| Ein **Launchpad** (The Forge) | Kein Dashboard |
| Ein **Output-System** — alles mit ID und Provenienz | Kein Framework / Library |
| Ein **KMP-Monorepo** mit stabilen Contracts | Kein Mono-Lith |

---

## 🏗 Architektur

```
Workspace → Run → Artifact → Export
    ↕           ↕           ↕
  Forge      Contract    Output Layer
```

### Modul-Map

```
anvil-kmp/
├── core/
│   ├── contracts/   :core:contracts   ← ModuleSlotContract, BellowsContract
│   ├── domain/      :core:domain      ← Workspace, Run, Artifact, Snapshot, Memory
│   ├── pipeline/    :core:pipeline    ← RunStep sealed, RunResult sealed
│   └── quality/     :core:quality     ← QualityState, QualityGuard
├── modules/
│   ├── forge/
│   │   └── knight/  :modules:forge:knight  ← Okio, Unified Diff, diff-match-patch
│   └── bellows/     :modules:bellows        ← LLM-Router, Provider-Adapters
├── surfaces/
│   └── commander/   :surfaces:commander    ← Compose Desktop Shell
└── app/
    ├── android/     :app:android            ← Compose Android Entry Point
    └── desktop/     :app:desktop            ← Compose Desktop Entry Point (JVM)
```

### Zustands-Grammatik

| Zustand | Bedeutung | Farb-Intent |
|---------|-----------|-------------|
| `STABLE` | Alles in Ordnung | Grün |
| `DEGRADED` | Eingeschränkt funktionsfähig | Blau |
| `BLOCKED` | Wartet auf Bedingung | Orange |
| `FAILED` | Fehler, Eingriff nötig | Rot |

Gilt für Workspaces, Module, Runs und Artifacts gleichermaßen.

---

## 🎛 Run-Pipeline (MVP)

```
PLAN → PATCH → DIFF → GATE → ARTIFACT
                 ↑
           Mensch entscheidet
```

Drei MVP-Gates: **Plan Gate** (Änderungen sinnvoll?) → **Diff Gate** (Änderungen korrekt?) → **Artifact Gate** (Output exportierbar?).

---

## 📖 Docs

| Datei | Inhalt |
|-------|--------|
| `docs/ANVIL_CONCEPT_CONTRACT.md` | Kanonische Begriffe |
| `docs/MODULE_CONTRACT.md` | Modul-Vertrag |
| `docs/WORKSPACE_MODEL.md` | Workspace-Struktur |
| `docs/ARTIFACT_OUTPUT_LAYER.md` | Output-System |
| `docs/EXECUTION_CORE_ARCHITECTURE.md` | Native Execution Core Skeleton |
| `GATES.md` | Gate-Übersicht (A1–A24, AX, AT1–AT4) |
| `SKILLS.md` | Was KI-Agenten können/dürfen |
| `CLAUDE.md` | Regeln für Claude/Codex |
| `AGENTS.md` | Agenten-Governance |

---

## 🚀 Gates

Siehe [`GATES.md`](GATES.md).  
KMP-spezifische Gates kommen unter Gate-Serie **B** (Bootstrap → Build → Test → MVP).

---

## 📦 Repo-Struktur

```
ANVIL/
├── anvil-kmp/          ← KMP-Monorepo (Gradle, Kotlin — aktive Entwicklung)
├── app/                ← JS/Pake-Prototyp (historisch, nicht aktiv)
├── src/core/           ← Skeleton (Architektur-Docs, kein Code)
├── docs/               ← Architekturdokumentation
├── principles/         ← Designprinzipien
├── templates/          ← Gate-Templates, Projekt-Templates
├── modules/            ← JS-Module (historisch)
└── donor-analysis/     ← Pattern-Analyse externer Repos
```

> **Hinweis:** `app/` und `src/core/` werden nicht weiterentwickelt.  
> Aktive Entwicklung findet ausschließlich in `anvil-kmp/` statt.

---

<p align="center"><em>Werkbank. Nicht Showroom.</em></p>
