# BARD Externalization Handoff

**Status:** Korrektur nach falschem Lösch-Only-Schritt
**Source snapshot:** `3385012` — letzter Commit vor der Entfernung von `anvil-kmp/modules/bard`

## Was schief lief

BARD sollte in ein eigenes privates Repo ausgelagert werden. Der erste Schritt hat
BARD aus dem aktiven ANVIL-Build entfernt, aber keinen expliziten Export-/Handoff-
Pfad für das private Repo angelegt. Das war zu grob: Auslagern ist nicht dasselbe
wie kommentarlos löschen.

## Korrektur

Das BARD-Modul bleibt aus dem aktiven ANVIL-Build entfernt, aber der Code ist über
Git-History rekonstruierbar und es gibt jetzt ein explizites Export-Script:

```bash
scripts/export-bard-private-repo.sh /path/to/private/anvil-bard
```

Das Script exportiert den Snapshot aus:

```text
3385012:anvil-kmp/modules/bard
```

in das Zielverzeichnis und legt dort `EXTERNALIZED_FROM_ANVIL.md` als Herkunftsmarker
an.

## Zielzustand

- ANVIL enthält keinen aktiven BARD-Code und kein `bard-profile`.
- Das private BARD-Repo enthält Implementierung, Profile, Beispiele und Regressionen.
- ANVIL konsumiert nur:
  - `anvil.bard.creative-seed/v1`
  - `anvil.bard.creative-brief/v1`
  - `anvil.bard.production-intent/v1`
  - Artifact-Refs und Contract-Registry-Einträge.

## Nach dem Export im privaten Repo

1. Eigenes Git-Repo initialisieren oder vorhandenes privates Repo nutzen.
2. Exportierten Inhalt committen.
3. Build-Konfiguration an das private Repo anpassen.
4. Private `bard-profile`-Daten nur dort weiterentwickeln.
5. ANVIL nur über versionierte Contract-Artefakte anbinden.
