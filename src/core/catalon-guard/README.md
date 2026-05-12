# CATALON-GUARD — Guardrails & Quality Control

> Status: Skeleton (Gate 001)  
> Named entry point. Implementation lives in `src/core/safety/`.

CATALON-GUARD is Anvil's enforcement layer. It checks gates, enforces kill criteria, manages permissions, and requires human review at defined checkpoints.

## Named System → Implementation Mapping

| Named System | Implementation Home |
|---|---|
| CATALON-GUARD | `src/core/catalon-guard/` (this dir, entry point) |
| Safety Engine | `src/core/safety/` (implementation) |

The `catalon-guard/` directory holds the guardrail contract. The `safety/` directory holds the policy implementations.

## Responsibilities

- Gate acceptance criteria validation
- Kill criteria enforcement (stop a run that violates scope)
- Permission model — what each module/agent may do
- Human review gate — when a run must pause for human approval
- Scope validation — a run cannot touch files outside its declared scope

## v0 Status

- Schema: [`src/core/types/gate.schema.json`](../types/gate.schema.json)
- Spec: [`docs/ANVIL_GOVERNANCE.md`](../../../docs/ANVIL_GOVERNANCE.md)
- Architecture: [`docs/EXECUTION_CORE_ARCHITECTURE.md`](../../../docs/EXECUTION_CORE_ARCHITECTURE.md) (Gate AT4)
- Known issue: Token manager (Gate A13) uses `storage.local` permission not yet in approved list

## Rules

- CATALON-GUARD may stop any run at any time if kill criteria are triggered
- CATALON-GUARD does not skip gates for convenience
- Human review is required by default — `human_review_required: false` requires explicit gate permission
- CATALON-GUARD does not store secrets — it validates that secrets are handled by the correct subsystem
