# Reusable Anvil App Template

This folder is a reusable **Anvil app template** with clear separation between client, server, theme, and setup documentation.

## Structure

```text
templates/anvil-app/
├─ anvil.yaml
├─ README.md
├─ TEMPLATE_SETUP.md
├─ client_code/
├─ server_code/
├─ theme/
└─ docs/
```

## What this template includes

- predictable Anvil app scaffold
- startup/config module for template defaults
- server stubs for auth, permissions, bootstrap
- centralized theme tokens and CSS
- setup and customization documentation

## What to customize first

1. `anvil.yaml` → app name, startup form, services, dependencies, schema
2. `client_code/startup/config.py` → app constants, roles, feature toggles
3. `theme/parameters.yaml` + `theme/theme.css` → branding/colors/typography
4. `server_code/` modules → real auth, permissions, bootstrap, services, repositories
5. `docs/` + `TEMPLATE_SETUP.md` → project-specific conventions

## Safety note

This template intentionally ships with minimal non-production stubs. It does **not** claim production-ready authentication, authorization, or integrations.
