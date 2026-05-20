package io.anvil.modules.bellows

import io.anvil.core.contracts.BellowsContract
import io.anvil.core.contracts.BellowsExhaustedException
import io.anvil.core.contracts.CredentialVaultContract
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState

class BellowsRouter(
    private val adapters: List<ProviderAdapter>,
    private val vault: CredentialVaultContract,
) : BellowsContract, ModuleSlotContract {

    override val name = "Bellows"
    override val purpose = "LLM-Routing: leitet ModelRequest an verfügbare Provider weiter."

    override suspend fun route(request: ModelRequest): ModelResponse {
        for (adapter in adapters) {
            // SAFETY: LOCAL_ONLY → niemals Cloud-Adapter (CLAUDE.md §6, Safety Policy §4)
            if (request.privacyMode == PrivacyMode.LOCAL_ONLY && !adapter.isLocal) continue
            if (adapter.qualityState() == QualityState.FAILED) continue
            return adapter.complete(request, vault)
        }
        throw BellowsExhaustedException(
            if (request.privacyMode == PrivacyMode.LOCAL_ONLY)
                "Kein lokales Modell verfügbar. PrivacyMode.LOCAL_ONLY verbietet Cloud-Fallback."
            else "Alle Provider erschöpft — kein Fallback mehr."
        )
    }

    override fun qualityState(): QualityState =
        if (adapters.any { it.qualityState() != QualityState.FAILED }) QualityState.STABLE
        else QualityState.DEGRADED
}
