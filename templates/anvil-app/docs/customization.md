# Customization Guide

## Quick path

1. Rename app and update metadata in `anvil.yaml`
2. Update template constants in `client_code/startup/config.py`
3. Implement real auth/permissions/bootstrap in `server_code/`
4. Apply branding in `theme/`

## Recommended conventions

- Keep feature flags centralized in `client_code/startup/config.py`
- Keep permission decisions in `server_code/permissions.py`
- Keep external integration clients under `server_code/services/`
- Keep data access wrappers under `server_code/repositories/`
