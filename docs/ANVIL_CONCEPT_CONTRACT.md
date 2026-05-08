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

| | Anvil | Ink & Iron Glow (IIG) |
|---|---|---|
| **Was** | IDE / Entwicklungswerkbank | Tattoo-Studio-Marke |
| **Zielgruppe** | Entwickler (Christian) | Studio-Kunden |
| **Design** | Werkstatt-Ästhetik, State Surfaces | IIG Design System (kupfer/dunkel) |
| **Repos** | `ANVIL` | `Homepage`, `Anvil-Bellows` (ehem. CATALON-GUARD) |

Anvil-UI-Texte dürfen keine IIG-Branding-Elemente enthalten.  
The Forge ist kein Inki-Feature.

### Anvil ≠ Anvil-Bellows (ehem. CATALON-GUARD)

Anvil-Bellows ist ein eigenständiges Projekt unter dem IIG-Dach.  
Der Name „Bellows" (Blasebalg) referenziert die Schmiede-Metapher, gehört aber zum IIG-Ökosystem, nicht zu Anvil-IDE.

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
