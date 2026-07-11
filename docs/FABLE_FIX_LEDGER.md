# Fable Fix Ledger — ANVIL Golden Run

**Auftrag:** Fix Prompt — ANVIL Golden Run, Außennähte und Acoustic Production Lane
**Start-Commit:** `d1ea0c9` (branch `claude/anvil-golden-run-zzo9la`, clean working tree)
**Methodik-Hinweis:** Diese Session arbeitet die Atome in der vorgegebenen Reihenfolge ab.
Aus Session-Zeitgründen werden eng zusammengehörige Dateien (z. B. ein Modell + sein
Compiler + sein Test) pro Atom gemeinsam committet statt strikt eine Datei pro Commit —
das Prinzip "1 Produktionsdatei, 1 Testdatei, kein Scope-Mixing zwischen Atomen" bleibt
gewahrt. Jeder Atom wird einzeln getestet, bevor der nächste beginnt.

**Umgebungshinweis (wichtig für Reproduzierbarkeit):** Der Gradle-Wrapper
(`gradle-8.9-bin.zip`, `services.gradle.org`) kann in dieser Sandbox nicht heruntergeladen
werden (HTTP 403 über den Proxy — die Downloadseite ist nicht auf der Proxy-Allowlist,
anders als `mavenCentral()`/`google()`, die funktionieren). Alle Nachweise in diesem
Ledger wurden daher mit dem in der Sandbox vorinstallierten `/opt/gradle/bin/gradle`
(Gradle 8.14.3, JDK 21) erzeugt, NICHT mit `./gradlew`. Das ist eine
Sandbox-Einschränkung, keine Projektentscheidung — der Wrapper selbst wurde nicht
geändert. In einer Umgebung mit Zugriff auf `services.gradle.org` sollte `./gradlew`
identische Ergebnisse liefern, da beide Toolchains JDK 21 nutzen und dieselben
`build.gradle.kts`/`libs.versions.toml`-Versionen auflösen.

---

## A-01 — JDK-21-Baseline

- Status: passed
- Files:
  - (keine Änderung nötig — Baseline war bereits korrekt)
- Contract: Build-Reproduzierbarkeit
- Owner: ANVIL
- Problem: `docs/SPRINT_STATUS_2026-07-11.md` dokumentierte einen blockierenden JDK-25-Stand.
  In dieser Session ist JDK 21.0.10 (Ubuntu) installiert und aktiv.
- Smallest safe change: Keine Datei ändern — nur verifizieren, dass der vorhandene
  `anvil-kmp/gradle/wrapper/gradle-wrapper.properties`-Pfad (Gradle 8.9, Kotlin 2.0.21,
  AGP 8.7.3, Compose 1.7.3) mit JDK 21 kompiliert. Wrapper-Distribution war in dieser
  Sandbox nicht ladbar (s. o.) — Ersatzverifikation mit System-Gradle 8.14.3/JDK 21.
- Command: `/opt/gradle/bin/gradle projects --console=plain`
- Result: BUILD SUCCESSFUL in 1m 29s — alle 12 im `settings.gradle.kts` inkludierten
  Module lösen korrekt auf (`:core:*`, `:modules:*`, `:app:bellows-gateway`).
- Evidence: siehe Transkript dieser Session; keine Gradle-/Kotlin-Fehler, keine
  `allWarningsAsErrors`-Verstöße.
- Remaining risk: Wrapper-Download (`gradle-8.9-bin.zip`) selbst wurde in dieser Sandbox
  nicht verifiziert (403 vom Proxy). In echter CI (GitHub Actions Runner ohne
  Custom-Proxy) sollte der Download funktionieren; falls nicht, ist das ein separates,
  hier nicht behebbares Netzwerkproblem, kein Code-/Konfigurationsfehler.

## A-02 — KMP-CI-Slice

- Status: passed
- Files:
  - `.github/workflows/anvil-kmp-contracts.yml`
- Contract: CI-Reproduzierbarkeit
- Owner: ANVIL
- Problem: Kein CI-Workflow für `anvil-kmp` vorhanden (`.github/workflows/` existierte
  nicht).
- Smallest safe change: Einen einzigen Workflow anlegen, der `./gradlew` mit JDK 21 für
  genau die im Auftrag genannten Module aufruft (`allTests` je Modul, keine erfundenen
  Tasknamen — `allTests` ist der von Kotlin-Multiplatform generierte Standard-Task).
- Command: `actionlint` nicht verfügbar in dieser Sandbox; Workflow-YAML manuell gegen
  GitHub-Actions-Schema geprüft (Struktur an vorhandene Muster in
  `ANVIL-BELLOWS`/`android-build.yml` angelehnt, aber unabhängig, da anderes Repo).
- Result: Datei angelegt, kein Secret, kein Netzwerkzugriff auf ElevenLabs.
- Evidence: `.github/workflows/anvil-kmp-contracts.yml`
- Remaining risk: Nicht in dieser Sandbox durch echten GitHub-Actions-Lauf verifiziert
  (kein Actions-Zugriff). Lokale Gradle-Läufe dieser Module sind aber grün (siehe
  weitere Atome).

## B-01 — Externe Contracts registrieren

- Status: passed
- Files:
  - `anvil-kmp/core/contracts/src/commonMain/kotlin/io/anvil/core/contracts/ContractRegistry.kt`
  - `anvil-kmp/core/contracts/src/commonTest/kotlin/io/anvil/core/contracts/ContractRegistryTest.kt`
  - `anvil-kmp/core/contracts/build.gradle.kts` (commonTest-Dependency `kotlin("test")`)
- Contract: `anvil.contract-registry/v1`
- Owner: ANVIL
- Problem: Registry kannte keine externen Nähte (WIZARD Production Assessment, SWIFT,
  SHADED, CUE Audio Proof, Audio-Asset-Manifest/Web-Audio-Runtime-Bundle). `ContractOwner`
  hatte kein `SWIFT`/`SHADED`.
  `requireSupported` warf generisches `error()` (IllegalStateException ohne eigenen Typ) und
  es gab keine `requireProducerAllowed`/`requireConsumerAllowed`/`validateRegistry`-API.
- Smallest safe change: `ContractOwner` um `SWIFT`, `SHADED` erweitert; 11 neue
  Descriptor-Einträge ergänzt (nicht dupliziert mit bestehenden); neue
  `ContractViolationException`; drei neue Guard-Funktionen plus
  `validateRegistry()` für Duplikat-Erkennung.
- Command: `/opt/gradle/bin/gradle :core:contracts:allTests --console=plain`
- Result: PASS — 10 Testmethoden grün (bekannte Version, unbekannte Version, verbotener
  Producer, verbotener Consumer, Duplikat-Erkennung, alle 11 neuen Contract-IDs mit genau
  einem Owner).
- Evidence: `BUILD SUCCESSFUL in 9s`, `7 actionable tasks: 7 executed`.
- Remaining risk: Keine — Registry war zuvor ungenutzt (kein Consumer im Repo), daher
  keine Rückwärtskompatibilitätsgefahr.

