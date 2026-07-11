package io.anvil.app.bellows

import io.anvil.core.contracts.BellowsExhaustedException
import io.anvil.core.contracts.ChatMessage
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.ToolCall
import io.anvil.core.contracts.ToolDefinition
import io.anvil.modules.bellows.BellowsRouter
import io.anvil.modules.bellows.wire.OpenAiChatChunk
import io.anvil.modules.bellows.wire.OpenAiChatRequest
import io.anvil.modules.bellows.wire.OpenAiChatResponse
import io.anvil.modules.bellows.wire.OpenAiChoice
import io.anvil.modules.bellows.wire.OpenAiChunkChoice
import io.anvil.modules.bellows.wire.OpenAiDelta
import io.anvil.modules.bellows.wire.OpenAiError
import io.anvil.modules.bellows.wire.OpenAiErrorBody
import io.anvil.modules.bellows.wire.OpenAiFunctionDef
import io.anvil.modules.bellows.wire.OpenAiMessage
import io.anvil.modules.bellows.wire.OpenAiModelCard
import io.anvil.modules.bellows.wire.OpenAiModelList
import io.anvil.modules.bellows.wire.OpenAiToolCall
import io.anvil.modules.bellows.wire.OpenAiToolCallFunction
import io.anvil.modules.bellows.wire.OpenAiToolDefinition
import io.anvil.modules.bellows.wire.OpenAiUsage
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.security.MessageDigest
import java.util.UUID

@Serializable
data class HealthDto(
    val status: String,
    val adapters: Map<String, String>,
    val models: List<String>,
)

/**
 * Der Bellows-Gateway als Ktor-Application-Modul.
 *
 * Endpoints (OpenAI-kompatibel — OpenCode & Co. zeigen einfach auf `/v1`):
 *  - `POST /v1/chat/completions` — Chat (stream true|false)
 *  - `GET  /v1/models`           — verfügbare Modelle
 *  - `GET  /health`              — Zustand des Routers + je Adapter
 *
 * @param router     der konfigurierte [BellowsRouter].
 * @param gatewayKey optionaler Bearer-Token zum Schutz des Endpoints (`null` ⇒ offen, nur localhost empfohlen).
 */
fun Application.bellowsGateway(router: BellowsRouter, gatewayKey: String? = null) {
    install(ContentNegotiation) { json(gatewayJson) }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Anvil-Privacy")
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Options)
    }
    install(StatusPages) {
        exception<BellowsExhaustedException> { call, cause ->
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                OpenAiError(OpenAiErrorBody(cause.message ?: "Kein Provider verfügbar", "bellows_exhausted")),
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                OpenAiError(OpenAiErrorBody(cause.message ?: "Interner Fehler", "internal_error")),
            )
        }
    }

    routing {
        get("/") {
            call.respondText("Anvil Bellows Gateway — OpenAI-kompatibel. Siehe /health und /v1/models.")
        }

        get("/health") {
            call.respond(
                HealthDto(
                    status = router.qualityState().name,
                    adapters = router.adapterStates().mapValues { it.value.name },
                    models = router.listModels(),
                ),
            )
        }

        get("/v1/models") {
            if (!call.authorize(gatewayKey)) return@get
            call.respond(OpenAiModelList(data = router.listModels().map { OpenAiModelCard(id = it) }))
        }

        post("/v1/chat/completions") {
            if (!call.authorize(gatewayKey)) return@post
            val body = call.receive<OpenAiChatRequest>()
            if (body.messages.isEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    OpenAiError(OpenAiErrorBody("'messages' darf nicht leer sein", "invalid_request")),
                )
                return@post
            }
            val request = ModelRequest(
                messages = body.messages.map { it.toChatMessage() },
                model = body.model,
                privacyMode = call.privacyMode(),
                maxTokens = body.maxTokens,
                temperature = body.temperature,
                stream = body.stream,
                tools = body.tools?.map { it.toToolDefinition() },
                toolChoice = body.toolChoice,
            )
            val response = router.route(request) // Fehler ⇒ StatusPages

            if (body.stream) {
                call.respondOpenAiStream(response)
            } else {
                call.respond(response.toOpenAiResponse())
            }
        }
    }
}

