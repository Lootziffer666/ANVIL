# Skills Architecture Concept

**Gate:** AX2  
**Stand:** 2026-06-10  
**Quellen:** nexu-io/open-design, alirezarezvani/claude-skills, jnMetaCode/agency-agents-zh  
**Ziel-Bereich:** ModuleSlotContract, The Forge, Commander Surface

---

## Zusammenfassung

Drei unabhaengige Open-Source-Projekte konvergieren auf ein gemeinsames Skill-Pattern:
- **open-design:** 259+ Skills als modulare Instruktions-Pakete mit DESIGN.md als Brand-Contract
- **claude-skills:** 337 Skills/Agents/Plugins, SKILL.md-Format, stdlib-only Python Tools
- **agency-agents-zh:** 216 Expert-Rollen mit SOUL.md + IDENTITY.md + AGENTS.md

Dieses Dokument schlaegt ein Skill-System fuer ANVIL vor, das auf dem bestehenden `ModuleSlotContract` (B2) aufbaut.

---

## Was ein Skill ist und nicht ist

**Ein ANVIL-Skill ist:**
- Eine deklarative Instruktions-Einheit mit Metadaten
- Ein Subset von `ModuleSlotContract` (leichtgewichtiger als ein volles Modul)
- Aktivierbar ueber Commander Surface oder programmatisch via RunStep
- Privacy-bewusst (LOCAL_ONLY vs. CLOUD_ALLOWED Markierung)

**Ein ANVIL-Skill ist NICHT:**
- Ein vollstaendiges Modul mit eigenem Build-Zyklus
- Ein externer Plugin mit Runtime-Dependencies
- Ein beliebiger Prompt (Skills haben Struktur und Validierung)
- Ein Fork oder Import aus open-design/claude-skills

---

## Skill-Format: SKILL.md

Inspiriert von open-design (DESIGN.md) und claude-skills (SKILL.md), adaptiert fuer ANVIL:

```
anvil-kmp/skills/{category}/{skill-name}/
  SKILL.md          -- Instruktionen (Markdown)
  skill.json        -- Metadaten (maschinenlesbar)
  tools/            -- Optionale Tool-Skripte (Kotlin/stdlib-only)
  examples/         -- Beispiel-Inputs/Outputs
```

### skill.json Schema

```json
{
  "name": "diff-review",
  "version": "1.0.0",
  "category": "code-generation",
  "privacy": "LOCAL_ONLY",
  "requires": ["KnightContract"],
  "provides": ["diff-analysis", "review-comment"],
  "activation": {
    "trigger": "manual",
    "surface": "commander"
  },
  "contract": "ModuleSlotContract",
  "author": "anvil-internal"
}
```

### SKILL.md Struktur

```markdown
# {Skill-Name}

## Zweck
Was dieser Skill tut (1-2 Saetze).

## Kontext
Welche Informationen der Skill braucht (Dateien, History, Workspace-State).

## Regeln
1. ...
2. ...

## Output-Format
Wie das Ergebnis aussieht.

## Beispiele
...
```

---

## Skill-Kategorien fuer ANVIL

Abgeleitet aus den drei Quell-Repos, gefiltert auf ANVIL-Relevanz:

| Kategorie | Beschreibung | Beispiel-Skills | Quelle |
|-----------|--------------|-----------------|--------|
| `code-generation` | Code erzeugen/transformieren | diff-review, refactor-suggest, test-writer | claude-skills |
| `workspace-management` | Workspace-Operationen | file-organizer, dependency-check, tree-analyzer | open-design |
| `prompt-engineering` | Prompt-Optimierung | prompt-compress, context-select, few-shot-builder | claude-skills |
| `quality-assurance` | Qualitaetspruefung | lint-check, security-scan, style-enforce | agency-agents-zh |
| `orchestration` | Multi-Step-Koordination | plan-executor, parallel-dispatch, checkpoint-manager | open-design |

**Nicht uebernommen:** Marketing, C-Level-Advisory, Finance, Design-System-Skills (nicht ANVIL-relevant).

---

## Integration mit bestehendem ModuleSlotContract

ANVILs `ModuleSlotContract` (B2) definiert bereits:
```kotlin
interface ModuleSlotContract {
    val slotId: String
    val moduleName: String
    val version: String
    fun activate(): Boolean
    fun deactivate(): Boolean
    fun qualityState(): QualityState
}
```

### Vorschlag: SkillSlotContract als Spezialisierung

