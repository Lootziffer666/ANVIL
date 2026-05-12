# Module Slot Registry

> Status: Skeleton (Gate 001)  
> No implementation yet.

This directory owns the **module discovery and slot registry** — the runtime layer that finds installed modules, validates their contracts, and makes them available to The Forge.

## Responsibility

- Scan `modules/` for valid `module.json` files
- Validate each module against the Module Contract (`docs/MODULE_CONTRACT.md`)
- Maintain a runtime registry of available, invalid, and disabled modules
- Report slot status to The Forge

## What This Is Not

- Not a module itself — this is infrastructure
- Not a package manager — modules are installed by placing them in `modules/`
- Not The Forge UI — this provides data to The Forge, not the UI

## Contract Reference

Every module must follow [`docs/MODULE_CONTRACT.md`](../../../docs/MODULE_CONTRACT.md).  
A module without a valid `module.json` is invisible to this registry.

## v0 Status

Currently: The Forge in `app/app.js` reads module data from `app/data.js` (hardcoded).  
This directory will house the runtime replacement once the Execution Core is active.