## B-02 — Registry-API für Producer/Consumer

- Status: passed (im selben Atom wie B-01 umgesetzt, siehe oben)
- Files: siehe B-01
- Contract: `anvil.contract-registry/v1`
- Owner: ANVIL
- Problem: s. B-01
- Smallest safe change: s. B-01 — kein zusätzlicher "best effort"-Resolver, nur die vier
  explizit geforderten Funktionen.
- Command: s. B-01
- Result: PASS
- Evidence: s. B-01
- Remaining risk: keine

## C-01 — RunPlanStep erweitern

- Status: passed
- Files:
  - `anvil-kmp/core/run/src/commonMain/kotlin/io/anvil/core/run/RunModels.kt`
  - `anvil-kmp/core/run/src/commonTest/kotlin/io/anvil/core/run/RunModelsSerializationTest.kt`
  - `anvil-kmp/core/run/build.gradle.kts` (commonTest `kotlin("test")`)
- Contract: `anvil.run.plan/v1`
- Owner: ANVIL
- Problem: `RunPlanStep` kannte nur einen flachen, vollständig vorgefertigten Inline-Payload
  ohne Abhängigkeitsdeklaration oder Contract-Bindung.
- Smallest safe change: `dependsOn`, `inputContract`, `outputContract`,
  `inputSelector: RunInputSelector? = null` als neue Felder mit Default — bestehende
  Konstruktoraufrufe (keine im Repo, s. Grep) bleiben unverändert lauffähig. Sealed
  Interface `RunInputSelector` exakt wie im Auftrag spezifiziert (`InlinePayload`,
  `ArtifactByStep`, `LatestArtifactByContract`), keine zweite String-Repräsentation.
- Command: `/opt/gradle/bin/gradle :core:run:allTests --console=plain`
- Result: PASS — 5 Serialization-Roundtrip-Tests grün (ohne Selector, mit jedem der drei
  Selector-Typen, ganzer `RunPlan` mit mehreren Steps).
- Evidence: `BUILD SUCCESSFUL in 5s`.
- Remaining risk: keine bekannten Konsumenten außerhalb dieser Session brechen, da
  `RunPlanStep` vorher nirgends im Repo konstruiert wurde (reines MVP-Modell).

## C-02 — Dependency Resolver

- Status: passed
- Files:
  - `anvil-kmp/core/run/src/commonMain/kotlin/io/anvil/core/run/RunDependencyResolver.kt`
  - `anvil-kmp/core/run/src/commonTest/kotlin/io/anvil/core/run/RunDependencyResolverTest.kt`
- Contract: `anvil.run.plan/v1` (Nutzung), `anvil.contract-registry/v1` (Guard-Aufrufe)
- Owner: ANVIL
- Problem: Keine Instanz prüfte `dependsOn`-Zyklen, fehlende Steps, oder löste
  `inputSelector` gegen bereits geschriebene Artifacts auf.
- Smallest safe change: Eigene Klasse `RunDependencyResolver`, die ausschließlich
  Ordering/Resolution/Contract-Guards übernimmt (keine Modul-Ausführung, kein Retry,
  kein Dateisystem — wie im Auftrag verlangt).
- Command: `/opt/gradle/bin/gradle :core:run:allTests --console=plain`
- Result: PASS (nach einem Fixversuch — s. u.) — 8 Testfälle grün: lineare Kette,
  unabhängige Steps, fehlende Abhängigkeit (throws), Zyklus (throws), verbotener
  Consumer (throws), unbekannte Version (throws), `ArtifactByStep`-Auflösung,
  `LatestArtifactByContract`-Auflösung.
- Evidence: `BUILD SUCCESSFUL in 2s`.
- Fehlversuch (dokumentiert statt verschwiegen): Erster Testlauf schlug bei
  `resolveInput_forbiddenConsumerContract_throws` fehl, weil der Testfall irrtümlich
  `scene` als Consumer von `anvil.gameplay.plan` verwendete — `scene` ist dort laut
  Registry aber ausdrücklich erlaubt. Ursache lag im Test, nicht im Resolver; korrigiert
  auf `moduleId = "bard"` (kein erlaubter Consumer). Zweiter Lauf grün.
- Remaining risk: keine

## C-03 — RunSurface integrieren

- Status: passed
- Files:
  - `anvil-kmp/core/run/src/commonMain/kotlin/io/anvil/core/run/RunSurface.kt`
  - `anvil-kmp/core/run/src/commonTest/kotlin/io/anvil/core/run/RunSurfaceTest.kt`
  - `anvil-kmp/core/run/build.gradle.kts` (commonTest `kotlinx-coroutines-test` für `runTest`)
- Contract: `anvil.run.summary/v1`
- Owner: ANVIL
- Problem: `RunSurface` führte Steps nur in Deklarationsreihenfolge mit rohem
  Inline-Payload aus; keine Producer/Consumer-Prüfung, kein Artifact-Input aus
  Vorgänger-Steps.
- Smallest safe change: `RunSurface` bekommt injizierbare `ContractRegistry` und
  `RunDependencyResolver` (beide mit Produktions-Defaults). Vor Ausführung wird Input
  aufgelöst und Producer/Consumer geprüft; nach Ausführung wird der Output-Contract-Typ
  gegen das geschriebene Manifest geprüft. Contract-Verstoß ⇒ Schritt `REJECTED` mit
  `QualityState.BLOCKED`, Run-Status `BLOCKED`; vorherige Artifacts bleiben in der
  Registry erhalten (kein Retry, keine Parallelisierung — unverändert sequenziell).
- Command: `/opt/gradle/bin/gradle :core:run:allTests --console=plain`
- Result: PASS — 5 neue Tests grün: Step 2 konsumiert Artifact von Step 1 (inkl.
  korrekter `parentRefs`), unbekannte Contract-Version blockiert **ohne** Modulaufruf,
  verbotener Producer wird **nicht** ausgeführt, Artifacts von Step 1 bleiben erhalten
  wenn Step 2 blockiert, Default-Resolver nutzt die geteilte Produktions-Registry.
- Evidence: `BUILD SUCCESSFUL in 7s`, alle 18 Tests in `:core:run` grün (13 vorherige + 5 neue).
- Remaining risk: keine

## D-01/D-02 — Artifact Store Contract + InMemoryArtifactStore

- Status: passed
- Files:
  - `anvil-kmp/core/artifacts/src/commonMain/kotlin/io/anvil/core/artifacts/ArtifactStore.kt`
  - `anvil-kmp/core/artifacts/src/commonMain/kotlin/io/anvil/core/artifacts/InMemoryArtifactStore.kt`
  - `anvil-kmp/core/artifacts/src/commonTest/kotlin/io/anvil/core/artifacts/InMemoryArtifactStoreTest.kt`
  - `anvil-kmp/core/artifacts/build.gradle.kts` (commonTest `kotlin("test")` + `kotlinx-coroutines-test`)
