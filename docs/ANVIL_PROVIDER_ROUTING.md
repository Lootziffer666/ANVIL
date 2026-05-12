# ANVIL_PROVIDER_ROUTING.md — OPENDORK

**Gate:** 001 — Core Skeleton  
**Stand:** 2026-05-12  
**Status:** Verbindlich (Spec)

---

> Default to absence. Do not add visible UI unless the current state requires it. Empty space is an active design state, not unused space.
>
> Every visible element must justify its existence by reducing user burden right now.
>
> Before adding anything, remove. Before proposing UI, justify absence. Before creating controls, name the state that makes them necessary. If no state requires the element, do not render it.

---

## What OPENDORK Is

OPENDORK is Anvil's provider routing layer. It manages access to AI providers (APIs), selects models per task, and handles fallbacks when a provider is unavailable or rate-limited.

OPENDORK is infrastructure. It is not a module. It does not appear in The Forge.

Implementation home: [`src/core/opendork/`](../src/core/opendork/) → [`src/core/providers/`](../src/core/providers/)

---

## Core Responsibilities

### 1. Multiple Providers / API Endpoints

OPENDORK holds a registry of configured providers. Each provider entry contains:

```json
{
  "id": "string",
  "name": "string",
  "endpoint": "string (base URL)",
  "auth_type": "bearer | api-key | oauth",
  "models": ["string"],
  "status": "active | degraded | disabled",
  "fallback_to": "provider-id | null"
}
```

No provider is the single source of truth. OPENDORK treats all providers as potentially unavailable.

Reference: [`docs/PROVIDER_REGISTRY.md`](PROVIDER_REGISTRY.md) for the current provider catalog.

---

### 2. Model Selection Per Task

Different task types call for different models. OPENDORK maps task types to model preferences:

```json
{
  "task_type": "code | summarize | plan | test | review | embed",
  "preferred_model": "provider-id/model-id",
  "fallback_models": ["provider-id/model-id"]
}
```

Rules:
- No workflow may hardcode a provider-id or model-id directly.
- Model selection happens at run time via OPENDORK, not at workflow definition time.
- Task types are defined by CATALON, not by OPENDORK.

---

### 3. Fallback Chains

When a provider call fails (network error, rate limit, auth failure):

1. OPENDORK logs the failure with timestamp and error code.
2. OPENDORK selects the next provider in the fallback chain for that task type.
3. If all fallbacks are exhausted, the run moves to `act_now` state — human review required.
4. No silent retry beyond the defined fallback chain.

```
primary → fallback_1 → fallback_2 → act_now (human review)
```

---

### 4. No Hard Provider Binding Per Workflow

A workflow definition (Plan) may express a *preference* but not a *requirement*:

```json
{
  "preferred_provider": "openai",
  "allow_fallback": true
}
```

If `allow_fallback` is false and the provider is unavailable, the Run fails with a clear error — it does not silently use a different provider.

---

### 5. Future: Automatic Intra-Provider Model Switching

When a model within a provider is deprecated or capacity-limited, OPENDORK will support automatic switching to the next model in the provider's model list — without changing the provider.

This is planned, not implemented. No code for this in v0.

---

## v0 Status

The existing provider registry lives in `app/data.js` (Gate A14). It is a hardcoded JavaScript array — not a routing engine.

OPENDORK in v0 is:
- A schema (`src/core/types/provider.schema.json`)
- A named directory (`src/core/opendork/`, `src/core/providers/`)
- This spec document

OPENDORK implementation begins in a future gate once the Execution Core is active.

---

## What OPENDORK Does Not Do

- OPENDORK does not store API keys. Key management is CATALON-GUARD's responsibility via the permission model.
- OPENDORK does not define task types. Task types come from CATALON.
- OPENDORK does not expose a UI. Provider selection is never surfaced to the user unless a fallback fails and human action is required.

---

## Cross-References

- [`docs/PROVIDER_REGISTRY.md`](PROVIDER_REGISTRY.md) — current provider catalog (hardcoded, Gate A14)
- [`docs/TOKEN_MANAGEMENT.md`](TOKEN_MANAGEMENT.md) — API token lifecycle
- [`src/core/opendork/README.md`](../src/core/opendork/README.md) — directory readme
- [`src/core/types/provider.schema.json`](../src/core/types/provider.schema.json) — provider schema
