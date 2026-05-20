package io.anvil.core.quality

import io.anvil.core.contracts.CredentialKey
import io.anvil.core.contracts.CredentialVaultContract
import io.anvil.core.contracts.QualityState

class InMemoryVault : CredentialVaultContract {
    private val store = mutableMapOf<String, String>()

    override suspend fun store(key: CredentialKey, secret: String) { store[key.id] = secret }
    override suspend fun retrieve(key: CredentialKey): String? = store[key.id]
    override suspend fun delete(key: CredentialKey) { store.remove(key.id) }

    override fun qualityState(): QualityState =
        if (store.isNotEmpty()) QualityState.STABLE else QualityState.DEGRADED
}
