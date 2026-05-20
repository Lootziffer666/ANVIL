package io.anvil.modules.bellows

import io.anvil.core.contracts.CredentialVaultContract
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.QualityState

interface ProviderAdapter {
    val id: String
    val isLocal: Boolean
    suspend fun complete(request: ModelRequest, vault: CredentialVaultContract): ModelResponse
    fun qualityState(): QualityState
}
