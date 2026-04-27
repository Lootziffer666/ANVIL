# Deployment Notes

## Before first deployment

- Verify startup form in `anvil.yaml`
- Verify service configuration in `anvil.yaml`
- Disable unused feature flags in `client_code/startup/config.py`
- Remove demo bootstrap behavior unless intentionally used

## Risk reminders

- Current auth/permissions/bootstrap modules are starter stubs.
- Do not treat placeholders as production controls.
