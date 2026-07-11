# Workspace Sync Protocol

**Gate:** A19
**Status:** Active; Gate B19/A19 hat jetzt einen KMP-Prototyp über Artifact-/Run-Registry.
**Dateien:** `app/sync.js`

## Zweck

Anvil-Workspaces müssen zwischen Android und Windows synchronisiert werden.
Kein Cloud-Zwang — alles lokal oder per Self-Hosted Git.

## Sync-Bundle

Ein Sync-Bundle ist eine JSON-Datei, die den gesamten Workspace-State enthält:

```json
{
  "_anvil_sync": true,
  "version": 1,
  "exported_at": "2026-05-08T12:00:00Z",
  "exported_from": "android",
  "device_id": "ANVIL_ANDROID_abc123",
  "custom_providers": [...],
  "tokens_meta": [...],  // NUR Metadaten, KEINE Keys!
  "settings": {...},
  "outputs_manifest": [...]
}
```

## Sicherheitsregeln

| Was | Exportiert? |
|-----|-------------|
| Provider-Konfiguration | ✅ Ja |
| Custom Providers | ✅ Ja |
| Token Labels/Provider | ✅ Ja (Metadaten) |
| Token Keys/Secrets | ❌ NIEMALS |
| Einstellungen | ✅ Ja |
| Output-Manifeste | ✅ Ja |
| Output-Dateien | ❌ Nein (zu groß) |

→ Token-Keys müssen auf jedem Gerät neu eingegeben werden.

## Sync-Methoden

### 1. Datei-Transfer (einfach)
```
Android: Export → .json → USB/Bluetooth/WiFi-Direct → Windows: Import
```

### 2. Git-Based (empfohlen)
```
Android: Export → .anvil-sync.json → git push
Windows: git pull → Import
```

Empfohlener Self-Hosted Git: [go-gitea/gitea](https://github.com/go-gitea/gitea)

### 3. Lokales Netzwerk (geplant)
```
Android + Windows im selben WiFi → mDNS Discovery → HTTP Sync
```

## Merge-Strategie

- **Custom Providers:** Union (beide Seiten behalten)
- **Tokens:** Metadaten mergen, Keys bleiben lokal
- **Settings:** Neueres Datum gewinnt
- **Outputs:** Manifest-Union, Dateien bleiben lokal

## UI-Flow

1. *Export:* Settings → Sync → Export Bundle → Download .json
2. *Import:* Settings → Sync → Import → Datei wählen → Report
3. *Status:* Sync-Zone zeigt letzten Export/Import + Device-ID

## Starred Repos (Referenz)

| Repo | Rolle |
|------|-------|
| [go-gitea/gitea](https://github.com/go-gitea/gitea) | Self-Hosted Git für Sync |
| [tw93/Pake](https://github.com/tw93/Pake) | Desktop Shell (A18) |
| [homarr-labs/homarr](https://github.com/homarr-labs/homarr) | Dashboard als Sync-Hub |


---

## Gate B19/A19 — Workspace Sync über Artifact-/Run-Registry

Der aktuelle MVP lebt in `anvil-kmp/core/sync` und macht Sync-Bundles zu
serialisierbaren KMP-Verträgen statt losen UI-JSONs:

- `WorkspaceSyncExportRequest/v1` beschreibt Bundle, Workspace, Export-Run,
  Device und optionale Run-Refs.
- `WorkspaceSyncBundle/v1` enthält ausschließlich Artifact-Manifeste und
  Run-Zusammenfassungen, keine Artifact-Payloads und keine Secrets.
- `WorkspaceSyncService.exportArtifact(...)` schreibt das Sync-Bundle über
  `ArtifactWriter` als eigenes Artifact zurück.
- `WorkspaceSyncService.merge(...)` importiert nur unbekannte Artifact-Manifeste
  desselben Workspace und erzeugt einen `WorkspaceSyncMergeReport/v1`.

### Zusätzliche Regeln

1. Sync-Bundles dürfen keine Artifact-Payloads enthalten.
2. Cross-Workspace-Manifeste werden beim Export ausgeschlossen und beim Import
   blockiert.
3. Doppelte Artifact-IDs werden nicht überschrieben, sondern im Merge-Report als
   übersprungen markiert.
4. Prüfsummen müssen `sha256:`-präfigiert bleiben.
5. Token-Secrets bleiben weiterhin außerhalb des Bundles; nur Bellows Vault darf
   Secrets halten.
