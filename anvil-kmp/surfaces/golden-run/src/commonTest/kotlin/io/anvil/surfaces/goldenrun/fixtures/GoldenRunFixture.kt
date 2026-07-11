package io.anvil.surfaces.goldenrun.fixtures

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState
import io.anvil.modules.gameplay.GameplayCompileRequest
import io.anvil.modules.interfacecompiler.InterfaceIntent
import io.anvil.modules.scene.SceneIntent

/**
 * Golden Run scenario (Gate I-01):
 *
 *   Zwei Spieler bewegen gemeinsam eine schwere Kiste. Der Träger wird langsamer.
 *   Der zweite Spieler öffnet den Weg. Gefahr steigt. SHADED zeigt Staub und
 *   Belastung. Audio blendet am nächsten Taktanfang Percussion ein.
 *
 * Deliberately small: 2 gameplay verbs (`push`, `open`), 2 coop roles (`carrier`,
 * `opener`), one INTERACT-shaped interaction zone, one world event, one
 * state-reactive audio cue, and a SHADED scene-config ref. Every "Fixture" type
 * below is a deterministic offline stand-in — none of them claim real creative,
 * production, or technical verification (that stays with the sibling systems).
 */
object GoldenRunFixture {
    const val WORKSPACE_ID = "ws-golden-run"
    const val RUN_ID = "run-golden-1"
    const val ARTIFACT_ROOT = "artifact://golden-run"
    const val CREATED_AT = "2026-07-11T00:00:00Z"

    const val CREATIVE_SEED = "Two players must move a heavy crate together while danger rises around them."

    fun gameplayRequest(creativeBriefRef: String, productionIntentRef: String) = GameplayCompileRequest(
        requestId = "REQ_GOLDEN_GAMEPLAY",
        workspaceId = WORKSPACE_ID,
        runId = RUN_ID,
        creativeBriefRef = creativeBriefRef,
        productionIntentRef = productionIntentRef,
        verbs = listOf("push", "open"),
        playerRoles = listOf("carrier", "opener"),
        worldChangingActions = listOf("crate.moved", "path.opened"),
        recoveryModel = "the opener can re-open a jammed path; the carrier can drop the crate to recover stamina",
        capabilityCast = listOf("push", "pull", "lift-heavy"),
        acceptanceCriteria = listOf("crate reaches the marked destination", "path obstruction is cleared"),
    )

    fun sceneIntent(creativeBriefRef: String, productionIntentRef: String, gameplayPlanRef: String, interactionRefs: List<String>) = SceneIntent(
        intentId = "INTENT_GOLDEN_SCENE",
        workspaceId = WORKSPACE_ID,
        runId = RUN_ID,
        creativeBriefRef = creativeBriefRef,
        productionIntentRef = productionIntentRef,
        environmentRefs = listOf("env-coop-passage"),
        gameplayPlanRef = gameplayPlanRef,
        interactionRefs = interactionRefs,
        requiredRoles = listOf("carrier", "opener"),
        shadedConfigRef = "shaded.scene-config/fixture-scene",
    )

    fun interfaceIntent(creativeBriefRef: String, gameplayPlanRef: String, sceneBundleRef: String) = InterfaceIntent(
        intentId = "INTENT_GOLDEN_INTERFACE",
        workspaceId = WORKSPACE_ID,
        runId = RUN_ID,
        creativeBriefRef = creativeBriefRef,
        gameplayPlanRef = gameplayPlanRef,
        sceneBundleRef = sceneBundleRef,
        requiredActions = listOf("MOVE", "INTERACT", "CANCEL"),
        coopRoles = listOf("carrier", "opener"),
    )
}

/** Deterministic BARD stand-in — never claims real creative fidelity. */
class GoldenRunFakeBardPort : ExternalToolPort {
    override val toolId: String = "fake-bard"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.bard.creative-seed"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("anvil.bard.creative-brief"))

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = ExternalToolResult.Produced(
        contractId = ContractId("anvil.bard.creative-brief"),
        version = 1,
        payload = """{"schema":"anvil.bard.creative-brief/v1","fixture":true,"seedRef":"golden-seed","tone":"coop-survival-warmth"}""",
    )
}

/** Deterministic WIZARD stand-in. */
class GoldenRunFakeWizardPort : ExternalToolPort {
    override val toolId: String = "fake-wizard"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.bard.production-intent"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("anvil.wizard.production-assessment"))

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = ExternalToolResult.Produced(
        contractId = ContractId("anvil.wizard.production-assessment"),
        version = 1,
        payload = """{"schema":"anvil.wizard.production-assessment/v1","fixture":true,"capabilityCast":["push","pull","lift-heavy"]}""",
    )
}

/** Deterministic SWIFT stand-in. */
class GoldenRunFakeSwiftPort : ExternalToolPort {
    override val toolId: String = "fake-swift"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.wizard.capability-cast"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("swift.actor-bundle"))

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = ExternalToolResult.Produced(
        contractId = ContractId("swift.actor-bundle"),
        version = 1,
        payload = """{"schema":"swift.actor-bundle/v1","fixture":true,"actors":["ACTOR_CARRIER","ACTOR_OPENER"]}""",
    )
}

/** Deterministic SHADED stand-in. */
class GoldenRunFakeShadedPort : ExternalToolPort {
    override val toolId: String = "fake-shaded"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.scene-bundle"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("shaded.scene-config"))

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = ExternalToolResult.Produced(
        contractId = ContractId("shaded.scene-config"),
        version = 1,
        payload = """{"schema":"shaded.scene-config/v1","fixture":true,"params":{"dustLevel":0.6,"pressure":0.5,"dayNight":0.6}}""",
    )
}

/** Deterministic CUE stand-in — only CUE may claim real technical verification; this never does. */
class GoldenRunFakeCuePort : ExternalToolPort {
    override val toolId: String = "fake-cue"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.runnable-build"))
    override val producedOutputContracts: List<ContractId> = listOf(
        ContractId("cue.playable-proof"),
        ContractId("cue.temporal-proof"),
        ContractId("cue.audio-proof"),
    )

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = when (request.contractId.value) {
        "cue.playable-proof" -> ExternalToolResult.Produced(
            ContractId("cue.playable-proof"), 1,
            """{"schema":"cue.playable-proof/v1","fixture":true,"launched":true,"crashed":false}""",
        )
        "cue.temporal-proof" -> ExternalToolResult.Produced(
            ContractId("cue.temporal-proof"), 1,
            """{"schema":"cue.temporal-proof/v1","fixture":true,"stateBefore":"idle","stateAfter":"crate-moved-path-opened","withinExpectedWindowMs":true}""",
        )
        "cue.audio-proof" -> ExternalToolResult.Produced(
            ContractId("cue.audio-proof"), 1,
            """{"schema":"cue.audio-proof/v1","fixture":true,"cueFired":true,"stateReaction":true,"transitionTimingOk":true,"loopContinuityOk":true,"clippingDetected":false,"voiceAudible":true}""",
        )
        else -> ExternalToolResult.BlockedExternalContract("GoldenRunFakeCuePort does not produce ${request.contractId.value}.")
    }
}