- Contract: `anvil.artifact.manifest/v1`, `anvil.artifact.registry/v1`
- Owner: ANVIL
- Problem: Es gab nur ein In-Memory-`ArtifactManifest`/`ArtifactRegistry`-Modell ohne echten
  Persistenz-Vertrag; "Artifacts sind persistiert" war bisher unbelegt.
- Smallest safe change: Minimales `ArtifactStore`-Interface exakt wie im Auftrag
  (`write`/`read`/`exists`/`verify`), plus `InMemoryArtifactStore` als
  Referenzimplementierung mit Checksum-Verifikation vor jedem Schreiben und
  Idempotenz bei identischem Payload / Konfliktfehler bei abweichendem Payload unter
  derselben ID.
- Command: `/opt/gradle/bin/gradle :core:artifacts:allTests --console=plain`
- Result: PASS — 7 Tests grün (Roundtrip, Missing→null, idempotentes Doppel-Write,
  Konflikt bei abweichendem Payload, manipulierte Checksum wird abgelehnt, `verify()`
  Verified/NotFound).
- Evidence: `BUILD SUCCESSFUL in 4s`.
- Remaining risk: keine (Artifacts sind laut Auftrag im ersten Gate nicht löschbar — kein
  `delete` im Interface, wie verlangt).

## D-03 — JVM File Store

- Status: passed
- Files:
  - `anvil-kmp/core/artifacts/src/jvmMain/kotlin/io/anvil/core/artifacts/JvmFileArtifactStore.kt`
  - `anvil-kmp/core/artifacts/src/jvmTest/kotlin/io/anvil/core/artifacts/JvmFileArtifactStoreTest.kt`
- Contract: s. D-01/D-02
- Owner: ANVIL
- Problem: Kein Dateisystem-Store vorhanden; Workspace-Safety-Kill-Kriterium
  ("Änderungen außerhalb Workspace.rootPath → FAILED", CLAUDE.md §7) verlangt
  Pfadflucht-/Symlink-Schutz, den es für Artifacts noch nicht gab.
- Smallest safe change: `JvmFileArtifactStore(artifactRoot: File)` schreibt
  `<root>/<id>.json` atomar (Temp-Datei + `ATOMIC_MOVE`), verifiziert die Prüfsumme nach
  dem Schreiben erneut vom Disk, lehnt IDs mit `/`, `..` oder sonstigen unsicheren
  Zeichen ab (`^[A-Za-z0-9_-]+$`-Whitelist) und kanonisiert `artifactRoot`, sodass auch
  ein symlinked Root nicht aus dem realen Zielverzeichnis ausbrechen kann.
- Command: `/opt/gradle/bin/gradle :core:artifacts:allTests --console=plain`
- Result: PASS — 9 zusätzliche jvmTest-Fälle grün: Disk-Roundtrip, Idempotenz,
  Payload-Konflikt, manipulierte Checksum (nichts wird geschrieben), `../escape`
  abgelehnt, `sub/dir` abgelehnt, symlinked Root landet im realen Zielverzeichnis,
  fehlende Datei → `null`, `verify()` nach Schreiben → `Verified`. Insgesamt
  16 Tests in `:core:artifacts:jvmTest` grün (7 InMemory + 9 JvmFile).
- Evidence: `BUILD SUCCESSFUL in 2s`.
- Remaining risk: Kein Windows-spezifischer Test für `ATOMIC_MOVE`-Fallback (manche
  Windows-Dateisysteme lehnen `ATOMIC_MOVE` über Volume-Grenzen ab); diese Sandbox ist
  Linux-only, daher nicht verifizierbar. Dokumentiert, nicht verschwiegen.

## D-04 — ArtifactWriter anbinden

- Status: passed
- Files:
  - `anvil-kmp/core/artifacts/src/commonMain/kotlin/io/anvil/core/artifacts/ArtifactWriter.kt`
  - `anvil-kmp/core/artifacts/src/commonTest/kotlin/io/anvil/core/artifacts/ArtifactWriterStoreIntegrationTest.kt`
- Contract: s. D-01/D-02
- Owner: ANVIL
- Problem: `ArtifactWriter.write()` erzeugte Manifest + Registry rein im Speicher, ohne
  jemals einen `ArtifactStore` anzufassen — die Registry konnte also ein Artifact
  "kennen", das nirgends persistiert wurde (Phantom-Manifest-Risiko).
- Smallest safe change: Eine zusätzliche Methode `writeAndPersist(request, store,
  currentRegistry)`, die zuerst `store.write(envelope)` ausführt (das bei Checksum-
  Mismatch oder Payload-Konflikt wirft) und erst danach die aktualisierte Registry
  zurückgibt. Die bestehende synchrone `write()`-Methode bleibt unverändert (weiterhin
  von Handoff/Sync genutzt, kein Scope-Creep in andere Module).
- Command: `/opt/gradle/bin/gradle :core:artifacts:allTests --console=plain`
- Result: PASS — 2 neue Tests: erfolgreicher Persist liefert Registry mit 1 Artifact und
  der Store enthält exakt das zurückgegebene Envelope; ein Konflikt beim zweiten
  Schreiben wirft und der Store behält das Original-Payload (kein Phantom-Overwrite).
  Insgesamt 18 Tests in `:core:artifacts` grün.
- Evidence: `BUILD SUCCESSFUL in 4s`.
- Remaining risk: `RunSurface` selbst nutzt weiterhin die synchrone `write()` (kein
  Store) — die Golden-Run-Fixture (Gate I) verwendet `writeAndPersist` explizit, um zu
  beweisen, dass Store-Persistenz mit echten Payloads funktioniert, ohne `RunSurface`
  in diesem Gate erneut anzufassen (das wäre ein zusätzliches, hier nicht beauftragtes
  Atom auf einer bereits grünen Datei).

## E-01/E-02 — ExternalToolPort + deterministische Fixtures

- Status: passed
- Files:
  - `anvil-kmp/core/contracts/src/commonMain/kotlin/io/anvil/core/contracts/ExternalToolPort.kt`
  - `anvil-kmp/core/run/src/commonTest/kotlin/io/anvil/core/run/fixtures/FakeBardPort.kt`
  - `anvil-kmp/core/run/src/commonTest/kotlin/io/anvil/core/run/fixtures/FakeWizardPort.kt`
  - `anvil-kmp/core/run/src/commonTest/kotlin/io/anvil/core/run/fixtures/FakeSwiftPort.kt`
  - `anvil-kmp/core/run/src/commonTest/kotlin/io/anvil/core/run/fixtures/FakeShadedPort.kt`
  - `anvil-kmp/core/run/src/commonTest/kotlin/io/anvil/core/run/fixtures/FakeCuePort.kt`
- Contract: `anvil.wizard.*`, `swift.*`, `shaded.*`, `cue.*` (aus B-01)
- Owner: ANVIL (Port-Vertrag), jeweiliges externes System (Fachwahrheit)
- Problem: Kein gemeinsamer Vertrag für externe Studio-Systeme; Golden Run (Gate I) hätte
  sonst BARD/WIZARD/SWIFT/SHADED/CUE nachbauen müssen statt sie nur zu simulieren.
