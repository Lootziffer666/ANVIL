# OPENDORK — Provider Router

> Status: Skeleton (Gate 001)  
> Named entry point. Implementation lives in `src/core/providers/`.

OPENDORK is Anvil's provider routing layer. It manages access to AI providers (APIs), selects models per task, and handles fallbacks.

## Named System → Implementation Mapping

| Named System | Implementation Home |
|---|---|
| OPENDORK | `src/core/opendork/` (this dir, entry point) |
| Provider Core | `src/core/providers/` (implementation) |

The `opendork/` directory holds the routing contract and configuration. The `providers/` directory holds the actual provider interface and credential management code.

This split exists because the name "OPENDORK" is the system identity, while "providers" is the technical implementation category. Both are preserved to avoid silent renaming.

## Responsibilities

- Multi-provider registry
- Model selection per task type
- Fallback chain execution
- No hard provider binding per workflow

## v0 Status

- Schema: [`src/core/types/provider.schema.json`](../types/provider.schema.json)
- Spec: [`docs/ANVIL_PROVIDER_ROUTING.md`](../../../docs/ANVIL_PROVIDER_ROUTING.md)
- Current provider catalog (hardcoded): `app/data.js` PROVIDER_REGISTRY (Gate A14)

## Rules

- OPENDORK does not store API keys (that is CATALON-GUARD's domain)
- OPENDORK does not expose a UI
- All provider calls go through OPENDORK — no module calls a provider directly
