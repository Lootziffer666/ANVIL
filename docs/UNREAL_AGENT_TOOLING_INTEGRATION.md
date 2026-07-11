# ANVIL Unreal Agent Tooling Integration

**Gate:** B10 candidate — Unreal MCP Bridge / SHADED Production Hands
**Status:** Evaluation / integration plan
**Date:** 2026-07-11

## Kurzurteil

ANVIL sollte Unreal-Agent-Tools nicht als neue kreative Autorität einbauen. Sie
sind **SHADED-/Execution-Hände**: sie dürfen bauen, messen, rückrollen und Beweise
liefern, aber nicht BARDs CreativeBrief verändern und nicht WIZARDs
Produktionsentscheidung ersetzen.

Empfohlene Reihenfolge:

1. **VibeUE als First Pilot** — bester Fit für SHADED, Worldbuilding,
   Umwelt-Storytelling, Undo/Checkpoints und Performance-Profiling auf Unreal 5.8+.
2. **Unreal Agent Harness als CUE-Proof Adapter** — stark für `see → act → check → fix`,
   Screenshot-/Viewport-Beweise und QA-Loop.
3. **Autonomix nur im Quarantäne-Modus** — mächtig, aber hohes Risiko für Scope Creep,
   autonome Projektmutation und Systemhölle.
4. **UnrealMotionGraphicsMCP später gezielt für UI/UMG**.
5. **Convai später für NPC-Lebendigkeit**, nicht für den ersten SHADED-Engpass.
6. **UnrealGenAISupport nur beobachten**, da das freie MCP laut README nicht aktiv
   weiterentwickelt wird.

## Quellen

- VibeUE GitHub: https://github.com/kevinpbuckley/VibeUE
- VibeUE Docs: https://www.vibeue.com/docs
- Autonomix GitHub: https://github.com/PRQELT/Autonomix
- Unreal Agent Harness GitHub: https://github.com/per-simmons/unreal-agent-harness
- UnrealGenAISupport GitHub: https://github.com/prajwalshettydev/UnrealGenAISupport
- UnrealMotionGraphicsMCP GitHub: https://github.com/winyunq/UnrealMotionGraphicsMCP
- Convai Unreal SDK GitHub: https://github.com/Conv-AI/Convai-UnrealEngine-SDK

## Geprüfte Quellenlage am 2026-07-11

| Tool | Aktueller Befund | ANVIL-Fit |
|------|------------------|-----------|
| VibeUE | UE 5.8+ MCP Expansion; nutzt Unreal MCP, ToolsetRegistry und AgentSkill; bringt Terrain, UMG, Niagara, Animation, Audio, Performance/Profiling und Undo/Checkpoint-Transaktionen. | **Primärer SHADED-Hands-Pilot** |
| Autonomix | In-Editor AI Developer; erstellt Blueprints, C++, Levels, Materials, Widgets, PCG, Animation etc.; eigene agentische Tool-Loop, Checkpoints, Multi-Provider. | **Quarantäne / spätere Evaluation** |
| Unreal Agent Harness | Harness für UE 5.8 mit Händen, Augen, Wissen und QA-Loop; explizit raw/Plumbing-Aufwand; enthält Capture/Decode/QA-Hilfen. | **CUE-Agent Proof Adapter** |
| UnrealGenAISupport | Breite GenAI-Funktionalität, aber freies MCP wird laut README nicht aktiv weiterentwickelt. | Beobachten, nicht Hauptbasis |
| UnrealMotionGraphicsMCP | Spezialisierter UMG-MCP mit Widget Trees, Slot Properties, Blueprint Graphs, UI Materials, Animation Tracks und JSON-Roundtrips. | UI-Spezialist später |
| Convai Unreal SDK | NPC-Konversation, Knowledge Base, Multiplayer-Interaction, Lip-sync/Animation. | Später: Figuren/NPCs |

## ANVIL-Zuständigkeitsgrenzen

### BARD

BARD bleibt Bedeutungskompiler und lebt als eigenes privates Repo; ANVIL/Unreal-Tooling konsumiert nur seine Contracts:

- erzeugt `CreativeBrief/v1`, `ProductionIntent/v1`, Locks und Acceptance Criteria;
- prüft nach CUE nur kreative Werktreue;
- darf keine Unreal-Tools direkt steuern.

### WIZARD

WIZARD entscheidet, welche Produktionsroute genutzt wird:

