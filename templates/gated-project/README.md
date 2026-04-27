# Gated Project Template

A reusable ANVIL project scaffold for small-to-medium apps and utilities.

This template combines lessons from:

- PAINKILLER — gated implementation + CI-first discipline
- DEAFPIPER — staged product pipeline + artifact handoffs
- OPENDORK — runtime profiles, validation profiles, artifacts and reports
- CATALON-GUARD — cautionary lesson: a README alone is not enough

## What This Template Is For

Use this when starting a project that should be built by AI agents with human direction and strict scope control.

## Root Files

```text
README.md
AGENTS.md
claude.md
instructions.md
knownbugs.md
```

## Directories

```text
.github/
  PULL_REQUEST_TEMPLATE.md
  ISSUE_TEMPLATE/
  workflows/

handoff/
  README.md
  GATE_X_HANDOFF_TEMPLATE.md

work/
  01_setup_prompting/
  02_research_validation/
  03_prd_planning/
  04_ux_ui/
  05_architecture/
  06_build/
  07_quality_security_compliance/
  08_marketing_gtm/
  09_analytics_iteration/
  10_handoff/

artifacts/
  raw/
  validated/
  rejected/
  gold/
  diffs/
  reports/
  replays/

config/
  runtime-profiles.json
  validation-profiles.json
  provider-catalog.example.json
```

## Minimal Use

For a tiny app, you do not need every `work/` stage immediately.

Use at minimum:

```text
instructions.md
AGENTS.md
knownbugs.md
handoff/
README.md
.github/PULL_REQUEST_TEMPLATE.md
.github/workflows/
```

## Main Rule

Do not let the template become heavier than the project.

Use only the layers that reduce friction.

Vor jeder Übernahme in dieses Template: [Template Cleanup Checklist](../TEMPLATE_CLEANUP_CHECKLIST.md).
