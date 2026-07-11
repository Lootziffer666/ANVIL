# Agent Handoff Format

**Gate:** A8 — Blueprint Export: Agent-Ready Prompt Packs
**Stand:** 2026-05-08
**Status:** Verbindlich; Gate B18/A8 hat jetzt einen KMP-Prototyp über den Artifact Layer.

---

## Zweck

Anvil soll Arbeit an Viktor/Claude/Codex sauber übergeben können.
Prompt Packs enthalten alles, was ein Agent braucht, um eine Aufgabe
ohne Rückfragen zu starten.

## Format: Agent Prompt Pack

Jedes Pack ist ein Markdown-Dokument mit folgender Struktur:

```markdown
# Agent Prompt Pack: {Projektname}

## Ziel
Was soll erreicht werden?

## Ist-Zustand
Was existiert bereits? Repo-Stand, Branch, letzte Gate.

## Nächste Gates
Welche Gates stehen an? Definition of Done pro Gate.

## Harte Constraints
Was darf NICHT passieren? Rote Linien.

## Definition of Done
Wann ist die Aufgabe erledigt?

## Kill-Kriterien
Wann abbrechen?

## Agent-Zielsystem
Viktor / Claude / Codex / Manuell

## Kontext-Dateien
Welche Dateien muss der Agent kennen?
```

## Optionales JSON-Manifest

```json
{
  "pack_type": "agent-handoff",
  "version": 1,
  "project": "...",
  "agent_target": "viktor",
  "gates": [...],
  "constraints": [...],
  "kill_criteria": [...]
}
```

## Regeln

1. Ein Pack pro Übergabe
2. Pack muss direkt an Agent-System kopierbar sein
3. Keine impliziten Annahmen — alles explizit
4. Kontext-Dateien werden referenziert, nicht eingebettet
5. Kill-Kriterien sind Pflicht


---

## Gate B18/A8 — Handoff Export über Artifact Layer

Der funktionale MVP lebt in `anvil-kmp/core/handoff` und macht A8 nicht mehr zu
freiem Markdown-Export, sondern zu einem artifact-backed Contract:

- `HandoffExportRequest/v1` beschreibt Ziel, Agent-Zielsystem, Gates,
  Constraints, Definition of Done, Kill-Kriterien und referenzierte Artifacts.
- `HandoffPackage/v1` ist das erzeugte Prompt Pack; es kann als Markdown oder
  JSON gerendert werden.
- `HandoffExporter` validiert Pflichtfelder, löst Artifact-Refs ausschließlich
  gegen eine `ArtifactRegistry` auf und schreibt das Paket wieder über
  `ArtifactWriter` zurück.

### Zusätzliche Regeln

1. Kein Handoff ohne mindestens ein Artifact aus der Registry.
2. Fehlende oder unbekannte Artifact-Refs blockieren den Export.
3. Kill-Kriterien bleiben Pflicht.
4. Kontext-Dateien werden weiter referenziert, nicht eingebettet.
5. Das Handoff-Paket selbst ist ein normales Artifact mit Manifest, Run,
   Workspace, Ursprung und Prüfsumme.
