# Gates — Übersicht

## Gate-System

Gates sind sequenziell, jede hat:
- **Ziel** — Was soll erreicht werden?
- **Aufgaben** — Konkrete Schritte
- **Definition of Done** — Wann ist die Gate erledigt?
- **Kill-Kriterien** — Wann abbrechen?

## Abgeschlossene Gates

| Gate | Name | Status |
|------|------|--------|
| A1 | Repo Access Proof | ✅ |
| A2 | Naming & Concept Contract | ✅ |
| A3 | Shell & Surface | ✅ |
| A4 | Workspace Model | ✅ |
| A5 | Module Slot Contract | ✅ |
| A6 | The Forge — Module Launchpad | ✅ |
| A7 | Artifact Output Layer | ✅ |
| A8 | Blueprint Export: Agent-Ready Prompt Packs | ✅ |
| A9 | Tasker/App-Factory Blueprint Track | ✅ |
| A10 | Local Preview / Run Surface | ✅ |
| A11 | Governance Files | ✅ |
| A12 | First Real Module: Prompt Pack Builder | ✅ |

| A13 | Token Management | ✅ |
| A14 | Provider Registry + Multi-Provider | ✅ |
| A15 | Nvidia Build Models | ✅ |
| A16 | HuggingFace Launcher Surface | ✅ |

## Änderungsprotokoll

Jede neue Gate wird hier eingetragen. Gate-Reihenfolge wird nicht nachträglich geändert.

## Gate A17 — Platform Abstraction Layer
- **Branch:** `gates-a17-a20`
- **Dateien:** `app/platform.js`, `docs/PLATFORM_ABSTRACTION.md`
- **Zweck:** OS-Erkennung, portable Pfade, Feature Flags, Platform Indicator
- **Akzeptanz:** Platform wird korrekt erkannt, Pfade sind intern immer POSIX

## Gate A18 — Pake Desktop Shell
- **Branch:** `gates-a17-a20`
- **Dateien:** `pake.config.json`, `docs/PAKE_DESKTOP_SHELL.md`
- **Zweck:** Anvil als native Windows/Mac/Linux Desktop-App (5 MB statt 200 MB)
- **Quelle:** tw93/Pake, gitbutlerapp/gitbutler (Tauri-Pattern)

## Gate A19 — Workspace Sync Protocol
- **Branch:** `gates-a17-a20`
- **Dateien:** `app/sync.js`, `docs/WORKSPACE_SYNC.md`
- **Zweck:** Cross-Device Sync (Android ↔ Windows), Export/Import Bundles
- **Sicherheit:** Token-Keys werden NIEMALS exportiert

## Gate A20 — OmniRoute Gateway Bridge
- **Branch:** `gates-a17-a20`
- **Dateien:** `docs/OMNIROUTE_BRIDGE.md`
- **Zweck:** Ein Endpoint für 160+ Provider, Smart Fallback, Sync-freundlich
- **Quelle:** diegosouzapw/OmniRoute
