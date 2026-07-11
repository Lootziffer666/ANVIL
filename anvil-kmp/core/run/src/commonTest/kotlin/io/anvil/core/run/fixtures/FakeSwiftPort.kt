package io.anvil.core.run.fixtures

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState

/** Deterministic SWIFT stand-in: produces a fixture ActorBundle reference for two coop actors. */
class FakeSwiftPort : ExternalToolPort {
    override val toolId: String = "fake-swift"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.wizard.capability-cast"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("swift.actor-bundle"))

    override suspend fun health(): ExternalToolHealth =
        ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — not a real SWIFT instance.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = when (request.contractId.value) {
        "anvil.wizard.capability-cast" -> ExternalToolResult.Produced(
            contractId = ContractId("swift.actor-bundle"),
            version = 1,
            payload = """{"schema":"swift.actor-bundle/v1","fixture":true,"actors":[{"id":"ACTOR_CARRIER","spriteSheet":"fixture://carrier.png","manifest":"fixture://carrier.manifest.json"},{"id":"ACTOR_OPENER","spriteSheet":"fixture://opener.png","manifest":"fixture://opener.manifest.json"}]}""",
        )
        else -> ExternalToolResult.BlockedExternalContract("FakeSwiftPort does not produce ${request.contractId.value}.")
    }
}
