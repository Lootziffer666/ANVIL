# Import-Plan: Template-System (ANVIL)

## PAINKILLER

### Übernehmen (Template-Core)
- `templates/gated-project/instructions.md` (Gate-Ablauf als Kernsteuerung)
- `templates/gated-project/README.md` (Zweck, Einstieg, Build-/Run-Pfad)
- `templates/gated-project/claude.md` (Arbeitsregeln/Operator-Disziplin)
- `templates/gated-project/knownbugs.md` (permanentes Risk/Bug-Register)
- `templates/gated-project/handoff/GATE_X_HANDOFF_TEMPLATE.md` (standardisierte Übergaben)
- `templates/gated-project/handoff/README.md` (Handoff-Nutzung)
- `templates/gated-project/AGENTS.md` (PR-/Gate-/Arbeitsdisziplin inkl. Handoff/knownbugs)

### Übernehmen (Optionales Modul)
- Kein separates Pflichtmodul: PAINKILLER-Inhalte sind bereits als Core-Gate-Dateien in `templates/gated-project/*` verankert.

### Nicht übernehmen
- Produktspezifische GitHub-Upload-Logik
- PAINKILLER-Branding bzw. CATALON-GUARD-Farbtokens als globaler Default
- Git Data API-Implementierungsdetails ohne echten GitHub-Write-Bedarf
- Upload-/Security-Annahmen für Apps ohne Upload-Use-Case

### Begründung (Friction vs. Value)
Hoher Value, weil Gate-Führung, Handoff-Standard und Knownbugs-Disziplin direkt die sichere Ausführung verbessern. Geringe Friction, da vorhandene Template-Dateien 1:1 als Kernstruktur genutzt werden können.

---

## DEAFPIPER

### Übernehmen (Template-Core)
- Kein Core-Import in den Default-Templatekern.

### Übernehmen (Optionales Modul)
- Stage-Modul **nur** als Modulvorlage unter `templates/gated-project/modules/stage-work/` mit
  - `templates/gated-project/modules/stage-work/work/01`
  - `templates/gated-project/modules/stage-work/work/02`
  - `templates/gated-project/modules/stage-work/work/03`
  - `templates/gated-project/modules/stage-work/work/04`
  - `templates/gated-project/modules/stage-work/work/05`
  - `templates/gated-project/modules/stage-work/work/06`
  - `templates/gated-project/modules/stage-work/work/07`
  - `templates/gated-project/modules/stage-work/work/08`
  - `templates/gated-project/modules/stage-work/work/09`
  - `templates/gated-project/modules/stage-work/work/10`

### Nicht übernehmen
- Alle 10 Stages standardmäßig in kleine Implementierungs-Apps erzwingen
- Übermäßige JSON-Handoff-Bürokratie für kleine Tools
- CustomGPT-Profile als Pflicht-Setup
- Marketing-/Legal-Stages vor echtem Produktbedarf

### Begründung (Friction vs. Value)
Mittlerer Value als skalierbare Vorlage für größere Vorhaben, aber hohe Friction als Default. Deshalb nur opt-in Modul statt Bestandteil des Core-Templates.

---

## OPENDORK

### Übernehmen (Template-Core)
- Kein verpflichtender Core-Import.

### Übernehmen (Optionales Modul)
- `templates/gated-project/config/runtime-profiles.json`
- `templates/gated-project/config/validation-profiles.json`
- `templates/gated-project/config/provider-catalog.example.json`

### Nicht übernehmen
- Provider-Routing in jede App
- Spend-Logging in Non-AI-Apps
- Modellkatalog ohne Provider-Switching-Bedarf
- Aggressive unattended Runtime-Profile für write-fähige Tools

### Begründung (Friction vs. Value)
Hoher Value für AI-/Provider-sensitive Projekte, aber unnötige Komplexität für einfache oder nicht-AI-orientierte Anwendungen. Daher optional und kontextabhängig.

---

## CATALON-GUARD

### Übernehmen (Template-Core)
- Kein direktes Dateiset.

### Übernehmen (Optionales Modul)
- Kein Dateimodul; Nutzung nur als Qualitätskriterium/Negativregel in Review und Dokumentationsprüfung.

### Nicht übernehmen
- Vage README-only-Statusdokumentation
- Generisches Automation-Test-Framing ohne produktspezifische Gates
- Unklare Beziehung zwischen Zweck, aktuellem Status und ausführbaren Build-Kommandos

### Begründung (Friction vs. Value)
Als Negativfilter hoher Value (verhindert unklare, nicht-exekutierbare Templates) bei minimaler Friction, weil keine zusätzlichen Artefakte eingeführt werden.
