# ANVIL ↔ TRIVIUM Integration Contract

## Role

ANVIL is the production orchestrator. TRIVIUM provides engine-neutral meaning, realization plans and transformation contracts. ANVIL schedules the work and keeps the pipeline coherent.

ANVIL must never make a target engine the canonical source of truth. Unity, Unreal, Godot, Ren'Py, browser, audio-only and other runtimes are output grammars or local scene runners.

## Production flow

```text
mini-me / author intent
→ WIZARD production brief and source candidates
→ TRIVIUM contracts and route planning
→ SWIFT / external tools transform material
→ engine adapters realize scenes
→ SHADED projects world state
→ CUE verifies obligations
→ MYTHIC provisions, builds and deploys
```

## Job graph

ANVIL should execute a DAG of typed jobs rather than a fixed linear script.

```yaml
job: realize_clockwork_mansion
requires:
  - world_contract
  - selected_sources
steps:
  - inspect_sources
  - choose_routes
  - run_transformations
  - assemble_target
  - verify_contract
outputs:
  - runnable_scene
  - translation_report
  - evidence_bundle
```

A failed route may fall back to another registered route. Example: direct prefab transfer fails → extract mesh/material/animations → reconstruct target scene → verify.

## Engine federation

A small game may contain scenes realized by different engines. ANVIL owns the handoff contract and shared world state, not simultaneous shared-memory simulation.

Scene boundaries should exchange semantic state:

- inventory and abilities
- relationships and memories
- world flags and consequences
- entry/exit anchors
- permitted outputs

Avoid handoffs that require exact native physics, particles or live object references. Prefer doors, portals, elevators, blackouts, dreams, chapter boundaries and other semantic cuts.

## Engine hopping as mechanic

ANVIL may intentionally expose engine/runtimes as different world grammars. The invariant is one continuous world contract, not one continuous process.

## Boundaries

ANVIL does not:

- redefine TRIVIUM meaning
- implement every converter
- declare success without CUE evidence
- force a source into one engine when a local runtime module is cheaper and faithful

## Canonical references

- Architecture: https://github.com/Lootziffer666/TRIVIUM/blob/docs/semantic-realization-direction/docs/architecture-v1.1.md
- Realization contracts: https://github.com/Lootziffer666/TRIVIUM/blob/docs/semantic-realization-direction/docs/realization-contracts.md
- Tool candidates: https://github.com/Lootziffer666/TRIVIUM/blob/docs/semantic-realization-direction/docs/tool-candidate-catalog.md
