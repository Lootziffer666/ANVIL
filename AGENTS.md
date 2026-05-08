# AGENTS.md — Agenten-Regeln für Anvil

## Wer darf was

| Agent | Darf | Darf nicht |
|-------|------|-----------|
| Viktor AI | Gates implementieren, PRs erstellen, Docs schreiben | Eigene Gates definieren, Module ohne Contract |
| Claude / Codex | Code-Reviews, Refactoring, Bugfixes | Architektur-Entscheidungen, Gate-Reihenfolge ändern |
| Mensch | Alles | — |

## Regeln für alle Agenten

1. **Kein Commit ohne Gate-Referenz** — Jeder Commit nennt seine Gate
2. **Kein Modul ohne Contract** — MODULE_CONTRACT.md einhalten
3. **Kein Output ohne Manifest** — ARTIFACT_OUTPUT_LAYER.md einhalten
4. **Definition of Done prüfen** — Vor dem PR: alle DoD-Punkte durch
5. **Kill-Kriterien beachten** — Bei Scope Creep: sofort stoppen
6. **Tests oder Proof** — Jede Gate hat mindestens eine Verifizierung

## Bei Drift

Wenn ein Agent unsicher ist:
1. docs/KNOWN_DRIFT_RISKS.md lesen
2. Im Zweifel: Gate pausieren, nicht weitermachen
3. Mensch fragen
