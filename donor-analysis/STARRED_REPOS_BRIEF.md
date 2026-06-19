# Starred Repos Donor Analysis Brief

**Stand:** 2026-06-10  
**Owner:** Christian Amine (Lootziffer666)  
**Gate:** AX2  
**Status:** Verbindlich fuer Pattern-Studium. Kein Code-Import.

---

## Was diese Analyse ist und nicht ist

**Diese Analyse ist:**
- Eine Pattern-Studie externer Open-Source-Repos auf GitHub (Starred)
- Eine Konzept-Quelle fuer ANVIL-interne Architekturentscheidungen
- Eine Bewertung von Skills-, Compression- und Orchestration-Patterns

**Diese Analyse ist NICHT:**
- Ein Code-Import-Plan
- Eine Integration-Roadmap mit Zeitplan
- Ein Fork- oder Dependency-Vorschlag
- Eine Empfehlung fuer Copy-Paste aus fremden Repos

---

## Die starke ANVIL-Frage

Die richtige Frage lautet **NICHT**:

> "Wie bauen wir open-design/tau/headroom in ANVIL ein?"

Sondern:

> **"Welche Architektur-Patterns aus diesen Repos validieren oder erweitern ANVILs bestehende Contracts (BellowsContract, ModuleSlotContract, RunStep-Pipeline) - ohne neue Abhaengigkeiten einzufuehren?"**

---

## Analysierte Repos

| # | Repo | Stars | Kern-Pattern | ANVIL-Relevanz |
|---|------|-------|--------------|----------------|
| 1 | nexu-io/open-design | 67k | Skills als modulare Instruktions-Pakete, AMR (Agentic Model Router) | Hoch |
| 2 | AbdoKnbGit/tau | 225 | Multi-Provider-Orchestration, Session-Management, DAG-Teams | Hoch |
| 3 | chopratejas/headroom | 37k | Token-Compression (60-95%), CacheAligner, CCR | Hoch |
| 4 | alirezarezvani/claude-skills | 18k | 337 Skills, SKILL.md-Format, stdlib-only Python | Mittel |
| 5 | jnMetaCode/agency-agents-zh | 15k | 216 Expert-Roles, DAG-Orchestration, SOUL.md | Mittel |
| 6 | microsoft/UFO | 9k | DAG-Task-Decomposition, Multi-Device, AIP (WebSocket) | Mittel |
| 7 | pedrofariasx/mimo-ai-proxy | - | Session-Intelligence, Delta-Payloads, Account-Rotation | Niedrig |

---

## Entscheidungstabelle

| Repo | Decision | Ziel-Bereich in ANVIL | Risiko | Begruendung |
|------|----------|----------------------|--------|-------------|
| open-design | **ADAPT** | Skill-System (ModuleSlotContract), The Forge | Niedrig | Skills-as-Packages Pattern validiert ANVILs Module-Slot-Architektur. AMR-Pattern aehnelt BellowsRouter. Kein Code-Import noetig. |
| tau | **STUDY** | BellowsRouter (Multi-Provider), RunStep-Pipeline | Niedrig | 25 Provider-Adapter zeigt, dass ANVILs ProviderAdapter-Interface korrekt designt ist. Session-Management fuer lange Runs relevant. |
| headroom | **ADAPT** | Bellows (Token-Pipeline), neues Modul bellows-headroom | Mittel | Compression vor LLM-Call spart Tokens/Kosten. Braucht eigenes Modul. AST-Compression fuer Code besonders relevant. |
| claude-skills | **STUDY** | Skill-Format-Definition, Commander Surface | Niedrig | SKILL.md-Format als Referenz. stdlib-only Constraint passt zu ANVILs Sicherheitsphilosophie. |
| agency-agents-zh | **STUDY** | Run-Pipeline (Rollen-basierte Steps), DAG | Niedrig | SOUL.md-Pattern interessant fuer Agent-Identity in Runs. DAG-Orchestration bestaetigt UFO-Findings. |
| UFO | **ADAPT** | RunStep-Pipeline (DAG-Erweiterung), Multi-Device | Mittel | DAG-basierte Task-Decomposition erweitert die lineare PLAN-PATCH-DIFF-GATE-ARTIFACT Pipeline. Constellation-Pattern fuer parallele Steps. |
| mimo-ai-proxy | **IGNORE** | - | - | Delta-Payload-Optimierung ist Nische. ANVILs BellowsRouter braucht kein Proxy-Gateway mit Session-Fingerprinting. |

