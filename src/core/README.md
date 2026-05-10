# Anvil Execution Core

> Status: Skeleton (Gate AT4)  
> No active execution code exists yet.

This is the root of Anvil's native Execution Core.  
See [`docs/EXECUTION_CORE_ARCHITECTURE.md`](../../docs/EXECUTION_CORE_ARCHITECTURE.md) for the full architecture.

## Structure

```text
core/
├── planning/          ← Plan Engine
├── tasks/             ← Task Engine
├── runs/              ← Run Engine
├── repo/              ← Repo Context Engine
├── execution/
│   ├── file-mutations/ ← File Mutation Engine
│   └── commands/       ← Command Guard
├── providers/         ← Provider Core
├── artifacts/         ← Artifact Engine
├── safety/            ← Safety Engine
└── branching/         ← Branch / PR Engine
```

## Rules

- No donor code in this directory. Only Anvil-native implementations.
- All code must use Anvil terminology exclusively.
- See `docs/CODEBASE_TRANSPLANT_RULES.md` for transplant governance.
