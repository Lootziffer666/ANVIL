# ANVIL

**Weaving Anvil** is the shared workspace for project principles, gated agent workflows, global knownbugs, prompts, templates, and cross-project UX doctrine.

Tagline:

```text
ink & iron glow
```

This repository is not a dumping ground for every app. It is the operational forge that keeps multiple projects aligned without erasing their boundaries.

## Purpose

ANVIL collects the reusable parts of the workflow that proved valuable during PAINKILLER:

- gated implementation
- CI-first verification
- agent instruction files
- handoff discipline
- knownbugs as persistent development memory
- reusable prompts
- shared design principles
- cross-project UX grammar

## Project Families

Current intended project families:

- CatchIt — state surfaces, mobility trust UX, anti-dashboard grammar
- FLOW / LOOM / SPIN / SMASH — language, writing, normalization, structure
- Borderline — Zen-OS-style friction reduction across Android/app context
- Tabula — system cleanup / workspace-order utility direction
- PAINKILLER — reference implementation for gated AI-assisted development

## Repository Zones

```text
apps/                 Project-facing landing pages and migration notes.
design-systems/       Cross-project visual and interaction-system fragments.
knownbugs-global/     Cross-project bug/risk/failure-pattern memory.
principles/           UX, product, and agent-development doctrine.
projects/             Index and status documents for active projects.
prompts/              Reusable prompts for gates, reviews, recovery, audits.
shared/               Shared modules only after reuse is proven.
templates/            Reusable repo/project scaffolds.
.github/              GitHub PR and issue templates.
```

## Rule

Shared principles may be centralised early. Shared code is extracted only after reuse is proven.

Vor jeder Übernahme in Templates: [Template Cleanup Checklist](templates/TEMPLATE_CLEANUP_CHECKLIST.md).

Do not move app source code into this repository without a specific migration plan.
