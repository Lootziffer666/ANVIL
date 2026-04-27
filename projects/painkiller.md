# PAINKILLER

Reference project for the ANVIL gated-agent workflow.

## Role In ANVIL

PAINKILLER proved the core workflow:

- one gate per run
- CI-first verification
- `AGENTS.md` + `claude.md`
- `knownbugs.md`
- handoff files
- PR templates
- issue templates
- safety reviews before risky gates

## Lessons Extracted

- Local Android SDK absence must not block progress when CI verifies Android builds.
- Push/PR credential failure must trigger patch export and stop.
- Gate prompts should name hard non-goals explicitly.
- Handoff files are more reliable than chat memory or stale PR titles.