- VibeUE für Terrain, Szene, Niagara, UMG, Performance;
- Unreal Agent Harness für Beweisaufbau/QA;
- Autonomix nur, wenn eine explizite Quarantäne-Policy aktiv ist.

WIZARD übersetzt BARDs `ProductionIntent` in einen **Unreal Tool Plan**, aber ohne
BARD-Intent zu überschreiben.

### SHADED / Execution Core

SHADED beziehungsweise der Execution Core ruft Unreal-MCP-Tools auf. Diese Ebene:

- spricht mit Unreal 5.8+ MCP auf `127.0.0.1`;
- serialisiert alle mutierenden Editor-Aktionen;
- schreibt jede Tool-Aktion als Artifact mit Run-ID, Tool, Payload, Ergebnis,
  Screenshot-/Log-/Trace-Refs und SHA-256;
- nutzt Undo/Checkpoint, bevor mutierende Schritte laufen.

### CUE-AGENT

CUE konsumiert Playable-/Temporal-/Visual-Proofs:

- Editor startet / PIE startet;
- Szene ist bedienbar;
- sichtbare Reaktion tritt ein;
- Transitionen sind zeitlich konsistent;
- Screenshot, Log, Trace und ggf. Performance-Profil sind Artifact-Beweise.

CUE entscheidet technische Spielbarkeit. BARD entscheidet danach nur kreative
Übereinstimmung.

## Zielarchitektur

```text
Mensch / Commander
  ↓ CreativeSeed/v1
BARD
  ↓ CreativeBrief/v1 + ProductionIntent/v1
WIZARD
  ↓ UnrealToolPlan/v1
Execution Core / SHADED Bridge
  ↓ native Unreal MCP localhost
VibeUE / Unreal Toolsets / optional Autonomix quarantine
  ↓ Unreal artifacts, screenshots, traces, logs, checkpoint refs
CUE-AGENT
  ↓ PlayableProof + TemporalProof
BARD
  ↓ CreativeFidelityReport/v1
Commander
```

## Neue Contract-Idee: UnrealToolPlan/v1

Der nächste Gate sollte noch **keine Plugin-Abhängigkeit** hinzufügen. Stattdessen
brauchen wir einen schmalen ANVIL-Vertrag zwischen WIZARD und Execution Core:

```json
{
  "schema": "anvil.unreal.tool-plan/v1",
  "planId": "UTP_...",
  "workspaceId": "WS_...",
  "runId": "RUN_...",
  "creativeBriefRef": "BRD_...",
  "productionIntentRef": "BRD_...#productionIntent",
  "engine": {
    "minVersion": "5.8",
    "mcpEndpoint": "http://127.0.0.1:8000/mcp",
    "requiresNativeMcp": true
  },
  "policy": {
    "mutationMode": "serialized",
    "requiresCheckpointBeforeMutation": true,
    "allowAutonomix": false,
    "allowNetworkTerrain": false
  },
  "steps": [
    {
      "id": "UTS_001",
      "adapter": "vibeue",
      "capability": "scene.blockout",
      "intentRef": "productionIntent.worldIntent.reactiveStates",
      "mutation": true,
      "expectedProofs": ["screenshot", "log", "checkpoint"]
    }
  ],
  "acceptanceCriteria": ["cooperation-visible", "failure-creates-new-state"]
}
```

## Tool-Policy

### VibeUE: aufnehmen als `vibeue` Adapter

Warum:

- nutzt Unreal 5.8+ native MCP statt eigenem Server;
- ergänzt Editor-Toolsets für Terrain, Foliage, Animation, Niagara, UMG, Audio,
  Blueprint-Tiefe und Performance-Profiling;
- bietet Transaktions-/Undo-/Checkpoint-Denken, das gut zu ANVIL-Artefakten passt;
- empfiehlt Agenten, erst Tools/Skills zu entdecken und Python-Batches zu nutzen.

ANVIL-Regel:

- VibeUE darf nur aus `UnrealToolPlan/v1` ausgeführt werden;
- kein direkter Prompt „mach mir eine Szene“ ohne WIZARD-Plan;
- mutierende Calls immer einzeln serialisieren;
- vor jedem mutierenden Call: Checkpoint-Artifact;
- nach jedem Call: Screenshot/Log/ToolResult-Artifact.

### Unreal Agent Harness: als CUE-Proof Adapter auswerten

Warum:

