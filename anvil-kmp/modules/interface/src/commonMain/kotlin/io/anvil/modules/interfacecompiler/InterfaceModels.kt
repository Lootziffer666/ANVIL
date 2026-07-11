package io.anvil.modules.interfacecompiler

import kotlinx.serialization.Serializable

@Serializable
enum class InterfaceOperation { COMPILE, VALIDATE }

@JvmInline
@Serializable
value class InterfaceBundleId(val value: String)

@JvmInline
@Serializable
value class InputActionId(val value: String)

@JvmInline
@Serializable
value class HudStateId(val value: String)

@JvmInline
@Serializable
value class MenuId(val value: String)

@Serializable
data class InterfaceIntent(
    val schema: String = SCHEMA,
    val intentId: String,
    val workspaceId: String,
    val runId: String,
    val creativeBriefRef: String,
    val gameplayPlanRef: String,
    val sceneBundleRef: String,
    val requiredActions: List<String>,
    val coopRoles: List<String>,
    val accessibilityNeeds: List<AccessibilityNeed> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.interface.intent/v1" }
}

@Serializable
data class InterfaceBundle(
    val schema: String = SCHEMA,
    val bundleId: InterfaceBundleId,
    val creativeBriefRef: String,
    val gameplayPlanRef: String,
    val sceneBundleRef: String,
    val inputActionMap: InputActionMap,
    val prompts: List<InteractionPrompt>,
    val hudStates: List<HUDState>,
    val menuGraph: MenuGraph,
    val tutorialFlow: TutorialFlow,
    val accessibilityProfile: AccessibilityProfile,
    val interfaceOwner: String = "anvil-interface-compiler",
) {
    companion object { const val SCHEMA = "anvil.interface.bundle/v1" }
}

@Serializable
data class InputActionMap(
    val schema: String = SCHEMA,
    val actions: List<InputAction>,
) {
    companion object { const val SCHEMA = "anvil.interface.input-action-map/v1" }
}

@Serializable
data class InputAction(
    val id: InputActionId,
    val semanticName: String,
    val bindings: List<InputBinding>,
    val contexts: List<String>,
)

@Serializable
data class InputBinding(
    val device: InputDevice,
    val control: String,
)

@Serializable
enum class InputDevice { KEYBOARD, GAMEPAD, TOUCH }

@Serializable
data class InteractionPrompt(
    val schema: String = SCHEMA,
    val actionRef: InputActionId,
    val label: String,
    val appearsWhen: String,
) {
    companion object { const val SCHEMA = "anvil.interface.interaction-prompt/v1" }
}

@Serializable
data class HUDState(
    val schema: String = SCHEMA,
    val id: HudStateId,
    val visibleElements: List<String>,
    val hiddenUntil: String? = null,
) {
    companion object { const val SCHEMA = "anvil.interface.hud-state/v1" }
}

@Serializable
data class MenuGraph(
    val schema: String = SCHEMA,
    val root: MenuId,
    val nodes: List<MenuNode>,
) {
    companion object { const val SCHEMA = "anvil.interface.menu-graph/v1" }
}

@Serializable
data class MenuNode(
    val id: MenuId,
    val label: String,
    val actions: List<InputActionId>,
)

@Serializable
data class TutorialFlow(
    val schema: String = SCHEMA,
    val steps: List<TutorialStep>,
) {
    companion object { const val SCHEMA = "anvil.interface.tutorial-flow/v1" }
}

@Serializable
data class TutorialStep(
    val id: String,
    val promptRef: InputActionId,
    val completionSignal: String,
)

@Serializable
data class AccessibilityProfile(
    val schema: String = SCHEMA,
    val needs: List<AccessibilityNeed>,
    val subtitleRequired: Boolean,
    val remappingAllowed: Boolean,
    val colorOnlySignalsForbidden: Boolean,
) {
    companion object { const val SCHEMA = "anvil.interface.accessibility-profile/v1" }
}

@Serializable
enum class AccessibilityNeed { SUBTITLES, REMAPPABLE_INPUT, HIGH_CONTRAST, REDUCED_MOTION, COOP_ROLE_CLARITY }

@Serializable
data class InterfaceValidationReport(
    val schema: String = SCHEMA,
    val bundleRef: InterfaceBundleId,
    val passed: List<InterfaceValidationCheck>,
    val failed: List<InterfaceValidationCheck>,
) {
    companion object { const val SCHEMA = "anvil.interface.validation-report/v1" }
}

@Serializable
data class InterfaceValidationCheck(
    val id: String,
    val passed: Boolean,
    val message: String,
)
