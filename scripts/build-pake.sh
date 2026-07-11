#!/usr/bin/env bash
set -euo pipefail

if ! command -v pake >/dev/null 2>&1; then
  echo "pake CLI not found. Install with: npm install -g pake-cli" >&2
  exit 2
fi

pake ./app/index.html \
  --name "Anvil" \
  --icon assets/anvil-icon.svg \
  --width 1280 --height 800 \
  --transparent
