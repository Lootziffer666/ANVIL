package io.anvil.modules.bellows

import io.anvil.core.contracts.BellowsContract
import io.anvil.core.contracts.BellowsExhaustedException
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import kotlinx.coroutines.CancellationException

/**
 * Produktionsreifer Router über mehrere [ProviderAdapter].
 *
 * Routing-Reihenfolge:
 *  1. **Privacy** — `LOCAL_ONLY` filtert auf lokale Adapter; **niemals** Cloud-Fallback
 *     (docs/SAFETY_POLICY.md §4, Kill-Kriterium).
 *  2. **Modell-Match** — Adapter, die das angefragte Modell bedienen, zuerst.
 *  3. **Gesundheit** — `FAILED`-Adapter werden übersprungen.
 *  4. **Fallback-Kette** — der erste Adapter, der erfolgreich antwortet, gewinnt.
 *
 * Im Fehlerfall nie `null`, sondern [BellowsExhaustedException].
 */
class BellowsRouter(
    private val adapters: List<ProviderAdapter>,
) : BellowsContract {

    override suspend fun route(request: ModelRequest): ModelResponse {
        // Strip bellows/ prefix: "bellows/auto" or "bellows/" → null (any), "bellows/gpt-4o" → "gpt-4o"
        val effectiveModel = request.model?.let { m ->
            if (m.startsWith("bellows/")) {
                val stripped = m.removePrefix("bellows/")
                if (stripped == "auto" || stripped.isBlank()) null else stripped
            } else {
                m
            }
        }
        val routeRequest = if (effectiveModel == request.model) request else request.copy(model = effectiveModel)

        val byPrivacy = when (routeRequest.privacyMode) {
            PrivacyMode.LOCAL_ONLY -> adapters.filter { it.isLocal }
            PrivacyMode.OPEN -> adapters
        }
        if (byPrivacy.isEmpty()) {
            throw BellowsExhaustedException(
                if (routeRequest.privacyMode == PrivacyMode.LOCAL_ONLY) {
                    "Kein lokales Modell verfügbar. PrivacyMode.LOCAL_ONLY verbietet Cloud-Fallback."
                } else {
                    "Kein Provider konfiguriert."
                },
            )
        }

        val (matching, others) = byPrivacy.partition { it.handles(routeRequest.model) }
        val candidates = (matching + others).filter { it.qualityState() != QualityState.FAILED }
        if (candidates.isEmpty()) {
            throw BellowsExhaustedException("Alle passenden Provider sind im FAILED-Zustand.")
        }

        var lastError: Exception? = null
        for (adapter in candidates) {
            try {
                return adapter.route(routeRequest)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw BellowsExhaustedException(
            "Alle Provider erschöpft (model=${routeRequest.model ?: "<default>"}, " +
                "privacyMode=${routeRequest.privacyMode}). Letzter Fehler: ${lastError?.message}",
        )
    }

    /** Aggregierter Qualitätszustand über alle Adapter (für /health + State Surface). */
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

    /** Alle bekannten Modell-IDs über alle Adapter (für `GET /v1/models`). Prepends bellows/auto. */
    fun listModels(): List<String> =
        listOf("bellows/auto") + adapters.flatMap { it.models }.distinct().sorted()

    /** Zustand je Adapter — für `/health`. */
    fun adapterStates(): Map<String, QualityState> =
        adapters.associate { it.id to it.qualityState() }
}
