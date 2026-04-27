# Tabula

Tabula is a system / workspace cleanup and order utility direction.

## Role In ANVIL

Tabula may reuse ANVIL principles around:

- safe preview before destructive action
- explicit file/system state
- gated cleanup
- no silent deletion
- recoverability

## Boundary

Any cleanup or file-moving feature is destructive-adjacent and must be gated conservatively.
