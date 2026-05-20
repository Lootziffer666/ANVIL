# Known Drift Risks

**Letzte Aktualisierung:** 2026-05-20 (Drift-Bereinigung)

---

## Bestehende Risiken

### 1. Dashboard-Drift
**Risiko:** Die UI wird zu einem klassischen Dashboard.  
**Gegenmaßnahme:** Anti-Dashboard-Prinzip (`principles/anti-dashboard.md`).  
State Surface: Zustand zuerst, nicht Menüs.  
**Status:** ✅ Aktuell unter Kontrolle — UI folgt State Surface Design.

### 2. Scope Creep
**Risiko:** Gates werden zu groß, Features schleichen sich ein.  
**Gegenmaßnahme:** Kill-Kriterien pro Gate. Stoppen bei Creep.  
**Status:** ⚠️ Aktiv — Gates A13–A20 wurden in einem Batch gepusht (2 PRs für 8 Gates). Erhöhtes Risiko.

### 3. Module ohne Contract
**Risiko:** Neue Module umgehen den MODULE_CONTRACT.md.  
**Gegenmaßnahme:** Kein Merge ohne Contract-Konformität.  
**Status:** ⚠️ `android-blueprint` existiert in `data.js` MODULE_REGISTRY, hat aber kein `modules/android-blueprint/module.json`. Drift bereits eingetreten.

### 4. Framework-Drift
**Risiko:** Anvil wird zu einem Framework statt einer Werkbank.  
**Gegenmaßnahme:** Concept Contract (`ANVIL_CONCEPT_CONTRACT.md`) prüfen.  
**Status:** ✅ Aktuell unter Kontrolle.

### 5. API-Key Leaks
**Risiko:** API Keys / Tokens in Code oder Commits.  
**Gegenmaßnahme:** `.gitignore`, keine Hardcoded Keys, Token-System.  
**Status:** ⚠️ Token Manager speichert `_key` im Klartext in localStorage. Kein Encryption. Kein Leak in Git, aber trivial auslesbar im Browser.

### 6. Overengineering
**Risiko:** Zu viel Abstraktion, zu wenig Nutzen.  
**Gegenmaßnahme:** Jede Gate muss konkreten, sichtbaren Nutzen liefern.  
**Status:** ✅ Aktuell unter Kontrolle — Code ist bewusst einfach (Vanilla JS, kein Framework).

---

## Neue Risiken (Gate AX entdeckt)

### 7. Status-Inflation ✅ BEHOBEN
**Risiko:** Gates als "done" markiert, obwohl nur Docs vorhanden (A9, A18, A20).  
**Realität:** 5 von 20 Gates sind nicht `done` — davon 3 reine Docs-Gates.  
**Gegenmaßnahme:** Statusklassen-System eingeführt (done/prototype/docs-only/partial/blocked/superseded). Keine Gate darf als "done" gelten, wenn nur Docs existieren.  
**Betroffene Gates:** A7 (partial), A8 (partial), A9 (docs-only), A10 (prototype), A13 (prototype), A18 (docs-only), A20 (docs-only)  
**Status:** GATES.md durch Gate AX aktualisiert — alle Gates tragen jetzt korrekten Status. Verifiziert 2026-05-20.

### 8. Naming-Drift: "Anvil Bellows" ✅ BEHOBEN
**Risiko:** `pake.config.json` und Docs verwenden "Anvil Bellows" als Pake-Build-Name.  
**Problem:** Laut ANVIL_CONCEPT_CONTRACT.md ist "Anvil-Bellows" ein eigenständiges IIG-Projekt (ehem. CATALON-GUARD). Nicht das Anvil-IDE.  
**Gegenmaßnahme:** Name in pake.config.json und PAKE_DESKTOP_SHELL.md zu "Anvil" korrigieren.  
**Dateien:** `pake.config.json`, `docs/PAKE_DESKTOP_SHELL.md`  
**Status:** Korrigiert — beide Dateien verwenden jetzt "Anvil" (verifiziert 2026-05-10).

### 9. Permission-Drift: Token Manager ✅ BEHOBEN
**Risiko:** `modules/token-manager/module.json` nutzt Permission `storage.local`.  
**Problem:** `storage.local` war nicht in der erlaubten Permission-Liste von MODULE_CONTRACT.md.  
**Gegenmaßnahme:** `storage.local` in `docs/MODULE_CONTRACT.md` nachgetragen. Verifiziert 2026-05-20.

### 10. Execution-Gap ⚠️ TEILWEISE BEHOBEN
**Risiko:** Anvil hat kein Execution Core. Module werden definiert, aber nie ausgeführt.  
**Folge:** Die gesamte Pipeline (Module → Artifact → Output → Registry) existiert nur als Spec.  
**Stand 2026-05-20:** B-Gate-Serie läuft:
- B1 ✅ Safety Policy (`docs/SAFETY_POLICY.md`) — Execution-Regeln verbindlich
- B2 ✅ KMP Core Contracts (`core/contracts/`, `core/quality/`) — Interfaces
- B3 ✅ KMP Core Domain (`core/domain/`) — Workspace, Run, Artifact, Snapshot, MemoryEntry + alle Domain-IDs
- B4 ✅ KMP Core Pipeline (`core/pipeline/`) — RunStep, RunResult, StepRecord
- B5 ✅ KMP Bellows (`modules/bellows/`) — BellowsRouter, ProviderAdapter, LOCAL_ONLY-Enforcement
- B6 ✅ KMP Knight (`modules/forge/knight/`) — KnightReader, KnightWriter, KnightDiff, Knight-Facade, ScopeGuard
- B7 ✅ kotlinx-datetime — currentTimestamp() Stub ersetzt (Clock.System.now())
- B8 ✅ Plan + Task Domain Models (`core/domain/`) — PlanLifecycle, TaskStatus, RiskLevel, dependsOn-DAG
- B9 ✅ Warden (`core/quality/`) — CommandGuard + CommandPolicy (Safety Policy §1)
- B10 ✅ RunContext + RunEngine Interface (`core/pipeline/`)  
**Verbleibend:** Konkrete RunEngine-Implementierung `:modules:forge:runner` (B11), konkrete Provider-Adapter mit Ktor, Artifact-Engine.

