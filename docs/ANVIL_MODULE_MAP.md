# ANVIL_MODULE_MAP.md

**Gate:** 001 — Core Skeleton  
**Stand:** 2026-05-12  
**Status:** Verbindlich

---

> Default to absence. Do not add visible UI unless the current state requires it. Empty space is an active design state, not unused space.
>
> Every visible element must justify its existence by reducing user burden right now.
>
> Before adding anything, remove. Before proposing UI, justify absence. Before creating controls, name the state that makes them necessary. If no state requires the element, do not render it.

---

## Module Matrix

| Modul | Ursprung | Zweck | Darf | Darf nicht | v0-Status | Offene Konflikte |
|---|---|---|---|---|---|---|
| **OPENDORK** | New (Gate 001) | Provider/API routing, Modellwahl, Fallbacks | Mehrere Provider verwalten, Modell pro Aufgabe wählen, Fallback-Ketten definieren | Provider-Logik in Workflows hartcodieren, einen einzigen Provider erzwingen | `skeleton` | `src/core/opendork/` → `src/core/providers/` (Namens-Mapping) |
| **CATALON** | New (Gate 001) | Agent-/Workflow-Orchestrierung, Plan-Task-Run-Modell | Pläne anlegen, Tasks dekomponieren, Runs isolieren | Direkt ins Dateisystem schreiben ohne Run-Kontext, Scope außerhalb des Workspace modifizieren | `skeleton` | Aufgeteilt auf `planning/`, `tasks/`, `runs/` |
| **CATALON-GUARD** | New (Gate 001) | Guardrails, Gate-Enforcement, Qualitätskontrolle | Gates prüfen, Kill-Kriterien durchsetzen, Human-Review anfordern | Gates stillschweigend überspringen, Akzeptanz-Kriterien ändern ohne neuen Gate-Commit | `skeleton` | `src/core/catalon-guard/` → `src/core/safety/` (Namens-Mapping) |
| **DEAFPIPER** | New (Gate 001) | Strukturierte Übergabe, Agent-zu-Agent-Kommandokanal | Handoff-Pakete erzeugen, Kontext für nächsten Agent exportieren | Stille Übergaben ohne Artefakt, Kontext in Freitext-Zusammenfassungen verlieren | `skeleton` | Kein Gegenstück in existierender Skeleton-Struktur |
| **registry** | New (Gate 001) | Projekt- und Workspace-Verzeichnis | Bekannte Projekte eintragen, Statusänderungen verfolgen | Projekte stillschweigend entfernen, Einträge ohne Gate-Referenz anlegen | `skeleton` | Kein Gegenstück in existierender Skeleton-Struktur |
| **gates** | New (Gate 001) | Gate-Ausführungs-Engine | Gate-Status lesen, Acceptance-Kriterien prüfen, Kill-Kriterien auswerten | Gates automatisch als done markieren ohne verifizierbaren Output | `skeleton` | Kein Gegenstück in existierender Skeleton-Struktur |
| **artifacts** | Gate AT4 | Artifact-Storage-Engine | Outputs speichern, Manifeste anlegen, Registry aktualisieren | Outputs ohne ID ablegen, Manifeste weglassen | `skeleton` | `src/core/artifacts/` existiert bereits; `docs/ARTIFACT_OUTPUT_LAYER.md` ist partiell |
| **run-state** | Gate AT4 | Run-State-Persistenz | Run-ID vergeben, Zustand speichern, Recovery-Schritt dokumentieren | Run ohne Artifact abschließen, Failed-State ohne Fehlertext | `skeleton` | `src/core/run-state/` → `src/core/runs/` (Namens-Mapping) |
| **providers** (impl.) | Gate AT4 | Implementierungs-Heimat für OPENDORK | Provider-Interface bereitstellen, Credential-Management | Provider-Logik doppelt halten (opendork/ und providers/ gleichzeitig) | `skeleton` | Wird von OPENDORK-README referenziert |
| **safety** (impl.) | Gate AT4 | Implementierungs-Heimat für CATALON-GUARD | Safety-Policies, Scope-Validation | Safety-Checks in Modulen implementieren statt in safety/ | `skeleton` | Wird von CATALON-GUARD-README referenziert |
| **modules** (slot-registry) | New (Gate 001) | Modul-Discovery und Slot-Verwaltung | Module per Contract registrieren, Forge-Sichtbarkeit steuern | Module ohne `module.json` registrieren | `skeleton` | Ergänzt bestehende `modules/` Verzeichnisstruktur |

---

## Module-Slot-Contract

Alle Module (nicht Core-Systeme) folgen dem Vertrag in [`docs/MODULE_CONTRACT.md`](MODULE_CONTRACT.md).

Core-Systeme (OPENDORK, CATALON, CATALON-GUARD, DEAFPIPER) sind Infrastruktur — sie füllen keinen Module Slot. Sie werden nicht in The Forge angezeigt.

---

## Status-Legende

| Status | Bedeutung |
|---|---|
| `skeleton` | Verzeichnis und README existieren, kein Execution-Code |
| `partial` | Teilweise implementiert, kein vollständiger Contract erfüllt |
| `prototype` | Funktioniert, aber nicht production-ready |
| `done` | Gate-Akzeptanz-Kriterien erfüllt |

---

## Regeln

1. Jedes Modul / System muss einen Eintrag in dieser Matrix haben, bevor Implementierung beginnt.
2. Konflikte werden explizit in der letzten Spalte festgehalten — nicht stillschweigend gelöst.
3. Status wird nur durch Gate-Commit erhöht.
4. "Darf nicht"-Einträge sind Basis für CATALON-GUARD Kill-Kriterien.
