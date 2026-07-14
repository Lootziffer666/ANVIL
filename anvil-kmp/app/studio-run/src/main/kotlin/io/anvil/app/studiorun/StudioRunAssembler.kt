package io.anvil.app.studiorun

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.run.RunContractRef
import io.anvil.core.run.RunPlan
import io.anvil.core.run.RunPlanId
import io.anvil.core.run.RunPlanStep
import io.anvil.modules.acoustic.AcousticOperation
import io.anvil.modules.acoustic.AcousticRuntimeModule
import io.anvil.modules.acoustic.AudioIntent
import io.anvil.modules.gameplay.GameplayCompileRequest
import io.anvil.modules.gameplay.GameplayCompilerModule
import io.anvil.modules.gameplay.GameplayOperation
import io.anvil.modules.interfacecompiler.InterfaceCompilerModule
import io.anvil.modules.interfacecompiler.InterfaceIntent
import io.anvil.modules.interfacecompiler.InterfaceOperation
import io.anvil.modules.scene.SceneCompilerModule
import io.anvil.modules.scene.SceneIntent
import io.anvil.modules.scene.SceneOperation
import io.anvil.modules.target.ProductionBundle
import io.anvil.modules.target.ProductionBundleId
import io.anvil.modules.target.TargetAdapterModule
import io.anvil.modules.target.TargetDescriptor
import io.anvil.modules.target.TargetEngine
import io.anvil.modules.target.TargetOperation
import io.anvil.modules.target.TargetPlatform
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Turns a [StudioRunInput] (3-word seed + engine choice) into a real [RunPlan] executed
 * against the real native module chain — Gameplay → Scene → Interface → Acoustic → Target
 * — the same shape `GoldenRunTest` proves with fixtures, here run for real with a real
 * seed. Audio *asset generation* (`AcousticProducerModule` + a real `AcousticProvider`) is
 * deliberately out of this plan: the user is building that pipeline separately (2026-07-13
 * gap survey). `AcousticRuntimeModule` (AudioIntent → AudioCueGraph) stays in — it is
 * Anvil's own native compiler, no external audio backend required.
 *
 * Pure and deterministic (no wall-clock, no IO) so it is unit-testable without a real
 * RunSurface execution — [Main] supplies `runId`/`createdAt` at the process boundary.
 */
data class StudioRunInput(
    val seedWords: List<String>,
    val workspaceId: String,
    val runId: String,
    val artifactRoot: String,
    val verbs: List<String> = listOf("interact"),
    val playerRoles: List<String> = listOf("player"),
    val targetEngine: TargetEngine = TargetEngine.WEB,
    val targetPlatform: TargetPlatform = TargetPlatform.WEB,
) {
    init {
        require(seedWords.isNotEmpty()) { "StudioRunInput requires at least one seed word." }
        require(verbs.isNotEmpty()) { "StudioRunInput requires at least one verb." }
        require(playerRoles.isNotEmpty()) { "StudioRunInput requires at least one player role." }
    }
}

data class StudioRunAssembly(
    val plan: RunPlan,
    val modules: Map<String, ModuleSlotContract>,
)

object StudioRunAssembler {

    private val json = Json { ignoreUnknownKeys = true }

