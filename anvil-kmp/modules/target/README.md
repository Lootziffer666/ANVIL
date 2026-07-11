# ANVIL Target Adapter

**Gate:** B13 — Target Adapter MVP
**Status:** Contract-first MVP

## Zweck

Der Target Adapter macht aus ANVIL-Produktionsartefakten ein zielplattformfähiges
Build-Paket. Er besitzt nicht die Inhalte selbst: Gameplay, Scene, SWIFT, SHADED,
Audio und Interface bleiben jeweils bei ihren Wahrheitsbesitzern.

## Operationen

| Operation | Input | Output |
|-----------|-------|--------|
| `PREPARE` | `anvil.production-bundle/v1` | `anvil.runnable-build/v1` |
| `VALIDATE` | `anvil.runnable-build/v1` | `anvil.build-health-report/v1` |

## Wahrheitsgrenze

- Target Adapter übersetzt Contracts in Zielprojekt-Struktur.
- Target Adapter darf keine Regeln, Szene, Assets, Audio oder Creative Locks verändern.
- CUE entscheidet später technische Spielbarkeit; der Adapter meldet nur Build-Health.
