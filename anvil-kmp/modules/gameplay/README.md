# ANVIL Gameplay Compiler

**Gate:** B11 — Gameplay Compiler Contracts
**Status:** Contract-first MVP

## Zweck

Der Gameplay Compiler besitzt Regelwahrheit: Er kompiliert BARDs Spielgrammatik und
WIZARDs Capability Cast in ausführbare Interaktions-, Zustands- und Proof-Verträge.
Er schreibt keine kreative Absicht, keine visuellen Weltzustände und kein
Actor-Wissen.

## Operationen

| Operation | Input | Output |
|-----------|-------|--------|
| `COMPILE` | `anvil.gameplay.compile-request/v1` | `anvil.gameplay.plan/v1` |
| `VALIDATE` | `anvil.gameplay.plan/v1` | `anvil.gameplay.validation-report/v1` |

## Wahrheitsgrenze

- BARD beschreibt Bedeutung.
- WIZARD castet Systeme.
- SHADED zeigt Folgen.
- Actor Runtime besitzt Figurenwissen.
- Gameplay Compiler / Runtime entscheidet, welche Regeln gelten und welche StatePatches entstehen.
