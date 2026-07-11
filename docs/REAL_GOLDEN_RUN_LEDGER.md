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

**Gate 1 (WIZARD) Status: ABGESCHLOSSEN.** Alle vier Atome (R-01–R-04) real
implementiert, real getestet (23/23 grün), Build grün. WIZARD ist ab hier kein
Fixture mehr für diesen Vertrag — `POST /api/production-assessment` liefert
echte Daten aus der echten Asset-Datenbank.

---