// ── Auth & Privacy ─────────────────────────────────────────────────────────────

private suspend fun io.ktor.server.application.ApplicationCall.authorize(gatewayKey: String?): Boolean {
    if (gatewayKey.isNullOrBlank()) return true
    val token = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")?.trim()
    // Konstantzeit-Vergleich gegen Timing-Attacks (CWE-208).
    if (token != null &&
        MessageDigest.isEqual(token.toByteArray(Charsets.UTF_8), gatewayKey.toByteArray(Charsets.UTF_8))
    ) {
        return true
    }
    respond(
        HttpStatusCode.Unauthorized,
        OpenAiError(OpenAiErrorBody("Ungültiger oder fehlender API-Key", "invalid_api_key")),
    )
    return false
}

private fun io.ktor.server.application.ApplicationCall.privacyMode(): PrivacyMode =
    when (request.headers["X-Anvil-Privacy"]?.lowercase()?.replace("-", "_")) {
        "local_only", "local" -> PrivacyMode.LOCAL_ONLY
        else -> PrivacyMode.OPEN
    }

// ── Mapping & Streaming ─────────────────────────────────────────────────────────

private fun chatId(): String = "chatcmpl-" + UUID.randomUUID().toString().replace("-", "").take(24)

private fun ModelResponse.toOpenAiResponse(): OpenAiChatResponse = OpenAiChatResponse(
    id = chatId(),
    created = System.currentTimeMillis() / 1000,
    model = modelUsed,
    choices = listOf(
        OpenAiChoice(
            index = 0,
            message = OpenAiMessage(
                role = "assistant",
                // OpenAI-Konvention: `content` ist `null`, wenn die Antwort ausschließlich
                // aus Tool-Aufrufen besteht.
                content = if (content.isEmpty() && !toolCalls.isNullOrEmpty()) null else content,
                toolCalls = toolCalls?.map { it.toOpenAiToolCall() },
            ),
            finishReason = finishReason ?: if (toolCalls.isNullOrEmpty()) "stop" else "tool_calls",
        ),
    ),
    usage = usage?.let { OpenAiUsage(it.promptTokens, it.completionTokens, it.totalTokens) },
)

private fun OpenAiMessage.toChatMessage() = ChatMessage(
    role = role,
    content = content ?: "",
    name = name,
    toolCallId = toolCallId,
    toolCalls = toolCalls?.map { it.toToolCall() },
)

private fun OpenAiToolDefinition.toToolDefinition() = ToolDefinition(
    name = function.name,
    description = function.description,
    parameters = function.parameters,
)

private fun OpenAiToolCall.toToolCall() = ToolCall(id = id, name = function.name, arguments = function.arguments)

private fun ToolCall.toOpenAiToolCall() = OpenAiToolCall(
    id = id,
    function = OpenAiToolCallFunction(name = name, arguments = arguments),
)

/**
 * Sendet die Antwort als OpenAI-kompatiblen SSE-Stream.
 *
 * Hinweis: Bellows ruft den Upstream nicht-inkrementell ab und emittiert das Ergebnis
 * dann als Chunks (Rolle → Inhalt → finish → `[DONE]`). Für OpenCode & OpenAI-Clients
 * voll kompatibel; echtes token-weises Passthrough-Streaming ist ein Folgeschritt.
 */
private suspend fun io.ktor.server.application.ApplicationCall.respondOpenAiStream(response: ModelResponse) {
    val id = chatId()
    val created = System.currentTimeMillis() / 1000
    val model = response.modelUsed
    respondTextWriter(contentType = ContentType.Text.EventStream) {
        fun emit(choice: OpenAiChunkChoice) {
            val chunk = OpenAiChatChunk(id = id, created = created, model = model, choices = listOf(choice))
            write("data: ${gatewayJson.encodeToString(chunk)}\n\n")
            flush()
        }
        emit(OpenAiChunkChoice(delta = OpenAiDelta(role = "assistant")))
        emit(OpenAiChunkChoice(delta = OpenAiDelta(content = response.content)))
        emit(OpenAiChunkChoice(delta = OpenAiDelta(), finishReason = response.finishReason ?: "stop"))
        write("data: [DONE]\n\n")
        flush()
    }
}