### 11. Docs-vs-Code-Drift ⚠️ TEILWEISE BEHOBEN
**Risiko:** Docs beschreiben Features, die nicht existieren, als ob sie vorhanden wären.  
**Stand 2026-05-20:**
- `docs/OMNIROUTE_BRIDGE.md` — ✅ trägt `**Status:** Referenz`
- `docs/PAKE_DESKTOP_SHELL.md` — ✅ trägt `**Status:** Referenz`
- `docs/ANDROID_BLUEPRINT_TRACK.md` — ✅ `android-blueprint` Target-Status auf `docs-only` korrigiert; `**Status:** docs-only` gesetzt  
**Verbleibend:** Docs-only Gates (A9, A20) enthalten Implementierungsdetails ohne Execution-Code. Akzeptiert bis Execution Core existiert.

### 12. Test-Lücke
**Risiko:** Null Tests im gesamten Repo. Kein CI/CD.  
**Folge:** Jede Änderung kann bestehende Funktionalität brechen, ohne dass es auffällt.  
**Gegenmaßnahme:** Mindestens Smoke-Tests für Kernfunktionen (TokenManager, AnvilSync, buildPromptPack).

---

## Neue Risiken (Gates AT1–AT4 identifiziert)

### 13. Donor-Codebase Assimilation Drift ⚠️ AKTIV (kontrolliert)
**Risiko:** Donor-Code wird unkontrolliert in aktive Anvil-Pfade kopiert, ohne Transplant Map, ohne Provenance, ohne Umbenennung.  
**Folge:** Fremde Produktidentität im Anvil-Repo. Lizenz-Verletzungen. Architektur-Chaos.  
**Gegenmaßnahmen:**
- `docs/CODEBASE_TRANSPLANT_RULES.md` — repo-weite kanonische Regeln
- `docs/provenance/TRANSPLANT_MAP.md` — jede Übernahme muss hier stehen
- `docs/provenance/OGCODE_SOURCE_AUDIT.md` — Lizenz-Audit
- Gates AT1–AT4 — vollständige Vorbereitungskette
- `docs/SAFETY_POLICY.md` §6 — Transplant Execution Checklist (Gate B1)
- Kein Code-Import ohne Map-Eintrag
**Status:** Kontrolliert. B2 verifiziert: 0 Donor-Code-Zeilen, Anvil-Terminologie, TRANSPLANT_MAP.md NATIVE-Eintrag gesetzt (2026-05-20). Einhaltung bei jeder B-Gate-Implementierung erneut prüfen.

### 14. Premature Execution Core Implementation ✅ BEHOBEN
**Risiko:** Execution-Code wird implementiert, bevor Architektur und Transplant-Regeln stehen.  
**Folge:** Code ohne Sicherheitskonzept (kein Command Guard, kein Review Gate). Rückbau teuer.  
**Gegenmaßnahmen:**
- AT4 definiert Skeleton ohne Execution-Code
- `docs/EXECUTION_CORE_ARCHITECTURE.md` definiert Regeln
- A21–A24 als "deferred until Execution Core exists" markiert
- Kein Execution-Runner, Provider-Call, Shell-Runner, Branch-Automation vor Safety-Policy
**Status:** ✅ Safety Policy verabschiedet (Gate B1, `docs/SAFETY_POLICY.md`, 2026-05-20). Command Guard Allowlist, Scope-Beschränkung, Credential-Policy, Privacy-Mode-Enforcement und Transplant-Checklist verbindlich. Gate B2 (KMP Contracts) konform mit dieser Policy implementiert.

### 15. ogcode Product Identity Leak ⚠️ AKTIV
**Risiko:** Donor-Produktbegriffe ("ogcode", "Striker", "ogden") erscheinen in aktiven Anvil-Dateien.  
**Folge:** Identitätsverwirrung. Markenrechtsprobleme. Unprofessioneller Eindruck.  
**Gegenmaßnahmen:**
- `CODEBASE_TRANSPLANT_RULES.md` Abschnitt 6: Naming Rules
- Verbotene Begriffe in Produkt-/UI-/Core-Dateien definiert
- Erlaubte Anvil-Begriffe definiert
- Donor-Referenzen nur in `docs/provenance/` erlaubt
**Status:** Regeln stehen. Compliance-Prüfung 2026-05-20: Keine verbotenen Begriffe ("ogcode", "Striker", "ogden") außerhalb `docs/provenance/` gefunden. Muss bei jeder Transplant-Aktion erneut geprüft werden.
