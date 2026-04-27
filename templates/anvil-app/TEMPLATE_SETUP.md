# TEMPLATE_SETUP Checklist

Use this checklist when adapting the template to a new Anvil app.

## 1) App metadata and startup

- [ ] Update `anvil.yaml` `name`
- [ ] Confirm `startup_form`
- [ ] Add/update `services` and `dependencies`
- [ ] Define/adjust `db_schema` if needed

## 2) Client configuration

- [ ] Update `client_code/startup/config.py` (`APP_NAME`, `APP_VERSION`)
- [ ] Replace `FEATURE_FLAGS` with project toggles
- [ ] Replace `ROLES` with real role model
- [ ] Set `INTEGRATIONS` toggles

## 3) Theme and branding

- [ ] Replace tokens in `theme/parameters.yaml`
- [ ] Update `theme/theme.css` styles
- [ ] Replace logo/assets in `theme/assets/`
- [ ] Adjust `theme/standard-page.html` metadata/title

## 4) Server implementation

- [ ] Replace `server_code/auth.py` stubs
- [ ] Replace `server_code/permissions.py` with real policy
- [ ] Implement real initialization in `server_code/bootstrap.py`
- [ ] Add concrete modules under `server_code/services/`
- [ ] Add repositories under `server_code/repositories/`

## 5) Documentation

- [ ] Update `README.md` with project purpose and boundaries
- [ ] Update `docs/architecture.md`
- [ ] Update `docs/customization.md`
- [ ] Update `docs/deployment.md`
