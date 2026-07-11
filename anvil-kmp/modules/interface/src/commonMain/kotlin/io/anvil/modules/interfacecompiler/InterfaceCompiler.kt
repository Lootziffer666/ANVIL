package io.anvil.modules.interfacecompiler

import io.anvil.core.contracts.BootResult
import io.anvil.core.contracts.ExecutionPhase
import io.anvil.core.contracts.ModuleArtifactRef
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InterfaceCompilerModule : ModuleSlotContract {
    override val name: String = "anvil-interface-compiler"
    override val purpose: String =
        "Compiles semantic input and interface contracts without binding gameplay to raw controls."

    private var quality = QualityState.STABLE
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override fun qualityState(): QualityState = quality

    override suspend fun boot(ctx: ModuleContext): BootResult = BootResult(
        moduleId = name,
        qualityState = quality,
        executionPhase = ExecutionPhase.IDLE,
        message = "Interface Compiler booted; UI emits semantic actions, not gameplay outcomes.",
    )

    override suspend fun handle(step: ModuleRunStep): StepResult = when (InterfaceOperation.valueOf(step.operation)) {
        InterfaceOperation.COMPILE -> {
            val bundle = compile(json.decodeFromString<InterfaceIntent>(step.payload))
            val payload = json.encodeToString(bundle)
            complete(step.context, InterfaceBundle.SCHEMA, payload)
        }
        InterfaceOperation.VALIDATE -> {
            val report = validate(json.decodeFromString<InterfaceBundle>(step.payload))
            val payload = json.encodeToString(report)
            complete(step.context, InterfaceValidationReport.SCHEMA, payload)
        }
    }

    fun compile(intent: InterfaceIntent): InterfaceBundle {
        require(intent.schema == InterfaceIntent.SCHEMA) { "Unsupported interface intent schema: ${intent.schema}" }
        val requested = intent.requiredActions.ifEmpty { listOf("MOVE", "LOOK", "INTERACT", "CANCEL") }
        val actions = requested.distinct().map { action -> actionFor(action) }
        val interact = actions.firstOrNull { it.semanticName == "INTERACT" } ?: actionFor("INTERACT")
        val allActions = if (actions.any { it.semanticName == "INTERACT" }) actions else actions + interact

        return InterfaceBundle(
            bundleId = InterfaceBundleId("IFB_${stableHash(intent.intentId + intent.gameplayPlanRef)}"),
            creativeBriefRef = intent.creativeBriefRef,
            gameplayPlanRef = intent.gameplayPlanRef,
            sceneBundleRef = intent.sceneBundleRef,
            inputActionMap = InputActionMap(actions = allActions),
            prompts = listOf(
                InteractionPrompt(
                    actionRef = interact.id,
                    label = "Interact",
                    appearsWhen = "player inside an InteractionZone and gameplay runtime exposes an available interaction",
                ),
            ),
            hudStates = listOf(
                HUDState(
                    id = HudStateId("HUD_MINIMAL"),
                    visibleElements = listOf("interaction-prompt", "role-indicator"),
                    hiddenUntil = "scene-ready",
                ),
            ),
            menuGraph = MenuGraph(
                root = MenuId("MENU_ROOT"),
                nodes = listOf(MenuNode(MenuId("MENU_ROOT"), "Pause", allActions.map { it.id })),
            ),
            tutorialFlow = TutorialFlow(
                steps = listOf(TutorialStep("TUT_INTERACT", interact.id, "first-successful-interact-action")),
            ),
            accessibilityProfile = AccessibilityProfile(
                needs = (intent.accessibilityNeeds + AccessibilityNeed.REMAPPABLE_INPUT + AccessibilityNeed.COOP_ROLE_CLARITY).distinct(),
                subtitleRequired = AccessibilityNeed.SUBTITLES in intent.accessibilityNeeds,
                remappingAllowed = true,
                colorOnlySignalsForbidden = true,
            ),
        )
    }

    fun validate(bundle: InterfaceBundle): InterfaceValidationReport {
        val actionIds = bundle.inputActionMap.actions.map { it.id }.toSet()
        val checks = listOf(
            check("has-actions", bundle.inputActionMap.actions.isNotEmpty(), "InputActionMap must define semantic actions."),
            check("has-interact", bundle.inputActionMap.actions.any { it.semanticName == "INTERACT" }, "Interface bundle must define INTERACT."),
            check("no-empty-bindings", bundle.inputActionMap.actions.all { it.bindings.isNotEmpty() }, "Every action needs at least one binding."),
            check("prompt-actions-exist", bundle.prompts.all { it.actionRef in actionIds }, "Prompts must reference existing actions."),
            check("remapping-allowed", bundle.accessibilityProfile.remappingAllowed, "MVP requires remappable input."),
            check("interface-owner-only", bundle.interfaceOwner == name, "Interface owns controls and feedback only, not gameplay outcomes."),
        )
        return InterfaceValidationReport(
            bundleRef = bundle.bundleId,
            passed = checks.filter { it.passed },
            failed = checks.filterNot { it.passed },
        )
    }

    private fun actionFor(action: String): InputAction {
        val normalized = action.uppercase()
        return InputAction(
            id = InputActionId("ACT_$normalized"),
            semanticName = normalized,
            bindings = defaultBindings(normalized),
            contexts = listOf("gameplay", "menu-safe"),
        )
    }

    private fun defaultBindings(action: String): List<InputBinding> = when (action) {
        "MOVE" -> listOf(InputBinding(InputDevice.KEYBOARD, "WASD"), InputBinding(InputDevice.GAMEPAD, "LeftStick"), InputBinding(InputDevice.TOUCH, "VirtualStick"))
        "LOOK" -> listOf(InputBinding(InputDevice.KEYBOARD, "Mouse"), InputBinding(InputDevice.GAMEPAD, "RightStick"), InputBinding(InputDevice.TOUCH, "Drag"))
        "INTERACT" -> listOf(InputBinding(InputDevice.KEYBOARD, "E"), InputBinding(InputDevice.GAMEPAD, "ButtonSouth"), InputBinding(InputDevice.TOUCH, "TapPrompt"))
        "CANCEL" -> listOf(InputBinding(InputDevice.KEYBOARD, "Escape"), InputBinding(InputDevice.GAMEPAD, "ButtonEast"), InputBinding(InputDevice.TOUCH, "BackGesture"))
        else -> listOf(InputBinding(InputDevice.KEYBOARD, action))
    }

    private fun complete(context: ModuleContext, type: String, payload: String): StepResult.Completed {
        val hash = stableHash(payload)
        return StepResult.Completed(
            artifact = ModuleArtifactRef(
                id = "ART_INTERFACE_$hash",
                workspaceId = context.workspaceId,
                runId = context.runId,
                moduleOrigin = name,
                type = type,
                uri = "${context.artifactRoot}/interface/$hash.json",
                sha256 = "sha256:$hash",
                timestamp = "pending-artifact-writer-timestamp",
            ),
            payload = payload,
            executionPhase = ExecutionPhase.COMPLETE,
        )
    }

    private fun check(id: String, passed: Boolean, message: String) = InterfaceValidationCheck(id, passed, message)

    private fun stableHash(text: String): String = text.fold(0) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }.toString(16)
}