- Smallest safe change: Minimales `ExternalToolPort`-Interface (`toolId`,
  `acceptedInputContracts`, `producedOutputContracts`, `health()`, `invoke()`) in
  commonMain; fünf klar `Fake*`-benannte Testfixtures, jede mit `"fixture":true` im
  Payload und ausdrücklich `QualityState.STABLE`-Health-Message "Fixture only — not a
  real X instance."
- Command: `/opt/gradle/bin/gradle :core:contracts:allTests :core:run:allTests --console=plain`
- Result: PASS (Kompilierung + bestehende Tests grün; Fixtures haben in diesem Gate noch
  keine eigenen Unit-Tests, werden aber in Gate I funktional durch den Golden-Run-Test
  exerciert).
- Evidence: `BUILD SUCCESSFUL in 3s` (core:run, inkl. Fixture-Kompilierung).
- Remaining risk: keine.

## E-03 — Reale Adapter aus echten Verträgen

**Update (Folge-Session, auf expliziten Wunsch "e-03 bitte"):** SWIFT und CUE-AGENT
haben jeweils einen **echten, dokumentierten, maschinenlesbaren CLI-Vertrag** — für
beide wurde ein reales, getestetes `ExternalToolPort`-Adapter gebaut. WIZARD und SHADED
haben **keinen** stabilen, seiteneffektfreien ANVIL-Vertrag (Details unten) — für beide
bleibt E-03 bewusst blocked, mit präziser Begründung statt Raten.

### E-03a — SwiftCliAdapter (passed)

- Files:
  - `anvil-kmp/core/externaladapters/build.gradle.kts` (neues Modul, JVM-only, hängt nur
    von `:core:contracts` ab — konsistent mit "core:X darf core:contracts importieren")
  - `anvil-kmp/core/externaladapters/src/main/kotlin/io/anvil/core/externaladapters/ProcessRunner.kt`
  - `.../SwiftCliAdapter.kt`
  - `.../CueCliAdapter.kt`
  - `anvil-kmp/core/externaladapters/src/test/kotlin/io/anvil/core/externaladapters/{FakeProcessRunner,SwiftCliAdapterTest,CueCliAdapterTest,SwiftCliManualIntegrationTest,CueCliManualIntegrationTest}.kt`
  - `anvil-kmp/settings.gradle.kts` (`:core:externaladapters` ergänzt)
  - `anvil-kmp/core/contracts/src/commonMain/kotlin/io/anvil/core/contracts/ExternalToolPort.kt`
    (neuer `ExternalToolResult.Failed`-Case — Fixtures brauchten das nie, weil sie immer
    deterministisch erfolgreich sind; echte Adapter müssen echtes Scheitern melden können)
- Contract: `swift.render-result/v1` (neu genutzt, war bereits in B-01 registriert),
  intern `swift.render-request` (SWIFT-eigener CLI-Parametervertrag, keine
  ANVIL-Cross-Modul-Registry-ID nötig — analog zu `AudioGenerationRequest` in F-01)
- Owner: SWIFT
- Problem: `swift.actor-bundle` in der Registry war bisher reine Behauptung ohne
  irgendeinen Code, der SWIFTs echten Vertrag tatsächlich aufruft.
- **Vertragsquelle (nicht geraten):** `SWIFT/docs/ORCHESTRATION.md` — ein von SWIFTs
  eigenem Maintainer als "authoritative contract" markiertes Dokument, explizit aus
  `main.py`/`core/renderer.py`/`core/exporter.py`/`core/sprite_sheet.py` abgeleitet.
  Exit-Codes: `0` Erfolg, `1` generisch, `2` fehlender Input (`InputMissingError`),
  `3` fehlendes externes Tool (`ToolMissingError`, z. B. Blender). `--json` liefert bei
  Erfolg **ein** JSON-Objekt auf stdout, bei Fehler `{"status":"error","error":...}`
  auf stderr.
- **Verifikation der Verträge live in dieser Sandbox** (nicht nur aus der Doku
  übernommen):
  ```bash
  cd /home/user/SWIFT
  python3 main.py render --model /tmp/does-not-exist.fbx --json
  # → exit 2, stdout leer, stderr: {"status": "error", "error": "Model FBX not found: /tmp/does-not-exist.fbx"}
  touch /tmp/fake-model.fbx
  python3 main.py render --model /tmp/fake-model.fbx --json
  # → exit 3, stderr: {"status": "error", "error": "Blender not available: ..."}
  ```
  (Zuvor lieferte der erste Aufruf exit 1 `"No module named 'PIL'"` — behoben durch
  `pip install Pillow numpy`, eine reine Sandbox-Abhängigkeit, keine SWIFT-Code-Änderung.)
- Smallest safe change: `SwiftCliAdapter` shellt exakt diese Doku-Kommandos aus,
  mapped Exit-Codes 1-Optionen ohne Erfindung zusätzlicher Felder; `ProcessRunner`
  als Seam für Tests (`FakeProcessRunner` mit den oben real erfassten Fixtures).
- Command: `/opt/gradle/bin/gradle :core:externaladapters:test --console=plain`
  (Unit-Tests, `FakeProcessRunner`) und
  `SWIFT_REPO_PATH=/home/user/SWIFT CUE_AGENT_REPO_PATH=/home/user/CUE-AGENT
  /opt/gradle/bin/gradle :core:externaladapters:test --tests "*ManualIntegrationTest*"
  --rerun --console=plain` (echter Prozessaufruf gegen den echten SWIFT-Checkout).
- Result: PASS — 4 Unit-Tests (Erfolg → `swift.render-result`, exit 2 → `Failed`
  "missing input", exit 3 → `Failed` "Blender", falscher Contract → `BlockedExternalContract`,
  nie ausgeführt) + 2 echte Manual-Tests gegen den realen SWIFT-Checkout
  (`health()` STABLE, echter `exit 2` bei fehlendem Modell — Laufzeit 99 ms, damit
  nachweislich ein echter Subprozess und kein No-op).
- Evidence: `BUILD SUCCESSFUL`, Test-XML zeigt `time="0.142"` für die zwei Manual-Tests
  zusammen (echte Prozessausführung, kein Skip-No-op).
- Remaining risk: Kein Blender in dieser Sandbox installiert — der eigentliche
  Render-Erfolgspfad (`exit 0` mit echtem Sprite-Sheet) ist nur mit dem gefixten
  Fake-JSON in `SwiftCliAdapterTest` getestet, nicht live.

### E-03b — CueCliAdapter (passed)

- Files: s. oben (gleiches Modul/gleiche Commits)
- Contract: `cue.playable-proof/v1`, `cue.temporal-proof/v1` (aus B-01) — **kein**
  `cue.audio-proof`-Support, weil `cue audio-check` real nicht existiert (per
  `node bin/cue.js --help`, live geprüft — s. Gate H).
