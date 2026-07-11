# Artifact Output Layer

**Gate:** A7 — Artifact Output Layer
**Stand:** 2026-05-08
**Status:** Prototype — KMP Manifest-/Registry-MVP in `anvil-kmp/core/artifacts` begonnen

---

## Zweck

Alles, was Anvil erzeugt, muss auffindbar und wiederverwendbar sein.
Keine anonymen Exporte. Jeder Output bekommt eine ID, einen Zeitstempel,
einen Ursprung und einen Typ.

## Output-Typen

| Typ | Extension | MIME-artig | Beschreibung |
|-----|-----------|-----------|-------------|
| Markdown | `.md` | `text/markdown` | Dokumente, Berichte, Prompts |
| JSON | `.json` | `application/json` | Manifeste, Konfigurationen |
| ZIP | `.zip` | `application/zip` | Export-Pakete |
| Patch | `.patch` | `text/x-diff` | Code-Änderungen |
| Prompt | `.prompt.md` | `text/markdown` | Agent-Prompts |
| Config | `.config.json` | `application/json` | Modul-Konfigurationen |

## Verzeichnisstruktur

```
outputs/
├── {output_id}/
│   ├── manifest.json     # Metadaten
│   └── {dateiname}.{ext} # Eigentlicher Output
├── latest/               # Symlink zum letzten Output
└── registry.json         # Index aller Outputs
```

## Output-Manifest

Jeder Output hat ein `manifest.json`:

```json
{
  "output_id": "OUT_20260508_143000_001",
  "created_at": "2026-05-08T14:30:00Z",
  "origin": {
    "module": "prompt-pack-builder",
    "workspace": "Android Text Tool",
    "run_id": "RUN_20260508_143000"
  },
  "type": "text/markdown",
  "filename": "prompt-pack.md",
  "size_bytes": 2048,
  "checksum_sha256": "abc123..."
}
```

## Naming-Schema

`OUT_{YYYYMMDD}_{HHMMSS}_{NNN}` — Sortierbar, eindeutig, lesbar.

## Registry

`outputs/registry.json` — Array aller Output-Manifeste.
Wird bei jedem neuen Output aktualisiert.

## Regeln

1. Kein Output ohne Manifest
2. Kein Output ohne `output_id`
3. Kein Output ohne `origin.module`
4. Kein Output ohne Zeitstempel
5. Kein Output ohne Dateityp


## KMP-MVP (Gate B16 / A7-Fortsetzung)

Der KMP-Pfad ergänzt den historischen Output-Layer um erste serialisierbare
Contracts:

- `ArtifactManifest/v1` — ID, Ursprung, Run, Typ, URI, Größe, SHA-256 und Parent-Refs.
- `ArtifactRegistry/v1` — geordnete Liste registrierter Artifact-Manifeste.
- `ArtifactWriteRequest` — verbindet `ModuleArtifactRef` + Payload + Timestamp.
- `ArtifactWriter` — validiert Pflichtfelder und erzeugt `ArtifactEnvelope` + aktualisierte Registry.

Noch nicht enthalten: plattformspezifisches Dateisystem-Schreiben, `latest/`-Symlink
und Persistenz in `outputs/registry.json`. Diese Schritte gehören in den nächsten
Run-/Workspace-Slice.
