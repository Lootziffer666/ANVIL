#!/usr/bin/env bash
set -euo pipefail

# Export the last in-repo ANVIL-BARD module snapshot into a separate private repo
# without re-adding BARD to the active ANVIL build.
#
# Usage:
#   scripts/export-bard-private-repo.sh /path/to/private/anvil-bard [source-ref]
#
# Default source-ref is the last commit before BARD was externalized from ANVIL.

TARGET_DIR="${1:-}"
SOURCE_REF="${2:-3385012}"
SOURCE_PREFIX="anvil-kmp/modules/bard"

if [[ -z "$TARGET_DIR" ]]; then
  echo "Usage: $0 /path/to/private/anvil-bard [source-ref]" >&2
  exit 64
fi

if ! git cat-file -e "${SOURCE_REF}^{commit}" 2>/dev/null; then
  echo "Source ref not found: ${SOURCE_REF}" >&2
  exit 65
fi

if ! git ls-tree -r --name-only "${SOURCE_REF}" -- "${SOURCE_PREFIX}" | grep -q .; then
  echo "${SOURCE_PREFIX} does not exist at ${SOURCE_REF}" >&2
  exit 66
fi

mkdir -p "$TARGET_DIR"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

git archive "$SOURCE_REF" "$SOURCE_PREFIX" | tar -x -C "$TMP_DIR"
find "$TARGET_DIR" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
cp -R "$TMP_DIR/$SOURCE_PREFIX/." "$TARGET_DIR/"

cat > "$TARGET_DIR/EXTERNALIZED_FROM_ANVIL.md" <<MARKER
# ANVIL-BARD externalization marker

This private repo snapshot was exported from ANVIL.

- Source repository path: \`$SOURCE_PREFIX\`
- Source commit: \`$SOURCE_REF\`
- Exported at: \`$(date -u +%Y-%m-%dT%H:%M:%SZ)\`

ANVIL should consume only versioned BARD contracts/artifact refs. Private profile
content and BARD implementation code must remain in this private repo.
MARKER

echo "Exported ANVIL-BARD snapshot from ${SOURCE_REF}:${SOURCE_PREFIX} to ${TARGET_DIR}"