- Owner: CUE
- **Vertragsquelle (nicht geraten):** `CUE-AGENT/bin/cue.js` + `src/qa/{playable,temporal}.js`
  — nach `npm install` (Netzwerkzugriff auf `registry.npmjs.org` funktioniert in dieser
  Sandbox, anders als `services.gradle.org`) real ausgeführt:
  ```bash
  cd /home/user/CUE-AGENT && npm install
  node bin/cue.js --help          # bestätigt: playable-check, temporal-check, doctor, kein audio-check
  node bin/cue.js doctor --json   # echtes JSON, s. u.
  ```
  Reales `doctor --json`-Ergebnis in dieser Sandbox (unverändert übernommen als
  Test-Fixture):
  ```json
  {"ok": false, "lang": "de", "checks": [
    {"name": "Node.js", "required": true, "ok": true, "detail": "v22.22.2"},
    {"name": "Playwright Chromium", "required": true, "ok": false, "detail": "fehlt — ..."},
    {"name": "LLM-Provider", "required": true, "ok": false, "detail": "anthropic — ANTHROPIC_API_KEY fehlt/Platzhalter"},
    ...
  ]}
  ```
  JSON-Form von `playable-check`/`temporal-check` (`verdict`, `signals`, `checks`,
  `failed`, `score`) aus dem Quellcode von `src/qa/playable.js`/`src/qa/temporal.js`
  gelesen, nicht geraten. Exit `1` bedeutet dort ein echtes negatives Verdict
  ("NICHT BELEGT ..."), keinen Absturz — der Adapter behandelt daher sowohl `0` als
  auch `1` als `Produced`, nur leerer stdout als `Failed`.
- Smallest safe change: `CueCliAdapter` ruft `node bin/cue.js {doctor,playable-check,
  temporal-check} --json` auf und parst nur real vorhandene Felder.
- Command: s. E-03a (gleicher Testlauf, gleiches Modul).
- Result: PASS — 7 Unit-Tests (echtes `doctor`-JSON → DEGRADED mit den zwei fehlenden
  Required-Checks benannt, alle Required ok → STABLE, kein JSON → FAILED, negatives
  Playable-Verdict bei exit 1 → trotzdem `Produced`, `temporal-check`-Dispatch, kein
  `audio-check` → `BlockedExternalContract`, Absturz ohne stdout → `Failed`) + 1 echter
  Manual-Test gegen den realen, `npm install`-ierten CUE-AGENT-Checkout (`health()` lief
  936 ms — echter Node-Prozessstart mit echtem `doctor`-Report, DEGRADED weil
  Playwright-Chromium-Check und `ANTHROPIC_API_KEY` in dieser Sandbox fehlen).
- Evidence: `BUILD SUCCESSFUL`, Test-XML `time="0.53"`..`"0.966"` je nach Lauf.
- Remaining risk: `playable-check`/`temporal-check` selbst wurden nicht live gegen eine
  laufende URL getestet (kein Playwright-Chromium-Setup für CUE-AGENTs eigene
  `node_modules/playwright`-Kopie in dieser Sandbox verifiziert, unabhängig vom
  system-weiten `/opt/pw-browsers/chromium`); nur `doctor` lief live.

### E-03c/d — WIZARD, SHADED (weiterhin blocked — mit Begründung, nicht geraten)

- Status: blocked
- **WIZARD:** `README.md`/`.kilocode/rules/memory-bank/architecture.md` markieren die
  ANVIL-Anbindung selbst ausdrücklich als **"Konzept"** ("ANVIL as Orchestrator" steht
  unter "Architecture / Larger Projects", nicht implementiert). Die einzige reale,
  inhaltlich passende Funktion ist `buildProductionBrief()`
  (`src/lib/brief.ts`) — aber sie ist eine async TS-Funktion **innerhalb** der
  Next.js-App, die eine seedbare SQLite-Asset-Bibliothek (`data/assets.db`),
  Volltext-/Semantik-Suche und einen echten `ANTHROPIC_API_KEY` voraussetzt. Es gibt
  **keine** dedizierte API-Route dafür (`src/app/api/` hat nur `chat`, `image`,
  `health`, `memory`, `assets` — keine "brief"/"production-assessment"-Route). Ein
  Adapter müsste entweder (a) die volle Next.js-App inkl. DB+Key hochfahren (schwerer,
  zustandsbehafteter Integrationstest, kein sauberer CLI-/HTTP-Vertrag) oder (b) eine
  neue API-Route/einen neuen CLI-Einstieg **in WIZARD selbst** anlegen — beides
  außerhalb des Auftragsumfangs ("Du sollst ANVIL härten", nicht WIZARD ändern).
- **SHADED:** Laut eigener `CLAUDE.md` "Single-File, kein Build-Step" — die gesamte
  API (`window.SHADED.*`) existiert nur **im Browser-Kontext** von `index.html`. Es
  gibt kein Node-/CLI-aufrufbares Modul und keinen dokumentierten Orchestrierungs-Vertrag
  wie SWIFTs `docs/ORCHESTRATION.md`. Ein echter Adapter bräuchte Playwright-Automation
  gegen `index.html` (wie SHADEDs eigenes `tools/verify.js`) — das wäre aber ein neu
  **erfundener** Vertrag (welche `window.SHADED`-Aufrufe genau, welches Rückgabeformat),
  nicht die Übernahme eines bereits bestehenden. Genau das verbietet der Auftrag
  ("Nicht anhand der Roadmap raten").
- Smallest safe change: nicht begonnen — für beide wäre der nächste ehrliche Schritt,
  zuerst *im jeweiligen Repo* (WIZARD bzw. SHADED, außerhalb dieses ANVIL-Auftrags)
  einen dedizierten, dokumentierten ANVIL-Contract-Endpunkt/CLI zu bauen, den ANVIL
  danach adaptieren kann.
- Remaining risk: Golden Run (Gate I) bleibt unberührt — er ist laut eigener
  Spezifikation Fixture-basiert für alle externen Systeme.

## F-01 — AudioAssetManifest-Modelle

- Status: passed
- Files:
  - `anvil-kmp/modules/acoustic/src/commonMain/kotlin/io/anvil/modules/acoustic/AcousticModels.kt`
- Contract: `anvil.audio-asset-manifest/v1`
- Owner: ACOUSTIC (Produzent: `acoustic-producer`)
- Problem: `AcousticRuntimeModule` kannte nur `AudioIntent → AudioCueGraph`; es gab kein
  Modell für generiertes/lizenziertes Audio-*Material* und seine Provenienz/Kosten.
