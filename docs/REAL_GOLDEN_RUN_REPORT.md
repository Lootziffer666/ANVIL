# Real Multi-Repo Golden Run — Report

Ersetzt den vorigen, teilweise fixture-basierten Golden Run (`docs/GOLDEN_RUN_REPORT.md`,
`docs/FABLE_FIX_LEDGER.md`) durch einen realen, belegbaren Multi-Repo-Durchstich über
WIZARD, SHADED, CUE-AGENT und ANVIL. Vollständiger Atom-für-Atom-Verlauf inkl. aller
Kommandos/Ergebnisse: `docs/REAL_GOLDEN_RUN_LEDGER.md`. Dieses Dokument fasst zusammen.

**BARD bleibt in diesem Auftrag Fixture** (explizit erlaubt). **SWIFT** wurde in dieser
Runde nicht angefasst — sein realer Adapter (`SwiftCliAdapter`) wurde bereits in Gate
E-03 (vorige Runde) real gebaut und verifiziert.

## Gates (chronologisch)

| Gate | Repo | Inhalt | Status |
|---|---|---|---|
| 1 (R-01..R-04) | WIZARD | `anvil.wizard.production-assessment/v1` Contract + Mapper + `POST /api/production-assessment` + `GET /api/health`-Capability | ✅ |
| 2 (R-05/R-06) | ANVIL | `WizardHttpAdapter` (real HTTP) + Contract-Registry-Reconciliation | ✅ |
| 3 (R-07..R-11) | SHADED | `shaded.scene-project/v1` Contract + Facade-Erweiterung + `window.SHADED_ORCHESTRATOR` + `tools/orchestrate.js` CLI + Doku | ✅ |
| 4 (R-12) | ANVIL | `ShadedCliAdapter` (real CLI) | ✅ |
| 5 (R-13..R-17) | CUE-AGENT | `cue audio-check` (Gate H, real, key-frei) | ✅ |
| 6 (R-18) | ANVIL | `CueCliAdapter` um `audio-check` erweitert | ✅ |
| 7 (R-19..R-21) | ANVIL | `RealGoldenRunTest` — echte Interop aller drei Adapter gegen echte Sibling-Checkouts | ✅ |
| 8 | ANVIL | Dieser Report | ✅ |

## Geänderte Dateien nach Repository

**WIZARD** (Branch `claude/anvil-golden-run-zzo9la`):
- `src/lib/contracts/productionAssessment.ts` (+Test)
- `src/lib/productionAssessment.ts` (+Test)
- `src/app/api/production-assessment/route.ts` (+Test)
- `src/app/api/health/route.ts` (+Test, erstmals getestet)
- `vitest.config.ts`, `package.json`/`package-lock.json` (vitest als erster Testrunner)

**SHADED** (Branch `claude/anvil-golden-run-zzo9la`):
- `contracts/shaded-scene-project.schema.json`
- `editor/facade.js` (+`loadProject`/`exportProject`/`addActorBundle`/`getRuntimeStatus`/`getDebugSnapshot`), `editor/facade.test.js`
- `editor/app.js` (`window.SHADED_ORCHESTRATOR`)
- `tools/orchestrate.js`, `tools/orchestrate-example-request.json`
- `docs/ORCHESTRATION.md`, `CLAUDE.md`, `package.json`

**CUE-AGENT** (Branch `claude/anvil-golden-run-zzo9la`):
- `src/qa/audio-scenario.schema.json`, `src/qa/audio-check.js`
- `bin/cue.js` (Dispatch + Hilfetext)
- `test/audio-check.smoke.test.js`
- `docs/AUDIO_CHECK.md`

**ANVIL** (Branch `claude/anvil-golden-run-zzo9la`):
- `anvil-kmp/core/externaladapters/.../WizardHttpAdapter.kt` (+Test)
- `anvil-kmp/core/externaladapters/.../ShadedCliAdapter.kt` (+Test)
- `anvil-kmp/core/externaladapters/.../CueCliAdapter.kt` (audio-check-Dispatch, +Tests)
- `anvil-kmp/core/contracts/.../ContractRegistry.kt` (+`shaded.scene-project`, `anvil`-Consumer ergänzt für `anvil.wizard.production-assessment` und `shaded.scene-project`), `ContractRegistryTest.kt`
- `anvil-kmp/surfaces/golden-run/src/jvmTest/kotlin/io/anvil/surfaces/goldenrun/RealGoldenRunTest.kt`, `build.gradle.kts`
- `docs/REAL_GOLDEN_RUN_LEDGER.md`, `docs/REAL_GOLDEN_RUN_REPORT.md` (dieses Dokument)

## Tests ausgeführt (real, mit Befehl + Ergebnis)

