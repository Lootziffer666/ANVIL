# Architecture

## Layers

- `anvil.yaml` = app metadata/startup/dependencies/services/schema
- `client_code/` = UI components, pages, flows, startup config
- `server_code/` = auth/permissions/bootstrap/services/repositories
- `theme/` = design tokens, CSS, HTML shell, assets

## Refactor Notes

- Created `client_code/startup/config.py` as central template config.
- Added page package `client_code/_pages/HomePage/` as startup-friendly form package.
- Added server boundaries: `auth.py`, `permissions.py`, `bootstrap.py`, `services/`, `repositories/`.
- Centralized visual tokens into `theme/parameters.yaml` and CSS defaults in `theme/theme.css`.

## Compatibility Notes

This scaffold aims to stay close to standard Anvil app folder expectations. If your Anvil editor requires additional generated metadata for specific Forms, add it in-place under the corresponding package.
