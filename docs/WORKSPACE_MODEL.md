# WORKSPACE_MODEL.md

**Gate:** A4 — Workspace Model  
**Stand:** 2026-05-08

---

## Zweck

Ein Workspace ist die zentrale Arbeitseinheit in Anvil. Er beschreibt, *was* gebaut wird, *womit* (Module), und *wohin* (Build Target).

## Datenstruktur

```json
{
  "name": "string — Projektname",
  "description": "string — Kurzbeschreibung, was gebaut wird",
  "modules": ["string[] — IDs der eingebundenen Module"],
  "inputs": [
    {
      "name": "string — Bezeichnung des Inputs",
      "type": "string — MIME-artiger Typ, z.B. text/markdown, file/*"
    }
  ],
  "outputs": [
    {
      "name": "string — Bezeichnung des Outputs",
      "type": "string — MIME-artiger Typ, z.B. application/pdf"
    }
  ],
  "buildTarget": "string — Zielformat: android-apk | desktop-exe | export-json | export-pdf",
  "status": "string — stable | adapting | act-now | failed"
}
```

## Felder im Detail

| Feld | Pflicht | Beschreibung |
|------|---------|-------------|
| `name` | ✅ | Eindeutiger Workspace-Name |
| `description` | ✅ | Was wird hier gebaut? Eine Zeile. |
| `modules` | ✅ | Liste von Modul-IDs (müssen im MODULE_CONTRACT registriert sein) |
| `inputs` | ❌ | Was fließt rein. Kann leer sein bei reinen Generator-Workspaces. |
| `outputs` | ❌ | Was kommt raus. Wird vom Build Target und den Modulen bestimmt. |
| `buildTarget` | ✅ | Wohin wird gebaut. Bestimmt den Export-Pfad. |
| `status` | ✅ | Aktueller Zustand nach State Surface Grammar. |

## Zustandsübergänge

```text
empty → stable       Workspace angelegt, alle Module grün
stable → adapting    Build gestartet oder Modul aktualisiert
adapting → stable    Build erfolgreich
adapting → act-now   Build braucht Nutzer-Input
adapting → failed    Build fehlgeschlagen
failed → adapting    Nutzer hat Fehler behoben, Retry
act-now → adapting   Nutzer hat Input geliefert
```

## Persistenz

Aktuell: `localStorage` im Browser (Prototyp-Phase).  
Zukünftig: JSON-Dateien im Workspace-Verzeichnis (`workspace.json`).

## Beispiel

Siehe `examples/workspace.sample.json`.
