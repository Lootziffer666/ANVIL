package io.anvil.modules.bellows

import io.anvil.core.contracts.BellowsContract
import io.anvil.core.contracts.BellowsExhaustedException
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState

class BellowsRouter(
    private val adapters: List<ProviderAdapter>,
) : BellowsContract {

    override suspend fun route(request: ModelRequest): ModelResponse {
        val candidates = when (request.privacyMode) {
            PrivacyMode.LOCAL_ONLY -> adapters.filter { it.isLocal }
            PrivacyMode.OPEN -> adapters
        }
        if (candidates.isEmpty()) {
            throw BellowsExhaustedException(
                "No adapter available for privacyMode=${request.privacyMode}"
            )
        }
        val healthy = candidates.filter { it.qualityState() != QualityState.FAILED }
        if (healthy.isEmpty()) {
            throw BellowsExhaustedException("All adapters are in FAILED state")
        }
        return healthy.first().route(request)
    }

    fun qualityState(): QualityState {
        val states = adapters.map { it.qualityState() }
        return when {
            states.isEmpty() -> QualityState.BLOCKED
            states.all { it == QualityState.FAILED } -> QualityState.FAILED
            states.any { it == QualityState.FAILED } -> QualityState.DEGRADED
            states.any { it == QualityState.DEGRADED } -> QualityState.DEGRADED
            states.any { it == QualityState.BLOCKED } -> QualityState.BLOCKED
            else -> QualityState.STABLE
        }
    }
}