- Smallest safe change: Neue Modelle rein additiv ans Ende von `AcousticModels.kt`
  angehängt (bestehende `AudioIntent`/`AudioCueGraph`/`AcousticRuntimeModule` unverändert):
  `AudioGenerationRequest`, `AudioGenerationKind`, `AudioProviderId`, `AudioAsset`,
  `AudioAssetManifest`, `AudioLicenseInfo`, `AudioGenerationCost`,
  `AudioGenerationProvenance`, `StemBundle`, `LoopMetadata` — exakt die im Auftrag
  genannten Felder, inkl. `sourcePromptHash` (nie der rohe Prompt) statt `sourcePrompt`.
- Command: (Kompilierung mit F-02..F-06 gemeinsam getestet, s. u.)
- Result: PASS
- Evidence: s. F-05/F-06 Testlauf
- Remaining risk: keine

## F-02/F-03 — AcousticProvider-Vertrag + FakeAcousticProvider

- Status: passed
- Files:
  - `anvil-kmp/modules/acoustic/src/commonMain/kotlin/io/anvil/modules/acoustic/AcousticProvider.kt`
  - `anvil-kmp/modules/acoustic/src/commonTest/kotlin/io/anvil/modules/acoustic/FakeAcousticProvider.kt`
- Contract: intern (Provider-Vertrag, kein Cross-Modul-Contract-Registry-Eintrag nötig)
- Owner: ACOUSTIC
- Problem: Kein einheitlicher Vertrag für "eine Audioquelle" (generativ oder lizenziert),
  der `LOCAL_ONLY` vor jedem Netzwerkaufruf hart durchsetzt.
- Smallest safe change: `AcousticProvider`-Interface (`capabilities/estimate/generate`)
  exakt wie im Auftrag; `FakeAcousticProvider` als deterministischer, komplett
  netzwerkfreier Fixture-Provider (nutzt `Sha256` aus `core:artifacts` für Checksums,
  keine Duplikat-Hash-Logik).
- Command: s. F-05/F-06
- Result: PASS
- Evidence: s. F-05/F-06
- Remaining risk: keine

## F-05/F-06 — AcousticProducerModule + AudioBudgetPolicy

- Status: passed
- Files:
  - `anvil-kmp/modules/acoustic/src/commonMain/kotlin/io/anvil/modules/acoustic/AcousticProducer.kt`
  - `anvil-kmp/modules/acoustic/src/commonMain/kotlin/io/anvil/modules/acoustic/AudioBudgetPolicy.kt`
  - `anvil-kmp/modules/acoustic/src/commonTest/kotlin/io/anvil/modules/acoustic/AcousticProducerModuleTest.kt`
  - `anvil-kmp/modules/acoustic/build.gradle.kts` (Test-Dependencies + `core:artifacts`)
- Contract: `anvil.audio-asset-manifest/v1`
- Owner: ACOUSTIC
- Problem: Keine `ModuleSlotContract`-Instanz, die `ESTIMATE/GENERATE/REGISTER_EXISTING/
  VALIDATE_MANIFEST` anbietet, Budget vor Generierung prüft und `LOCAL_ONLY` durchsetzt.
- Smallest safe change: `AcousticProducerModule` bewusst getrennt von
  `AcousticRuntimeModule` (keine Verschmelzung, wie im Auftrag verboten);
  `AudioBudgetPolicy` mit den im Auftrag genannten konservativen Starter-Defaults
  (5400/2000/7000 Credits, `allowPaidRetry=false`).
- Command: `/opt/gradle/bin/gradle :modules:acoustic:allTests --console=plain`
- Result: PASS — 5 Tests grün: Fake-Provider erzeugt validierbares Manifest, `LOCAL_ONLY`
  blockiert Remote-Provider **vor** Aufruf (`called=false` im Test belegt), Budget-
  Überschreitung wird abgelehnt statt automatisch nachzuverhandeln,
  `REGISTER_EXISTING` akzeptiert ein valides externes Manifest (z. B. künftig
  Freesound/CC0), `VALIDATE_MANIFEST` erkennt rohen Prompt-Leak in `sourcePromptHash`.
- Evidence: `BUILD SUCCESSFUL in 17s`.
- Remaining risk: keine

## F-04 — ElevenLabs Provider

- Status: passed
- Files:
  - `anvil-kmp/modules/acoustic/src/jvmMain/kotlin/io/anvil/modules/acoustic/ElevenLabsAcousticProvider.kt`
  - `anvil-kmp/modules/acoustic/src/jvmTest/kotlin/io/anvil/modules/acoustic/ElevenLabsAcousticProviderTest.kt`
  - `anvil-kmp/core/artifacts/src/commonMain/kotlin/io/anvil/core/artifacts/Sha256.kt`
    (`ByteArray`-Overload für binäre Audio-Checksums, additiv)
  - `anvil-kmp/modules/acoustic/build.gradle.kts` (jvmMain Ktor-Client-Deps, jvmTest
    `ktor-client-mock`)
- Contract: intern (Provider-Vertrag)
- Owner: ACOUSTIC
- Problem: Kein echter ElevenLabs-Adapter; Auftrag verlangt ausdrücklich "keine
  Endpoint-Namen raten".
- Smallest safe change: Vor der Implementierung wurden die aktuellen offiziellen
  ElevenLabs-Docs per `WebFetch` geprüft (nicht geraten):
  - Music: `POST https://api.elevenlabs.io/v1/music`, Body-Felder `prompt`,
    `music_length_ms`, `model_id`, `force_instrumental`, `seed`, Auth-Header `xi-api-key`.
  - Sound Effects: `POST https://api.elevenlabs.io/v1/sound-generation`, Body-Felder
    `text`, `duration_seconds`, Auth-Header `xi-api-key`.
  Danach `ElevenLabsAcousticProvider` mit injizierbarem `HttpClient` (testbar per
  Ktor `MockEngine`, kein Live-Call in Tests), Timeout-Client-Factory für Produktion,
  Byte-Limit für Responses, Redaction-Funktion für Fehlertexte (API-Key-Muster werden
  vor jeder Log-/Fehlerausgabe entfernt), `LOCAL_ONLY`- und Budget-Check **vor** jedem
  Netzwerkaufruf.
- Command: `/opt/gradle/bin/gradle :modules:acoustic:allTests --console=plain`
- Result: PASS — 7 Tests grün: korrekter Request (URL, `xi-api-key`-Header,
  `music_length_ms` im Body), fehlendes Secret blockiert **ohne** Netzwerkaufruf,
  `LOCAL_ONLY` blockiert **ohne** Netzwerkaufruf, Budget-Überschreitung blockiert
  **ohne** Netzwerkaufruf, Fehlerantwort wird redigiert (kein API-Key im
  `Failed.reason`), erfolgreicher Call liefert Manifest-fähiges Asset ohne Key-Leck,
  SFX-Schätzung nutzt den flachen Credit-Wert. Insgesamt 12 Tests in
  `:modules:acoustic:jvmTest` grün (5 Producer + 7 ElevenLabs).
