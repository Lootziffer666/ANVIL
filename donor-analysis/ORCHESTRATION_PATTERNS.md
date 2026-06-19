# Orchestration Patterns

**Gate:** AX2  
**Stand:** 2026-06-10  
**Quellen:** AbdoKnbGit/tau, microsoft/UFO  
**Ziel-Bereich:** RunStep-Pipeline (B4), BellowsRouter Multi-Provider (B9)

---

## Zusammenfassung

tau und UFO implementieren fortschrittliche Agent-Orchestration-Patterns:
- **tau:** Multi-Provider (25 Adapter), Session-Management, DAG-Teams, Self-Learning
- **UFO:** DAG-basierte Task-Decomposition (Constellation), Dynamic DAG Editing, Multi-Device, AIP (WebSocket)

Dieses Dokument mapped diese Patterns auf ANVILs bestehende Run-Pipeline (PLAN-PATCH-DIFF-GATE-ARTIFACT) und identifiziert Erweiterungs-Moeglichkeiten.

---

## ANVILs aktuelle Pipeline (B4)

```kotlin
sealed class RunStep {
    data class ReadFile(...) : RunStep()
    data class WriteFile(...) : RunStep()
    data class PromptLlm(...) : RunStep()
    data class RunCommand(...) : RunStep()
    data class SaveCheckpoint(...) : RunStep()
}
```

Die Pipeline ist **linear** und **sequenziell**: Ein Run fuehrt Steps nacheinander aus.

Konzeptuelles Modell (aus Architektur-Docs):
```
PLAN -> PATCH -> DIFF -> GATE -> ARTIFACT
```

---

## Pattern 1: DAG-basierte Task-Decomposition (UFO)

### Was UFO tut

UFO3 zerlegt eine Aufgabe in einen **Directed Acyclic Graph (DAG)**:
- Jeder Knoten = ein Task (Agent-Aktion)
- Kanten = Abhaengigkeiten
- Parallele Knoten ohne Abhaengigkeit werden gleichzeitig ausgefuehrt
- "Constellation Pattern": Mehrere Device-Agents arbeiten parallel

### Mapping auf ANVIL

ANVILs PLAN-PATCH-DIFF-GATE-ARTIFACT koennte erweitert werden:

```
           PLAN
          /    \
     PATCH-A   PATCH-B      <-- Parallele Patches (z.B. verschiedene Dateien)
          \    /
           DIFF
            |
           GATE
            |
         ARTIFACT
```

### Vorgeschlagene Erweiterung: RunGraph

```kotlin
// Erweiterung von B4 RunStep
sealed class RunNode {
    data class Sequential(val steps: List<RunStep>) : RunNode()
    data class Parallel(val branches: List<RunNode>) : RunNode()
    data class Conditional(val condition: RunCondition, val ifTrue: RunNode, val ifFalse: RunNode?) : RunNode()
}

data class RunGraph(
    val root: RunNode,
    val edges: List<RunEdge>,  // Abhaengigkeiten
    val checkpoints: List<String>  // Auto-Checkpoint nach diesen Node-IDs
)

data class RunEdge(
    val from: String,  // Node-ID
    val to: String,    // Node-ID
    val type: EdgeType
)

enum class EdgeType {
    DEPENDS_ON,     // to wartet auf from
    PRODUCES,       // from erzeugt Input fuer to
    GATES           // from muss PASS sein, damit to startet
}
```

### Beispiel: Multi-File-Refactoring

```kotlin
val refactorGraph = RunGraph(
    root = RunNode.Sequential(listOf(
        RunNode.Sequential(listOf(RunStep.PromptLlm(planRequest))),  // PLAN
        RunNode.Parallel(listOf(                                      // PATCH (parallel)
            RunNode.Sequential(listOf(RunStep.WriteFile(fileA))),
            RunNode.Sequential(listOf(RunStep.WriteFile(fileB))),
            RunNode.Sequential(listOf(RunStep.WriteFile(fileC)))
        )),
        RunNode.Sequential(listOf(RunStep.RunCommand(buildCheck))),   // GATE
        RunNode.Sequential(listOf(RunStep.SaveCheckpoint(snap)))      // ARTIFACT
    ))
)
```

---

## Pattern 2: Multi-Provider-Orchestration (tau)

### Was tau tut

tau orchestriert 25 Provider-Adapter mit:
- **Fallback-Ketten:** Provider A -> B -> C bei Failure
- **Team-Mode:** Coordinator + Workers (DAG mit Rollen)
- **Provider-Selektion:** nach Capability, Kosten, Latenz
- **Session-Persistence:** Kontext ueber Provider-Wechsel hinweg

