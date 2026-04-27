"""Role and permission helpers for template projects."""

from typing import Iterable


def has_role(user_roles: Iterable[str], required_role: str) -> bool:
    """Minimal deterministic permission check."""
    return required_role in set(user_roles)
