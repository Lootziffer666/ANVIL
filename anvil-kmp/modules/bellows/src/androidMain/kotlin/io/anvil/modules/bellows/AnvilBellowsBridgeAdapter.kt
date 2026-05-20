package io.anvil.modules.bellows

import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.QualityState

class AnvilBellowsBridgeAdapter(
    private val client: BellowsLegacyClient,
) : ProviderAdapter {

    // ANVIL-BELLOWS routes to cloud providers — not a local model
    override val isLocal: Boolean = false

    private var _quality = QualityState.STABLE

    override suspend fun route(request: ModelRequest): ModelResponse {
        return try {
            val content = client.route(request.prompt, request.modelHint)
            _quality = QualityState.STABLE
            ModelResponse(content = content, modelUsed = "anvil-bellows-bridge")
        } catch (e: Exception) {
            _quality = QualityState.DEGRADED
            throw e
        }
    }

    override fun qualityState(): QualityState = _quality
}
