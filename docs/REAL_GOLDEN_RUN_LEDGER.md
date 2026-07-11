# Real Golden Run — Atom Ledger

Diese Datei protokolliert den Fix-Auftrag "Real Multi-Repo Golden Run"
(Ersatz des fixture-basierten Golden Run aus dem vorigen Auftrag durch einen
realen, belegbaren Multi-Repo-Durchstich über WIZARD, SHADED, CUE-AGENT und
ANVIL — BARD bleibt in diesem Auftrag Fixture).

Jeder Atom wird **vor** der Umsetzung mit den ersten 6 Feldern eröffnet
(Problem/Owner/Contract/Smallest safe change/Command sind Plan, keine
Behauptung) und **nach** Ausführung mit Result/Evidence/Remaining risk
geschlossen. Kein Atom gilt als abgeschlossen, ohne dass sein Test-Kommando
tatsächlich gelaufen ist und das Ergebnis hier mit echtem Output belegt ist.

Reihenfolge folgt der im Fix-Prompt vorgegebenen Empfehlung:
WIZARD Contract+Route → ANVIL WizardHttpAdapter → SHADED Scene Contract+CLI →
ANVIL ShadedCliAdapter → CUE audio-check → ANVIL CueCliAdapter erweitern →
RealGoldenRunTest → Report.

---

## R-00 — WIZARD: Test-Runner-Fundament (vitest)

- **Repo:** WIZARD
- **Status:** DONE
- **Files:** `package.json` (`devDependencies.vitest`, `scripts.test`), `vitest.config.ts`
- **Contract:** keiner (reine Test-Infrastruktur)
- **Owner:** WIZARD
- **Problem:** WIZARD hatte keinerlei Testrunner (kein jest/vitest, keine
  `*.test.ts`-Datei existierte). Jeder Atom in diesem Auftrag verlangt aber
  eine echte, laufende Testdatei — ohne Runner ist das nicht erfüllbar.
- **Smallest safe change:** `vitest` als einzige neue devDependency, minimale
  `vitest.config.ts` (spiegelt den bestehenden `@/*`-Pfad-Alias aus
  `tsconfig.json`), `"test": "vitest run"` in `package.json`. Keine
  Umstrukturierung, kein Ersatz für `tsc`/`eslint`.
- **Command:** `npx vitest run` (Smoke-Test mit trivialem `describe/it` vorab verifiziert)
- **Result:** PASS — `Test Files 1 passed (1)`, `Tests 1 passed (1)`.
- **Evidence:** manueller Lauf im Sandbox-Terminal, Output oben.
- **Remaining risk:** Keiner der bestehenden Build-Scripts (`build`, `lint`,
  `typecheck`) wurde verändert; `vitest` ist rein additiv.

---

## R-01 — WIZARD: `productionAssessment` Contract-Typen

- **Repo:** WIZARD
- **Status:** DONE
- **Files:** `src/lib/contracts/productionAssessment.ts`, `src/lib/contracts/productionAssessment.test.ts`
- **Contract:** `anvil.wizard.production-assessment/v1` (neu, Owner WIZARD)
- **Owner:** WIZARD (Produktions-Auswahl-Wahrheit)
- **Problem:** Es gibt noch keinen versionierten, exportierten Vertragstyp für
  das, was WIZARD nach außen (an ANVIL) über eine Produktionsidee aussagt.
  `BriefResult` (`src/lib/brief.ts`) ist die interne Wahrheit, aber kein
  stabiler externer Vertrag (keine Versionsnummer, keine Abgrenzung
  interner/optionaler Felder).
- **Smallest safe change:** Neues Modul mit reinen Typdeklarationen +
  Konstante `PRODUCTION_ASSESSMENT_CONTRACT_ID` + zwei Laufzeit-Prüfungen
  (`isProductionAssessmentRequest` für eingehende HTTP-Bodies,
  `isProductionAssessment` für Konsumenten-Tests), ohne bestehenden Code zu
  verändern. `substitutions`/`provenanceWarnings` als eigene Vertragsfelder
  ergänzt (Vorgabe des Fix-Prompts), gespeist aus bereits vorhandenen
  `broad`/`proven`/`warnings`-Daten aus `brief.ts` — keine zweite
  Auswahl-Logik.
- **Command:** `npx vitest run src/lib/contracts/productionAssessment.test.ts`
- **Result:** PASS — `Test Files 1 passed (1)`, `Tests 12 passed (12)`. `npx tsc --noEmit` zusätzlich clean.
- **Evidence:** manueller Lauf im Sandbox-Terminal, Output oben (12/12 grün, keine tsc-Fehler).
- **Remaining risk:** `substitutions`-Feld ist im WIZARD-internen `BriefResult`
  noch nicht befüllt (folgt in R-02, Mapper-Atom) — dieser Atom liefert nur
  die Typform, keine Daten.