    fun assemble(input: StudioRunInput): StudioRunAssembly {
        val seedRef = "seed:" + input.seedWords.joinToString("-") { it.lowercase() }
        val creativeBriefRef = "$seedRef:brief"
        val productionIntentRef = "$seedRef:production-intent"

        val gameplayRequest = GameplayCompileRequest(
            requestId = "REQ_${input.runId}_GAMEPLAY",
            workspaceId = input.workspaceId,
            runId = input.runId,
            creativeBriefRef = creativeBriefRef,
            productionIntentRef = productionIntentRef,
            verbs = input.verbs,
            playerRoles = input.playerRoles,
            worldChangingActions = listOf("world.changed"),
            recoveryModel = "no recovery model specified for this seed run",
            capabilityCast = input.verbs,
            acceptanceCriteria = listOf("run completes without a blocked or failed step"),
        )
        // Precompute deterministic refs the same way GoldenRunTest does: module payloads
        // carry logical string refs, not ArtifactStore hash IDs, so calling the pure
        // compile() function directly (outside RunSurface) to learn those refs ahead of
        // building the plan is not circular — it only depends on fields already fixed above.
        val previewGameplayPlan = GameplayCompilerModule().compile(gameplayRequest)
        val interactionRefs = previewGameplayPlan.interactions.map { it.id.value }

        val sceneIntent = SceneIntent(
            intentId = "INTENT_${input.runId}_SCENE",
            workspaceId = input.workspaceId,
            runId = input.runId,
            creativeBriefRef = creativeBriefRef,
            productionIntentRef = productionIntentRef,
            environmentRefs = listOf("$seedRef:environment"),
            gameplayPlanRef = previewGameplayPlan.planId.value,
            interactionRefs = interactionRefs,
            requiredRoles = input.playerRoles,
        )
        val previewSceneBundle = SceneCompilerModule().compile(sceneIntent)

        val interfaceIntent = InterfaceIntent(
            intentId = "INTENT_${input.runId}_INTERFACE",
            workspaceId = input.workspaceId,
            runId = input.runId,
            creativeBriefRef = creativeBriefRef,
            gameplayPlanRef = previewGameplayPlan.planId.value,
            sceneBundleRef = previewSceneBundle.sceneId.value,
            requiredActions = listOf("MOVE", "INTERACT"),
            coopRoles = input.playerRoles,
        )

        val audioIntent = AudioIntent(
            intentId = "INTENT_${input.runId}_AUDIO",
            workspaceId = input.workspaceId,
            runId = input.runId,
            creativeBriefRef = creativeBriefRef,
            gameplayPlanRef = previewGameplayPlan.planId.value,
            sceneBundleRef = previewSceneBundle.sceneId.value,
            emotionalFunctions = listOf("seed:${input.seedWords.joinToString(" ")}"),
            worldStateInputs = emptyList(),
            requiredCues = emptyList(),
        )

        val productionBundle = ProductionBundle(
            bundleId = ProductionBundleId("PB_${input.runId}"),
            workspaceId = input.workspaceId,
            runId = input.runId,
            target = TargetDescriptor(input.targetEngine, "1.0", input.targetPlatform),
            creativeBriefRef = creativeBriefRef,
            gameplayPlanRef = previewGameplayPlan.planId.value,
            sceneBundleRef = previewSceneBundle.sceneId.value,
            actorBundleRefs = emptyList(),
        )

        val modules: Map<String, ModuleSlotContract> = mapOf(
            "gameplay" to GameplayCompilerModule(),
            "scene" to SceneCompilerModule(),
            "interface" to InterfaceCompilerModule(),
            "acoustic" to AcousticRuntimeModule(),
            "target" to TargetAdapterModule(),
        )

        val plan = RunPlan(
            planId = RunPlanId("PLAN_${input.runId}"),
            workspaceId = input.workspaceId,
            runId = input.runId,
            artifactRoot = input.artifactRoot,
            steps = listOf(
                RunPlanStep(
                    id = "S_GAMEPLAY", moduleId = "gameplay", operation = GameplayOperation.COMPILE.name,
                    payload = json.encodeToString(gameplayRequest),
                    outputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 1),
                ),
                RunPlanStep(
                    id = "S_SCENE", moduleId = "scene", operation = SceneOperation.COMPILE.name,
                    payload = json.encodeToString(sceneIntent),
                    dependsOn = listOf("S_GAMEPLAY"),
                    inputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 1),
                    outputContract = RunContractRef(ContractId("anvil.scene-bundle"), 1),
                ),
                RunPlanStep(
                    id = "S_INTERFACE", moduleId = "interface", operation = InterfaceOperation.COMPILE.name,
                    payload = json.encodeToString(interfaceIntent),
                    dependsOn = listOf("S_SCENE"),
                    inputContract = RunContractRef(ContractId("anvil.scene-bundle"), 1),
                    outputContract = RunContractRef(ContractId("anvil.interface.bundle"), 1),
                ),
                RunPlanStep(
                    id = "S_ACOUSTIC", moduleId = "acoustic", operation = AcousticOperation.COMPILE.name,
                    payload = json.encodeToString(audioIntent),
                    dependsOn = listOf("S_SCENE"),
                    inputContract = RunContractRef(ContractId("anvil.scene-bundle"), 1),
                    outputContract = RunContractRef(ContractId("anvil.audio-cue-graph"), 1),
                ),
                RunPlanStep(
                    id = "S_TARGET", moduleId = "target", operation = TargetOperation.PREPARE.name,
                    payload = json.encodeToString(productionBundle),
                    dependsOn = listOf("S_INTERFACE", "S_ACOUSTIC"),
                    inputContract = RunContractRef(ContractId("anvil.audio-cue-graph"), 1),
                    outputContract = RunContractRef(ContractId("anvil.runnable-build"), 1),
                ),
            ),
        )

        return StudioRunAssembly(plan, modules)
    }
}
