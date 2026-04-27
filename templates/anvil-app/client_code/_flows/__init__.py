"""Navigation and state-transition helper functions for the client layer."""


def resolve_landing_page(is_authenticated: bool) -> str:
    """Return symbolic page key for simple flow examples."""
    return "home" if is_authenticated else "home"