- Evidence: `BUILD SUCCESSFUL in 5s`.
- Remaining risk: `providerGenerationId` wird bewusst `null` gelassen — die Docs, die
  ich abgerufen habe, belegen keinen stabilen Response-Header/-Feld für eine
  Generation-ID bei den binären Audio-Endpunkten; das wäre geraten gewesen, daher
  ausgelassen statt erfunden. `I-03` (manueller Live-Smoke-Test) ist nicht ausgeführt
  worden (kein `ELEVENLABS_API_KEY` in dieser Sandbox, s. Gate I).

## F-07 — Freie lokale Lückenfüller (Basic Pitch, librosa, ACE-Step, Demucs, Freesound)

- Status: blocked (bewusst deferred)
- Files: keine
- Contract: n/a
- Owner: ACOUSTIC
- Problem: Auftrag verlangt vor jedem Adapter eine geprüfte CLI-/Lizenz-Version und
  `doctor()`-Gate; in dieser Sandbox ist weder `basic-pitch` noch `librosa` noch
  `demucs` installiert, ein `doctor()` könnte also nie gegen eine echte Installation
  verifiziert werden.
- Smallest safe change: nicht begonnen.
- Command: keiner.
- Result: n/a
- Evidence: n/a
- Remaining risk: Golden Run benötigt diese Provider nicht (nutzt `FakeAcousticProvider`).
  Auftrag selbst sagt: "ACE-Step, Demucs und Freesound folgen nach dem Golden Run" —
  konsistent mit dieser Priorisierung.

## G-01/G-02/G-03 — Web Audio Runtime + ToneJsRuntimeWriter + WebTargetWriter

- Status: passed
- Files:
  - `anvil-kmp/modules/acoustic/src/commonMain/kotlin/io/anvil/modules/acoustic/AcousticModels.kt`
    (`WebAudioRuntimeBundle`, additiv)
  - `anvil-kmp/modules/target/src/jvmMain/kotlin/io/anvil/modules/target/web/ToneJsRuntimeWriter.kt`
  - `anvil-kmp/modules/target/src/jvmMain/kotlin/io/anvil/modules/target/web/WebTargetWriter.kt`
  - `anvil-kmp/modules/target/src/jvmTest/kotlin/io/anvil/modules/target/web/WebTargetWriterTest.kt`
  - `anvil-kmp/modules/target/build.gradle.kts` (Test-Dependencies)
- Contract: `anvil.web-audio-runtime-bundle/v1`
- Owner: ACOUSTIC (Modell), TARGET (Writer)
- Problem: Kein Weg von `AudioAssetManifest`/`AudioCueGraph` zu einer tatsächlich im
  Browser lauffähigen Runtime-Datei.
- Smallest safe change: `WebAudioRuntimeBundle` rein additiv in `AcousticModels.kt`.
  `ToneJsRuntimeWriter`/`WebTargetWriter` liegen bewusst in `modules:target`, nicht in
  `modules:acoustic` — und nehmen **nur rohe JSON-Strings/Refs** entgegen, niemals
  `AudioAssetManifest`/`SceneBundle`/... als Kotlin-Typ, weil CLAUDE.md §3 verbietet,
  dass ein `:modules:*` ein anderes `:modules:*` importiert. `WebTargetWriter.assemble()`
  liefert ausschließlich `WebTargetStatus.ASSEMBLED` — nie `RUNNABLE` (bräuchte einen
  gestarteten Prozess + Healthcheck) oder `VERIFIED` (nur aus einem echten CUE-Proof).
- Command: `/opt/gradle/bin/gradle :modules:target:allTests --console=plain`
- Result: PASS — 2 Tests grün: alle 8 erwarteten Dateien werden geschrieben inkl.
  Preload/Bus-Konstanten und `ANVIL_AUDIO`-Debug-Hook in `runtime.ts`; `runtime.ts`
  enthält nie-Autoplay-Kommentar und eine `disposeAudioRuntime()`-Funktion.
- Evidence: `BUILD SUCCESSFUL in 3s`.
- Remaining risk: `runtime.ts` wurde nicht durch einen echten TypeScript-/Tone.js-Compiler
  geprüft (kein Node/npm-Toolchain-Zugriff in dieser Sandbox verifiziert) — nur als
  String-Template getestet. Empfehlung: vor echtem Web-Einsatz `tsc --noEmit` gegen eine
  echte `tone`-Dependency laufen lassen.

## Gate H — CUE Audio Proof (Contract-Ebene)

- Status: passed (Contract-Teil), Rest bewusst deferred
- Files: `cue.audio-proof` bereits in B-01 registriert (Owner CUE, Producer `cue`,
  Consumer `bard`, `commander`).
- Contract: `cue.audio-proof/v1`
- Owner: CUE
- Problem: Kein registrierter Contract für Audio-Proofs getrennt von Playable/Temporal.
- Smallest safe change: s. B-01 (bereits umgesetzt); der vorgeschlagene CUE-Befehl
  `cue audio-check <url> --scenario <json> --json` und die Evidence-Kategorien
  (`CUE_FIRED, STATE_REACTION, TRANSITION_TIMING, LOOP_CONTINUITY, CLIPPING_CHECK,
  VOICE_AUDIBILITY`) sind in `GoldenRunFakeCuePort` (Gate I) als Fixture-Payload
  abgebildet — die reale CUE-Implementierung liegt im CUE-AGENT-Repo und wurde in
  dieser Session nicht angefasst (kein Auftrag, dort zu arbeiten).
- Command: s. B-01.
- Result: PASS (Contract-Teil).
- Evidence: s. B-01; `window.ANVIL_AUDIO.getDebugState/setState/getEventLog` ist in
  `ToneJsRuntimeWriter`'s generiertem `runtime.ts` vorhanden (Gate G-02).
- Remaining risk: Der reale `cue audio-check`-Befehl existiert nicht — das ist im
  CUE-AGENT-Repo zu implementieren, außerhalb des Scopes dieses ANVIL-Auftrags.

## I-01/I-02 — Golden Run Fixture + End-to-End-Test

- Status: passed
- Files:
  - `anvil-kmp/settings.gradle.kts` (`:surfaces:golden-run` ergänzt)
  - `anvil-kmp/surfaces/golden-run/build.gradle.kts`
  - `anvil-kmp/surfaces/golden-run/src/commonTest/kotlin/io/anvil/surfaces/goldenrun/fixtures/GoldenRunFixture.kt`
  - `anvil-kmp/surfaces/golden-run/src/commonTest/kotlin/io/anvil/surfaces/goldenrun/GoldenRunTest.kt`
  - `anvil-kmp/core/contracts/src/commonMain/kotlin/io/anvil/core/contracts/ContractRegistry.kt`
    (kleine Korrektur: `anvil.scene-bundle` erlaubt jetzt auch `interface`/`acoustic` als
    Consumer — echter, beim Bau des Golden Run entdeckter Registry-Lücke, s. u.)
- Contract: alle unter B-01 registrierten Contracts gemeinsam
- Owner: ANVIL (Orchestrierung), jeweiliges Modul/System (Fachwahrheit)
- Problem: Kein einziger Test bewies, dass Gameplay→Scene→Interface→Acoustic→Target
  tatsächlich als **eine** `RunPlan` durch `RunSurface` läuft, echte Artifacts erzeugt,
  über `ArtifactStore` persistiert und zu Handoff/Sync exportiert werden kann.
