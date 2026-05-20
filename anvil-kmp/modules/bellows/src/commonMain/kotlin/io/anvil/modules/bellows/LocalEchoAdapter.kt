package io.anvil.modules.bellows

import io.anvil.core.contracts.CredentialVaultContract
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.ProviderAdapter
import io.anvil.core.contracts.QualityState

class LocalEchoAdapter : ProviderAdapter {
    override val id = "local-echo"
    override val isLocal = true

    override suspend fun complete(request: ModelRequest, vault: CredentialVaultContract): ModelResponse =
        ModelResponse(content = "[ECHO] ${request.prompt}", modelUsed = id)

    override fun qualityState() = QualityState.STABLE
}
