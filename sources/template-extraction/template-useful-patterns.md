# Reusable Template Patterns

## 1. Gated Development Core

From PAINKILLER.

Every serious project starts with:

```text
AGENTS.md
claude.md
instructions.md
README.md
knownbugs.md
handoff/
.github/PULL_REQUEST_TEMPLATE.md
.github/ISSUE_TEMPLATE/
.github/workflows/
```

Rules:

- one gate per run
- hard non-goals per gate
- checks before commit
- handoff after every gate
- append-only knownbugs
- CI is authoritative where local environments are incomplete
- push/PR failure means patch export and stop

## 2. Stage Pipeline Layer

From DEAFPIPER.

For product development beyond coding, use stage folders with clear input/output contracts:

```text
work/01_setup_prompting
work/02_research_validation
work/03_prd_planning
work/04_ux_ui
work/05_architecture
work/06_build
work/07_quality_security_compliance
work/08_marketing_gtm
work/09_analytics_iteration
work/10_handoff
```

Each stage should declare:

- inputs
- outputs
- quality gates
- template artifacts
- optional deep-dive references

## 3. Runtime Profiles

From OPENDORK.

Reusable execution modes:

```text
interactive
batch
overnight_safe
overnight_aggressive
```

Template interpretation:

- `interactive` — user is present, ask only when needed, prefer small steps
- `batch` — run a bounded set of tasks, stop on ambiguity
- `overnight_safe` — unattended, conservative, no destructive actions
- `overnight_aggressive` — only for explicitly safe generation/analysis loops, never for writes or secrets

## 4. Validation Profiles

From OPENDORK.

Each runtime or gate can have a validation profile.

Examples:

```text
length
status-tag
scope-guard
no-secrets
no-future-gate
ci-green
handoff-present
```

Validation should be explicit and named, not implied.

## 5. Artifact Classes

From OPENDORK + DEAFPIPER.

Template projects should distinguish:

```text
raw/
validated/
rejected/
gold/
diffs/
reports/
replays/
```

This matters because rejected artifacts can still be useful as FLOW test inputs or future debugging material.

## 6. Provider / Model Catalog Pattern

From OPENDORK.

For AI-assisted projects, avoid hardcoding model behavior in prompts only.

Use a small config pattern:

```json
{
  "models": [
    {
      "modelName": "...",
      "providerClient": "...",
      "inputCostPer1K": 0,
      "outputCostPer1K": 0,
      "enabled": true
    }
  ]
}
```

Template use:

- document which model class is used for planning
- document which model class is used for execution
- document budget / token assumptions when relevant

## 7. Minimal README Is Not Enough

From CATALON-GUARD.

A short README with purpose/current status/next steps is useful but insufficient for agent-safe development.

Every active agent-driven project needs:

- exact build commands
- current gate/status
- known limitations
- repo structure
- source of truth declaration
- contribution/agent rules

## 8. Cross-Cutting Modules

From DEAFPIPER.

Useful reusable modules across projects:

- Translation / localization
- Slides / presentation builder
- Brand Guardian
- Research digest
- Release handoff
- QA report
- Privacy / risk check

These belong in ANVIL templates before they become app code.
