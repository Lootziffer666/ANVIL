# CLAUDE.md — Spezifische Regeln

## Kontext

Anvil ist eine KI-native IDE/Werkbank. Entwicklung folgt Gates.

## Regeln

1. Lies immer zuerst:
   - `docs/ANVIL_CONCEPT_CONTRACT.md`
   - `docs/MODULE_CONTRACT.md`
   - `docs/GATES.md`

2. Jeder Commit referenziert eine Gate (z.B. "Gate A7: ...")

3. Kein Scope Creep — nur das implementieren, was die Gate verlangt

4. State Surface Design:
   - Kein Dashboard
   - Zustand kommt zuerst
   - Recovery statt Fehlermeldung
   - Anti-Dashboard-Prinzip (see `principles/anti-dashboard.md`)

5. Module Contract einhalten — keine Ausnahmen

6. Bei Unsicherheit: stoppen, nicht raten
