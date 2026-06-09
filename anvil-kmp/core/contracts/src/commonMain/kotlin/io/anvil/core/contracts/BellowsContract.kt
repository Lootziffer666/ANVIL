package io.anvil.core.contracts

import kotlinx.serialization.Serializable

/**
 * Bellows — der LLM-Routing-Layer.
 *
 * Eine Implementierung nimmt einen [ModelRequest] entgegen, wählt einen passenden
 * Provider (Cloud oder lokal) und liefert einen [ModelResponse]. Im Fehlerfall wird
 * nie `null` zurückgegeben, sondern eine [BellowsExhaustedException] geworfen.
 */
interface BellowsContract {
    suspend fun route(request: ModelRequest): ModelResponse
}

/** Eine einzelne Chat-Nachricht im OpenAI-kompatiblen Format. */
@Serializable
data class ChatMessage(
    /** "system" | "user" | "assistant" | "tool" */
    val role: String,
    val content: String,
)

/**
 * Eine LLM-Anfrage. Kanonischer Name laut ANVIL_CONCEPT_CONTRACT (nie `AIRequest`).
 *
 * @param messages       Konversation im OpenAI-Stil (mind. eine Nachricht).
 * @param model          Optionaler Modell-/Route-Hinweis (z.B. "gpt-4o-mini",
 *                       "hermes", "openrouter/auto"). `null` ⇒ Default-Route.
 * @param privacyMode    [PrivacyMode.LOCAL_ONLY] verbietet jeden Cloud-Provider.
 * @param maxTokens      Obergrenze für die Antwort-Tokens.
 * @param temperature    Sampling-Temperatur.
 * @param stream         Ob die Antwort als Stream (SSE) erwartet wird.
 * @param costCapUsd     Optionales Kosten-Limit; Routen, die es überschreiten, werden übersprungen.
 */
@Serializable
data class ModelRequest(
    val messages: List<ChatMessage>,
    val model: String? = null,
    val privacyMode: PrivacyMode = PrivacyMode.OPEN,
    val maxTokens: Int? = null,
    val temperature: Double? = null,
    val stream: Boolean = false,
    val costCapUsd: Double? = null,
)

/** Die Antwort eines Providers. Kanonischer Name (nie `AIResponse`). */
@Serializable
data class ModelResponse(
    val content: String,
    /** Tatsächlich genutztes Modell, inkl. Provider-Präfix (z.B. "openrouter:gpt-4o-mini"). */
    val modelUsed: String,
    val usage: TokenUsage? = null,
    /** "stop" | "length" | "content_filter" | … */
    val finishReason: String? = null,
)

@Serializable
data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
)

/**
 * Datenschutz-Modus. `LOCAL_ONLY` ist hart: kein Fallback auf Cloud-Provider
 * (siehe docs/SAFETY_POLICY.md §4 — Kill-Kriterium).
 */
enum class PrivacyMode {
    LOCAL_ONLY,
    OPEN,
}

/** Wird geworfen, wenn kein passender Provider eine Anfrage bedienen kann. */
class BellowsExhaustedException(message: String) : Exception(message)
