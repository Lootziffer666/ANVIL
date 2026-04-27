"""Template-level client defaults.

Customize this file first when forking the template.
"""

APP_NAME = "Anvil Template"
APP_VERSION = "0.1.0"
FEATURE_FLAGS = {
    "enable_demo_bootstrap": True,
}
ROLES = [
    "admin",
    "editor",
    "viewer",
]
INTEGRATIONS = {
    "analytics": False,
    "email": False,
    "payments": False,
}
