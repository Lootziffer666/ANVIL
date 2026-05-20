# CLAUDE.md — Regeln für Claude, Codex und andere KI-Agenten

> Lies diese Datei **zuerst**, bevor du irgendetwas in diesem Repo tust.

---

## Kontext

Anvil ist eine KI-native IDE/Werkbank.  
Stack: **Kotlin Multiplatform (KMP)** + **Compose Multiplatform** + Gate-getriebene Entwicklung.  
Aktive Entwicklung: `anvil-kmp/` — nicht `app/` oder `src/core/`.

---

## 1. Lese-Pflicht (in dieser Reihenfolge)

1. `docs/ANVIL_CONCEPT_CONTRACT.md` — kanonische Begriffe
2. `docs/MODULE_CONTRACT.md` — Modul-Vertrag
3. `GATES.md` — welche Gates existieren, welche offen sind
4. `SKILLS.md` — was du darfst / nicht darfst
5. `docs/EXECUTION_CORE_ARCHITECTURE.md` — Architektur-Übersicht

---

## 2. Gate-Disziplin

- **Jeder Commit referenziert eine Gate** — z.B. `"Gate B1: settings.gradle.kts"`
- **Kein Scope Creep** — nur das implementieren, was die Gate explizit verlangt
- **Kill-Kriterien kennen** — jede Gate hat eine Kill-Bedingung; bei Auslösung: sofort stoppen

---

## 3. KMP-Architektur-Regeln

```
:core:contracts   → keine Deps außer Kotlin stdlib + kotlinx.serialization
:core:domain      → darf :core:contracts importieren
:core:pipeline    → darf :core:contracts, :core:domain importieren
:core:quality     → darf :core:contracts importieren
:modules:*        → darf :core:* importieren, NICHT andere :modules:*
:surfaces:*       → darf :core:*, :modules:* importieren
:app:*            → darf alles importieren
```

**Verboten:** Zirkuläre Abhängigkeiten. `core` darf nie `modules` importieren.

---

## 4. Naming — Canon-Begriffe

| Concept | Kanonischer Name | Verboten |
|---------|-----------------|---------|
| LLM-Routing-Layer | **Bellows** / `BellowsContract` | ~~Provider Hub~~, ~~AI Layer~~ |
| Datei-I/O + Diff | **Knight** | ~~FileHandler~~, ~~DiffEngine~~ |
| Modul-Launchpad | **The Forge** | ~~Module Store~~, ~~Dashboard~~ |
| Sicherheits-Guard | **Warden** (in :core:quality) | ~~Safety Engine~~ |
| LLM-Anfrage | `ModelRequest` / `ModelResponse` | ~~AIRequest~~ |
| Datenschutz-Modus | `PrivacyMode.LOCAL_ONLY` | ~~offlineMode~~ |

---

## 5. QualityState — immer explizit

Jedes Modul das `ModuleSlotContract` implementiert **muss** `qualityState()` implementieren.  
Kein Modul darf `STABLE` zurückgeben, ohne dass es geprüft wurde.

```kotlin
// RICHTIG:
override fun qualityState(): QualityState =
    if (isHealthy()) QualityState.STABLE else QualityState.DEGRADED

// FALSCH (nie tun):
override fun qualityState(): QualityState = QualityState.STABLE
```

---

## 6. Privacy — LOCAL_ONLY ist hart

```kotlin
// In BellowsRouter: LOCAL_ONLY ist niemals Fallback auf Cloud
if (request.privacyMode == PrivacyMode.LOCAL_ONLY && !adapter.isLocal) continue
// Wenn kein lokales Modell → BellowsExhaustedException, nicht CloudFallback
```

---

## 7. Workspace-Safety — Kill-Kriterium

**Jede Änderung außerhalb `Workspace.rootPath` → `QualityState.FAILED`.**  
Das ist das härteste Kill-Kriterium im MVP. Kein Silent-Fail. Immer Exception.

---

## 8. State Surface Design

- Kein Dashboard (siehe `principles/anti-dashboard.md`)
- Zustand kommt zuerst — UI zeigt was das System ist, nicht was es kann
- Recovery statt Fehlermeldung
- Vier Zustände: `STABLE` / `DEGRADED` / `BLOCKED` / `FAILED`

---

## 9. Bei Unsicherheit

**Stoppen, nicht raten.** Im Zweifel:
1. `docs/KNOWN_DRIFT_RISKS.md` lesen
2. Gate pausieren
3. Mensch fragen

---

## 10. Aktive Dateien vs. historische Artefakte

| Verzeichnis | Status | Aktion |
|-------------|--------|--------|
| `anvil-kmp/` | ✅ Aktiv — hier entwickeln | Normale Arbeit |
| `src/core/` | 📄 Docs-Skeleton | Nur lesen, nie Code hinzufügen |
| `app/` | 🗄 Historisch (JS/Pake) | Nicht anfassen |
| `modules/` | 🗄 Historisch (JS) | Nicht anfassen |
