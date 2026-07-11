# ANVIL Studio Roadmap — Wahrheitsbesitzer und fehlende Compiler

**Gate:** B10 candidate — Studio Roadmap / Contract Registry
**Status:** Roadmap verbindlich für nächste Gate-Planung
**Date:** 2026-07-11

## Befund

ANVIL braucht nicht „noch ein halbes Studio“. Die großen bestehenden Rollen sind
besetzt:

| Rolle | Besitzer | Wahrheit |
|-------|----------|----------|
| Bedeutung, Ton, Weltversprechen | BARD (externes privates Repo) | Kreative Absicht |
| Produktionsentscheidung | WIZARD | Fähigkeiten, Assets, Route |
| Orchestrierung | ANVIL | Plan, Task, Run, Artifact |
| Figuren-/Actor-Produktion | SWIFT | Character-/Presentation-Bundles |
| Visuelle Weltkohärenz | SHADED | Sichtbare Weltzustände |
| Beweisführung | CUE-AGENT | Playable-/Temporal-/Visual-Proofs |
| Modellrouting | BELLOWS | Provider, Privacy, Kosten, Fallbacks |
| Kontrollierte Dateioperationen | KNIGHT | Workspace-konforme Writes/Patches |

Die Roadmap-Lücke ist nicht primär „mehr Assets“, sondern **fehlende
Wahrheitsbesitzer** für Regeln, Bühne, Actor-Kognition, Klang, Bedienung, Runtime-
Persistenz, Target-Build und objektive Spielerfahrungsdaten.

## Lückenkarte

| Ebene | Status | Fehlender Besitzer |
|-------|--------|--------------------|
| Bedeutung, Ton, Weltversprechen | BARD entsteht extern | — |
| Assets, Fähigkeiten, Produktionswissen | WIZARD | — |
| Charakterdarstellung und Animation | SWIFT | — |
| Atmosphäre und visuelle Weltzustände | SHADED | — |
| Ablauf und Modulsteuerung | ANVIL | — |
| QA und Beweisführung | CUE-AGENT | — |
| Spielregeln und Interaktionen | offen | Gameplay Compiler |
| Raum und Szenenmontage | offen / 3D-RE-GEN | Scene Compiler |
| NPC-Verhalten und Weltgedächtnis | offen | Actor Runtime |
| Audio und adaptive Musik | offen | Acoustic Layer |
| Input, HUD und Bediengrammatik | offen | Interface Compiler |
| Savegame und Replay | offen | Runtime Memory |
| Engine-Projekt und Build | offen | Target Adapter |
| Spielerfahrung und Balancing | teilweise CUE | Playtest Layer |
| Asset-Fertigung und Normalisierung | teilweise WIZARD/SWIFT | 3D-RE-GEN Asset Lane |
| Schema-Drift-Vermeidung | offen | Contract Registry |

## P0 — erster echter vollständiger Produktionslauf

P0 erzeugt einen vertikalen Slice, der aus menschlicher Intuition ein lauffähiges,
belegbares Mini-Spiel erzeugt. Ziel ist nicht Produktionsumfang, sondern durchgehende
Wahrheitskette.

### 1. BARD abschließen (externes privates Repo)

BARD liefert den gesperrten kreativen Vertrag. Der Code und das private Profilpaket liegen nicht in diesem ANVIL-Repo; ANVIL konsumiert nur die versionierten Contracts und Artifact-Refs:

- `CreativeSeed/v1`
- `CreativeBrief/v1`
- `ProductionIntent/v1`
- Locks und Acceptance Criteria
- Regression/Challenge-Report
- Creative-Fidelity-Audit nach CUE

DoD:

- Drei-Wort-Seed erzeugt drei echte Varianten.
- Ein Kandidat wird zu `locked CreativeBrief/v1`.
- `ProductionIntent/v1` ist ohne Prosainterpretation durch WIZARD konsumierbar.
- Keine privaten Profilregeln verlassen BARD.
- Kein BARD-Code oder `bard-profile` liegt im ANVIL-Repo.

### 2. Gameplay Compiler

Der Gameplay Compiler besitzt Regelwahrheit: was gilt, was geprüft wird, was Zustand
verändert und welche neuen Handlungen möglich werden.

Kernverträge:

