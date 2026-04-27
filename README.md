# ANVIL

**Weaving Anvil** is a shared workspace for project principles, gated agent workflows, global knownbugs, and reusable templates.

## Purpose

ANVIL centralizes reusable practices before reusable code:

- gated implementation discipline
- CI-first verification mindset
- knownbugs and handoff memory patterns
- reusable template scaffolds
- shared design and workflow principles

## Reusable App Template

This repository now includes a dedicated reusable Anvil app template at:

```text
templates/anvil-app/
```

That template is organized around canonical Anvil concerns:

- `anvil.yaml`
- `client_code/`
- `server_code/`
- `theme/`
- setup and architecture docs

## Repository Zones

```text
knownbugs-global/     Cross-project bug/risk/failure-pattern memory.
principles/           UX, product, and agent-development doctrine.
projects/             Index and status documents for active projects.
sources/              Extraction notes from reference repositories.
templates/            Reusable project and app scaffolds.
```

## Rule

Shared principles may be centralised early. Shared code is extracted only after reuse is proven.

Vor jeder Übernahme in Templates: [Template Cleanup Checklist](templates/TEMPLATE_CLEANUP_CHECKLIST.md).

Do not move app source code into this repository without a specific migration plan.
