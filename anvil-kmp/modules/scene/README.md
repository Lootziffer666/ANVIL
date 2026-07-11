# ANVIL Scene Compiler

**Gate:** B12 — Scene Compiler / 3D-RE-GEN Contracts
**Status:** Contract-first MVP

## Zweck

Der Scene Compiler besitzt Raumwahrheit: Er kompiliert Produktionsabsicht,
Gameplay-Anker und Environment-Referenzen in ein `SceneBundle/v1` mit Entitäten,
Ankern, Navigation, Collision, Spawnpunkten, Kameras und Interaction Zones.

## Operationen

| Operation | Input | Output |
|-----------|-------|--------|
| `COMPILE` | `anvil.scene.intent/v1` | `anvil.scene-bundle/v1` |
| `VALIDATE` | `anvil.scene-bundle/v1` | `anvil.scene.validation-report/v1` |

## Wahrheitsgrenze

- Gameplay Compiler besitzt Regeln und StatePatches.
- SHADED besitzt visuelle Weltkohärenz und Darstellung.
- Actor Runtime besitzt Figurenwissen.
- Scene Compiler besitzt nur räumliche Struktur: Wo ist was, wo darf man laufen, wo beginnt eine Interaktion?
