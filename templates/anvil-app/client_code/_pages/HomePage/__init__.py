"""Minimal startup page for template validation in Anvil editor."""

from anvil import *  # type: ignore

from ...startup import get_startup_banner


class HomePage(HomePageTemplate):
    def __init__(self, **properties):
        self.init_components(**properties)
        if hasattr(self, "headline_label"):
            self.headline_label.text = get_startup_banner()
