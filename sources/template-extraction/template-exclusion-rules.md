# Template Exclusion Rules

Not every source-project idea belongs in a template.

## Do Not Copy From PAINKILLER

- product-specific GitHub upload logic
- PAINKILLER branding or CATALON-GUARD color tokens as default for all apps
- Git Data API implementation details unless the new project needs GitHub writes
- upload/security assumptions for non-upload apps

## Do Not Copy From DEAFPIPER

- all 10 stages into small implementation-only apps by default
- excessive JSON handoff bureaucracy for tiny tools
- CustomGPT profiles as mandatory setup
- marketing/legal stages before the product needs them

## Do Not Copy From OPENDORK

- provider routing into every app
- spend logging into non-AI apps
- model catalog where no provider switching exists
- aggressive unattended runtime profiles for write-capable tools

## Do Not Copy From CATALON-GUARD

- vague README-only status documentation
- generic automation-test framing where the project needs product-specific gates
- unclear relation between purpose, current status, and executable build commands

## Global Rule

Template defaults must reduce friction, not create ceremony.

A template is successful when it makes the next safe action obvious.
