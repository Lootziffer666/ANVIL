# Token Management

**Gate:** A13  
**Status:** Verbindlich

## Zweck

Anvil braucht ein sicheres System, um API-Tokens zu erstellen, speichern,
rotieren und löschen. Kein Token im Code. Kein Token im Git.

## Architektur

```
Token Storage (localStorage / encrypted)
  ↕
Token Manager (CRUD + Validation)
  ↕
Provider System (nutzt Tokens für API-Calls)
```

## Token-Objekt

```json
{
  "token_id": "TOK_001",
  "provider": "nvidia",
  "label": "Nvidia Build API",
  "key_preview": "nvapi-...x4f2",
  "created_at": "2026-05-08T12:00:00Z",
  "last_used": null,
  "status": "active",
  "scopes": ["inference", "build"]
}
```

## Regeln

1. Tokens werden **nur im Browser** gespeichert (localStorage)
2. Nur die letzten 4 Zeichen werden angezeigt (`key_preview`)
3. Export = nur Metadaten, nie der Key selbst
4. Rotation: neuer Key → alter Key wird gelöscht
5. Jeder Token gehört zu genau einem Provider
6. Ohne gültigen Token: Provider-Status = `needs-token`

## UI

- Token-Liste: Label, Provider, Status, Letzte Nutzung
- Erstellen: Provider wählen → Label → Key eingeben
- Löschen: Bestätigung erforderlich
- Kein Copy-to-Clipboard für Token (Sicherheit)