- WIZARD: `npx vitest run` → 4 Testdateien, 23/23 grün. `npx tsc --noEmit`, `npx eslint`, `npx next build` — alle clean/grün.
- SHADED: `npm run check` (Syntax + Schema-JSON aller neuen Dateien) grün. `node editor/facade.test.js` → 13/13 PASS (echtes headless Chromium). `node tools/orchestrate.js --project tools/orchestrate-example-request.json --json` → Exit 0, echtes JSON. Zwei echte Fehlerpfade (Exit 2) verifiziert. `node tools/verify-editor.js` → alle 11 bestehenden Checks weiterhin PASS (keine Regression).
- CUE-AGENT: `node --test test/audio-check.smoke.test.js` → 3/3 PASS. `npm test` (volle Suite) → 35 Tests, 33 pass, 0 fail, 2 skip (unverändert vorbestehend). Echter CLI-Lauf `node bin/cue.js audio-check <url> --json` → Exit 0 gegen echten Mock-Server.
- ANVIL: `/opt/gradle/bin/gradle :core:contracts:jvmTest :core:run:jvmTest :surfaces:golden-run:jvmTest :core:externaladapters:test --console=plain` durchgehend `BUILD SUCCESSFUL`. Einzeln aufgeschlüsselt in `docs/REAL_GOLDEN_RUN_LEDGER.md`.

## Reale externe Aufrufe (kein Fixture)

1. **WIZARD:** echter `npm run dev`-Prozess gestartet, echter `GET /api/health` + `POST /api/production-assessment` HTTP-Roundtrip über `WizardHttpAdapter` (Ktor/Java-Engine) gegen die echte, im Repo committete `data/assets.db` (2470 Assets).
2. **SHADED:** echter `node tools/orchestrate.js`-Subprozess über `ShadedCliAdapter`, der intern echtes headless Chromium (Playwright) startet, die echte `editor/index.html` lädt und `window.SHADED_ORCHESTRATOR` real treibt.
3. **CUE-AGENT:** echter `node bin/cue.js audio-check`/`doctor`-Subprozess über `CueCliAdapter`, headless Chromium gegen zwei real lokal servierte Seiten (mit/ohne `window.ANVIL_AUDIO`-Hook).
4. **`RealGoldenRunTest`** verkettet 1–3 in einem einzigen JVM-Testlauf, jeweils inkl. echtem Negativfall (unerreichbarer Endpunkt / fehlende Datei / fehlender Audio-Hook).

## Produzierte Artefakte (mit Prüfsummen/Parent-Refs, wo zutreffend)

- `RunSurface`-Artefakte im bestehenden `GoldenRunTest` tragen weiterhin `sha256:`-Prüfsummen (unverändert, fixture-basiert, s. `docs/GOLDEN_RUN_REPORT.md`).
- Die neuen realen Artefakte in diesem Auftrag sind **Protokoll-Nachweise, keine Content-Artefakte**: JSON-Antworten (WIZARD `anvil.wizard.production-assessment/v1`, SHADED `shaded.scene-project/v1` Debug-Snapshot, CUE `cue.audio-proof`-Report) — vollständig im Ledger dokumentiert, nicht separat gehasht (kein Artifact-Store-Write in diesem Auftrag, das bleibt Aufgabe eines künftigen End-to-End-Content-Runs, s. "Remaining risk" in R-19).

## Real Golden Run — Status

**Alle drei zuvor fixture-blockierten externen Systeme (WIZARD, SHADED, CUE) sind jetzt
real ansteuerbar und wurden real (positiv UND negativ) verifiziert.** Der neue
`RealGoldenRunTest` beweist echte Interop; der bestehende, fixture-basierte
`GoldenRunTest` bleibt unverändert grün (keine Regression an der internen
RunSurface-Kette).

**Was NICHT bewiesen ist:** eine inhaltlich verkettete End-to-End-Pipeline (WIZARDs
Production Assessment → eine daraus abgeleitete SHADED-Szene → CUEs Audio-Check GENAU
dieser Szene). Die drei Systeme wurden unabhängig voneinander real bewiesen, nicht als
eine zusammenhängende Datenkette. Das ist eine ehrliche Grenze dieses Auftrags, keine
verschwiegene Lücke.

## Blocked

Keine `BLOCKED_EXTERNAL_CONTRACT`-Fälle in dieser Runde — alle drei geplanten externen
Verträge (WIZARD-HTTP, SHADED-CLI, CUE-audio-check) existierten am Ende real und wurden
real erreicht.

## Intentionally deferred

- **BARD:** bleibt Fixture (expliziter Auftrag).
- **CLIPPING_CHECK / VOICE_AUDIBILITY** (`cue audio-check`): mit dem aktuellen
  `window.ANVIL_AUDIO`-Vertrag technisch nicht prüfbar (kein Pegel-/Analyser-Hook) —
  ehrlich als `ok: null` ausgewiesen, nie fingiert. Erfordert eine künftige Erweiterung
  von `ToneJsRuntimeWriter` (ANVIL-seitig).
- **Echter, deployter ANVIL-Web-Audio-Build:** existiert noch nicht (`WebTargetWriter`
  liefert nur `ASSEMBLED`, nie `RUNNABLE`) — `audio-check` wurde nur gegen handgeschriebene,
  vertragsgetreue Mock-Seiten real verifiziert.
- **Inhaltlich verkettete End-to-End-Pipeline** (s. o.) — nächste Runde.
- **SWIFT:** nicht Teil dieser Runde (bereits in Gate E-03 real gebaut).

Keine unbewiesene Erfolgsprosa: jede Behauptung oben ist im Ledger mit Kommando +
tatsächlichem Terminal-Output belegt.
