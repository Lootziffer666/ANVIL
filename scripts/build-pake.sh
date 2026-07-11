#!/usr/bin/env bash
set -euo pipefail

PAKE_CMD=(pake)
if ! command -v pake >/dev/null 2>&1; then
  PAKE_CMD=(npx --yes pake-cli)
fi

"${PAKE_CMD[@]}" ./app/index.html \
  --name "Anvil" \
  --icon assets/anvil-icon.svg \
  --width 1280 --height 800 \
  --use-local-file