- liefert genau den Loop, den CUE braucht: sehen, handeln, prüfen, fixen;
- enthält Capture/Decode/QA-Denken;
- nennt selbst harte Constraints: Mutationen serialisieren, Captures über MCP-Tool-Layer.

ANVIL-Regel:

- Harness-Skripte nicht als Produktionsautorität verwenden;
- nur als Proof- und QA-Schicht übernehmen;
- seine `ue_qa.py`-ähnlichen Konzepte in CUE-Artefakte übersetzen:
  screenshot, decoded viewport JSON, refdiff, log extract, overlap/trace checks.

### Autonomix: Quarantäne statt Hauptbasis

Warum vorsichtig:

- Autonomix ist ein eigener autonomer Developer mit Chat-Loop und Multi-Providern;
- kann C++ und Projektdateien verändern;
- eigene Provider- und Checkpoint-Schicht kann ANVILs Privacy-/Artifact-/Gate-Regeln umgehen.

ANVIL-Regel:

- default `allowAutonomix=false`;
- nur in separater Sandbox-Workspace-Kopie;
- nur mit lokalem Provider oder expliziter Privacy-Ausnahme;
- alle Autonomix-Änderungen müssen als Diff-Artefakt zurück in ANVIL importiert werden;
- kein direkter Schreibzugriff auf den kanonischen Workspace.

### Zweite Reihe

- **UnrealMotionGraphicsMCP:** später als `umg-specialist` Adapter, wenn Launchpad/CatchIt-ähnliche UIs im Fokus sind.
- **Convai:** später für NPC-Konversation/Lip-sync, wenn Character-Briefs stabil sind.
- **UnrealGenAISupport:** vorerst nicht als Hauptbasis wegen Pflege-/MCP-Status.

## Gate-Vorschlag

### Gate B10: Unreal MCP ToolPlan Contract

Definition of Done:

- `UnrealToolPlan/v1` und `UnrealToolResult/v1` als KMP-serializable Contracts.
- Policy-Felder: `mutationMode`, `requiresCheckpointBeforeMutation`,
  `allowAutonomix`, `allowNetworkTerrain`, `privacyMode`.
- Adapter-IDs: `native-unreal-mcp`, `vibeue`, `unreal-agent-harness`,
  `autonomix-quarantine`, `umg-mcp`, `convai`.
- Keine Plugin-Abhängigkeit, kein Unreal-Binary im Repo.
- Unit-Proof: Plan mit mutierender VibeUE-Step verlangt Checkpoint und Proof-Artefakte.

### Gate B11: Local Unreal Bridge Spike

Definition of Done:

- JVM/desktop-only Bridge kann eine lokale MCP-Konfiguration lesen.
- Bridge kann einen read-only Tool-Discovery-Call ausführen.
- Kein mutierender Call ohne Checkpoint-Policy.
- Resultate werden als ANVIL-Artifacts zurückgegeben.

### Gate B12: CUE Unreal Proof Adapter

Definition of Done:

- PlayableProof kann Screenshot, viewport decode, log extract und temporal markers referenzieren.
- CUE kann `insufficient_evidence` melden, ohne kreative Bewertung zu übernehmen.
- BARD `AUDIT` konsumiert nur Proof-Refs und gibt nur Creative-Fidelity aus.

## Kill-Kriterien

Stoppen, wenn eines davon passiert:

- Ein Unreal-Tool verändert BARD-Locks oder `CreativeBrief` direkt.
- Ein Plugin wählt Assets/Lizenzen ohne WIZARD-Plan.
- Autonomix schreibt in den kanonischen Workspace statt in Quarantäne.
- Ein mutierender MCP-Call läuft ohne Checkpoint-Artefakt.
- Zwei mutierende Unreal-Calls laufen parallel.
- Ein Tool behauptet technische Spielbarkeit ohne CUE-Proof.
- `LOCAL_ONLY` wird durch Cloud-Provider, hosted terrain oder Plugin-API stillschweigend verletzt.

## Entscheidung

**Ja, integrieren — aber als Tool-Layer, nicht als ANVIL-Hirn.**

VibeUE ist der stärkste Startpunkt für SHADED-Hände. Unreal Agent Harness ist der
beste Startpunkt für CUE-Beweise. Autonomix bleibt interessant, aber gehört in eine
Quarantäne-Spur, bis ANVIL Artifact-, Privacy-, Checkpoint- und Diff-Regeln erzwingen
kann.
