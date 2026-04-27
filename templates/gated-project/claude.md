# claude.md — Gated Project Working Rules

This file is the project-local working brief for Claude Code and similar coding agents.

Use `AGENTS.md` for general rules and this file for project-specific execution notes.

## Current Project State

Replace with:

- current gate
- last verified build
- active branch rules
- known environment limitations
- current non-goals

## Working Mode

Default:

```text
one gate, one commit, one handoff
```

Do not continue into the next gate unless explicitly instructed.

## Checks

Fill in project-specific checks.

Examples:

```bash
./gradlew :domain:test
./gradlew :domain:build
./gradlew :app:assembleDebug
```

or:

```bash
dotnet restore
dotnet build
dotnet test
```

or:

```bash
npm test
npm run build
```

## Local Environment Notes

Document known local limitations here.

Do not promote environment-only limitations to product bugs when CI is authoritative.

## Stop Conditions

Stop immediately if:

- scope becomes unclear
- tests fail for reasons outside current gate scope
- push / PR creation fails
- credentials are missing
- a destructive action would be required
- current gate cannot be safely marked PASS