### Mapping auf ANVILs BellowsRouter (B9)

BellowsRouter hat bereits:
- Provider-Match (Modell-Capabilities)
- Health-Check
- Fallback-Kette (naechster Adapter bei Failure)
- Privacy-Check (LOCAL_ONLY Enforcement)

**Erweiterungen aus tau:**

```kotlin
// Erweiterte Provider-Selektion
data class ProviderSelection(
    val strategy: SelectionStrategy,
    val constraints: List<ProviderConstraint>
)

enum class SelectionStrategy {
    CHEAPEST,       // Guenstigster Provider fuer Task
    FASTEST,        // Niedrigste Latenz
    BEST_QUALITY,   // Hoechste Output-Qualitaet (basierend auf History)
    ROUND_ROBIN,    // Gleichmaessige Verteilung
    CAPABILITY_MATCH // Bester Match fuer spezifische Aufgabe
}

data class ProviderConstraint(
    val type: ConstraintType,
    val value: String
)

enum class ConstraintType {
    MAX_COST_PER_TOKEN,
    MAX_LATENCY_MS,
    MIN_CONTEXT_WINDOW,
    REQUIRED_CAPABILITY,  // z.B. "code-generation", "reasoning"
    PRIVACY_MODE
}
```

### Team-Mode (DAG mit Rollen)

tau's Team-Mode mapped auf ANVIL:

```kotlin
// Coordinator plant, Workers fuehren aus
data class RunTeam(
    val coordinator: ProviderRole,  // Plant den DAG
    val workers: List<ProviderRole>  // Fuehren Steps aus
)

data class ProviderRole(
    val name: String,           // z.B. "planner", "coder", "reviewer"
    val preferredProvider: String?,  // z.B. "openai/gpt-4o" fuer Planner
    val fallbackProviders: List<String>,
    val capabilities: List<String>
)
```

**Beispiel:** Planner (Claude) plant den Run, Coder (DeepSeek) schreibt Code, Reviewer (GPT-4o) prueft.

---

## Pattern 3: Session/Snapshot-Management (tau)

### Was tau tut

- **Snapshot with Time Traveling:** Shadow-Git-Repo fuer jeden Agent-Zustand
- **Session-Branching:** Explorative Pfade ohne Hauptsession zu gefaehrden
- **Session-Cloning:** Parallele Exploration desselben Ausgangszustands
- **Resume:** Abgebrochene Sessions fortsetzen

### Mapping auf ANVILs Snapshot (B3) + SaveCheckpoint (B4)

ANVIL hat bereits:
```kotlin
data class Snapshot(
    val id: String,
    val workspaceId: String,
    val runId: String?,
    val takenAt: Instant,
    val checkpoint: CheckpointData
)

sealed class RunStep {
    data class SaveCheckpoint(val label: String) : RunStep()
}
```

**Erweiterungen aus tau:**

```kotlin
// Session-Branching
data class SessionBranch(
    val branchId: String,
    val parentSnapshotId: String,
    val label: String,
    val status: BranchStatus
)

enum class BranchStatus {
    ACTIVE,     // Aktuell in Bearbeitung
    MERGED,     // Zurueck in Hauptsession
    ABANDONED,  // Verworfen
    PAUSED      // Pausiert, resumable
}

// Session-Management in Run
data class RunSession(
    val sessionId: String,
    val runId: String,
    val branches: List<SessionBranch>,
    val currentBranch: String,
    val autoCheckpointInterval: Int  // Steps zwischen Auto-Checkpoints
)
```

---

## Pattern 4: Dynamic DAG Editing (UFO)

### Was UFO tut

UFO erlaubt Runtime-Modifikation des Task-DAGs:
- Agent erkennt waehrend Ausfuehrung, dass der Plan suboptimal ist
- DAG wird modifiziert (Nodes hinzugefuegt/entfernt/umgeroutet)
- Keine vollstaendige Neu-Planung noetig

### Mapping auf ANVIL

```kotlin
interface DynamicRunGraph {
    fun addNode(node: RunNode, after: String): RunGraph
    fun removeNode(nodeId: String): RunGraph
    fun reroute(from: String, newTarget: String): RunGraph
    fun insertParallel(existingNodeId: String, newNode: RunNode): RunGraph
}
```

**Use Case:** Run startet mit Plan "Refactor File A", entdeckt waehrend PATCH dass File B auch geaendert werden muss -> DAG wird dynamisch erweitert.

**ANVIL-Relevanz:** Hoch fuer komplexe Refactoring-Tasks. Niedrig fuer einfache single-file Aenderungen.

---

## Pattern 5: Self-Learning (tau)

### Was tau tut

