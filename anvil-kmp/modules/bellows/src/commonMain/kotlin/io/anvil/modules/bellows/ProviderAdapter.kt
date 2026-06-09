package io.anvil.modules.bellows

import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.QualityState

/**
 * Ein pluggbarer Provider hinter Bellows. Jeder Adapter kapselt genau einen
 * Upstream — einen Cloud-Endpoint oder einen lokalen Modellserver.
 */
interface ProviderAdapter {
    /** Stabile ID, z.B. "openai", "openrouter", "local-hermes". */
    val id: String

    /** true ⇒ läuft lokal (kein Cloud-Provider). Entscheidend für [io.anvil.core.contracts.PrivacyMode.LOCAL_ONLY]. */
    val isLocal: Boolean

    /** Modell-IDs/Aliasse, die dieser Adapter bedient (für /v1/models + Routing). Leer ⇒ Passthrough. */
    val models: List<String>

    /** Ob dieser Adapter das angefragte Modell bedienen kann. `null` ⇒ Default-Route. */
    fun handles(model: String?): Boolean

    /** Führt die Anfrage gegen den Upstream aus. Wirft bei Fehler eine [ProviderException]. */
    suspend fun route(request: ModelRequest): ModelResponse

    fun qualityState(): QualityState
}

/** Fehler eines konkreten Providers. `statusCode == -1` ⇒ Transport-/Verbindungsfehler. */
class ProviderException(
    val providerId: String,
    val statusCode: Int,
    val detail: String,
    cause: Throwable? = null,
) : Exception("Provider '$providerId' failed (status=$statusCode): $detail", cause)
