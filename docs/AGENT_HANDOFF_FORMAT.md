# Agent Handoff Format

**Gate:** A8 — Blueprint Export: Agent-Ready Prompt Packs
**Stand:** 2026-05-08
**Status:** Verbindlich

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
