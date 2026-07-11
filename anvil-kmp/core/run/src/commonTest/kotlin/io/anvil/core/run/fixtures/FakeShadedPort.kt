package io.anvil.core.run.fixtures

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState

/** Deterministic SHADED stand-in: produces a fixture SceneConfig + ActorBinding reference. */
class FakeShadedPort : ExternalToolPort {
    override val toolId: String = "fake-shaded"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.scene-bundle"), ContractId("swift.actor-bundle"))
    override val producedOutputContracts: List<ContractId> = listOf(
        ContractId("shaded.scene-config"),
        ContractId("shaded.actor-binding"),
    )

    override suspend fun health(): ExternalToolHealth =
        ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — not a real SHADED instance.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = when (request.contractId.value) {
        "anvil.scene-bundle" -> ExternalToolResult.Produced(
            contractId = ContractId("shaded.scene-config"),
            version = 1,
            payload = """{"schema":"shaded.scene-config/v1","fixture":true,"sceneBundleRef":"fixture-scene","params":{"dayNight":0.7,"storm":0.4,"dustLevel":0.3}}""",
        )
        else -> ExternalToolResult.BlockedExternalContract("FakeShadedPort does not produce ${request.contractId.value}.")
    }
}
