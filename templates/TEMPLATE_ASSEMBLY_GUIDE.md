# TEMPLATE_ASSEMBLY_GUIDE

Ziel: Schnelle Zusammenstellung eines passenden Template-Umfangs je nach Projektgröße.

Quelle für Copy-Pfade (Basis): `templates/gated-project/`

---

## Preset `minimal`

**Inhalt:** nur Core-Gate-Dateien + minimale CI.

### Konkrete zu kopierende Pfade

Aus `templates/gated-project/`:

- `README.md`
- `instructions.md`
- `knownbugs.md`
- `AGENTS.md`
- `claude.md`
- `handoff/README.md`
- `handoff/GATE_X_HANDOFF_TEMPLATE.md`

Aus eurem CI-Standard (Repository-weit), minimal:

- `.github/workflows/*` (nur Basispipeline: lint/test/build)
- optional: `.github/PULL_REQUEST_TEMPLATE.md`

### Ausschlussliste

- `config/validation-profiles.json`
- `config/runtime-profiles.json`
- `config/provider-catalog.example.json`
- `work/**`
- `artifacts/**`

### Wann upgraden?

Upgrade auf `standard`, wenn mindestens eines zutrifft:

- wiederholte Review-Fehler durch fehlende, einheitliche Validierungsregeln
- mehr als ein Agent/Contributor arbeitet parallel an Gates
- manuelle Gate-Checks kosten regelmäßig > 30 Minuten pro PR

### Erwartete Wartungskosten (kurz)

**Niedrig** — hauptsächlich Pflege von `instructions.md`, `knownbugs.md` und CI-Basics.

---

## Preset `standard`

**Inhalt:** `minimal` + validation-profile Modul.

### Konkrete zu kopierende Pfade

Alle Pfade aus `minimal` plus:

- `config/validation-profiles.json`

Empfohlen ergänzend (falls noch nicht vorhanden):

- CI-Job, der die Validation-Profile gegen Gate-Artefakte/PR-Checks ausführt

### Ausschlussliste

- `config/runtime-profiles.json`
- `config/provider-catalog.example.json`
- `work/**`
- `artifacts/**`

### Wann upgraden?

Upgrade auf `extended`, wenn mindestens eines zutrifft:

- unterschiedliche Laufmodi (interaktiv, batch, unattended) werden aktiv genutzt
- Team braucht reproduzierbare Stage-Outputs über mehrere Fachbereiche hinweg
- Artefakte müssen als Klassen (`raw/validated/rejected/...`) getrennt versioniert werden

### Erwartete Wartungskosten (kurz)

**Mittel** — zusätzlich laufende Pflege der Validierungsregeln und ihrer CI-Integration.

---

## Preset `extended`

**Inhalt:** `standard` + runtime-profile + stage-pipeline + artifact-classes.

### Konkrete zu kopierende Pfade

Alle Pfade aus `standard` plus:

- `config/runtime-profiles.json`
- `config/provider-catalog.example.json`

Zusätzlich als Verzeichnisstruktur anlegen (aus dem Template-Pattern):

- `work/01_setup_prompting/`
- `work/02_research_validation/`
- `work/03_prd_planning/`
- `work/04_ux_ui/`
- `work/05_architecture/`
- `work/06_build/`
- `work/07_quality_security_compliance/`
- `work/08_marketing_gtm/`
- `work/09_analytics_iteration/`
- `work/10_handoff/`
- `artifacts/raw/`
- `artifacts/validated/`
- `artifacts/rejected/`
- `artifacts/gold/`
- `artifacts/diffs/`
- `artifacts/reports/`
- `artifacts/replays/`

### Ausschlussliste

- nichts funktional Relevantes; nur projektfremde Stage-Ordner weglassen
- keine überflüssigen Workflow-Jobs ohne echten Gate-Nutzen übernehmen

### Wann upgraden?

Kein weiteres Preset; stattdessen **modular erweitern**, wenn:

- zusätzliche Compliance-/Audit-Anforderungen dazukommen
- mehrere Produktlinien dieselbe Pipeline wiederverwenden
- Kosten-/Qualitätsmetriken pro Stage verpflichtend werden

### Erwartete Wartungskosten (kurz)

**Mittel bis hoch** — laufende Pflege von Profiles, Pipeline-Definitionen, Artefaktkonventionen und CI-Orchestrierung.

---

## Schnelle Entscheidungsregel

- **`minimal`**: Solo/kleines MVP, kurze Laufzeit, wenig Governance.
- **`standard`**: mehrere Mitwirkende + Bedarf an stabilen, expliziten Qualitätsregeln.
- **`extended`**: kontinuierliche, teamübergreifende Delivery mit formaler Artefakt- und Prozesssteuerung.
