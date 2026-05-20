# Safety Policy — Anvil Execution Core

**Gate:** B1 — Safety Policy  
**Status:** Verbindlich  
**Stand:** 2026-05-20

> Diese Policy ist Voraussetzung für jede Execution-Code-Implementierung.
> Kein Execution-Runner, Provider-Call, Shell-Runner oder Branch-Automation
> darf geschrieben werden, bevor diese Policy gilt.

---

## 1. Command Guard Allowlist

Nur diese Befehle dürfen via Command Guard ausgeführt werden:

```text
git          — Versionskontrolle (kein --force ohne explizite Gate-Freigabe)
gradle       — Build-System
kotlinc      — Kotlin-Compiler
adb          — Android Debug Bridge (nur für Test-Targets)
ktlint       — Kotlin-Linter
detekt       — Statische Analyse
```

Verboten (niemals via Command Guard):

```text
rm -rf           — Destruktive Löschung
curl             — Externe Netzwerk-Anfragen (nur via BellowsContract)
wget             — Externe Downloads
sh -c <beliebig> — Arbiträre Shell-Ausführung
eval             — Code-Eval
exec             — Prozess-Ersetzung
chmod 777        — Rechte-Eskalation
sudo / su        — Privilegien-Erweiterung
```

Neue Befehle müssen explizit in diese Liste aufgenommen werden, bevor ein Modul sie nutzen darf.

---

## 2. Scope-Beschränkung (Workspace.rootPath)

```
Jede Datei-Mutation darf ausschließlich innerhalb Workspace.rootPath stattfinden.
Verletzung → QualityState.FAILED — keine Exception-Unterdrückung, kein Silent-Fail.
```

Jede Datei-Mutations-Operation muss vor Ausführung prüfen:

```kotlin
require(targetPath.startsWith(workspace.rootPath)) {
    "ANVIL Workspace Safety: Pfad liegt außerhalb rootPath: $targetPath"
}
```

---

## 3. Credential Handling

- **Verboten:** Klartext-Speicher (kein `localStorage._key`, kein plaintext SQLite, keine `.env`-Datei ohne Verschlüsselung)
- **Pflicht:** `CredentialVaultContract` (Gate B2) — alle Provider-Keys nur über diesen Contract speichern
- **Android:** `EncryptedSharedPreferences` via `androidx.security:security-crypto`
- **JVM/Desktop:** Java Keystore (JCEKS) oder verschlüsselte Datei über JCE

---

## 4. Privacy Mode — LOCAL_ONLY ist hart

```kotlin
// BellowsRouter: LOCAL_ONLY → niemals Cloud-Fallback
if (request.privacyMode == PrivacyMode.LOCAL_ONLY && !adapter.isLocal) continue

// Kein lokales Modell verfügbar → Exception, nicht Fallback
throw BellowsExhaustedException(
    "Kein lokales Modell verfügbar. PrivacyMode.LOCAL_ONLY verbietet Cloud-Fallback."
)
```

---

## 5. Review Gate

- `human_review_required = true` ist Default für alle Runs und Plans
- Darf nur auf `false` gesetzt werden, wenn die Gate-Spezifikation dies explizit erlaubt
- Kein Auto-Merge: PRs erfordern menschliche Prüfung

---

## 6. Transplant Execution Checklist (Risk 13)

Vor jedem Commit, der ein Muster aus dem Donor-Codebase reimplementiert:

- [ ] Entsprechender REWRITE-Eintrag in `docs/provenance/TRANSPLANT_MAP.md` vorhanden
- [ ] Anvil-Terminologie verwendet — keine `ogcode`-, `Striker`-, `ogden`-Begriffe in Code oder Kommentaren
- [ ] Commit referenziert Gate (z.B. `Gate B2: ...`)
- [ ] Keine direkte Kopie von Donor-Quellcode — nur Muster, frische Implementierung
- [ ] Compliance-Prüfung: `grep -r "ogcode\|Striker\|ogden" anvil-kmp/` → kein Treffer erwartet

---

## 7. Kill-Kriterien

Die folgenden Zustände setzen den Execution Core sofort auf `QualityState.FAILED`:

| Trigger | Konsequenz |
|---------|-----------|
| Datei-Mutation außerhalb `Workspace.rootPath` | FAILED — Exception, kein Fortfahren |
| `PrivacyMode.LOCAL_ONLY` + Cloud-Request | FAILED — BellowsExhaustedException |
| Klartext-Credential in Speicher oder Log | FAILED — sofort stoppen |
| Command Guard: nicht-erlaubter Befehl | FAILED — Command verweigert, Exception |
| Modul ohne `ModuleSlotContract`-Implementierung | FAILED — nicht laden |
| Commit ohne Gate-Referenz | BLOCKED — Gate-Disziplin verletzt |

---

## Querverweise

- `docs/EXECUTION_CORE_ARCHITECTURE.md` — Architektur-Übersicht
- `docs/CODEBASE_TRANSPLANT_RULES.md` — Transplant-Governance
- `docs/provenance/TRANSPLANT_MAP.md` — Keep/Rewrite/Drop-Entscheidungen
- `anvil-kmp/core/contracts/` — Gate B2: CredentialVaultContract, BellowsContract
