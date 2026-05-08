# MODULE_CONTRACT.md

**Gate:** A5 — Module Slot Contract  
**Stand:** 2026-05-08

---

## Zweck

Jedes Modul, das in Anvil eingebunden wird, muss diesem Contract folgen. Keine Ausnahmen. Module wuchern nicht frei — sie docken nach exakt demselben Muster an.

## Modulvertrag

Jedes Modul muss folgende Felder definieren:

```json
{
  "name": "string — Anzeigename",
  "purpose": "string — Was macht dieses Modul? Ein Satz.",
  "inputs": ["string[] — Akzeptierte MIME-artige Typen"],
  "outputs": ["string[] — Produzierte MIME-artige Typen"],
  "requiredPermissions": ["string[] — Benötigte System-Rechte"],
  "canRunOffline": "boolean — Funktioniert ohne Netzwerk?",
  "canExport": "boolean — Kann Artifacts exportieren?",
  "failureBehavior": "string — Was passiert bei Fehler? Konkret."
}
```

## Felder im Detail

| Feld | Pflicht | Beschreibung |
|------|---------|-------------|
| `name` | ✅ | Eindeutiger, lesbarer Name |
| `purpose` | ✅ | Ein Satz: was dieses Modul tut |
| `inputs` | ✅ | Welche Datentypen akzeptiert es |
| `outputs` | ✅ | Welche Datentypen produziert es |
| `requiredPermissions` | ✅ | Welche Rechte braucht es (filesystem, network, etc.) |
| `canRunOffline` | ✅ | `true` oder `false` — keine Ausnahmen |
| `canExport` | ✅ | Kann das Modul sein Ergebnis als Artifact exportieren? |
| `failureBehavior` | ✅ | Konkreter Satz: was passiert bei Fehler |

## Erlaubte Permissions

```text
filesystem.read      — Dateien lesen
filesystem.write     — Dateien schreiben
network.api          — Externe APIs aufrufen
network.build-server — Build-Server kontaktieren
camera               — Kamerazugriff
clipboard            — Zwischenablage
```

Neue Permissions müssen hier dokumentiert werden, bevor ein Modul sie nutzen darf.

## Modulverzeichnis-Struktur

```text
modules/{module-id}/
├── module.json       — Contract-Datei (Pflichtfelder oben)
├── README.md         — Dokumentation
└── src/              — Quellcode (Struktur modulspezifisch)
```

## Status-Integration

Module tragen ihren eigenen Status:

| Status | Bedeutung |
|--------|-----------|
| `stable` | Modul funktioniert, kein Handlungsbedarf |
| `adapting` | Modul wird aktualisiert oder verarbeitet |
| `act-now` | Modul braucht Konfiguration oder Nutzer-Input |
| `failed` | Modul hat Fehler, kann nicht arbeiten |

## Regeln

1. Ein Modul füllt genau *einen* Module Slot.
2. Module kennen sich nicht gegenseitig — Kommunikation läuft über den Workspace.
3. Kein Modul darf den Workspace-Status direkt ändern.
4. `failureBehavior` muss konkret sein, nicht „gibt einen Fehler zurück".
5. Module ohne `module.json` werden von The Forge ignoriert.
