# ⚒ Anvil — Werkbank

**KI-native agentische IDE für Android & Windows**

Anvil ist eine minimalistische Werkbank, die Workspaces organisiert,
Module anbindet und Artifacts erzeugt. Anvil ist *kein* Dashboard,
kein Store, kein Framework.

## Was Anvil ist

- Eine **Werkbank** — Workspaces mit Modulen zusammenstecken
- Ein **Launchpad** (The Forge) — Module finden und aktivieren
- Ein **Output-System** — Alles auffindbar, alles mit ID

## Was Anvil nicht ist

- Kein App Store
- Kein Dashboard
- Kein Framework / keine Library
- Kein CMS
- Keine IDE mit eigenem Editor

## Architektur

```
Workspace → Module → Artifacts → Export
   ↕           ↕          ↕
  Forge    Contract    Output Layer
```

## Gates

Entwicklung folgt dem Gate-System:
- Jede Gate hat klare Aufgaben und Definition of Done
- Gates sind sequenziell
- Siehe `docs/GATES.md` für den vollständigen Überblick

## Docs

| Datei | Inhalt |
|-------|--------|
| `docs/ANVIL_CONCEPT_CONTRACT.md` | Kanonische Begriffe |
| `docs/MODULE_CONTRACT.md` | Modul-Vertrag |
| `docs/WORKSPACE_MODEL.md` | Workspace-Datenstruktur |
| `docs/ARTIFACT_OUTPUT_LAYER.md` | Output-System |
| `docs/AGENT_HANDOFF_FORMAT.md` | Agent Prompt Packs |
| `docs/ANDROID_BLUEPRINT_TRACK.md` | Android-Blueprints |
| `docs/GATES.md` | Gate-Übersicht |
| `docs/KNOWN_DRIFT_RISKS.md` | Bekannte Risiken |

## Agenten

Anvil wird von KI-Agenten mitentwickelt. Siehe:
- `AGENTS.md` — Wer darf was
- `CLAUDE.md` — Spezifische Regeln für Claude/Codex

## Lizenz

Proprietär. Alle Rechte vorbehalten.
