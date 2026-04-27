"""Server bootstrap helpers.

Use this module to initialize demo/default data when desired.
"""


def initialize_defaults(enable_demo: bool = False) -> dict:
    """Return a transparent summary of bootstrap actions."""
    actions = []
    if enable_demo:
        actions.append("demo-bootstrap-enabled")
    return {
        "initialized": True,
        "actions": actions,
    }
