# AGENTS.md — Agenten-Regeln für Anvil

> Aktuell: KMP-Bootstrap-Phase. Stack = Kotlin Multiplatform + Compose Multiplatform.

---

## Wer darf was

| Agent | Darf | Darf nicht |
|-------|------|------------|
| **Claude / Hyperagent** | Gates implementieren, PRs erstellen, Docs schreiben, Gradle-Dateien anlegen, Kotlin-Code für genehmigte Gates schreiben | Eigene Gates definieren, Architektur-Entscheidungen treffen, Module ohne Contract anlegen |
| **Codex / Copilot** | Code-Reviews, Refactoring innerhalb Gate-Scope, Bugfixes | Gate-Reihenfolge ändern, neue Abhängigkeiten hinzufügen ohne Gate |
| **Mensch (Christian)** | Alles | — |

---

## Regeln für alle Agenten

1. **Kein Commit ohne Gate-Referenz** — Jeder Commit nennt seine Gate (z.B. `"Gate B1: ..."`)
2. **Kein Modul ohne Contract** — `ModuleSlotContract` muss implementiert sein
3. **Kein Output ohne Manifest** — `ARTIFACT_OUTPUT_LAYER.md` einhalten
4. **Definition of Done prüfen** — Vor dem PR: alle DoD-Punkte der Gate durch
5. **Kill-Kriterien beachten** — Bei Scope Creep: sofort stoppen
6. **Tests oder Proof** — Jede Gate hat mindestens eine Verifikation
7. **Aktive Dateien kennen** — `anvil-kmp/` ist der einzige Arbeitsbereich für Code

---

## KMP-spezifische Regeln

- **Targets:** `commonMain`, `androidMain`, `jvmMain` (Desktop)
- **Kein iOS-Target** im MVP — explizit nicht inkludieren
- **Dependency-Richtung:** `core` → `modules` → `surfaces` → `app` (nie umgekehrt)
- **Bellows-Split:** `LocalModelBridge` ist der EINZIGE legale KMP-Split
  - `androidMain`: `LiteRTModelBridge`
  - `jvmMain`: `LlamaCppModelBridge` (via JNI)
  - `commonMain`: nur `BellowsContract` Interface
- **SQLDelight:** Scalar-Felder als echte Spalten, Listen/Maps als JSON-Blob
- **IDs:** Immer `@JvmInline value class` mit `String`-Backing — nie raw String

---

## Bellows-Exhaustion — kein Silent-Fail

```
Wenn alle Provider-Chains erschöpft → BellowsExhaustedException
Nie: null zurückgeben, leeren String, oder Cloud-Fallback bei LOCAL_ONLY
```

---

## Bei Drift

Wenn ein Agent unsicher ist:
1. `docs/KNOWN_DRIFT_RISKS.md` lesen
2. Im Zweifel: Gate pausieren, nicht weitermachen
3. Mensch fragen

---

## Commit-Konvention

```
Gate B1: anvil-kmp Gradle Bootstrap (settings + build + libs.versions.toml)
Gate B2: :core:contracts — ModuleSlotContract, BellowsContract
Gate B3: :core:domain — Workspace, Run, Artifact, Snapshot, MemoryEntry
```

Präfix immer: `Gate XY: beschreibung`