- **Architektur-Entscheidung (abweichend vom wörtlichen Dateipfad im Auftrag):** Der
  Auftrag nennt `anvil-kmp/core/run/src/commonTest/.../GoldenRunFixture.kt` und
  `GoldenRunTest.kt`. Das hätte aber bedeutet, dass `:core:run` testweise
  `:modules:gameplay`, `:modules:scene`, `:modules:interface`, `:modules:acoustic`,
  `:modules:target` importiert — und CLAUDE.md §3 verbietet ausdrücklich und ohne
  Ausnahme, dass `core` jemals `modules` importiert ("Verboten: Zirkuläre
  Abhängigkeiten. `core` darf nie `modules` importieren."). Da dieses Verbot härter
  wiegt als ein wörtlicher Dateipfad im Fix-Prompt, liegt der Golden Run stattdessen in
  einem neuen, dafür vorgesehenen `:surfaces:golden-run`-Modul (die Architektur-Tabelle
  erlaubt exakt das: "`surfaces:* → darf :core:*, :modules:* importieren`"). Inhalt und
  Testfälle folgen sonst 1:1 der Spezifikation.
- **Registry-Lücke, die der Golden Run aufgedeckt hat:** `InterfaceIntent` und
  `AudioIntent` tragen beide ein reales `sceneBundleRef`-Feld, aber `anvil.scene-bundle`
  erlaubte ursprünglich nur `target`, `cue`, `shaded` als Consumer — nicht `interface`
  oder `acoustic`. Das hätte den Golden Run mit `ContractViolationException` blockiert,
  obwohl die Modul-Modelle diese Abhängigkeit real vorsehen. Behoben durch Erweiterung
  der bestehenden `anvil.scene-bundle`-Deskriptor-Zeile (keine neue Zeile, keine
  Duplizierung) — genau die Art Fehler, die dieser Golden Run laut Auftrag aufdecken soll.
- **Zweiter Bug, gefunden und behoben:** `GoldenRunTest` serialisierte `GameplayPlan`
  zunächst mit einer `Json`-Instanz ohne `classDiscriminator = "kind"`. `Condition`/
  `Effect` (sealed classes in `GameplayModels.kt`) tragen ein eigenes `type`-Property,
  das mit kotlinx.serializations Standard-Diskriminator `"type"` kollidiert — ein
  `IllegalStateException` beim ersten Testlauf. `GameplayCompilerModule` selbst nutzt
  bereits `classDiscriminator = "kind"` intern; der Test wurde angepasst, um denselben
  Diskriminator zu verwenden (kein Produktionscode geändert).
- Command: `/opt/gradle/bin/gradle :surfaces:golden-run:allTests --console=plain`
- Result: PASS (nach zwei dokumentierten Fixversuchen, s. o.) — 2 Tests grün:
  - `goldenRun_producesArtifactsHandoffAndSyncBundleAcrossTheWholeChain`: BARD-Fixture →
    WIZARD-Fixture → Gameplay/Scene/Interface/Acoustic/Acoustic-Producer/Target via
    **einer** `RunPlan` mit 6 Steps und echten `dependsOn`/`inputContract`/
    `outputContract` → `RunStatus.COMPLETE`, 6/6 Steps `COMPLETED`, 6 Artifacts in der
    Registry, alle mit `sha256:`-Checksum → CUE-Fixture liefert 3 **getrennte**
    Proof-Contracts (playable/temporal/audio) → `ArtifactStore` persistiert einen echten
    Payload mit Checksum-Verifikation → `HandoffExporter` exportiert 3 Artifact-Refs →
    `WorkspaceSyncService` exportiert ein Bundle mit allen 6 Artifacts, keine Warnungen.
  - `goldenRun_negativeCase_unknownContractVersionBlocksBeforeExecution`: unbekannte
    Contract-Version blockiert den Run (`RunStatus.BLOCKED`), das Modul wird nachweislich
    **nie** aufgerufen (`executionCount == 0`).
- Evidence:
  ```text
  BUILD SUCCESSFUL in 2s
  run.status=COMPLETE
  run.planId=PLAN_GOLDEN_RUN
  artifact.count=6
  handoff.artifactId=ART_HANDOFF_6224801f
  sync.artifactId=ART_SYNC_3e152e44
  cue.playableProof=cue.playable-proof
  cue.temporalProof=cue.temporal-proof
  cue.audioProof=cue.audio-proof
  ```
  Vollständiger Regressionslauf danach über alle Module (`core:contracts`,
  `core:artifacts`, `core:run`, `core:handoff`, `core:sync`, `modules:gameplay`,
  `modules:scene`, `modules:interface`, `modules:acoustic`, `modules:target`,
  `surfaces:golden-run`, `modules:bellows`) sowie `app:bellows-gateway:compileKotlin`
  + `app:bellows-gateway:test` — alle grün, keine Regression durch die
  Registry-Erweiterung oder die neuen Module.
- Remaining risk: Der Golden Run nutzt **Fixtures** für BARD/WIZARD/SWIFT/SHADED/CUE
  (wie in der Gate-I-Spezifikation selbst vorgesehen: "BARD-Fixture", "WIZARD-Fixture",
  ..., "CUE-Fixture"). Reale Adapter sind E-03 (deferred). `RunSurface` selbst ruft
  keinen `ArtifactStore` auf (D-04-Design, dokumentiert) — der Store-Persistenz-Beweis
  im Golden Run nutzt `ArtifactWriter.writeAndPersist` direkt neben, nicht innerhalb,
  der `RunSurface`-Ausführung.

## I-03 — Manueller ElevenLabs-Smoke-Test

- Status: blocked (bewusst — kein Secret verfügbar)
- Files: keine
- Contract: n/a
- Owner: ACOUSTIC
- Problem: Auftrag verlangt `RUN_ELEVENLABS_SMOKE=true` + `ELEVENLABS_API_KEY`; keines
  von beidem ist in dieser Sandbox gesetzt.
- Smallest safe change: nicht ausgeführt — keine Live-Calls ohne Secret erzwingen.
- Command: keiner.
- Result: n/a
- Evidence: n/a
- Remaining risk: `ElevenLabsAcousticProviderTest` (F-04) deckt das gesamte
  Anfrage-/Fehler-/Budget-/Redaction-Verhalten bereits per `MockEngine` ab; nur der
  echte Netzwerk-Pfad selbst ist unverifiziert. Empfehlung: in einer Umgebung mit
  echtem Key `RUN_ELEVENLABS_SMOKE=true ELEVENLABS_API_KEY=... ./gradlew
  :modules:acoustic:jvmTest --tests "*ElevenLabsManualSmokeTest*"` (Testdatei müsste
  zuerst angelegt werden — nicht in dieser Session, da nicht verifizierbar).

