# ANVIL_CONCEPT_CONTRACT.md

**Gate:** A2 — Naming & Concept Contract  
**Stand:** 2026-05-08  
**Status:** Verbindlich

---

## Kanonische Begriffe

### Anvil

Die **Werkbank**. Eine minimale IDE / Entwicklungsumgebung, die als Basis dient.  
Anvil organisiert Workspaces, bindet Module an und produziert Artifacts.  
Anvil ist *nicht* eine App-Sammlung, kein Store, kein Dashboard.

> Anvil = das Werkzeug, auf dem geschmiedet wird.

### The Forge

Das **Launchpad**. Die Übersicht aller verfügbaren Module.  
The Forge zeigt: welches Werkzeug liegt bereit, in welchem Zustand ist es, was kann ich damit starten.  
The Forge ist *nicht* ein App Store. Es ist die Werkzeugleiste der Werkbank.

> The Forge = die Werkstatt, in der die Werkzeuge bereitliegen.

### Micro-App

Eine eigenständige, kleine Anwendung, die aus Anvil-Modulen zusammengebaut wurde.  
Ein Micro-App ist das *Ergebnis* eines Build-Prozesses, nicht ein Modul selbst.

### Module Slot

Der standardisierte Andockpunkt, über den ein Modul in Anvil eingebunden wird.  
Jedes Modul füllt genau einen Slot. Slots haben einen definierten Contract (siehe `MODULE_CONTRACT.md`).

### Blueprint

Eine gespeicherte Konfiguration / Vorlage für einen Workspace.  
Blueprints beschreiben, welche Module mit welchen Inputs zusammenarbeiten.

### Artifact

Das Ergebnis eines Build- oder Verarbeitungsschritts.  
Beispiele: eine APK, ein generiertes Dokument, ein exportierter Datensatz, ein transformierter Text.

### Build Target

Die Zielplattform oder das Zielformat eines Artifacts.  
Beispiele: `android-apk`, `desktop-exe`, `export-json`, `export-pdf`.

---

## Verbotene Begriffe

| Begriff | Grund | Korrekte Alternative |
|---------|-------|---------------------|
| Anvil Hub | Erzeugt App-Store-Assoziation | The Forge / Anvil |
| Anvil Store | Anvil verkauft nichts | The Forge |
| Anvil Dashboard | Anti-Dashboard-Prinzip | Anvil Shell / Werkbank |
| Anvil Home | Generisch, sagt nichts | Werkbank / Startoberfläche |
| Module Library | Bibliothek klingt passiv | The Forge |
| App Launcher | Anvil launcht keine Apps, es baut sie | The Forge |

---

## Abgrenzung

### Anvil ≠ Ink & Iron Glow

**Korrigiert (2026-07-14):** Diese Zeile stand hier vorher falsch: IIG war als
"Tattoo-Studio-Marke" mit "Studio-Kunden" als Zielgruppe beschrieben — nie verifiziert,
vermutlich aus dem Namensbestandteil "Ink" hergeleitet. Vom Nutzer direkt korrigiert:
**Ink & Iron Glow (IIG)** ist das unabhängige Kunstwerkstudio des Nutzers — Bücher,
Malerei, Tools, bewusst formoffen, keine einzelne Ausdrucksform. Anvil war ursprünglich
als eigenständige IDE gedacht; mittlerweile ist Anvil der Kopf dieses Indie-Studios — der
operative Kern, über den die Pipeline-Module (DECOMPILE, SHADED, LAB, TRIVIUM, CUE-AGENT,
SWIFT, MIXTRACT, Anvil-Bellows, MYTHIC, ...) zu einem fast autarken Spielstudio
zusammenlaufen. "Anvil ≠ IIG" gilt also als Ebenen-Unterscheidung (Anvil = Werkzeug/
operativer Hub, IIG = die kreative Praxis/Identität dahinter), nicht als Aussage über
getrennte, unverbundene Projekte.

| | Anvil | Ink & Iron Glow (IIG) |
|---|---|---|
| **Was** | Werkzeug / operativer Hub der Pipeline | unabhängiges, formoffenes Kunstwerkstudio |
| **Design** | Werkstatt-Ästhetik, State Surfaces | IIG Design System (kupfer/dunkel) |
| **Repos** | `ANVIL` | `Homepage`, `Anvil-Bellows` (ehem. CATALON-GUARD) |

Anvil-UI-Texte dürfen keine IIG-Branding-Elemente enthalten.  
The Forge ist kein Inki-Feature.

### Anvil × Anvil-Bellows (ehem. CATALON-GUARD)

Anvil-Bellows ist ein eigenständiges Repo/Deployment (Python/LiteLLM, kein Teil des
Kotlin-Monorepos `anvil-kmp`) — organisatorisch IIG-Infrastruktur, kein Anvil-IDE-natives
Modul. Das schließt geteilte Nutzung nicht aus, im Gegenteil: Anvil-Bellows ist der
netzwerk-erreichbare, budget-geschützte Multi-Provider-Gateway (Vertex AI, OpenRouter,
...), den sowohl die Nicht-Kotlin-Pipeline-Repos (DECOMPILE, SHADED, LAB, CUE-AGENT —
direkt per `BELLOWS_BASE_URL`, siehe `Anvil-Bellows/README.md`) als auch Anvils eigener
Kotlin-nativer Router (`:modules:bellows`, Gate B9) als **einen konfigurierten
Upstream-Provider** nutzen können, statt Cloud-Credentials zweimal zu verwalten. Siehe
`modules/bellows/README.md` für das Kompositions-Rezept. Die beiden Implementierungen
bleiben bewusst getrennte Prozesse (unterschiedliche Stacks, unterschiedliche
Default-Ports — 8765 vs. 4000) und stehen nicht in Konkurrenz zueinander: Anvils
Kotlin-Router ist die In-Process-Schnittstelle für Anvils eigene Module/Orchestrierung
(`RunSurface`); der standalone Proxy ist das dauerhaft laufende Backend, das jede
Sprache/jedes Repo über HTTP erreichen kann.

---

## Zustandssprache (State Surface Grammar)

Anvil nutzt vier kanonische Zustände für alle Oberflächen:

| Zustand | Bedeutung | Farb-Intent |
|---------|-----------|-------------|
| `Stable` | Alles in Ordnung, kein Handlungsbedarf | Grün / ruhig |
| `Adapting` | System arbeitet, Nutzer muss warten | Blau / neutral |
| `Act Now` | Nutzer muss handeln | Orange / warm |
| `Failed` | Fehler, Eingriff nötig | Rot / dringend |

Diese Zustände gelten für Workspaces, Module und Build Targets gleichermaßen.
