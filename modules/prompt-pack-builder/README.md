# Prompt Pack Builder

**Erstes echtes Modul** — erzeugt sofort Nutzen.

## Was es tut

1. Nimmt Eingaben entgegen:
   - Projektname
   - Ziel
   - Constraints
   - Nächste Gate
   - Agent-Zielsystem

2. Erzeugt:
   - Markdown Prompt Pack (direkt übergebbar)
   - Optional: JSON Manifest

## Kein LLM nötig. Keine externe API nötig.

Das Modul ist ein *Template-Renderer*, kein KI-System.
Es nimmt strukturierte Eingaben und erzeugt strukturierte Ausgaben.

## Nutzung

```javascript
const pack = buildPromptPack({
  project: "Mein Projekt",
  goal: "Feature X bauen",
  constraints: ["Kein Scope Creep", "Tests Pflicht"],
  nextGate: "Gate 5: Integration",
  agentTarget: "viktor",
  contextFiles: ["README.md", "docs/SPEC.md"]
});
// pack.markdown → Markdown-String
// pack.manifest → JSON-Objekt
```
