package io.anvil.core.contracts

interface ProviderAdapter {
    val id: String
    val isLocal: Boolean
    suspend fun complete(request: ModelRequest, vault: CredentialVaultContract): ModelResponse
    fun qualityState(): QualityState
}
