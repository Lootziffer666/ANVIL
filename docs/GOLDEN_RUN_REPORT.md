# Golden Run Report

**Status:** Golden Run **PASSED** (fixture-based — see scope notes below).
**Date:** 2026-07-11
**Base commit:** `d1ea0c9b19b0dcfd7550e1450ad88bdeb35a16bd` (branch `claude/anvil-golden-run-zzo9la`)

This report only states what was actually run and observed in this session. See
`docs/FABLE_FIX_LEDGER.md` for the full atom-by-atom trail (problem → smallest change →
command → result → evidence) that produced this run.

## Toolchain

| Component | Version | Note |
|---|---|---|
| JDK | 21.0.10 (Ubuntu 21.0.10+7) | pre-installed in this sandbox |
| Gradle | 8.14.3 (system, `/opt/gradle/bin/gradle`) | `./gradlew` (project wrapper pins 8.9) could not download its distribution in this sandbox — `services.gradle.org` returned HTTP 403 through the environment's proxy, while `mavenCentral()`/`google()` resolve fine. All commands below therefore ran on the system Gradle, not the wrapper. Both use JDK 21 and the same `libs.versions.toml` pins (Kotlin 2.0.21, AGP 8.7.3, Compose 1.7.3), so results should be reproducible with `./gradlew` in an environment where the wrapper can download. |
| Kotlin | 2.0.21 | from `libs.versions.toml`, unchanged |
| kotlinx.serialization | 1.7.3 | unchanged |
| Ktor | 3.0.3 | unchanged |

## Commands run (in order, final state)

```bash
/opt/gradle/bin/gradle projects --console=plain
/opt/gradle/bin/gradle :core:contracts:allTests --console=plain
/opt/gradle/bin/gradle :core:run:allTests --console=plain
/opt/gradle/bin/gradle :core:artifacts:allTests --console=plain
/opt/gradle/bin/gradle :modules:acoustic:allTests --console=plain
/opt/gradle/bin/gradle :modules:target:allTests --console=plain
/opt/gradle/bin/gradle :surfaces:golden-run:allTests --console=plain
/opt/gradle/bin/gradle \
  :core:contracts:allTests :core:artifacts:allTests :core:run:allTests \
  :core:handoff:allTests :core:sync:allTests :modules:gameplay:allTests \
  :modules:scene:allTests :modules:interface:allTests :modules:acoustic:allTests \
  :modules:target:allTests :surfaces:golden-run:allTests :modules:bellows:allTests \
  --console=plain
/opt/gradle/bin/gradle :app:bellows-gateway:compileKotlin :app:bellows-gateway:test --console=plain
```

All of the above returned `BUILD SUCCESSFUL`, with zero test failures on the final pass.

## RunPlan executed

`PLAN_GOLDEN_RUN` (workspace `ws-golden-run`, run `run-golden-1`), 6 steps, real
`RunDependencyResolver` ordering via `dependsOn`, real `ContractRegistry`
producer/consumer enforcement via `inputContract`/`outputContract`:

```text
S_GAMEPLAY        (gameplay)          → anvil.gameplay.plan/v1
S_SCENE           (scene)             → anvil.scene-bundle/v1        [dependsOn S_GAMEPLAY]
S_INTERFACE       (interface)         → anvil.interface.bundle/v1    [dependsOn S_SCENE]
S_ACOUSTIC        (acoustic)          → anvil.audio-cue-graph/v1     [dependsOn S_SCENE]
S_AUDIO_ASSET     (acoustic-producer) → anvil.audio-asset-manifest/v1 [dependsOn S_GAMEPLAY]
S_TARGET          (target)            → anvil.runnable-build/v1      [dependsOn S_INTERFACE, S_ACOUSTIC, S_AUDIO_ASSET]
```

Fixture scenario (`GoldenRunFixture`, Gate I-01): two players move a heavy crate
together; the carrier slows down, the opener clears the path, danger rises. 2 gameplay
verbs (`push`, `open`), 2 coop roles (`carrier`, `opener`), a SHADED scene-config ref,
and a state-reactive audio cue (`danger-rise`) — matching the fix prompt's minimal
scenario.

## Modules

| Module | Kind | Role in this run |
|---|---|---|
| BARD | Fixture (`GoldenRunFakeBardPort`) | CreativeBrief |
| WIZARD | Fixture (`GoldenRunFakeWizardPort`) | ProductionAssessment |
| Gameplay Compiler | Real | `GameplayPlan` (2 interactions, world events, proof requirements) |
| Scene Compiler | Real | `SceneBundle` (anchors, spawn points, interaction zones) |
| Interface Compiler | Real | `InterfaceBundle` (INTERACT + MOVE + CANCEL actions) |
| Acoustic Runtime | Real | `AudioCueGraph` (state-reactive cue graph) |
| Acoustic Producer | Real (+ local `AcousticProvider` test double) | `AudioAssetManifest` for an 8s percussion layer |
| Target Adapter | Real | `RunnableBuild` plan for a WEB target |
| SWIFT | Fixture (`GoldenRunFakeSwiftPort`) | ActorBundle ref (referenced, not executed, in this run) |
| SHADED | Fixture (`GoldenRunFakeShadedPort`) | SceneConfig ref (referenced, not executed, in this run) |
| CUE | Fixture (`GoldenRunFakeCuePort`) | 3 separate proofs: playable, temporal, audio |

