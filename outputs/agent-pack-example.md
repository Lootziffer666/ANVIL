# Agent Prompt Pack: Anvil — Gate A13+

## Ziel
Nächste 6 Gates für Anvil implementieren (A13–A18).

## Ist-Zustand
- Repo: `github.com/Lootziffer666/ANVIL`
- Branch: `gates-a7-a12`
- Gates A1–A12 abgeschlossen
- Shell läuft, Module dockbar, Outputs strukturiert

## Nächste Gates
- A13: Provider-System (Token-Management, Multi-Provider)
- A14: Nvidia Build Modelle Integration
- A15: HuggingFace Launcher Surface
- A16: Settings & Preferences Module
- A17: Module Discovery + Installation
- A18: Offline-First Sync

## Harte Constraints
- Keine Dashboard-Orgie — State Surface Design
- Kein App Store — The Forge bleibt Werkzeugleiste
- Module Contract (MODULE_CONTRACT.md) einhalten
- Kein API-Key im Code
- Gates sequentiell, nicht parallel

## Definition of Done
- Jede Gate hat eigenen Commit
- Jeder Commit hat Tests oder Proof
- Docs aktualisiert
- PR erstellt

## Kill-Kriterien
- Scope Creep → sofort stoppen, Gate nachschärfen
- Mehr als 3 Files ohne Test → stoppen
- GUI-Komplexität ohne Funktion → stoppen

## Agent-Zielsystem
Viktor AI (@viktor)

## Kontext-Dateien
- `docs/ANVIL_CONCEPT_CONTRACT.md`
- `docs/MODULE_CONTRACT.md`
- `docs/WORKSPACE_MODEL.md`
- `docs/ARTIFACT_OUTPUT_LAYER.md`
- `docs/AGENT_HANDOFF_FORMAT.md`
