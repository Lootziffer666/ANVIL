package io.anvil.app.studiorun

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState

/**
 * Deterministic, offline stand-ins for external studio systems that either have no real
 * adapter anywhere in this codebase yet (BARD — no real creative-AI implementation
 * exists), or were not configured for this run (no repo path / base URL supplied).
 * Same discipline as `:surfaces:golden-run`'s `GoldenRunFakeXPort` fixtures: every
 * payload self-labels `"fixture":true` and never claims real creative, production, or
 * technical fidelity. [StudioRunReport] prints REAL vs FIXTURE per system so a run's
 * console output never quietly overstates what actually happened.
 */
class FixtureBardPort(private val seedWords: List<String>) : ExternalToolPort {
    override val toolId: String = "fixture-bard"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.bard.creative-seed"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("anvil.bard.creative-brief"))

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — no real BARD adapter exists yet.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = ExternalToolResult.Produced(
        contractId = ContractId("anvil.bard.creative-brief"),
        version = 1,
        payload = """{"schema":"anvil.bard.creative-brief/v1","fixture":true,"seedWords":${seedWords.jsonArray()},"tone":"unspecified"}""",
    )
}

/** Deterministic WIZARD stand-in, used only when no real `WizardHttpAdapter` was configured. */
class FixtureWizardPort : ExternalToolPort {
    override val toolId: String = "fixture-wizard"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.wizard.production-assessment-request"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("anvil.wizard.production-assessment"))

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — WIZARD_BASE_URL not set.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = ExternalToolResult.Produced(
        contractId = ContractId("anvil.wizard.production-assessment"),
        version = 1,
        payload = """{"schema":"anvil.wizard.production-assessment/v1","fixture":true,"capabilityCast":[]}""",
    )
}

/** Deterministic SWIFT stand-in, used only when no real `SwiftCliAdapter` was configured. */
class FixtureSwiftPort : ExternalToolPort {
    override val toolId: String = "fixture-swift"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("swift.render-request"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("swift.actor-bundle"))

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — SWIFT_REPO_PATH not set.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = ExternalToolResult.Produced(
        contractId = ContractId("swift.actor-bundle"),
        version = 1,
        payload = """{"schema":"swift.actor-bundle/v1","fixture":true,"actors":[]}""",
    )
}

/** Deterministic SHADED stand-in, used only when no real `ShadedCliAdapter` was configured. */
class FixtureShadedPort : ExternalToolPort {
    override val toolId: String = "fixture-shaded"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("shaded.scene-project-request"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("shaded.scene-project"))

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — SHADED_REPO_PATH not set.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = ExternalToolResult.Produced(
        contractId = ContractId("shaded.scene-project"),
        version = 1,
        payload = """{"schema":"shaded.scene-project/v1","fixture":true,"ready":false}""",
    )
}

/** Deterministic CUE stand-in — only a real CUE-AGENT may claim real technical verification; this never does. */
class FixtureCuePort : ExternalToolPort {
    override val toolId: String = "fixture-cue"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.runnable-build"))
    override val producedOutputContracts: List<ContractId> = listOf(
        ContractId("cue.playable-proof"),
        ContractId("cue.temporal-proof"),
        ContractId("cue.audio-proof"),
    )

    override suspend fun health(): ExternalToolHealth = ExternalToolHealth(toolId, QualityState.STABLE, "Fixture only — CUE_AGENT_REPO_PATH not set.")

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult = when (request.contractId.value) {
        "cue.playable-proof" -> ExternalToolResult.Produced(ContractId("cue.playable-proof"), 1, """{"schema":"cue.playable-proof/v1","fixture":true,"launched":true,"crashed":false}""")
        "cue.temporal-proof" -> ExternalToolResult.Produced(ContractId("cue.temporal-proof"), 1, """{"schema":"cue.temporal-proof/v1","fixture":true}""")
        "cue.audio-proof" -> ExternalToolResult.Produced(ContractId("cue.audio-proof"), 1, """{"schema":"cue.audio-proof/v1","fixture":true}""")
        else -> ExternalToolResult.BlockedExternalContract("FixtureCuePort does not produce ${request.contractId.value}.")
    }
}

private fun List<String>.jsonArray(): String = joinToString(prefix = "[", postfix = "]") { "\"${it.replace("\"", "\\\"")}\"" }
