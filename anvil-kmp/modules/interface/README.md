# ANVIL Interface Compiler

**Gate:** B14 — Interface Compiler MVP
**Status:** Contract-first MVP

## Zweck

Der Interface Compiler besitzt Bediengrammatik: semantische Input Actions,
Prompts, HUD-State, Menüs, Tutorial-Flows und Accessibility-Profile. UI reagiert
nicht direkt auf Tastencodes; UI erzeugt semantische Actions, und Gameplay Runtime
entscheidet deren Bedeutung im Kontext.

## Operationen

| Operation | Input | Output |
|-----------|-------|--------|
| `COMPILE` | `anvil.interface.intent/v1` | `anvil.interface.bundle/v1` |
| `VALIDATE` | `anvil.interface.bundle/v1` | `anvil.interface.validation-report/v1` |

## Wahrheitsgrenze

- Interface Compiler definiert Bedienung und Feedback.
- Gameplay Compiler entscheidet, was `INTERACT` im aktuellen Kontext bedeutet.
- CUE beweist später Bedienbarkeit; Interface Compiler meldet nur Contract-Validität.
