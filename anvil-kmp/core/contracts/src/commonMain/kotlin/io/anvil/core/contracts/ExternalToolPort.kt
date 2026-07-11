package io.anvil.core.contracts

import kotlinx.serialization.Serializable

/**
 * Port for an external studio system (BARD, WIZARD, SWIFT, SHADED, CUE, ...) that ANVIL
 * does not own the truth of. ANVIL only knows the contract shape; the actual meaning,
 * assets, or verification live in the sibling system. commonMain declares the port and
 * its models only — CLI/HTTP transport is a platform-specific adapter, never here.
 */
interface ExternalToolPort {
    val toolId: String
    val acceptedInputContracts: List<ContractId>
    val producedOutputContracts: List<ContractId>

    suspend fun health(): ExternalToolHealth
    suspend fun invoke(request: ExternalToolRequest): ExternalToolResult
}

@Serializable
data class ExternalToolHealth(
    val toolId: String,
    val quality: QualityState,
    val message: String,
)

@Serializable
data class ExternalToolRequest(
    val contractId: ContractId,
    val version: Int,
    val privacyMode: PrivacyMode,
    val payload: String,
)

@Serializable
sealed interface ExternalToolResult {
    @Serializable
    data class Produced(
        val contractId: ContractId,
        val version: Int,
        val payload: String,
    ) : ExternalToolResult

    @Serializable
    data class BlockedExternalContract(
        val reason: String,
    ) : ExternalToolResult

    /** Genuine runtime failure (process crashed, tool missing, malformed output, ...) — distinct from [BlockedExternalContract], which means "this port never supports that contract". */
    @Serializable
    data class Failed(
        val reason: String,
    ) : ExternalToolResult
}