```kotlin
interface SkillSlotContract : ModuleSlotContract {
    val category: SkillCategory
    val privacy: PrivacyMode
    val requires: List<String>  // Contract-Dependencies
    val provides: List<String>  // Capabilities
    
    suspend fun execute(context: SkillContext): SkillResult
}

data class SkillContext(
    val workspace: Workspace,
    val selectedFiles: List<FileContent>,
    val userPrompt: String?,
    val memoryEntries: List<MemoryEntry>
)

sealed class SkillResult {
    data class Success(val output: String, val artifacts: List<Artifact>) : SkillResult()
    data class Partial(val output: String, val warnings: List<String>) : SkillResult()
    data class Failure(val reason: String, val qualityState: QualityState) : SkillResult()
}
```

---

## Skill-Aktivierung via Commander Surface

Aus Gate B8 (Compose Commander Shell) ergibt sich der Aktivierungs-Flow:

```
CommanderEvent.ActivateSkill(skillId)
  -> CommanderViewModel validiert Privacy + Dependencies
  -> SkillSlotContract.activate()
  -> SkillSlotContract.execute(context)
  -> CommanderState.activeSkill = running
  -> Result -> RunLog + DiffViewer (bei Code-Output)
```

### UI-Integration in Commander

- **WorkspaceBrowser:** Skill-Katalog als eigener Tab (neben Dateien)
- **QualityBadge:** Skill-Quality neben Knight/Bellows anzeigen
- **RunLog:** Skill-Execution als StepRecord protokollieren

---

## Privacy-Compliance

Aus SAFETY_POLICY.md (B1) und BellowsRouter (B5/B9):

| Privacy-Mode | Erlaubte Skills | LLM-Zugriff |
|--------------|-----------------|--------------|
| `LOCAL_ONLY` | Nur Skills mit `privacy: LOCAL_ONLY` | Nur lokale Provider (Ollama, LM Studio) |
| `CLOUD_ALLOWED` | Alle Skills | Alle Provider via BellowsRouter |

**Regel:** Ein Skill mit `privacy: CLOUD_ALLOWED` in einem `LOCAL_ONLY`-Workspace wird nicht aktiviert. Commander zeigt `QualityState.BLOCKED`.

---

## Abgrenzung zu vollem Modul

| Aspekt | Modul (z.B. Knight) | Skill |
|--------|---------------------|-------|
| Build | Eigenes Gradle-Subprojekt | Kein Build, interpretiert |
| Contract | `ModuleSlotContract` direkt | `SkillSlotContract` (Subset) |
| Groesse | Hunderte LOC, Tests | SKILL.md + skill.json + optionale Tools |
| Lifecycle | Permanent geladen | On-Demand aktiviert |
| Dependencies | Andere Module | Nur Contracts (deklarativ) |

---

## Pattern-Uebernahme aus Quell-Repos

### Von open-design (ADAPT)
- **Skills als Packages:** Ja, uebernehmen. SKILL.md + Metadaten.
- **DESIGN.md als Brand-Contract:** Nicht direkt, aber `skill.json` Metadaten erfuellen denselben Zweck.
- **AMR (Agentic Model Router):** Bereits durch BellowsRouter abgedeckt.

### Von claude-skills (STUDY)
- **stdlib-only Constraint:** Uebernehmen. ANVIL-Skills duerfen keine externen Dependencies haben.
- **579 Python-Scripts:** Nicht uebernehmen. ANVIL ist KMP. Aber: Format-Referenz fuer Tool-Definition.
- **Orchestration-System:** STUDY. Skill-Kombinationen sind ein spaeterer Schritt.

### Von agency-agents-zh (STUDY)
- **SOUL.md (Identity):** Interessant fuer Agent-Personas in Runs. Deferred.
- **DAG-Orchestration von Rollen:** Siehe ORCHESTRATION_PATTERNS.md.
- **20 Departments:** Zu granular fuer ANVIL. 5 Kategorien reichen.

---

## Risiken

| Risiko | Schwere | Mitigation |
|--------|---------|------------|
| Skills werden zu Prompts degradiert | Mittel | Validierung via skill.json Schema |
| Skill-Explosion (zu viele, keine Qualitaet) | Mittel | QualityGuard + Review-Gate |
| Privacy-Verletzung durch Cloud-Skill in LOCAL_ONLY | Hoch | Enforcement in SkillSlotContract.activate() |
| Scope Creep (Skills = Module) | Niedrig | Klare Abgrenzung: kein Build, kein Gradle |

---

## Empfehlung

1. **skill.json + SKILL.md** als Format definieren (eigener Gate, z.B. B-Skills-1)
2. **SkillSlotContract** als Interface in `:core:contracts` anlegen
3. **Erste Skills:** `diff-review`, `prompt-compress`, `test-writer` als Proof-of-Concept
4. **Commander-Integration** nach Gate B8 (bereits UI-ready)
5. **Kein Code-Import** aus den Quell-Repos. Nur Pattern-Uebernahme.
