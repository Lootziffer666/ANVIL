package io.anvil.modules.providers.anthropic

import io.anvil.core.contracts.BellowsExhaustedException
import io.anvil.core.contracts.CredentialKey
import io.anvil.core.contracts.CredentialVaultContract
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.ProviderAdapter
import io.anvil.core.contracts.QualityState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val API_KEY_NAME = "anthropic-api-key"
private const val API_VERSION = "2023-06-01"
private const val DEFAULT_MODEL = "claude-sonnet-4-6"
private const val MAX_TOKENS = 8096

class AnthropicAdapter(
    private val baseUrl: String = "https://api.anthropic.com",
) : ProviderAdapter {

    override val id = "anthropic"
    override val isLocal = false

    private val client = HttpClient {
        install(ContentNegotiation) { json() }
    }

    override suspend fun complete(request: ModelRequest, vault: CredentialVaultContract): ModelResponse {
        val apiKey = vault.retrieve(CredentialKey(API_KEY_NAME))
            ?: throw BellowsExhaustedException(
                "Kein Anthropic API-Key in Vault — CredentialKey($API_KEY_NAME)"
            )
        val resp: AnthropicResponse = client.post("$baseUrl/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", API_VERSION)
            contentType(ContentType.Application.Json)
            setBody(
                AnthropicRequest(
                    model = request.modelHint ?: DEFAULT_MODEL,
                    maxTokens = MAX_TOKENS,
                    messages = listOf(AnthropicMessage(role = "user", content = request.prompt)),
                )
            )
        }.body()
        return ModelResponse(
            content = resp.content.firstOrNull()?.text ?: "",
            modelUsed = resp.model,
        )
    }

    override fun qualityState() = QualityState.STABLE
}

@Serializable
data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val messages: List<AnthropicMessage>,
)

@Serializable
data class AnthropicMessage(val role: String, val content: String)

@Serializable
data class AnthropicResponse(val content: List<AnthropicContent>, val model: String)

@Serializable
data class AnthropicContent(val type: String, val text: String)
