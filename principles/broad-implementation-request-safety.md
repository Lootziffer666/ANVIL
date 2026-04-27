# Broad Implementation Request Safety

This policy exists because broad user requests such as "replace all fake implementations with productive code" can accidentally collapse multiple risky gates into one uncontrolled integration run.

## Trigger Phrases

If the user says any of the following or equivalent:

```text
replace all fake implementations
make everything productive
wire everything end-to-end
finish it now
just do the whole integration
remove all stubs
```

then the agent must **not** begin implementation immediately.

## Required Response

First create:

1. Fake/stub inventory
2. Risk classification
3. Safe gate split
4. Recommended first gate
5. Explicit non-goals

Then stop.

Implementation may begin only after the user explicitly selects one gate.

## Hard Rule

Never collapse these into one unattended implementation run:

- auth
- token storage
- HTTP clients
- file/SAF access
- UI navigation
- ViewModels/state machines
- write execution
- automatic retries
- conflict/merge behavior

## Reason

These areas combine security, user data, irreversible writes, and platform permissions. They need separate gates, tests, and handoffs.

## Short Form

```text
Everything productive = inventory first, gate second.
```
