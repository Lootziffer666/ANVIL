# Sprint-Bestandsaufnahme — ANVIL KMP / Studio Contracts

**Datum:** 2026-07-11
**Status:** Zwischenplateau nach B10–B19 / A7–A20 Sprint
**Ziel:** Festhalten, wo ANVIL nach dem Sprint steht, was stabilisiert wurde und was als Nächstes nicht weiter verbreitert, sondern gehärtet werden sollte.

---

## Kurzfassung

ANVIL steht nach diesem Sprint nicht mehr nur auf Konzept- und Dokumentationsebene.
Es gibt jetzt eine erste **KMP-Wirbelsäule** für:

- Artifact Output Layer
- Run Surface
- Agent Handoff
- Workspace Sync
- contract-first Studio-Module ohne BARD-Code
- BARD als externes privates Repo
- Bellows als kanonischen Modellrouting-Pfad

Der Sprint hat viele neue Flächen geöffnet. Der nächste Schritt sollte daher **Stabilisierung** sein: Build reparieren, Kern-Tests schreiben und einen kleinen End-to-End-Run nachweisen.

---

## Aktueller Gate-Stand

### A-Serie

| Gate | Status | Aktueller Befund |
|------|--------|------------------|
| A7 | `prototype` | Artifact Output Layer hat KMP-MVP mit Manifest/Registry/Writer. |
| A8 | `prototype` | Agent-Handoff läuft über Artifact Layer statt freiem Markdown. |
| A10 | `prototype` | Run Surface existiert als KMP-Kern. |
| A13 | `prototype` | Legacy Token Manager ist deaktiviert; Bellows CredentialVault ist aktiver Pfad. |
| A18 | `prototype` | Pake Desktop Shell hat Icon, Scripts und Packaging-Hilfen. |
| A19 | `done` + B19-Erweiterung | Workspace Sync existierte bereits; KMP Registry-Sync-MVP ergänzt. |
| A20 | `superseded` | OmniRoute ist keine Implementierungsrichtung mehr; Bellows bleibt. |

### B-Serie

| Gate | Status | Kern |
|------|--------|------|
| B10 | `docs-only` | Studio Roadmap / Wahrheitsbesitzer / Contract Registry Richtung. |
| B11 | `prototype` | Gameplay Compiler Contracts. |
| B12 | `prototype` | Scene Compiler / 3D-RE-GEN Contracts. |
| B13 | `prototype` | Target Adapter MVP. |
| B14 | `prototype` | Interface Compiler MVP. |
| B15 | `prototype` | Acoustic Runtime Slice. |
| B16 | `prototype` | Artifact Output Layer MVP. |
| B17 | `prototype` | Run Surface MVP. |
| B18 | `prototype` | Handoff Export MVP. |
| B19 | `prototype` | Workspace Sync Registry MVP. |

---

## Was jetzt im Repo existiert

### Core-Kerne

| Bereich | Pfad | Rolle |
|---------|------|-------|
| Contracts | `anvil-kmp/core/contracts` | `ModuleSlotContract`, `ExecutionPhase`, `ModuleContext`, `StepResult`, Artifact-Refs. |
| Artifacts | `anvil-kmp/core/artifacts` | `ArtifactManifest`, `ArtifactRegistry`, `ArtifactWriter`, Artifact-Envelopes. |
| Run | `anvil-kmp/core/run` | `RunPlan`, `RunSurface`, `RunSummary`, Schrittaufzeichnung. |
| Handoff | `anvil-kmp/core/handoff` | Artifact-backed Agent Prompt Packs. |
| Sync | `anvil-kmp/core/sync` | Workspace-Sync-Bundles über Artifact-/Run-Registry. |

### Studio-Module

| Modul | Pfad | Wahrheit / Aufgabe |
|-------|------|--------------------|
| BARD | externes privates Repo | Bedeutung, CreativeBrief, ProductionIntent, Challenge/Audit; in ANVIL nur Contract-Refs. |
| Gameplay | `anvil-kmp/modules/gameplay` | Regeln, Interactions, Conditions, Effects, StatePatch. |
| Scene | `anvil-kmp/modules/scene` | Bühne, SceneBundle, Anchors, Navigation, Kamera. |
| Interface | `anvil-kmp/modules/interface` | Input Actions, HUD, Prompts, Accessibility-Basis. |
| Acoustic | `anvil-kmp/modules/acoustic` | AudioIntent, AudioCueGraph, AudioProof-Slice. |
| Target | `anvil-kmp/modules/target` | ProductionBundle → RunnableBuild-Absicht. |

### Bellows-Entscheidung

