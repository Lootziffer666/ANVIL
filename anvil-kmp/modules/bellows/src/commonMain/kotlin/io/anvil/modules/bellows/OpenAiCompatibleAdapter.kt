package io.anvil.modules.bellows

import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.ToolCall
import io.anvil.core.contracts.ToolDefinition
import io.anvil.core.contracts.TokenUsage
import io.anvil.modules.bellows.wire.OpenAiChatRequest
import io.anvil.modules.bellows.wire.OpenAiChatResponse
import io.anvil.modules.bellows.wire.OpenAiFunctionDef
import io.anvil.modules.bellows.wire.OpenAiMessage
import io.anvil.modules.bellows.wire.OpenAiToolCall
import io.anvil.modules.bellows.wire.OpenAiToolCallFunction
import io.anvil.modules.bellows.wire.OpenAiToolDefinition
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException

/**
 * Universeller Adapter für jeden OpenAI-kompatiblen Endpoint.
 *
 * Deckt damit ab: OpenAI, OpenRouter, Nvidia NIM, Groq, Together — und lokal
 * **LM Studio, Ollama (`/v1`), llama.cpp-Server, vLLM** (z.B. für Hermes).
 * Genau ein Adapter, viele Backends — die LiteLLM/OmniRoute-Idee.
 *
 * @param baseUrl  Basis-URL inkl. `/v1` (z.B. `https://openrouter.ai/api/v1`,
 *                 `http://localhost:1234/v1`). Trailing `/chat/completions` optional.
 * @param apiKey   Bearer-Token (Klartext, bereits aus dem Vault aufgelöst) oder `null`/leer
 *                 für lokale Server ohne Auth.
 */
class OpenAiCompatibleAdapter(
    override val id: String,
    private val baseUrl: String,
    private val apiKey: String?,
    override val models: List<String>,
    override val isLocal: Boolean,
    private val client: HttpClient,
    private val extraHeaders: Map<String, String> = emptyMap(),
    private val defaultModel: String? = models.firstOrNull(),
) : ProviderAdapter {

    private var quality: QualityState = QualityState.STABLE

    override fun handles(model: String?): Boolean {
        if (model == null) return true
        if (models.isEmpty()) return true // Passthrough-Provider (z.B. openrouter/auto)
        return models.any { it.equals(model, ignoreCase = true) }
    }

    override suspend fun route(request: ModelRequest): ModelResponse {
        val targetModel = request.model?.takeIf { handles(it) } ?: defaultModel ?: request.model
        val payload = OpenAiChatRequest(
            model = targetModel,
            messages = request.messages.map { it.toOpenAiMessage() },
            maxTokens = request.maxTokens,
            temperature = request.temperature,
            stream = false,
            tools = request.tools?.map { it.toOpenAiToolDefinition() },
            toolChoice = request.toolChoice,
        )
        try {
            val response = client.post(chatCompletionsUrl()) {
                contentType(ContentType.Application.Json)
                apiKey?.takeIf { it.isNotBlank() }?.let {
                    header(HttpHeaders.Authorization, "Bearer $it")
                }
                extraHeaders.forEach { (k, v) -> header(k, v) }
                setBody(payload)
            }
            if (!response.status.isSuccess()) {
                throw ProviderException(id, response.status.value, response.bodyAsText().take(500))
            }
            val parsed: OpenAiChatResponse = response.body()
            val choice = parsed.choices.firstOrNull()
            val toolCalls = choice?.message?.toolCalls?.map { it.toToolCall() }
            val content = choice?.message?.content
                ?: if (toolCalls.isNullOrEmpty()) {
                    throw ProviderException(id, response.status.value, "Leere Completion von Provider '$id'")
                } else {
                    "" // Tool-Call-only-Antworten haben laut OpenAI-Vertrag `content: null`.
                }
            quality = QualityState.STABLE
            return ModelResponse(
                content = content,
                modelUsed = "$id:${parsed.model.ifBlank { targetModel ?: "unknown" }}",
                usage = parsed.usage?.let {
                    TokenUsage(it.promptTokens, it.completionTokens, it.totalTokens)
                },
                finishReason = choice.finishReason,
                toolCalls = toolCalls,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: ProviderException) {
            quality = QualityState.DEGRADED
            throw e
        } catch (e: Exception) {
            quality = QualityState.DEGRADED
            throw ProviderException(id, -1, e.message ?: e.toString(), e)
        }
    }

    private fun chatCompletionsUrl(): String {
        val base = baseUrl.trimEnd('/')
        return if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
    }

    override fun qualityState(): QualityState = quality
}

private fun io.anvil.core.contracts.ChatMessage.toOpenAiMessage() = OpenAiMessage(
    role = role,
    content = content,
    name = name,
    toolCallId = toolCallId,
    toolCalls = toolCalls?.map { it.toOpenAiToolCall() },
)

private fun ToolDefinition.toOpenAiToolDefinition() = OpenAiToolDefinition(
    function = OpenAiFunctionDef(name = name, description = description, parameters = parameters),
)

private fun ToolCall.toOpenAiToolCall() = OpenAiToolCall(
    id = id,
    function = OpenAiToolCallFunction(name = name, arguments = arguments),
)

private fun OpenAiToolCall.toToolCall() = ToolCall(
    id = id,
    name = function.name,
    arguments = function.arguments,
)
