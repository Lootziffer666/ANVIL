# CURRENT_TREE.md — ANVIL Repo Snapshot

**Erstellt:** 2026-05-08  
**Commit:** 424d21d (main, HEAD vor Gate-Arbeit)  
**Branch:** `main` → neuer Branch `gates-a1-a6`

## Root-Dateien

| Datei       | Typ      |
|-------------|----------|
| `README.md` | Doku     |

Keine weiteren Root-Dateien (kein `.gitignore`, kein `package.json`, kein `requirements.txt`, kein `AGENTS.md`, kein `claude.md` auf Root-Ebene).

## Verzeichnisstruktur

```text
ANVIL/
├── README.md
├── knownbugs-global/
│   ├── README.md
│   ├── agent-failure-patterns.md
│   ├── github-friction.md
│   └── knownbugs.md
├── principles/
│   ├── README.md
│   ├── anti-dashboard.md
│   ├── broad-implementation-request-safety.md
│   ├── ci-first-gated-development.md
│   ├── frictionless-design.md
│   ├── shared-code-policy.md
│   └── state-surface-design.md
├── projects/
│   ├── README.md
│   ├── borderline.md
│   ├── catchit.md
│   ├── flow-family.md
│   ├── module-roadmap.md
│   ├── painkiller.md
│   └── tabula.md
├── sources/
│   └── template-extraction/
│       ├── README.md
│       ├── import-plan-template-system.md
│       ├── source-map.md
│       ├── template-exclusion-rules.md
│       └── template-useful-patterns.md
└── templates/
    ├── TEMPLATE_ASSEMBLY_GUIDE.md
    ├── TEMPLATE_CHANGE_GATE.md
    ├── TEMPLATE_CLEANUP_CHECKLIST.md
    ├── anvil-app/
    │   ├── .gitignore
    │   ├── README.md
    │   ├── TEMPLATE_SETUP.md
    │   ├── anvil.yaml
    │   ├── client_code/ (startup, _pages/HomePage, _components, _flows)
    │   ├── server_code/ (auth, bootstrap, permissions, repositories/, services/)
    │   ├── theme/ (theme.css, parameters.yaml, standard-page.html, templates.yaml)
    │   └── docs/ (architecture.md, customization.md, deployment.md)
    ├── core-gated/
    │   ├── .github/ (PR template, CI workflow)
    │   ├── AGENTS.md
    │   ├── README.md
    │   ├── claude.md
    │   ├── handoff/ (gate handoff templates)
    │   ├── instructions.md
    │   └── knownbugs.md
    ├── gated-project/
    │   ├── AGENTS.md, README.md, claude.md, instructions.md, knownbugs.md
    │   ├── config/ (provider-catalog, runtime-profiles, validation-profiles)
    │   └── handoff/
    └── modules/
        ├── artifact-classes/
        ├── runtime-profiles/
        ├── stage-pipeline/
        └── validation-profiles/
```

## Erkannte Agent-/Design-Dateien

| Datei | Ort |
|-------|-----|
| `AGENTS.md` | `templates/core-gated/`, `templates/gated-project/` (nur in Templates) |
| `claude.md` | `templates/core-gated/`, `templates/gated-project/` (nur in Templates) |
| `README.md` | Root + diverse Unterverzeichnisse |
| Design-Dateien | `principles/state-surface-design.md`, `principles/anti-dashboard.md`, `principles/frictionless-design.md` |
| Knownbugs | `knownbugs-global/` (3 Dateien) |

## Erkenntnisse

- Repo ist aktuell ein **reines Doku-/Prinzipien-/Template-Repo** — kein lauffähiger Code auf Root-Ebene.
- Templates enthalten Scaffolding für `anvil-app`, `core-gated`, `gated-project` und `modules`.
- `projects/module-roadmap.md` definiert 19 Gates über 5 Phasen (Kivy-basiert, Android-fokussiert).
- Keine `.gitignore` auf Root-Ebene.
- Keine bestehende App-Oberfläche, kein Build-System.