---

## Decision-Legende

| Wert | Bedeutung |
|------|-----------|
| **STUDY** | Pattern studieren, Erkenntnisse dokumentieren. Kein direkter Architektur-Einfluss. |
| **ADAPT** | Konzept adaptieren fuer ANVIL. Eigene Implementierung auf Basis des Patterns. |
| **IGNORE** | Nicht relevant fuer ANVILs Architektur oder Scope. |

---

## Zusammenfassung der Erkenntnisse

### 1. Skills/Plugins sind ein validiertes Pattern

Drei unabhaengige Repos (open-design, claude-skills, agency-agents-zh) konvergieren auf dasselbe Pattern:
- **Skill = Markdown-Instruktion + optionale Tools + Metadaten**
- ANVILs `ModuleSlotContract` + `module.json` ist bereits strukturell kompatibel
- Empfehlung: Skill-Format als Untermenge von ModuleSlotContract definieren

### 2. Token-Compression gehoert in die LLM-Pipeline

headroom zeigt: Compression ist kein Afterthought, sondern ein Pipeline-Step *vor* dem LLM-Call.
- Passt in BellowsRouter zwischen Request-Erstellung und Provider-Dispatch
- AST-basierte Code-Compression und JSON-Crushing sind fuer ANVIL (IDE/Werkbank) besonders relevant
- CCR (reversible Compression) erhaelt Kontext-Qualitaet

### 3. DAG-Orchestration erweitert lineare Pipelines

tau und UFO zeigen: Fortschrittliche Agent-Systeme nutzen DAG statt linearer Ketten.
- ANVILs PLAN-PATCH-DIFF-GATE-ARTIFACT ist linear (B4: RunStep sealed)
- Parallele Steps (z.B. mehrere PATCH gleichzeitig) brauchen DAG-Scheduling
- UFOs "Dynamic DAG Editing" erlaubt Runtime-Anpassung der Pipeline

### 4. Session/Snapshot-Management ist Standard

tau's "Snapshot with Time Traveling" und UFOs Checkpoint-Pattern bestaetigen:
- ANVILs `Snapshot` (B3) und `SaveCheckpoint` RunStep (B4) sind korrekt designt
- Erweiterung: Session-Branching (tau) fuer explorative Runs

---

## Vertiefungsdokumente

| Dokument | Fokus |
|----------|-------|
| [SKILLS_ARCHITECTURE_CONCEPT.md](SKILLS_ARCHITECTURE_CONCEPT.md) | Skill-System fuer ANVIL (open-design + claude-skills + agency-agents-zh) |
| [HEADROOM_INTEGRATION_CONCEPT.md](HEADROOM_INTEGRATION_CONCEPT.md) | Token-Compression fuer Bellows (headroom) |
| [ORCHESTRATION_PATTERNS.md](ORCHESTRATION_PATTERNS.md) | DAG- und Multi-Provider-Orchestration (tau + UFO) |

---

## Donor-Regel (kanonisch)

> *Starred Repos sind Pattern-Quellen, keine Code-Quellen. Allowed: Architektur-Patterns studieren, Konzepte adaptieren, Format-Referenzen nutzen. Forbidden: Code-Import, Dependency-Einfuehrung, Lizenz-Konflikte ignorieren.*

---

## Naechste Schritte

1. **Skills-Format** (SKILLS_ARCHITECTURE_CONCEPT.md) in Gate-Planung aufnehmen
2. **bellows-headroom** Modul-Slot evaluieren (HEADROOM_INTEGRATION_CONCEPT.md)
3. **DAG-RunStep** Erweiterung der Pipeline pruefen (ORCHESTRATION_PATTERNS.md)
4. Alle drei Konzepte sind *nach* B9 (Bellows Gateway) priorisierbar
