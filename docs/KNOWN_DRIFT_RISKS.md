# Known Drift Risks

## Risiken, die bei der Entwicklung auftreten können

### 1. Dashboard-Drift
**Risiko:** Die UI wird zu einem klassischen Dashboard.
**Gegenmaßnahme:** Anti-Dashboard-Prinzip (principles/anti-dashboard.md).
State Surface: Zustand zuerst, nicht Menüs.

### 2. Scope Creep
**Risiko:** Gates werden zu groß, Features schleichen sich ein.
**Gegenmaßnahme:** Kill-Kriterien pro Gate. Stoppen bei Creep.

### 3. Module ohne Contract
**Risiko:** Neue Module umgehen den MODULE_CONTRACT.md.
**Gegenmaßnahme:** Kein Merge ohne Contract-Konformität.

### 4. Framework-Drift
**Risiko:** Anvil wird zu einem Framework statt einer Werkbank.
**Gegenmaßnahme:** Concept Contract (ANVIL_CONCEPT_CONTRACT.md) prüfen.

### 5. API-Key Leaks
**Risiko:** API Keys / Tokens in Code oder Commits.
**Gegenmaßnahme:** `.gitignore`, keine Hardcoded Keys, Token-System (zukünftige Gate).

### 6. Overengineering
**Risiko:** Zu viel Abstraktion, zu wenig Nutzen.
**Gegenmaßnahme:** Jede Gate muss konkreten, sichtbaren Nutzen liefern.
YAGNI: You Ain't Gonna Need It.