- `/learned` Command: Agent speichert Erkenntnisse aus der Session
- Korrektur-Mining: Aus fehlgeschlagenen Sessions werden Regeln extrahiert
- Persistent Memory: Learnings ueber Sessions hinweg

### Mapping auf ANVILs MemoryEntry (B3)

```kotlin
data class MemoryEntry(
    val id: String,
    val workspaceId: String,
    val runId: String?,
    val content: String,
    val kind: MemoryKind,
    val timestamp: Instant
)
```

**Erweiterung:**

```kotlin
enum class MemoryKind {
    LOG,            // Bestehend
    LEARNING,       // NEU: Aus tau - persistente Erkenntnis
    CORRECTION,     // NEU: Aus tau - Korrektur eines Fehlers
    PREFERENCE      // NEU: User-Praeferenz
}

data class Learning(
    val trigger: String,      // Situation, die das Learning ausgeloest hat
    val rule: String,         // Die gelernte Regel
    val confidence: Double,   // 0.0 - 1.0
    val appliedCount: Int     // Wie oft angewendet
)
```

---

## Priorisierung fuer ANVIL

| Pattern | Prioritaet | Aufwand | Abhaengigkeit | Gate-Vorschlag |
|---------|-----------|---------|---------------|----------------|
| DAG-RunGraph | Hoch | Mittel | B4 (RunStep) | B-Orch-1 |
| Multi-Provider-Selektion | Mittel | Niedrig | B9 (BellowsRouter) | B-Orch-2 |
| Session-Branching | Mittel | Mittel | B3 (Snapshot) | B-Orch-3 |
| Dynamic DAG Editing | Niedrig | Hoch | B-Orch-1 | B-Orch-4 |
| Self-Learning | Niedrig | Niedrig | B3 (MemoryEntry) | B-Orch-5 |

---

## Empfohlene Reihenfolge

### Phase 1: DAG-Grundlagen (Gate B-Orch-1)
- `RunNode` sealed class (Sequential, Parallel, Conditional)
- `RunGraph` mit Edges und Checkpoints
- Executor der Parallel-Branches via Coroutines dispatcht
- Tests: Parallele WriteFile-Steps, Conditional Gates

### Phase 2: Provider-Selektion (Gate B-Orch-2)
- `SelectionStrategy` in BellowsRouter
- `ProviderConstraint` Filtering
- Cost/Latency-Tracking pro Provider (aus BellowsRouter-Logs)
- Tests: Strategy-basierte Provider-Wahl

### Phase 3: Session-Management (Gate B-Orch-3)
- `SessionBranch` Datenmodell
- `RunSession` mit Branch-Verwaltung
- Auto-Checkpoint-Interval
- Branch-Merge und Abandon
- Tests: Branch/Merge/Resume Lifecycle

### Phase 4+: Advanced (Deferred)
- Dynamic DAG Editing (B-Orch-4)
- Self-Learning Integration (B-Orch-5)
- Team-Mode mit Rollen-basierter Provider-Zuweisung

---

## Risiken

| Risiko | Schwere | Mitigation |
|--------|---------|------------|
| DAG-Complexity fuer einfache Tasks | Niedrig | Sequential als Default, DAG nur bei parallelen Steps |
| Deadlocks in Parallel-Branches | Mittel | Cycle-Detection bei Graph-Erstellung, Timeout pro Branch |
| Provider-Switching verliert Kontext | Mittel | Session-Persistence (CCR aus HEADROOM_INTEGRATION_CONCEPT) |
| Dynamic DAG macht Runs unpredictable | Mittel | Gate-Checks nach jeder DAG-Modifikation |
| Over-Engineering fuer MVP | Hoch | Phase 1 nur, wenn echte Parallel-Use-Cases existieren |

---

## Abgrenzung

- **Kein Code-Import** aus tau oder UFO
- **Kein WebSocket/AIP** (UFO-Pattern fuer Multi-Device, nicht ANVIL-relevant aktuell)
- **Kein Team-Mode MVP** (zu komplex, erst nach Phase 1-3)
- **Kein Echtzeit-Collaboration** (deferred, siehe Gate A24)

---

## Zusammenhang mit anderen Konzept-Dokumenten

| Dokument | Verbindung |
|----------|------------|
| HEADROOM_INTEGRATION_CONCEPT.md | CCR loest das Kontext-Problem bei Provider-Wechsel (Pattern 2) |
| SKILLS_ARCHITECTURE_CONCEPT.md | Skills koennten als DAG-Nodes in RunGraph eingebunden werden |
| STARRED_REPOS_BRIEF.md | Ueberblick und Entscheidungstabelle fuer alle Patterns |
