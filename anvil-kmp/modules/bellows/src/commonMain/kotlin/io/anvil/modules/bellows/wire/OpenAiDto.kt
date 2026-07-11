package io.anvil.modules.bellows.wire

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * OpenAI-kompatible Wire-Typen (`/v1/chat/completions`, `/v1/models`).
 *
 * Diese Typen werden sowohl client-seitig (Provider-Adapter ⇒ Upstream) als auch
 * server-seitig (Gateway-Endpoint) genutzt — ein kanonisches Schema, kein Drift.
 */
@Serializable
data class OpenAiChatRequest(
    val model: String? = null,
    val messages: List<OpenAiMessage> = emptyList(),
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val temperature: Double? = null,
    val stream: Boolean = false,
    val tools: List<OpenAiToolDefinition>? = null,
    @SerialName("tool_choice") val toolChoice: String? = null,
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String? = null,
    val name: String? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenAiToolCall>? = null,
)

/** OpenAI Function-Calling: Tool-Definition, die im Request mitgeschickt wird. */
@Serializable
data class OpenAiToolDefinition(
    val type: String = "function",
    val function: OpenAiFunctionDef,
)

@Serializable
data class OpenAiFunctionDef(
    val name: String,
    val description: String? = null,
    val parameters: JsonElement? = null,
)

/** Ein vom Modell angeforderter Tool-Aufruf (in der Antwort, `message.tool_calls`). */
@Serializable
data class OpenAiToolCall(
    val id: String,
    val type: String = "function",
    val function: OpenAiToolCallFunction,
)

@Serializable
data class OpenAiToolCallFunction(
    val name: String,
    val arguments: String,
)

@Serializable
data class OpenAiChatResponse(
    val id: String = "",
    @SerialName("object") val objectType: String = "chat.completion",
    val created: Long = 0,
    val model: String = "",
    val choices: List<OpenAiChoice> = emptyList(),
    val usage: OpenAiUsage? = null,
)

@Serializable
data class OpenAiChoice(
    val index: Int = 0,
    val message: OpenAiMessage? = null,
    val delta: OpenAiMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class OpenAiUsage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0,
)

@Serializable
data class OpenAiModelList(
    @SerialName("object") val objectType: String = "list",
    val data: List<OpenAiModelCard> = emptyList(),
)

@Serializable
data class OpenAiModelCard(
    val id: String,
    @SerialName("object") val objectType: String = "model",
    val created: Long = 0,
    @SerialName("owned_by") val ownedBy: String = "anvil-bellows",
)

// ── Streaming (`stream: true`) — Server-Sent-Events Chunks ─────────────────────

@Serializable
data class OpenAiChatChunk(
    val id: String,
    @SerialName("object") val objectType: String = "chat.completion.chunk",
    val created: Long = 0,
    val model: String = "",
    val choices: List<OpenAiChunkChoice> = emptyList(),
)

@Serializable
data class OpenAiChunkChoice(
    val index: Int = 0,
    val delta: OpenAiDelta = OpenAiDelta(),
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class OpenAiDelta(
    val role: String? = null,
    val content: String? = null,
)

/** OpenAI-kompatibler Fehler-Body. */
@Serializable
data class OpenAiError(
    val error: OpenAiErrorBody,
)

@Serializable
data class OpenAiErrorBody(
    val message: String,
    val type: String = "bellows_error",
    val code: String? = null,
)
