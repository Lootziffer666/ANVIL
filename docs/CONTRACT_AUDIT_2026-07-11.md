# Contract Audit — Selbstkritik und Korrekturplan

**Datum:** 2026-07-11
**Scope:** von diesem Sprint erzeugte KMP-Cores und contract-first Studio-Module
**Ton:** ehrlich, absichtlich selbstkritisch

---

## Roast

Ich habe in einem Sprint sehr viel Oberfläche erzeugt und dabei mehrere typische
Architektur-Sünden begangen:

1. **Zu breit, zu schnell.** Ich habe BARD-Code fälschlich im ANVIL-Repo begonnen und außerdem Gameplay, Scene, Interface, Acoustic,
   Target, Artifact, Run, Handoff und Sync in kurzer Folge angelegt. Das erzeugt
   Momentum, aber auch viel Contract-Fläche ohne ausreichend Tests.
2. **Contracts waren teilweise nur nominell hart.** Viele Modelle tragen Schema-IDs,
   aber es gab noch keine zentrale Registry, keine Kompatibilitätsmatrix und kaum
   negative Tests für falsche Versionen.
3. **Artifact-Refs waren nicht temporal sauber.** Module setzten Platzhalter wie
   `pending-artifact-writer-timestamp`. Das war ein Vertragsbruch: Ein Artifact-Ref
   darf nicht so tun, als habe er Zeitbeweis, wenn er nur einen Platzhalter trägt.
4. **Checksums sind nur oberflächlich gehärtet.** Der Vertrag nennt `sha256`, aber
   die MVP-Module erzeugen aktuell stabile Kurz-Hashes mit `sha256:`-Prefix. Das ist
   für stabile IDs praktisch, aber kein echter kryptografischer Hash. Für einen
   späteren Release-Gate muss ein echter SHA-256-Provider in die Artifact-Schicht.
5. **Zu viele raw Strings.** Obwohl die Repo-Regeln Inline-ID-Klassen bevorzugen,
   tragen einige Cross-Module-Refs noch `String`. Für ein MVP ist das kompiliert,
   aber die Contract Registry muss diese Ref-Typen kanonisieren.
6. **RunSurface ist ein guter Anfang, aber noch kein Execution Core.** Es schreibt
   Artifacts und sequenziert Module, besitzt aber noch keine Retry-, Policy-,
   Migration-, Version- oder Dependency-Auflösung.
7. **Bellows-Docker wurde richtig getrennt, aber nur dokumentiert.** Die echte
   Operationalisierung gehört ins separate Anvil-Bellows Repo und ist hier nicht
   erledigt.

---

## Sofort geradegezogen

### 1. Temporal Contract für ModuleArtifactRef

`ModuleContext` trägt jetzt `createdAt`. `RunSurface` reicht seinen Run-Zeitstempel
an Module weiter. Die Module schreiben diesen Wert in `ModuleArtifactRef.timestamp`
statt Platzhalter zu verwenden.

Damit ist klar:

```text
RunSurface.createdAt
        ↓
ModuleContext.createdAt
        ↓
ModuleArtifactRef.timestamp
        ↓
ArtifactWriter.createdAt / ArtifactManifest.createdAt
```

### 2. Scene-Contract-Konstruktion

`SceneCompiler` nutzt für schema-getragene Modelle mit Default-Parametern benannte
Argumente. Dadurch werden `schema`, `id`, `label` und `transform` nicht mehr durch
Positionsargumente verwechselt.

### 3. Bellows Build/Test-Fehler

Bellows-Gateway löst Gateway-Key-Refs jetzt über lokale Werte auf, damit Kotlin
keinen Smart-Cast über fremde public Properties benötigt. `listModels()` liefert
konfigurierte Modelle statt zusätzlich einen virtuellen Auto-Eintrag in `/v1/models`.

### 4. Echte Payload-Checksums im ArtifactWriter

`ArtifactWriter` berechnet jetzt selbst einen echten SHA-256 über den Payload und
schreibt diesen Wert ins `ArtifactManifest`. Modul-Refs dürfen weiterhin stabile
IDs/URIs vorschlagen, aber die kanonische Manifest-Prüfsumme kommt aus der
Artifact-Schicht.

### 5. Maschinenlesbare Contract Registry

`core:contracts` enthält jetzt `ContractRegistry`, `ContractDescriptor` und eine
erste `AnvilContractRegistry.default` mit Ownern, Produzenten, Konsumenten und
Fail-closed-Verhalten für die wichtigsten Sprint-Contracts.

### 6. BARD ausgelagert

Der BARD-Code und das `bard-profile` wurden aus dem ANVIL-Repo entfernt. ANVIL hält nur
noch Contract-Refs und Registry-Einträge; die Implementierung gehört in ein eigenes
privates BARD-Repo.

---

## Bleibende Vertrags-Schulden

Diese Punkte sind bewusst nicht heimlich als erledigt markiert:

1. **Hash-Port fehlt noch.** Der ArtifactWriter hat jetzt commonMain-SHA-256,
   aber ein späterer Release-Gate sollte das hinter einen testbaren Hash-Port legen.
2. **Contract Registry ist noch MVP.** Owner, Producer und Consumer existieren,
   aber Migrationen, Fixtures und Kompatibilitätsregeln müssen folgen.
3. **Typed refs sind uneinheitlich.** Interne IDs sind oft Inline-Klassen, aber
   Cross-Artifact-/Cross-Module-Refs sind noch häufig Strings.
4. **Module-Outputs sind Demo-Qualität.** Die Module beweisen Contract-Form, nicht
   echte Produktionsintelligenz.
5. **End-to-End-Proof fehlt.** Es gibt Builds, aber noch keinen kleinen Seed → Run →
   Artifacts → Handoff → Sync Durchstich.

---

## Nächste harte Gate-Empfehlung

Nicht weiter verbreitern. Nächste Gate sollte lauten:

> **Contract Hardening Gate:** echte Checksums, Contract Registry, typed refs,
> Serialization-Roundtrips und ein Minimal-End-to-End-Run.

Definition of Done:

1. `ArtifactWriter` hat Roundtrip-Tests für echte SHA-256-Manifeste.
2. Contract Registry enthält Migrationen und Beispiel-Fixtures.
3. Jeder Contract kennt Owner, erlaubte Produzenten, erlaubte Konsumenten und
   Fail-closed-Regel bei unbekannter Version.
4. Serialization-Roundtrip-Tests für alle Core-Contracts laufen.
5. Ein minimaler Run erzeugt mindestens zwei Module-Outputs, ein Handoff-Paket und
   ein Sync-Bundle.
