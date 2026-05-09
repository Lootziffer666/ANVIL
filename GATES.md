# 🚪 GATES — ANVIL

> A1–A20 abgeschlossen.

---

## 🔜 Nächste Gates

### Gate A21: Android-APK-Builder
- **Branch:** `gate/a21-apk-builder`
- **To-Dos:**
  - [ ] Workspace → APK Export Pipeline
  - [ ] Module als Feature-Module im APK
  - [ ] Signierung mit Keystore
  - [ ] Build-Status Dashboard
- **Akzeptanz:** Funktionierendes APK aus Workspace
- **Kill:** Broken APK

### Gate A22: Plugin-Marketplace
- **Branch:** `gate/a22-marketplace`
- **To-Dos:**
  - [ ] Module Registry (JSON-basiert)
  - [ ] Install/Remove per CLI
  - [ ] Versions-Management
  - [ ] Dependency Resolution
- **Akzeptanz:** Module installierbar und auffindbar
- **Kill:** Zentrale Server-Abhängigkeit

### Gate A23: Agent Prompt Pack v2
- **Branch:** `gate/a23-prompt-pack-v2`
- **To-Dos:**
  - [ ] Strukturiertes Prompt-Format
  - [ ] Context-Window-Management
  - [ ] Multi-Agent-Handoff
  - [ ] Prompt-Versionierung
- **Akzeptanz:** Agent kann Workspace lesen und modifizieren
- **Kill:** Prompt > 100K Tokens

### Gate A24: Collaborative Workspaces
- **Branch:** `gate/a24-collab`
- **To-Dos:**
  - [ ] Workspace-Export/Import (ZIP)
  - [ ] Diff-Viewer für Workspace-Stände
  - [ ] Merge-Strategie
  - [ ] Conflict Resolution UI
- **Akzeptanz:** Workspace transferierbar
- **Kill:** Echtzeit-Sync (zu komplex für jetzt)
