#!/usr/bin/env bash
set -euo pipefail

test -f app/index.html
test -f pake.config.json
test -f assets/anvil-icon.svg
node -e "JSON.parse(require('fs').readFileSync('pake.config.json', 'utf8')); console.log('pake.config.json ok')"

if command -v pake >/dev/null 2>&1; then
  pake --version
else
  npx --yes pake-cli --version
fi
