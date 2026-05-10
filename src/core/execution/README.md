# Execution Engines

> Status: Skeleton — no implementation yet

Contains the engines that perform actual mutations and commands:

- `file-mutations/` — File Mutation Engine (read, write, edit with diff tracking and rollback)
- `commands/` — Command Guard (allowlisted, scoped, audited shell execution)