## Artifacts

```text
run.status      = COMPLETE
run.planId      = PLAN_GOLDEN_RUN
artifact.count  = 6   (one per RunPlan step, all COMPLETED, all sha256:-checksummed)
handoff.id      = ART_HANDOFF_6224801f
sync.id         = ART_SYNC_3e152e44
cue.proofs      = cue.playable-proof, cue.temporal-proof, cue.audio-proof (3 distinct contract ids)
```

Every artifact manifest in the registry carries a real (non-placeholder) `sha256:`
checksum computed by `Sha256`/`ArtifactWriter` from the actual produced payload, and a
(possibly empty, never malformed) `parentRefs` list. One artifact
(`ART_STORE_GOLDEN_GAMEPLAY`) was additionally persisted through
`ArtifactWriter.writeAndPersist` into a real `InMemoryArtifactStore` and read back
byte-for-byte identical, proving Gate D's store integration end to end.

`HandoffExporter.export()` produced a Markdown handoff package referencing 3 of those
artifacts; `WorkspaceSyncService.exportArtifact()` produced a sync bundle containing all
6, with zero warnings (no excluded/missing artifacts).

## Negative case

A second RunPlan step declaring `inputContract = anvil.gameplay.plan/v99` (an
unregistered version) was blocked (`RunStatus.BLOCKED`) **before** the target module was
ever invoked (`executionCount == 0` on a counting wrapper) — proving contract
enforcement happens ahead of execution, not after the fact.

## Bugs the Golden Run itself found and fixed

1. **Contract registry gap:** `anvil.scene-bundle`'s allowed consumers were
   `target, cue, shaded` — but the real `InterfaceIntent` and `AudioIntent` models both
   carry a `sceneBundleRef` field, meaning `interface` and `acoustic` are real consumers
   that the MVP registry never declared. Fixed by extending the existing descriptor
   (not duplicating it).
2. **Serialization discriminator collision:** the test's `Json` instance did not match
   `GameplayCompilerModule`'s internal `classDiscriminator = "kind"` config, and
   kotlinx.serialization's default `"type"` discriminator collides with `Condition`/
   `Effect`'s own `type` property, throwing `IllegalStateException` on the first
   attempt. Fixed in the test only (no production code changed).

## ElevenLabs plan basis (Gate F-06 budget defaults)

Values quoted in the fix prompt at the time of writing, used as configurable defaults
in `AudioBudgetPolicy`, not verified live against an actual ElevenLabs account in this
session (no API key present):

- 30,000 credits/month, commercial license, commercial music use
- Eleven Music ≈ 900 credits/minute
- Sound Effects ≈ 200 credits/generation

Endpoints (`POST /v1/music`, `POST /v1/sound-generation`, header `xi-api-key`) **were**
verified against ElevenLabs' current public API docs via live fetch in this session
(Gate F-04) — these are not guessed.

## Open risks / not verified

- Real SWIFT/SHADED/WIZARD/CUE adapters (Gate E-03): sibling repos are present in this
  environment but implementing and verifying four independent real adapters was judged
  out of scope for a single session; deferred, does not block this Golden Run since the
  run is explicitly fixture-based per the Gate I spec itself.
- ElevenLabs live network path (Gate I-03): no `ELEVENLABS_API_KEY` in this sandbox;
  only the MockEngine-based request/error/budget/redaction behavior is verified.
- `runtime.ts` (Gate G-02) was verified as a string template, not compiled by a real
  TypeScript/Tone.js toolchain.
- Gradle wrapper (`./gradlew`) itself was not exercised end-to-end in this sandbox
  (network-blocked distribution download); all commands above ran on system Gradle
  8.14.3 with the same JDK/Kotlin/AGP versions the wrapper would use.
- Local free-tier acoustic fillers (Basic Pitch/librosa/ACE-Step/Demucs/Freesound, Gate
  F-07) were not attempted — none of those tools are installed in this sandbox, and the
  fix prompt requires a verified installation before writing a `doctor()` gate.

## Forward-looking note (not implemented this session)

The user flagged `d-liya/capybara_2d_engine` as a promising **Web Target Adapter**
candidate for ANVIL — a small, agent-friendly 2.5D web runtime (`src/Game.ts` as the
whole public surface) that could sit between ANVIL's `RunnableBuild` and a real
browser-playable prototype for CUE to test, without becoming ANVIL's main engine or
replacing KorGE/Unreal/SHADED/SWIFT. Concretely proposed: a new
`anvil-target-capybara` adapter consuming the same contracts Target already knows
(`anvil.gameplay.plan`, `anvil.scene-bundle`, `anvil.interface.bundle`,
`anvil.audio-asset-manifest`, `anvil.audio-cue-graph`, `swift.actor-bundle`,
`shaded.scene-config`) and producing `anvil.runnable-build/v1` plus a new
`anvil.capybara.project/v1`. Not started in this session — recorded here for the next
gate to pick up.
