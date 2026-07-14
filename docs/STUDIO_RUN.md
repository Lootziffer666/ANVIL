# Studio Run — Bedienung

`:app:studio-run` ist die "3 Random words und ne Engineauswahl"-CLI: nimmt einen
Seed (ein paar Worte) + eine Ziel-Engine, baut daraus einen echten `RunPlan` über
Anvils native Modulkette (Gameplay → Scene → Interface → Acoustic → Target) und führt
ihn über den echten `RunSurface` aus — umschlossen von echten `ExternalToolPort`-Adaptern
für WIZARD/SHADED/SWIFT/CUE-AGENT, wo konfiguriert.

Vorher gab es jeden einzelnen Baustein (Adapter, Module, RunSurface) einzeln bewiesen
(Gate E-03, R-19..21, Gate I) — aber kein Programm, das sie außerhalb von Tests
tatsächlich zusammensteckt. Das ist Gate C1.

## 1. Bauen & ausführen

```bash
cd anvil-kmp
./gradlew :app:studio-run:installDist
app/studio-run/build/install/studio-run/bin/studio-run --seed "lantern rust harbor"
```

## 2. Beispiel — nur Fixtures (kein externes System konfiguriert)

```bash
studio-run --seed "lantern rust harbor"
```

Läuft vollständig offline: BARD/WIZARD/SWIFT/SHADED/CUE sind alle `FIXTURE` (klar so
beschriftet, jede Fixture-Payload trägt `"fixture":true`). Die native Modulkette
(Gameplay/Scene/Interface/Acoustic/Target) läuft **immer echt** — das ist reiner
Anvil-Kotlin-Code, kein externes System nötig.

## 3. Beispiel — mit echten Systemen

```bash
export WIZARD_BASE_URL=http://localhost:3000       # `npm run dev` in WIZARD muss laufen
export SHADED_REPO_PATH=/home/user/SHADED
export SWIFT_REPO_PATH=/home/user/SWIFT
export CUE_AGENT_REPO_PATH=/home/user/CUE-AGENT
studio-run --seed "lantern rust harbor" \
  --engine WEB --platform WEB \
  --cue-target-url http://localhost:8080
```

Jedes gesetzte Repo/URL wird als `REAL` markiert und sein `health()` real geprüft. WIZARDs
`production-assessment` läuft real gegen den Brief-Text aus dem Seed. CUEs
playable/temporal/audio-proof laufen nur, wenn `--cue-target-url` gesetzt ist — `S_TARGET`
produziert einen Build-**Plan**, keine servierte URL; ohne eine bereits laufende, erreichbare
Instanz gibt es real nichts, was CUE prüfen könnte.

## 4. CLI-Referenz

| Flag | Bedeutung | Default |
|------|-----------|---------|
| `--seed "wort wort wort"` | Pflicht. Seed-Worte, whitespace-getrennt. | — |
| `--engine` | `UNREAL`\|`GODOT`\|`WEB`\|`KORGE`\|`UEFN` | `WEB` |
| `--platform` | `WINDOWS`\|`ANDROID`\|`WEB`\|`LINUX`\|`MACOS` | `WEB` |
| `--verbs a,b,c` | Gameplay-Verben | `interact` |
| `--roles a,b,c` | Spielerrollen | `player` |
| `--wizard-url` | überschreibt `WIZARD_BASE_URL` | — |
| `--shaded-repo` | überschreibt `SHADED_REPO_PATH` | — |
| `--swift-repo` | überschreibt `SWIFT_REPO_PATH` | — |
| `--cue-repo` | überschreibt `CUE_AGENT_REPO_PATH` | — |
| `--cue-target-url` | bereits laufende Build-URL für echte CUE-Proofs | — |
| `--out <dir>` | Ausgabeverzeichnis für `run-summary`/`handoff`/`sync-bundle`.json | `studio-run-out/<runId>` |

## 5. Status & Grenzen (ehrlich)

- **BARD bleibt immer Fixture.** Es existiert nirgends im Pipeline-Verbund ein echter
  BARD-Adapter — "drei Worte → vollständiges kreatives Brief" ist kein gelöstes Problem,
  dieses Tool tut nicht so als ob.
- **Audio-Asset-Erzeugung ist bewusst nicht Teil des Plans** (`AcousticProducerModule` +
  ein echter `AcousticProvider`). Der Nutzer baut diese Pipeline separat.
  `AcousticRuntimeModule` (AudioIntent → AudioCueGraph) läuft trotzdem echt mit — reiner
  Anvil-Code, kein externes Audio-Backend nötig.
- **SHADED/SWIFT laufen für diesen Gate nur als Health-Check real mit**, nicht mit echten
  `invoke()`-Aufrufen — ihre echten Contracts brauchen konkrete Asset-Pfade (eine
  Szenen-PNG, ein FBX-Modell), die sich nicht aus drei Worten erzeugen lassen, ohne genau
  das zu erfinden, was dieses Tool nicht erfinden soll.
- **CUEs Proofs brauchen eine bereits servierte Build-URL.** `S_TARGET` liefert einen
  Build-Plan, kein deploytes/laufendes Spiel — echtes Deployment ist MYTHICs Aufgabe
  (Auto-Deploy, ausdrücklich auf eigene Repos beschränkt), nicht dieses CLIs.
