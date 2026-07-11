package io.anvil.core.run.fixtures

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState

/** Deterministic WIZARD stand-in: produces a fixture ProductionAssessment + CapabilityCast. */
class FakeWizardPort : ExternalToolPort {
    override val toolId: String = "fake-wizard"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.bard.production-intent"))
    override val producedOutputContracts: List<ContractId> = listOf(
        ContractId("anvil.wizard.production-assessment"),
        ContractId("anvil.wizard.capability-cast"),
    )

    override suspend fun health(): ExternalToolHealth =
        ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — not a real WIZARD instance.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = when (request.contractId.value) {
        "anvil.bard.production-intent" -> ExternalToolResult.Produced(
            contractId = ContractId("anvil.wizard.production-assessment"),
            version = 1,
            payload = """{"schema":"anvil.wizard.production-assessment/v1","fixture":true,"productionIntentRef":"fixture-intent","capabilityCast":["push","pull","lift-heavy"],"assetBudget":{"actors":2,"shaded-envs":1}}""",
        )
        else -> ExternalToolResult.BlockedExternalContract("FakeWizardPort does not produce ${request.contractId.value}.")
    }
}
