package io.anvil.core.contracts

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
    /** Nur für `role == "tool"`: Name des aufgerufenen Tools (OpenAI-Konvention). */
    val name: String? = null,
    /** Nur für `role == "tool"`: welchem [ToolCall.id] diese Ergebnis-Nachricht antwortet. */
    val toolCallId: String? = null,
    /** Nur für `role == "assistant"`, wenn das Modell Tools aufrufen will. */
    val toolCalls: List<ToolCall>? = null,
)

/** Ein Tool/eine Funktion, die das Modell aufrufen darf (OpenAI-Function-Calling-Schema). */
@Serializable
data class ToolDefinition(
    val name: String,
    val description: String? = null,
    /** JSON-Schema der Parameter (roh durchgereicht — Bellows validiert es nicht). */
    val parameters: JsonElement? = null,
)

/** Ein einzelner Tool-Aufruf, den das Modell in seiner Antwort angefordert hat. */
@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    /** Roh-JSON-String der Argumente (OpenAI liefert sie so, nicht als Objekt). */
    val arguments: String,
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
 * @param tools          Optionale Tool-/Funktionsdefinitionen (OpenAI-Function-Calling).
 *                       `null` ⇒ Verhalten unverändert wie vor Einführung von Tool-Calling.
 * @param toolChoice     Optional "auto" | "none" | "required" (OpenAI-Konvention).
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
    val tools: List<ToolDefinition>? = null,
    val toolChoice: String? = null,
)

/** Die Antwort eines Providers. Kanonischer Name (nie `AIResponse`). */
@Serializable
data class ModelResponse(
    val content: String,
    /** Tatsächlich genutztes Modell, inkl. Provider-Präfix (z.B. "openrouter:gpt-4o-mini"). */
    val modelUsed: String,
    val usage: TokenUsage? = null,
    /** "stop" | "length" | "content_filter" | "tool_calls" | … */
    val finishReason: String? = null,
    /** Vom Modell angeforderte Tool-Aufrufe, falls `finishReason == "tool_calls"`. */
    val toolCalls: List<ToolCall>? = null,
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
