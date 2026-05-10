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
| A7 | Artifact Output Layer | `partial` |
| A8 | Blueprint Export: Agent-Ready Prompt Packs | `partial` |
| A9 | Tasker/App-Factory Blueprint Track | `docs-only` |
| A10 | Local Preview / Run Surface | `prototype` |
| A11 | Governance Files | `done` |
| A12 | First Real Module: Prompt Pack Builder | `done` |
| A13 | Token Management | `prototype` |
| A14 | Provider Registry + Multi-Provider | `done` |
| A15 | Nvidia Build Models | `done` |
| A16 | HuggingFace Launcher Surface | `done` |
| A17 | Platform Abstraction Layer | `done` |
| A18 | Pake Desktop Shell | `docs-only` |
| A19 | Workspace Sync Protocol | `done` |
| A20 | OmniRoute Gateway Bridge | `docs-only` |
| AX | Repo Reality Lock | `done` |

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

## Änderungsprotokoll

Jede neue Gate wird hier eingetragen. Gate-Reihenfolge wird nicht nachträglich geändert.

| Datum | Gate | Änderung |
|-------|------|----------|
| 2026-05-08 | A1–A20 | Initial erstellt |
| 2026-05-09 | AX | Reconciliation, Statusklassen eingeführt, Status korrigiert |
