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

## Broad Implementation Request Safety

If the user asks to replace all fake/stub implementations, make everything productive, wire everything end-to-end, remove all stubs, or "just finish it", do not implement immediately.

First produce:

1. fake/stub inventory
2. risk classification
3. safe gate split
4. recommended first gate
5. explicit non-goals

Then stop and wait for the user to select one gate.

## Token / Usage Checkpoint Rule

If Claude Code shows a usage warning, context warning, or progress indicator suggesting roughly 90% usage / context / quota consumption, immediately secure the current work.

Required action:

1. Stop starting new implementation tasks.
2. Run the smallest meaningful verification available for the current work.
3. Update or create the relevant handoff file with current status.
4. Update `knownbugs.md` only if a real bug/risk was discovered.
5. Commit the current coherent work if it is safe to preserve.
6. If the work is not safe to commit, export a patch and write a clear recovery note.
7. Do not begin another gate or broad refactor after the warning.

Preferred commit message if preserving interrupted work:

```text
Gate X: checkpoint before usage limit
```

Never leave valuable work uncommitted merely because the ideal final verification could not complete before the usage limit. Be honest in the handoff about what passed, what failed, and what remains unverified.

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
- usage/context/quota warning appears and current work has not yet been committed or exported
