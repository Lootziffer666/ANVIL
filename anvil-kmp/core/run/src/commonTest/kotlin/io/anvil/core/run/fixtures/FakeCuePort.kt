package io.anvil.core.run.fixtures

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState

/**
 * Deterministic CUE stand-in. Only CUE may claim technical verification — this fixture
 * produces the three proof shapes (playable/temporal/audio) but every payload is tagged
 * `"fixture":true` so nothing downstream can mistake it for a real verified proof.
 */
class FakeCuePort : ExternalToolPort {
    override val toolId: String = "fake-cue"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.runnable-build"))
    override val producedOutputContracts: List<ContractId> = listOf(
        ContractId("cue.playable-proof"),
        ContractId("cue.temporal-proof"),
        ContractId("cue.audio-proof"),
    )

    override suspend fun health(): ExternalToolHealth =
        ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — not a real CUE-AGENT instance.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = when (request.contractId.value) {
        "anvil.runnable-build" -> ExternalToolResult.Produced(
            contractId = ContractId("cue.playable-proof"),
            version = 1,
            payload = """{"schema":"cue.playable-proof/v1","fixture":true,"buildRef":"fixture-build","launched":true,"crashed":false}""",
        )
        else -> ExternalToolResult.BlockedExternalContract("FakeCuePort does not produce ${request.contractId.value}.")
    }

    suspend fun invokeTemporalProof(buildRef: String): ExternalToolResult.Produced = ExternalToolResult.Produced(
        contractId = ContractId("cue.temporal-proof"),
        version = 1,
        payload = """{"schema":"cue.temporal-proof/v1","fixture":true,"buildRef":"$buildRef","stateBefore":"idle","stateAfter":"crate-moved","withinExpectedWindowMs":true}""",
    )

    suspend fun invokeAudioProof(buildRef: String): ExternalToolResult.Produced = ExternalToolResult.Produced(
        contractId = ContractId("cue.audio-proof"),
        version = 1,
        payload = """{"schema":"cue.audio-proof/v1","fixture":true,"buildRef":"$buildRef","cueFired":true,"stateReaction":true,"transitionTimingOk":true,"loopContinuityOk":true,"clippingDetected":false,"voiceAudible":true}""",
    )
}
