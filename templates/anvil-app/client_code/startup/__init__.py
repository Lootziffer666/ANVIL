"""Client startup entrypoint.

Anvil compatibility note:
- Keep startup orchestration in this package.
- The startup form itself is controlled in anvil.yaml.
"""

from .config import APP_NAME, APP_VERSION


def get_startup_banner() -> str:
    """Small helper used by pages to display template metadata."""
    return f"{APP_NAME} v{APP_VERSION}"