- `GameplayPlan/v1`
- `InteractionDefinition/v1`
- `Condition/v1`
- `Effect/v1`
- `WorldEvent/v1`
- `StatePatch/v1`
- `GameplayProofRequirements/v1`

Minimaler Slice:

```json
{
  "schema": "anvil.gameplay.interaction/v1",
  "id": "INT_PICKUP_CRATE",
  "actorCapabilities": ["can-carry"],
  "targetTags": ["portable", "heavy"],
  "conditions": [
    { "type": "distance", "max": 1.5 },
    { "type": "handsFree", "value": true }
  ],
  "effects": [
    { "type": "inventory.attach", "slot": "carried" },
    { "type": "actor.movementModifier", "multiplier": 0.65 },
    { "type": "emit", "event": "HEAVY_OBJECT_LIFTED" }
  ]
}
```

Grenze:

- BARD beschreibt Bedeutung.
- WIZARD castet Systeme.
- SHADED zeigt Folgen.
- **Nur Gameplay Compiler / Runtime entscheidet, was regeltechnisch passiert.**

### 3. Scene Compiler / 3D-RE-GEN Vertrag

3D-RE-GEN wird nicht nur „3D erzeugen“, sondern Raum aus Produktionsabsicht
kompilieren.

Kernverträge:

- `SceneIntent/v1`
- `SceneGraph/v1`
- `SpatialAnchor/v1`
- `NavigationGraph/v1`
- `CollisionMap/v1`
- `InteractionZone/v1`
- `CameraPlan/v1`
- `SceneBundle/v1`

Minimaler `SceneBundle/v1`:

```json
{
  "schema": "anvil.scene-bundle/v1",
  "sceneId": "SCN_MARKET_001",
  "environmentRefs": [],
  "entities": [],
  "anchors": [],
  "collision": {},
  "navigation": {},
  "spawnPoints": [],
  "cameras": [],
  "interactionZones": [],
  "shadedConfigRef": "ART_SHADED_...",
  "audioZoneRefs": []
}
```

DoD:

- Mindestens ein begehbarer Ort mit Boden, Kollisionsgrenzen, Spawnpunkt,
  Interaktionszone und Kamera.
- SHADED-Konfiguration referenziert Szene, erfindet aber keine Raumwahrheit.

### 4. Target Adapter MVP

ANVIL orchestriert; ein Target Adapter macht daraus ein echtes Engine-Projekt.

Kernvertrag:

- `TargetAdapterContract/v1`
- `ProductionBundle/v1`
- `RunnableBuild/v1`
- `BuildHealthReport/v1`

Minimaler Adapter:

```json
{
  "target": "unreal-5.8",
  "accepts": [
    "anvil.scene-bundle/v1",
    "anvil.gameplay-plan/v1",
    "swift.actor-bundle/v1",
    "anvil.audio-cue-graph/v1"
  ],
  "produces": ["anvil.runnable-build/v1"]
}
```

DoD:

- Importiert SceneBundle, GameplayPlan und ActorBundle.
- Erzeugt startbares Projekt oder klaren `BLOCKED`-Report mit fehlenden Contracts.
- Kein Engine-spezifischer Export verändert BARD-Locks oder Gameplay-Regeln.

### 5. Interface Compiler MVP

UI reagiert auf semantische Actions, nicht direkt auf Tastencodes.

Kernverträge:

- `InputActionMap/v1`
- `InteractionPrompt/v1`
- `HUDState/v1`
- `MenuGraph/v1`
- `TutorialFlow/v1`
- `AccessibilityProfile/v1`

Regel:

```text
KEY_E / ButtonSouth / Tap
              ↓
          INTERACT
              ↓
Gameplay Runtime entscheidet, was INTERACT hier bedeutet.
```

DoD:

- `INTERACT`, `MOVE`, `LOOK`, `CANCEL` als semantische Actions.
- Gameplay Runtime, nicht UI, bestimmt konkrete Wirkung.
- CUE kann beweisen, dass Prompt sichtbar ist und Action auslösbar bleibt.

### 6. Acoustic Runtime Mini-Slice

Akustik ist nicht nur Musikgenerierung, sondern hörbarer Weltzustand.

Kernverträge:

