# Known Drift Risks

**Letzte Aktualisierung:** 2026-05-09 (Gate AX)

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

### 7. Status-Inflation ⚠️ AKTIV
**Risiko:** Gates als "done" markiert, obwohl nur Docs vorhanden (A9, A18, A20).  
**Realität:** 5 von 20 Gates sind nicht `done` — davon 3 reine Docs-Gates.  
**Gegenmaßnahme:** Statusklassen-System eingeführt (done/prototype/docs-only/partial/blocked/superseded). Keine Gate darf als "done" gelten, wenn nur Docs existieren.  
**Betroffene Gates:** A7 (partial), A8 (partial), A9 (docs-only), A10 (prototype), A13 (prototype), A18 (docs-only), A20 (docs-only)

### 8. Naming-Drift: "Anvil Bellows" ⚠️ AKTIV
**Risiko:** `pake.config.json` und Docs verwenden "Anvil Bellows" als Pake-Build-Name.  
**Problem:** Laut ANVIL_CONCEPT_CONTRACT.md ist "Anvil-Bellows" ein eigenständiges IIG-Projekt (ehem. CATALON-GUARD). Nicht das Anvil-IDE.  
**Gegenmaßnahme:** Name in pake.config.json und PAKE_DESKTOP_SHELL.md zu "Anvil" korrigieren.  
**Dateien:** `pake.config.json`, `docs/PAKE_DESKTOP_SHELL.md`

### 9. Permission-Drift: Token Manager ⚠️ AKTIV
**Risiko:** `modules/token-manager/module.json` nutzt Permission `storage.local`.  
**Problem:** `storage.local` ist nicht in der erlaubten Permission-Liste von MODULE_CONTRACT.md.  
Erlaubt sind: `filesystem.read`, `filesystem.write`, `network.api`, `network.build-server`, `camera`, `clipboard`.  
**Gegenmaßnahme:** Entweder `storage.local` in MODULE_CONTRACT.md aufnehmen, oder Token Manager Permission ändern.

### 10. Execution-Gap ⚠️ KRITISCH
**Risiko:** Anvil hat kein Execution Core. Module werden definiert, aber nie ausgeführt.  
**Folge:** Die gesamte Pipeline (Module → Artifact → Output → Registry) existiert nur als Spec.  
**Gegenmaßnahme:** Execution Core muss als eigene Gate priorisiert werden (vor A21–A24).  
**Vorschlag:** Gate AX+1 oder Gate-Nummer reservieren.

### 11. Docs-vs-Code-Drift ⚠️ AKTIV
**Risiko:** Docs beschreiben Features, die nicht existieren, als ob sie vorhanden wären.  
**Beispiele:**
- `docs/OMNIROUTE_BRIDGE.md` beschreibt Integration detailliert — kein Code vorhanden
- `docs/ANDROID_BLUEPRINT_TRACK.md` listet "✅ Aktiv" für android-blueprint Target — existiert nur in data.js
- `docs/PAKE_DESKTOP_SHELL.md` enthält Build-Befehle — kein Build möglich (keine Icons, kein Pake installiert)
**Gegenmaßnahme:** Docs müssen ihren Status klar deklarieren (z.B. "Status: Referenz" wie bei A18/A20, aber konsequenter).

### 12. Test-Lücke
**Risiko:** Null Tests im gesamten Repo. Kein CI/CD.  
**Folge:** Jede Änderung kann bestehende Funktionalität brechen, ohne dass es auffällt.  
**Gegenmaßnahme:** Mindestens Smoke-Tests für Kernfunktionen (TokenManager, AnvilSync, buildPromptPack).
