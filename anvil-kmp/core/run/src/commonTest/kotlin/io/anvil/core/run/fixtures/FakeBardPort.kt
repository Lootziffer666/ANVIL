package io.anvil.core.run.fixtures

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState

/**
 * Deterministic BARD stand-in for tests and the Golden Run fixture. Never claims
 * `verified` or `production` fidelity — it exists only to exercise the ANVIL-side
 * contract plumbing (Run/Artifact/Handoff/Sync), not to simulate real creative judgement.
 */
class FakeBardPort : ExternalToolPort {
    override val toolId: String = "fake-bard"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.bard.creative-seed"))
    override val producedOutputContracts: List<ContractId> = listOf(
        ContractId("anvil.bard.creative-brief"),
        ContractId("anvil.bard.production-intent"),
    )

    override suspend fun health(): ExternalToolHealth =
        ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — not a real BARD instance.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = when (request.contractId.value) {
        "anvil.bard.creative-seed" -> ExternalToolResult.Produced(
            contractId = ContractId("anvil.bard.creative-brief"),
            version = 1,
            payload = """{"schema":"anvil.bard.creative-brief/v1","fixture":true,"seedRef":"${escaped(request.payload)}","tone":"coop-survival-warmth","locks":["no-lethal-pvp","two-player-coop-required"]}""",
        )
        else -> ExternalToolResult.BlockedExternalContract("FakeBardPort does not produce ${request.contractId.value}.")
    }

    private fun escaped(text: String): String = text.replace("\"", "\\\"").take(120)
}