- `AudioIntent/v1`
- `AudioAssetManifest/v1`
- `MusicTheme/v1`
- `StemSet/v1`
- `AudioCue/v1`
- `AudioCueGraph/v1`
- `AudioState/v1`
- `MixSnapshot/v1`
- `VoiceLine/v1`
- `AudioProof/v1`

Minimaler Slice:

```json
{
  "schema": "anvil.audio-cue-graph/v1",
  "stateInputs": {
    "danger": "0..1",
    "wonder": "0..1",
    "exhaustion": "0..1",
    "storm": "0..1"
  },
  "layers": [
    { "id": "low_strings", "gain": "smoothstep(0.3, 0.8, danger)" },
    { "id": "distant_choir", "gain": "wonder * (1 - danger)" },
    { "id": "heavy_percussion", "gain": "danger * exhaustion" }
  ]
}
```

DoD:

- Gameplay- oder WorldEvents treiben mindestens einen hörbaren State.
- CUE kann Audio-Cue, Loop/Transition und Timing als `AudioProof/v1` referenzieren.
- Acoustic Runtime setzt keine Gameplay-Wahrheit selbst.

### 7. CUE-Evidence über Gesamtlauf

CUE beweist den vertikalen Lauf:

- Build startet.
- Szene lädt.
- Spieler kann interagieren.
- Gameplay-Event verändert Zustand.
- SHADED zeigt sichtbare Folge.
- Audio reagiert auf Zustand.
- Temporal Proof bleibt konsistent.
- BARD-Audit beurteilt nur Creative Fidelity.

## P1 — System wird lernfähig

### 8. Actor Runtime

Actor Runtime besitzt konkrete Figurenkognition: Wissen, Erinnerung, Beziehung,
Glauben, Absicht und Entscheidung.

Kernverträge:

- `ActorDefinition/v1`
- `ActorKnowledge/v1`
- `Observation/v1`
- `MemoryEntry/v1`
- `BeliefState/v1`
- `RelationshipState/v1`
- `Intent/v1`
- `BehaviorDecision/v1`
- `DialogueAct/v1`

Trennung:

| State | Bedeutung |
|-------|-----------|
| `WorldState` | Was tatsächlich gilt |
| `ActorBelief` | Was die Figur glaubt |
| `ActorMemory` | Was sie erlebt oder erfahren hat |
| `ActorIntent` | Was sie gerade erreichen will |

Regel:

- NPCs dürfen nicht allwissend sein.
- Ignorieren ist eine Reaktion.
- Unterbrochene Dialoge bleiben unterbrochen.
- Beziehungen ändern Möglichkeiten, nicht nur Zahlen.

### 9. Runtime Memory / Save + Replay

Produktionsgedächtnis ist nicht Spielgedächtnis. Runtime Memory besitzt Persistenz.

Kernverträge:

- `SaveState/v1`
- `WorldSnapshot/v1`
- `ActorMemorySnapshot/v1`
- `ProgressionState/v1`
- `ReplayLog/v1`
- `SaveMigration/v1`

Regel:

- Snapshot + Event Log, nicht nur ein riesiger JSON-Endzustand.
- CUE muss reproduzieren können: „Nach Ereignis 83 hätte NPC A diese Information
  nicht besitzen dürfen.“

### 10. Playtest Layer

CUE beweist technische Funktion. Playtest Layer misst objektive Spielerfahrung.

Kernverträge:

- `PlaytestScenario/v1`
- `SessionTrace/v1`
- `PlayerActionLog/v1`
- `FrictionEvent/v1`
- `ExperienceReport/v1`
- `BalanceFinding/v1`

Metriken:

- Entscheidungsdichte
- Wiederholungen
- Sackgassen
- ungenutzte Fähigkeiten
- Abbruchpunkte
- Wartezeiten
- Rollenungleichgewicht
- Recovery-Zeit
- Tutorialfehler
- Navigationsprobleme

Regel:

- Keine „KI entscheidet, ob es Spaß macht“-Behauptung.
- Nur objektive Signale und Hypothesen für Commander/WIZARD/BARD.

### 11. Contract Registry mit Migrationen

Alle Zahnräder brauchen eine zentrale Contract Registry gegen Schema-Drift.

Zielstruktur:

```text
contracts/
├── bard/
├── wizard/
├── gameplay/
├── scene/
├── swift/
├── shaded/
├── audio/
├── cue/
└── target/
```