---

## R-02 — WIZARD: `productionAssessment` Mapper

- **Repo:** WIZARD
- **Status:** DONE
- **Files:** `src/lib/productionAssessment.ts`, `src/lib/productionAssessment.test.ts`
- **Contract:** `anvil.wizard.production-assessment/v1`
- **Owner:** WIZARD
- **Problem:** `buildProductionBrief()` (brief.ts) ist die interne Wahrheit,
  liefert aber `BriefAsset`/`BriefResult` — nicht die stabile, versionierte
  Außenform aus R-01. `substitutions`/`provenanceWarnings` existieren nirgends.
- **Smallest safe change:** `buildProductionAssessment(request)` ruft
  ausschließlich `buildProductionBrief()` auf und formt das Ergebnis um.
  `provenanceWarnings` = dedupliziert über alle bereits geladenen Assets
  (`broad` + `starterKit`), gefiltert auf `source === "unknown"` oder
  vorhandene `warnings`. `substitutions` = für jede fehlende Rolle die ersten
  2 Treffer aus dem bereits berechneten `broad`-Array (kein neuer Suchlauf,
  keine zweite Auswahl-Logik). `capabilityCast`/`surfacePass`/
  `characterPipeline` werden 1:1 durchgereicht.
- **Command:** `npx vitest run src/lib/productionAssessment.test.ts`
- **Result:** PASS — `Test Files 1 passed (1)`, `Tests 6 passed (6)`, Laufzeit 646 ms.
  `npx tsc --noEmit` clean.
- **Evidence:** Test läuft gegen die echte, im Repo committete `data/assets.db`
  (2470 Assets, lokale FTS+Trigramm-Suche, kein Netzwerk, kein Key) — kein
  Mock. Sechs echte Briefs (Wüstenstadt-Koop, Fachwerkdorf, Raumschiff/Technik,
  MetaHuman-Dorf, Koop-Rennspiel, Ruinen-Wald) wurden gegen den echten Katalog
  gerechnet.
