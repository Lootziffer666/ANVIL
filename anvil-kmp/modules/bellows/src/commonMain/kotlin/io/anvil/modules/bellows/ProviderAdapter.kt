package io.anvil.modules.bellows

import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.QualityState

interface ProviderAdapter {
    val isLocal: Boolean
    suspend fun route(request: ModelRequest): ModelResponse
    fun qualityState(): QualityState
}