Jeder Contract braucht:

- kanonische ID
- Version
- JSON Schema oder Kotlin-Serializer
- Besitzer
- erlaubte Produzenten
- erlaubte Konsumenten
- Kompatibilitätsregeln
- Migrationen
- Beispiel-Fixtures
- Regressionstests

Fail-closed-Regel:

```text
BLOCKED:
unsupported contract anvil.scene-bundle/v2
supported: v1
```

### 12. Asset-Normalisierung und finale Lizenzprüfung

3D-RE-GEN Asset Lane besitzt Props, Environment, Tiles, Materialien, VFX-Quellen,
UI, Icons, Decals, LODs, Collider und Formatkonvertierung.

Kernverträge:

- `AssetRequest/v1`
- `AssetSpecification/v1`
- `GeneratedAssetBundle/v1`
- `NormalizedAsset/v1`
- `AssetValidation/v1`

Ein `NormalizedAsset/v1` braucht:

- Einheit und Maßstab
- Achsensystem
- Pivot
- Bounding Box
- Material-Slots
- Texturpfade
- LODs
- Collision
- Lizenz/Quelle
- Generator und Version
- Prüfsumme

## P2 — System wird veröffentlichungsfähig

13. Accessibility und Lokalisierung
14. Performance-Budgets pro Zielplattform
15. Crash-/Session-Telemetrie, bevorzugt lokal und datensparsam
16. Packaging, Signing und Distribution
17. Multiplayer-/Netcode-Adapter
18. Live-Update- und Migrationsstrategie

## Globale Regeln

### 1. Eine Wahrheit hat genau einen Besitzer

| Wahrheit | Besitzer |
|----------|----------|
| Kreative Absicht | BARD |
| Produktionsauswahl | WIZARD |
| Gameplay-Zustand | Gameplay Runtime |
| Raumstruktur | Scene Compiler |
| Figurenwissen | Actor Runtime |
| Visuelle Welt | SHADED |
| Akustische Welt | Acoustic Runtime |
| Technischer Beweis | CUE |
| Ablaufstatus | ANVIL |

### 2. Kein Modul schreibt fremde Wahrheit

- SHADED darf Wetter darstellen, aber nicht Gameplay-Zustand `storm=true` erfinden.
- Acoustic Runtime darf Gefahr hörbar machen, aber nicht eigenständig `danger=1` setzen.
- BARD darf einen dramatischen Sturm fordern, aber nicht behaupten, dass er technisch
  stattgefunden hat.

### 3. Keine Fertigmeldung ohne Beleg

Diese Zustände bleiben getrennt:

```text
produced ≠ integrated ≠ runnable ≠ verified ≠ creatively aligned
```

### 4. Jeder Output kennt seine Eltern

```json
{
  "artifactId": "ART_...",
  "parentRefs": ["BRD_...", "WIZ_...", "SCN_..."],
  "producer": "swift",
  "producerVersion": "1.4.0",
  "runId": "RUN_...",
  "checksum": "sha256:..."
}
```

### 5. Fail closed bei unbekannter Contract-Version

Kein Feldraten, kein stilles Mapping, keine dritte Zustandssprache. Unbekannte
Versionen blockieren den Run, bis eine Migration oder ein Adapter existiert.

## Entscheidender Befund

Es fehlen nicht zehn gleich große Produkte. Es fehlen vor allem fünf klare
Wahrheitsbesitzer:

| Kurzname | Besitzerfrage |
|----------|----------------|
| RULES | Was gilt? |
| STAGE | Wo gilt es? |
| CAST | Wer weiß und tut was? |
| SOUND | Wie klingt es? |
| SURFACE | Wie bedient man es? |

Dazu kommen:

| Kurzname | Besitzerfrage |
|----------|----------------|
| MEMORY | Was bleibt? |
| TARGET | Wie wird es ein echtes Spiel? |
| AUDIENCE | Wie verhält es sich beim Spieler? |

BARD, WIZARD, SWIFT, SHADED, CUE und ANVIL bilden Kopf, Werkstatt, Körper, Licht,
Prüfer und Dirigent. Die nächste Roadmap ergänzt Gesetze, Bühne,
Schauspielerhirne und Klang.