- **Remaining risk:** `substitutions`-Heuristik ("erste 2 `broad`-Treffer,
  rollenunabhängig") ist bewusst simpel — falls Konsumenten rollen-spezifische
  Substitution brauchen, muss das in einer späteren Version (`/v2`)
  nachgezogen werden, nicht rückwirkend in `/v1` gebogen werden.

---

## R-03 — WIZARD: `POST /api/production-assessment`

- **Repo:** WIZARD
- **Status:** DONE
- **Files:** `src/app/api/production-assessment/route.ts`, `src/app/api/production-assessment/route.test.ts`
- **Contract:** `anvil.wizard.production-assessment/v1`
- **Owner:** WIZARD
- **Problem:** Der Vertrag (R-01) und der Mapper (R-02) existierten, aber
  ANVIL kann sie nicht real erreichen — es gab keine HTTP-Route, über die
  ein externer Client (ANVILs `WizardHttpAdapter`, R-05) den Vertrag abrufen
  kann.
- **Smallest safe change:** Ein neuer Next.js Route Handler, der Body-JSON
  parst, mit `isProductionAssessmentRequest` validiert (400 bei ungültigem
  JSON oder Schema-Verstoß), sonst `buildProductionAssessment()` aufruft und
  1:1 als JSON zurückgibt (500 bei unerwartetem Fehler). Folgt demselben
  Try/Catch+NextResponse-Muster wie die bestehenden `/api/health`- und
  `/api/assets`-Routen — kein neues Fehler-Format erfunden.
- **Command:** `npx vitest run src/app/api/production-assessment/route.test.ts`
- **Result:** PASS — `Test Files 1 passed (1)`, `Tests 4 passed (4)` (200 mit
  echtem Assessment, 400 bei fehlendem brief, 400 bei maxPerRole=999, 400 bei
  kaputtem JSON). `npx tsc --noEmit` clean.
- **Evidence:** Test ruft den echten Route-Handler direkt mit einer echten
  `NextRequest` auf (kein gemockter Server) — durchläuft echten JSON-Parse,
  echte Validierung, echten Aufruf gegen die reale `data/assets.db`.
- **Remaining risk:** Keine Rate-Begrenzung/Auth auf dieser Route (wie bei den
  bestehenden `/api/*`-Routen auch nicht) — außerhalb des Scopes dieses Fixes.

---

## R-04 — WIZARD: `GET /api/health` um Capability + Contract erweitert

- **Repo:** WIZARD
- **Status:** DONE
- **Files:** `src/app/api/health/route.ts`, `src/app/api/health/route.test.ts`
- **Contract:** `anvil.wizard.production-assessment/v1` (nur referenziert, nicht neu definiert)
- **Owner:** WIZARD
- **Problem:** ANVILs `WizardHttpAdapter` (R-05) braucht einen Weg, um vor dem
  ersten echten Aufruf zu prüfen, ob WIZARD den `production-assessment`-Vertrag
  überhaupt unterstützt (Vermeidung stiller Fixture-Fallbacks — Health-Check
  ist der Ort, an dem "kann ich das echt?" beantwortet wird). Die bestehende
  Health-Route kannte nur DB-Stats, keine Capability-/Contract-Liste.
  Es gab zudem keinen Test für diese Route (erster Test-Atom für eine
  bestehende Datei in diesem Auftrag).
- **Smallest safe change:** Zwei zusätzliche Felder im bestehenden JSON-Body
  (`capabilities.productionAssessment: true`, `contracts: [PRODUCTION_ASSESSMENT_CONTRACT_ID]`),
  keine Änderung an bestehenden Feldern/Statuscodes.
- **Command:** `npx vitest run src/app/api/health/route.test.ts`
- **Result:** PASS — `Test Files 1 passed (1)`, `Tests 1 passed (1)`.
  Zusätzlich Gate-1-Gesamtverifikation: `npx tsc --noEmit` clean, `npx eslint` clean,
  `npx vitest run` → `Test Files 4 passed (4)`, `Tests 23 passed (23)`,
  `npx next build` → "Compiled successfully", `/api/production-assessment` als
  dynamische Route gelistet.
- **Evidence:** Alle vier Kommandos oben real im Sandbox-Terminal gelaufen,
  kompletter Output grün, keine Warnungen unterdrückt.
- **Remaining risk:** Keiner bekannt für diesen Atom.

---

## R-05 — ANVIL: `WizardHttpAdapter.kt`

- **Repo:** ANVIL
- **Status:** DONE
- **Files:** `anvil-kmp/core/externaladapters/src/main/kotlin/io/anvil/core/externaladapters/WizardHttpAdapter.kt`,
  `anvil-kmp/core/externaladapters/src/test/kotlin/io/anvil/core/externaladapters/WizardHttpAdapterTest.kt`,
  `anvil-kmp/core/externaladapters/build.gradle.kts` (Ktor-Client-Deps ergänzt)
- **Contract:** `anvil.wizard.production-assessment/v1` (Consumer-Seite)
- **Owner:** WIZARD bleibt Owner; ANVIL ist hier nur Consumer/Transport.
- **Problem:** Der bisherige Golden Run nutzte `FakeWizardPort`
  (`core/run/src/commonTest/.../fixtures/FakeWizardPort.kt`) — eine reine
  Fixture, die nie wirklich mit WIZARD sprach. R-01–R-04 haben WIZARD jetzt
  einen echten, erreichbaren Endpoint gegeben; es fehlte der ANVIL-seitige
  reale Adapter dafür.
- **Smallest safe change:** Neue Klasse nach demselben Muster wie
  `SwiftCliAdapter`/`CueCliAdapter` (`ExternalToolPort`-Implementierung,
  JVM-only, `core:externaladapters`), aber HTTP statt Prozess: `health()`
  fragt `GET /api/health` ab und prüft `capabilities.productionAssessment`;
  `invoke()` postet an `POST /api/production-assessment`, verlangt
  2xx, prüft Antwortgröße gegen eine Guard-Grenze, parst nur so weit JSON,
  wie nötig ist, um das `contract`-Feld zu prüfen (kein Duplizieren von
  WIZARDs vollem Schema), und gibt bei falschem/fehlendem Contract-Feld
  `BlockedExternalContract` zurück statt die Antwort blind zu akzeptieren.
  Kein neuer Ktor-Server, keine neue Transport-Abstraktion — dieselbe
  `ExternalToolPort`-Schnittstelle wie die bestehenden CLI-Adapter.
- **Command:** `/opt/gradle/bin/gradle :core:externaladapters:test --console=plain`
  (Hinweis: `./gradlew` kann in dieser Sandbox die Gradle-Distribution nicht
  laden, da der Proxy `services.gradle.org` blockt — system-installiertes
  Gradle 8.14.3/JDK 21 wird stattdessen genutzt, keine Projektänderung.)
- **Result:** PASS — `BUILD SUCCESSFUL`, `WizardHttpAdapterTest`:
  `tests="10" skipped="0" failures="0" errors="0"` (success, HTTP 400, HTTP
  500, Timeout, falscher Contract in der Antwort, kaputtes JSON, überlange
  Antwort, falscher Input-Contract, Health STABLE, Health DEGRADED).
- **Evidence:** `anvil-kmp/core/externaladapters/build/test-results/test/TEST-io.anvil.core.externaladapters.WizardHttpAdapterTest.xml`.
- **Remaining risk:** Adapter validiert nur das `contract`-Feld strukturell,
  nicht das volle Schema (bewusst — sonst zweite Typ-Wahrheit für WIZARDs
  Vertrag in Kotlin). Ein echter End-to-End-Lauf gegen einen laufenden
  WIZARD-Dev-Server (statt MockEngine) steht noch aus und ist Teil von R-19
  (RealGoldenRunTest).

---

## R-06 — ANVIL: Contract-Registry für `anvil.wizard.production-assessment` reconciliert

- **Repo:** ANVIL
- **Status:** DONE
- **Files:** `anvil-kmp/core/contracts/src/commonMain/kotlin/io/anvil/core/contracts/ContractRegistry.kt`,
  `anvil-kmp/core/contracts/src/commonTest/kotlin/io/anvil/core/contracts/ContractRegistryTest.kt`
- **Contract:** `anvil.wizard.production-assessment/v1`
- **Owner:** WIZARD (Owner unverändert; hier nur die Consumer-Liste betroffen)
- **Problem:** `WizardHttpAdapter` (R-05) lebt in `core:externaladapters` und
  konsumiert diesen Vertrag jetzt direkt innerhalb von ANVIL — die
  registrierte Consumer-Liste (`gameplay, scene, interface, acoustic,
  target, cue`) enthielt aber keinen `"anvil"`-Eintrag. Ein realer
  `requireConsumerAllowed(id, 1, "anvil")`-Aufruf (z. B. aus dem künftigen
  RealGoldenRunTest, R-19) wäre damit fälschlich als Regelverstoß
  geblockt worden, obwohl ANVIL selbst der rechtmäßige Consumer ist.
- **Smallest safe change:** Rein additiv `"anvil"` vorn in die
  `allowedConsumers`-Liste ergänzt. Kein bestehender Eintrag entfernt (`cue`
  bleibt Consumer, falls andere Flows sich bereits darauf verlassen — dafür
  gab es keinen Beleg für Entfernung, also nicht risikofrei "aufräumen").
  Zwei neue Registry-Tests: einer sperrt `"anvil"` als erlaubten Consumer
  fest, der zweite verhindert Regression der bestehenden sechs Consumer.
- **Command:** `/opt/gradle/bin/gradle :core:contracts:jvmTest :core:run:jvmTest :surfaces:golden-run:jvmTest :core:externaladapters:test --console=plain`
- **Result:** PASS — `BUILD SUCCESSFUL`, 47 actionable tasks (32 executed, 15
  up-to-date). `ContractRegistryTest`: `tests="12" skipped="0" failures="0"
  errors="0"` (10 vorher + 2 neue). Alle vier vom Vertrag berührten,
  tatsächlich in `settings.gradle.kts` eingebundenen Module (`core:contracts`,
  `core:run`, `surfaces:golden-run`, `core:externaladapters`) kompilieren und
  testen grün — keine Regression durch die Erweiterung.
- **Evidence:** Terminal-Output oben; `core/contracts/build/test-results/jvmTest/TEST-io.anvil.core.contracts.ContractRegistryTest.xml`.
- **Remaining risk:** `modules/bard` referenziert denselben Contract-String
  ebenfalls (`BardModels.kt`), ist aber laut `settings.gradle.kts` gar nicht
  in den Gradle-Build eingebunden (kein `:modules:bard`-Projekt existiert) —
  dieser Fund wurde geprüft, aber bewusst nicht angefasst: BARD darf laut
  Auftrag in dieser Runde Fixture/unangetastet bleiben.

---

## R-07..R-11 — SHADED: Scene-Project-Contract + Headless-Orchestrierung

- **Repo:** SHADED
- **Status:** DONE
- **Files:** `contracts/shaded-scene-project.schema.json` (R-07); `editor/facade.js`
  (+`addActorBundle`/`getRuntimeStatus`/`getDebugSnapshot`/`exportProject`/`loadProject`,
  R-08), `editor/facade.test.js` (R-08); `editor/app.js` (`window.SHADED_ORCHESTRATOR`,
  R-09); `tools/orchestrate.js`, `tools/orchestrate-example-request.json` (R-10);
  `docs/ORCHESTRATION.md` (R-11); `package.json` (`check`-Script erweitert); `CLAUDE.md`.
- **Contract:** neu `shaded.scene-project/v1` (Owner SHADED) + CLI-Vertrag `tools/orchestrate.js`
  (Beweis-Ziel für ANVILs `ShadedCliAdapter`, R-12)
- **Owner:** SHADED (sichtbare Szenen-Wahrheit)
- **Problem:** SHADED hatte keinen headless ansteuerbaren Vertrag — nur die interaktive
  Editor-UI (Maus/Tastatur) und die Runtime-`window.SHADED`-API (nur im Browser-Kontext
  sinnvoll nutzbar). ANVIL kann so nicht real prüfen/beweisen, dass eine Szene inkl.
  Actors/Storyboard tatsächlich lädt — es gab nur den fixture-basierten Pfad.
- **Smallest safe change:** Fünf neue Methoden AUF der bestehenden `SceneEditorFacade`
  (kein Fork, kein zweiter Actor-/Storyboard-Zustand — `addActorBundle` ruft dieselbe
  `window.SHADED.addActor()`-Wahrheit wie die interaktive `ActorPlacer`-Fassade auf;
  `loadProject`s Storyboard-Übernahme mutiert dieselbe Live-Referenz wie
  `StoryboardTimeline`). `window.SHADED_ORCHESTRATOR` in `app.js` bündelt sie nur für
  externe Headless-Skripte (kein Duplikat von `window.SHADED`). `tools/orchestrate.js`
  ist reine Orchestrierungs-Glue (echter lokaler Server + echtes headless Chromium,
  identisches Muster wie das bereits bestehende `tools/verify-editor.js`) — kein neuer
  Shader-/Analyse-Code.
  **Beim Testen echten Bug gefunden und gefixt:** `loadSceneFile()` rief `create()`
  auf, bevor das Bild asynchron dekodiert war (Race Condition) — behoben durch
  Rückgabe eines Promise, das auf die echte Canvas-Größenänderung wartet (gleiche
  Technik wie bereits für `paint-canvas` in `tools/verify-editor.js` etabliert; keine
  feste Sleep-Zeit).
- **Command:** `npm run check` (Syntax aller neuen/geänderten Dateien + Schema-JSON-Validität);
  `node editor/facade.test.js`; `node tools/orchestrate.js --project tools/orchestrate-example-request.json --json`;
  `node tools/orchestrate.js --project tools/does-not-exist.json --json` (Exit-2-Pfad);
  `node tools/verify-editor.js` (Regressionscheck der bestehenden interaktiven Editor-Suite).
- **Result:** PASS — `npm run check`: "index script parses", keine Syntaxfehler.
  `facade.test.js`: 13/13 PASS ("✅ facade.test PASSED"). `orchestrate.js` Erfolgsfall:
  Exit 0, echtes JSON (`{"status":"ok","ready":true,"actorCount":1,"storyboardSteps":1,...}`).
  `orchestrate.js` Fehlerfälle: Exit 2 bei fehlender Request-Datei UND bei fehlendem
  Szenenbild, jeweils mit `code:"missing_input"`. `verify-editor.js`: alle 11
  bestehenden Checks weiterhin PASS (keine Regression durch die `loadSceneFile()`-Änderung).
- **Evidence:** Terminal-Output oben, real ausgeführt (kein Mock-DOM, echtes headless
  Chromium via Playwright, echte Fixture-Dateien `file_00000000974871f49fe71f6b456f9579.png`
  + `tools/verify-test-actor.{png,json}`).
- **Remaining risk:** `orchestrate.js`s Depth-Auto-Probe-404-Falle (behoben durch
  Weiterreichen des echten Dateinamens) ist eine allgemeine Falle für künftige Aufrufer,
  die erfundene Dateinamen an `loadImageFile` übergeben — in `docs/ORCHESTRATION.md`
  nicht explizit dokumentiert (kleine Lücke, keine Funktionsgefährdung, da der reale
  `_depth`-Fehlversuch von `loadImageFile` selbst bereits harmlos abgefangen wird —
  nur `orchestrate.js`s strikte "keine Konsolenfehler"-Prüfung reagiert empfindlich
  darauf). `exportProject()`/`getDebugSnapshot()` können keine Bild-Bytes zurückgeben
  (dokumentiertes, bewusstes Schema-Limit, kein Bug).

---

**Gate 3 (SHADED) Status: ABGESCHLOSSEN.** Scene-Project-Contract, Facade-Erweiterung,
Orchestrator-Debug-API und CLI real implementiert und real verifiziert (13/13 Facade-Tests,
echter CLI-Erfolgslauf + zwei echte Fehlerpfade, keine Regression der bestehenden Editor-Suite).

---

## R-12 — ANVIL: `ShadedCliAdapter.kt`

- **Repo:** ANVIL
- **Status:** DONE
- **Files:** `anvil-kmp/core/externaladapters/src/main/kotlin/io/anvil/core/externaladapters/ShadedCliAdapter.kt`,
  `.../src/test/kotlin/io/anvil/core/externaladapters/ShadedCliAdapterTest.kt`,
  `.../src/test/kotlin/io/anvil/core/externaladapters/ShadedCliManualIntegrationTest.kt`,
  `anvil-kmp/core/contracts/.../ContractRegistry.kt` (+`shaded.scene-project`-Registrierung),
  `anvil-kmp/core/contracts/.../ContractRegistryTest.kt`
- **Contract:** `shaded.scene-project/v1` (Consumer-Seite; Owner SHADED)
- **Owner:** SHADED bleibt Owner; ANVIL ist hier Consumer/Transport.
- **Problem:** SHADEDs neuer, echter CLI-Vertrag (`tools/orchestrate.js`, R-10) hatte auf
  ANVIL-Seite noch keinen Adapter — ohne ihn bliebe SHADED für den Golden Run weiterhin
  ein Fixture-Pfad, obwohl SHADED selbst jetzt real ansteuerbar ist.
- **Smallest safe change:** Neue Klasse nach demselben Muster wie `SwiftCliAdapter`/
  `CueCliAdapter` (`ExternalToolPort`, JVM-only, `ProcessRunner`-Abstraktion). `invoke()`
  schreibt `request.payload` (rohes JSON, SHADEDs eigenes `orchestrate.js`-Request-Format)
  in eine Temp-Datei und shellt `node tools/orchestrate.js --project <tmp> --json`;
  Exit-Code-Mapping identisch zu `SwiftCliAdapter`s Konvention (0 Produced, 2 "missing
  input" → Failed, sonst generisch Failed). `health()` nutzt einen echten Trick: ein
  Aufruf OHNE `--project` lässt `orchestrate.js` sofort (vor jedem Browser-Start) mit
  `{"status":"error","code":"missing_input",...}` und Exit 2 antworten — ein schneller,
  echter Erreichbarkeits-Beweis ohne Playwright/Server zu starten. `shaded.scene-project`
  wurde additiv in `AnvilContractRegistry` registriert (Owner SHADED, Producer `shaded`,
  Consumer `anvil, target, cue` — `anvil` ergänzt aus demselben Grund wie bei R-06).
- **Command:** `/opt/gradle/bin/gradle :core:contracts:jvmTest :core:externaladapters:test --console=plain`;
  danach real gegen den echten SHADED-Checkout:
  `SHADED_REPO_PATH=/home/user/SHADED /opt/gradle/bin/gradle :core:externaladapters:test --tests "*ShadedCliManualIntegrationTest*" --console=plain --rerun`
- **Result:** PASS — `BUILD SUCCESSFUL`. `ShadedCliAdapterTest`: `tests="9"
  failures="0" errors="0"` (Health STABLE/DEGRADED/FAILED, echter Erfolgsfall mit real
  aus SHADED aufgezeichnetem JSON, Payload landet nachweislich in der Temp-Datei, zwei
  echte `missing_input`-Exit-2-Fälle, Crash-ohne-stdout, falscher Input-Contract).
  `ShadedCliManualIntegrationTest` mit `SHADED_REPO_PATH=/home/user/SHADED` (echter
  Checkout, kein Fixture-Pfad): `tests="1" failures="0" errors="0"` — `health()` liefert
  real `QualityState.STABLE`. `ContractRegistryTest`: weiterhin grün (12+2 neue Fälle).
- **Evidence:** Test-Report-XMLs unter
  `anvil-kmp/core/externaladapters/build/test-results/test/TEST-io.anvil.core.externaladapters.ShadedCli*.xml`.
  Die drei JSON-Fixtures in `ShadedCliAdapterTest` sind unbearbeitete, in dieser Session
  real erzeugte `node tools/orchestrate.js`-Ausgaben (Erfolg, fehlende Request-Datei,
  fehlendes Szenenbild — siehe R-07..R-11-Terminal-Output).
- **Remaining risk:** `invoke()` wurde nur gegen `FakeProcessRunner` mit real
  aufgezeichnetem JSON getestet, nicht als echter End-to-End-Shellout in diesem Atom
  (das folgt in R-19, `RealGoldenRunTest`, wo ein echter Chromium-Lauf über den
  Adapter selbst getriggert wird). Die Temp-Datei wird bei Erfolg UND Fehlschlag
  gelöscht (`finally`), aber nicht bei JVM-Absturz — vernachlässigbares Leck (OS-Temp).

---

**Gate 4 (ANVIL) Status: ABGESCHLOSSEN.** SHADEDs realer CLI-Vertrag hat jetzt einen
echten ANVIL-seitigen Adapter, real gegen den echten SHADED-Checkout verifiziert.

---

## R-13..R-17 — CUE-AGENT: `cue audio-check` (Gate H)

- **Repo:** CUE-AGENT
- **Status:** DONE
- **Files:** `src/qa/audio-scenario.schema.json` (R-13); `docs/AUDIO_CHECK.md` (R-14);
  `src/qa/audio-check.js` (R-15); `bin/cue.js` (Dispatch + Hilfetext, R-16);
  `test/audio-check.smoke.test.js` (R-17)
- **Contract:** neu `cue.audio-proof/v1` (bereits als Contract-ID in ANVILs Registry seit
  Gate H/B-01 vorhanden — diese Runde liefert die bislang fehlende REALE
  CLI-Implementierung dazu, kein neuer Contract-Eintrag nötig)
- **Owner:** CUE (technischer Beweis)
- **Problem:** `cue audio-check` existierte real nicht (per `node bin/cue.js --help`,
  bereits in der vorigen Runde/`FABLE_FIX_LEDGER.md` Gate H live verifiziert und
  dokumentiert als "im CUE-AGENT-Repo zu implementieren, außerhalb des damaligen
  Scopes"). ANVILs `CueCliAdapter` musste `cue.audio-proof` deshalb explizit
  `BlockedExternalContract` zurückgeben.
- **Smallest safe change:** Neues, eigenständiges `src/qa/audio-check.js` im selben
  Stil wie `playable.js`/`temporal.js` (kein Fork bestehender Dateien, nur ein
  winziges eigenes `runFlowSteps`-Duplikat für den optionalen `--scenario`-Flow,
  begründet dokumentiert statt eines riskanten Umbaus von `playable.js`).
  Erkennt exakt den REALEN `window.ANVIL_AUDIO`-Vertrag, den ANVILs
  `ToneJsRuntimeWriter.kt` tatsächlich generiert (`getDebugState`/`setState`/
  `getEventLog` — Quelle gelesen, nicht geraten). Von sechs Gate-H-Beweiskategorien
  sind vier mit dem AKTUELLEN Vertrag real+deterministisch prüfbar (CUE_FIRED,
  STATE_REACTION, TRANSITION_TIMING, LOOP_CONTINUITY); zwei (CLIPPING_CHECK,
  VOICE_AUDIBILITY) sind mit dem aktuellen Vertrag NICHT prüfbar (kein
  Pegel-/Analyser-Hook) — diese werden ehrlich mit `ok: null`, `required:false`
  ausgewiesen, nie fingiert (dokumentierte Fallback-Strategie in
  `docs/AUDIO_CHECK.md`, R-14). Fehlt `window.ANVIL_AUDIO` ganz, gibt es KEINEN
  stillen Fallback-Erfolg (anders als `temporal-check`s generischer Modus) —
  Verdict `KEIN AUDIO-VERTRAG GEFUNDEN`, Exit 1, weil der Audio-Vertrag selbst der
  Prüfzweck ist.
- **Command:** `node --test test/audio-check.smoke.test.js`; `npm test` (volle Suite,
  Regressionscheck); echter End-to-End-CLI-Lauf gegen einen echten lokalen
  Mock-Server: `node bin/cue.js audio-check http://127.0.0.1:8951/ --json`.
- **Result:** PASS — 3/3 neue Smoke-Tests grün (BELEGT-Fall mit allen vier
  prüfbaren Kategorien `ok:true` + beiden nicht-prüfbaren Kategorien `ok:null`,
  Fallback-Fall ohne stillen Erfolg, `--scenario`-Override). Volle Suite:
  `tests 35`, `pass 33`, `fail 0`, `skipped 2` (die 2 Skips sind bereits vor
  diesem Atom vorhanden, Chromium-Verfügbarkeits-Gates in anderen Tests,
  keine Regression). Echter CLI-Lauf: Exit 0, vollständiges reales JSON mit
  Verdict `"AUDIO-VERTRAG BELEGT (Clipping/Audibility nicht prüfbar)"`.
- **Evidence:** Terminal-Output oben (echter `node bin/cue.js audio-check`-Lauf mit
  vollem JSON), `npm test`-Output (35 Tests).
- **Remaining risk:** Es existiert noch KEIN echter, deployter Ziel-Build, der
  `window.ANVIL_AUDIO` tatsächlich auf `window` exponiert (ANVILs
  `WebTargetWriter` liefert laut `docs/FABLE_FIX_LEDGER.md` nur `ASSEMBLED`, nie
  `RUNNABLE`) — `audio-check` wurde daher nur gegen eine handgeschriebene,
  vertragsgetreue Mock-Seite real verifiziert, nicht gegen einen echten
  ANVIL-Web-Audio-Build. Das ist dieselbe Einschränkung, die `CueCliAdapterTest`
  bereits für `playable-check`/`temporal-check` dokumentiert (nie live gegen eine
  echte laufende URL getestet).

---

**Gate 5 (CUE-AGENT) Status: ABGESCHLOSSEN.** `cue audio-check` ist real
implementiert, real getestet, real per CLI verifiziert — CUE-AGENT ist für diesen
Vertrag kein Fixture-Blocker mehr.

---

## R-18 — ANVIL: `CueCliAdapter` um `audio-check` erweitert

- **Repo:** ANVIL
- **Status:** DONE
- **Files:** `anvil-kmp/core/externaladapters/src/main/kotlin/io/anvil/core/externaladapters/CueCliAdapter.kt`,
  `.../src/test/kotlin/io/anvil/core/externaladapters/CueCliAdapterTest.kt`
- **Contract:** `cue.audio-proof/v1` (bereits seit B-01/Gate H registriert, Owner CUE)
- **Owner:** CUE
- **Problem:** `CueCliAdapter` gab `cue.audio-proof` bisher explizit als
  `BlockedExternalContract` zurück, mit der Begründung "`cue audio-check` existiert
  nicht". Das stimmt seit Gate 5 (R-13..R-17) nicht mehr — der Adapter war jetzt
  selbst der letzte Fixture-Blocker für diesen Vertrag.
- **Smallest safe change:** Eine Zeile im `subcommand`-`when`-Dispatch
  (`"cue.audio-proof" -> "audio-check"`), `producedOutputContracts` um
  `cue.audio-proof` ergänzt, Klassendoc korrigiert (die alte "deliberately no
  audio-check support"-Begründung war jetzt sachlich falsch). Kein neuer
  Contract-Eintrag nötig (`cue.audio-proof` war schon registriert).
- **Command:** `/opt/gradle/bin/gradle :core:externaladapters:test --console=plain`
- **Result:** PASS — `BUILD SUCCESSFUL`. `CueCliAdapterTest`: `tests="8"
  failures="0" errors="0"` (die alte "audio-check existiert nicht"-Testerwartung
  wurde durch zwei neue Tests mit ECHT in dieser Session aufgezeichnetem
  `node bin/cue.js audio-check ... --json`-Output ersetzt: BELEGT-Fall exit 0
  → Produced, "KEIN AUDIO-VERTRAG GEFUNDEN"-Fall exit 1 → weiterhin Produced,
  genau wie bei `playable-check`s negativem Verdict).
- **Evidence:** `anvil-kmp/core/externaladapters/build/test-results/test/TEST-io.anvil.core.externaladapters.CueCliAdapterTest.xml`.
  Die beiden JSON-Fixtures sind Kopien der in R-13..R-17 real aufgezeichneten
  `audio-check`-Läufe (BELEGT gegen echten Mock-Server, "kein Hook gefunden"
  gegen echten Mock-Server ohne `ANVIL_AUDIO`).
- **Remaining risk:** Kein neuer Manual-Integration-Test für `invoke()` gegen den
  echten CUE-AGENT-Checkout (nur `health()` hat einen solchen Test,
  `CueCliManualIntegrationTest`) — der reale End-to-End-Beweis für `audio-check`
  wurde stattdessen direkt per Bash geführt (s. R-13..R-17-Evidence) und deckt
  denselben Kommandopfad ab, den dieser Adapter erzeugt.

---

**Gate 6 (ANVIL) Status: ABGESCHLOSSEN.**

---

**Gate 1 (WIZARD) Status: ABGESCHLOSSEN.** Alle vier Atome (R-01–R-04) real
implementiert, real getestet (23/23 grün), Build grün. WIZARD ist ab hier kein
Fixture mehr für diesen Vertrag — `POST /api/production-assessment` liefert
echte Daten aus der echten Asset-Datenbank.

---
