package io.anvil.modules.bellows

import io.anvil.core.contracts.CredentialVaultContract
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.ProviderAdapter
import io.anvil.core.contracts.QualityState

// Bridge: wraps the standalone ANVIL-BELLOWS Android library as a KMP ProviderAdapter.
// Actual ANVIL-BELLOWS dependency injection happens in :app:android, not here.
class AnvilBellowsBridgeAdapter(
    private val client: BellowsLegacyClient,
    override val id: String = "anvil-bellows-bridge",
    override val isLocal: Boolean = true,
) : ProviderAdapter {

    @Volatile private var healthy = true

    override suspend fun complete(request: ModelRequest, vault: CredentialVaultContract): ModelResponse {
        return try {
            val raw = client.complete(request.prompt, request.modelHint)
            healthy = true
            ModelResponse(content = raw, modelUsed = id)
        } catch (e: Exception) {
            healthy = false
            throw e
        }
    }

    override fun qualityState(): QualityState =
        if (healthy) QualityState.STABLE else QualityState.DEGRADED
}