Bellows bleibt der kanonische Gateway und Modellrouter. OmniRoute ist ausdrücklich
superseded/reference und wird nicht als zweite Provider-Wahrheit eingeführt.

Der Cloudserver-Plan ist dokumentiert als:

- Anvil-Bellows bleibt eigenes Repo.
- Betrieb per Docker auf Cloudserver.
- Geräte nutzen einen gemeinsamen OpenAI-kompatiblen `/v1`-Endpoint.
- Geräte halten keine Provider-Keys lokal.
- Provider-Keys bleiben serverseitig.
- `LOCAL_ONLY` darf nie heimlich Cloud-Fallback machen.

---

## Was der Sprint architekturell verändert hat

Vorher war ANVIL stark in Vision und Rollenmodell, aber viele Übergänge waren noch
unbewiesen:

```text
Modul erzeugt etwas
        ↓
Wo liegt es?
        ↓
Wer prüft es?
        ↓
Wie wird es übergeben?
        ↓
Wie bleibt es syncbar?
```

Jetzt gibt es eine erste technische Kette:

```text
ModuleSlotContract
        ↓
RunSurface
        ↓
ArtifactWriter / ArtifactRegistry
        ↓
Handoff / Sync / Module Artifacts
```

Und eine erste Studio-Kette:

```text
BARD(ext.)  → Bedeutung
Gameplay    → Regeln
Scene       → Bühne
Interface   → Bedienung
Acoustic    → Klang
Target      → Build-Ziel
Bellows     → Modellrouting
Artifact    → Beweise / Outputs
Run         → Ablauf
Handoff     → Übergabe
Sync        → Geräte-/Workspace-Abgleich
```

---

## Was noch nicht fertig ist

Der Sprint war breit. Die neuen Module sind **MVPs/Prototypen**, keine
Produktionsreife.

Offen:

1. **Build-Stabilisierung**
   - Die Umgebung mit JDK `25.0.2` blockiert Gradle/Kotlin-Ausführung früh.
   - Es braucht einen geprüften JDK-17/21-Pfad für CI und lokale Entwicklung.

2. **Tests**
   - `ArtifactWriter`
   - `RunSurface`
   - `HandoffExporter`
   - `WorkspaceSyncService`
   - Modul-Serialization-Roundtrips

3. **Erster Mini-End-to-End-Run**
   - Seed → externes BARD
   - externes BARD → Gameplay/Scene/Interface/Acoustic/Target
   - RunSurface → ArtifactRegistry
   - Handoff Export
   - Sync Bundle

4. **Bellows separat operationalisieren**
   - Dockerfile / Image / Compose gehören ins Anvil-Bellows Repo.
   - Dieses Repo hält nur die ANVIL-seitigen Routing- und Sicherheitsgrenzen.

5. **Keine weitere Verbreiterung, bevor Kern stabil ist**
   - Nicht sofort Actor Runtime, Playtest Layer oder Runtime Memory implementieren.
   - Erst beweisen, dass die vorhandene Kette baut, serialisiert und einen Minimal-Run trägt.

---

## Empfohlene nächste Sequenz

### P0 — Stabilisierung

1. JDK/Gradle-Umgebung fixieren.
2. Core-Kompilierung grün bekommen:
   - `:core:contracts`
   - `:core:artifacts`
   - `:core:run`
   - `:core:handoff`
   - `:core:sync`
3. Unit-Tests für die vier neuen Core-Kerne schreiben.
4. Serialization-Roundtrip-Tests für alle neuen Modelle schreiben.

### P1 — Minimaler Durchstich

1. Kleinen RunPlan mit 2–3 Modulen ausführen.
2. Artifacts in Registry schreiben.
3. Handoff daraus erzeugen.
4. Sync-Bundle daraus erzeugen.
5. Report dokumentieren.

### P2 — Bellows Betrieb

1. Im separaten Anvil-Bellows Repo Dockerfile/Compose umsetzen.
2. Gateway-Token und Provider-Keys serverseitig halten.
3. Device-Zugriff über HTTPS + Bearer/VPN absichern.
4. `LOCAL_ONLY`-Verhalten live prüfen.

---

## Sprint-Fazit

Der Sprint hat ANVIL von einer stark dokumentierten Vision zu einem ersten
contract-first KMP-Gerüst verschoben.

Der richtige nächste Schritt ist nicht „noch mehr Module“, sondern:

> **kleiner bauen, beweisen, testen, härten.**

Erst wenn Artifact → Run → Handoff → Sync zuverlässig funktioniert, sollte die
nächste Studio-Schicht wie Actor Runtime, Runtime Memory oder Playtest Layer folgen.
