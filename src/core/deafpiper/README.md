# DEAFPIPER — Structured Handoffs & Command Channel

> Status: Skeleton (Gate 001)  
> No implementation yet. No existing skeleton directory maps to this system.

DEAFPIPER is Anvil's handoff layer. When one agent finishes and another begins — or when a human takes over from an agent — DEAFPIPER ensures the transition happens with full context and no silent losses.

## Responsibilities

- Produce structured handoff packages at run completion
- Export context for the next agent (what was done, what failed, what's next)
- Provide a typed command channel between agents (not freetext summaries)
- Prevent context loss when sessions reset or agents switch

## What DEAFPIPER Is Not

- Not a messaging system
- Not a chat interface
- Not a general logging system (that is CATALON's run state)

DEAFPIPER is the **deliberate transfer** layer. It fires when a handoff decision is made — not on every log line.

## Handoff Package Format

A DEAFPIPER handoff package contains:

```json
{
  "handoff_id": "HAND_{YYYYMMDD}_{HHMMSS}_{NNN}",
  "from_agent": "agent-id",
  "to_agent": "agent-id or human",
  "run_id": "RUN_{YYYYMMDD}_{HHMMSS}_{NNN}",
  "completed": ["what was done"],
  "failed": ["what failed and why"],
  "next_action": "concrete description of what the next actor must do",
  "artifacts": ["output-id list"],
  "context_snapshot": "optional: minimal state needed to continue"
}
```

## v0 Status

- No implementation
- No existing skeleton directory in `src/core/` covers this role
- Existing handoff docs: [`docs/AGENT_HANDOFF_FORMAT.md`](../../../docs/AGENT_HANDOFF_FORMAT.md) (Gate A8, partial)

## Rules

- Every agent session that ends with incomplete work must produce a DEAFPIPER handoff package
- No "here's a summary of what I did" freetext — only structured handoff packages count
- Handoff packages are stored as artifacts in `outputs/`
- The next actor must receive the handoff_id before starting work
