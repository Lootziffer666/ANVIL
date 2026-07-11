# ANVIL Acoustic Runtime

**Gate:** B15 — Acoustic Runtime Slice
**Status:** Contract-first MVP

## Zweck

Die Acoustic Runtime besitzt hörbaren Weltzustand: Audio-Intent, Cue-Graphen,
State Inputs, Mix Snapshots, Voice Lines und Audio-Proof-Anforderungen. Sie setzt
keine Gameplay-Wahrheit selbst, sondern reagiert auf WorldEvents, Gameplay-State und
SHADED-/Scene-Zustände.

## Operationen

| Operation | Input | Output |
|-----------|-------|--------|
| `COMPILE` | `anvil.audio.intent/v1` | `anvil.audio-cue-graph/v1` |
| `VALIDATE` | `anvil.audio-cue-graph/v1` | `anvil.audio.validation-report/v1` |

## Wahrheitsgrenze

- BARD liefert emotionale Funktion.
- Gameplay Runtime liefert Ereignisse und Intensität.
- SHADED liefert sichtbaren Weltzustand.
- Acoustic Runtime macht Zustände hörbar, erfindet sie aber nicht.
- CUE beweist später Timing, Loop, Verständlichkeit und Reaktion.
