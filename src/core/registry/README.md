# Registry — Project & Workspace Directory

> Status: Skeleton (Gate 001)  
> No implementation yet.

This directory owns the **project registry** and **workspace registry** — the runtime service that tracks all known projects and workspaces within Anvil.

## Responsibilities

- Maintain `projects.json` — the machine-readable project registry
- Track project status, repo location, and gate phase
- Provide a read interface for other systems that need to know "which projects exist"

## Registry Files

```
src/core/registry/
├── projects.json     ← machine-readable project registry
└── README.md         ← this file
```

`projects.json` is the authoritative list. The human-readable version lives at [`docs/ANVIL_PROJECT_REGISTRY.md`](../../../docs/ANVIL_PROJECT_REGISTRY.md).

## v0 Status

In v0, `projects.json` is updated manually. No service reads it at runtime yet.

```json
{
  "projects": [
    {
      "id": "catchit",
      "name": "CatchIt",
      "repo": null,
      "status": "active",
      "gate_phase": "Planning",
      "notes": "State-surface UX + mobility trust tool"
    }
  ]
}
```

Full registry: [`docs/ANVIL_PROJECT_REGISTRY.md`](../../../docs/ANVIL_PROJECT_REGISTRY.md)  
Schema: [`src/core/types/project.schema.json`](../types/project.schema.json)

## Rules

- A project must have an entry here before implementation work begins
- Status changes are committed with a gate reference
- No silent deletions — archive instead
