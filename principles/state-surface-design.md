# State Surface Design

State Surface Design is the shared UX grammar extracted from CatchIt and generalized for other ANVIL projects.

A surface is not a dashboard. It is a stateful operational field.

## Core Rule

```text
The interface changes because responsibility changes.
```

The visible anchor depends on whether the system, the user, or the environment currently carries the semantic load.

## Typical Anchors

- countdown
- instruction
- confirmation
- failure reason
- next safe action
- blocked-state explanation

## Anti-Pattern

Do not show all known information just because it exists.

## Application

This principle may influence CatchIt, Borderline, PAINKILLER, FLOW-family tools, and later Zen-OS-facing utilities.

It does not require every app to look like CatchIt.
