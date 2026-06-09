package io.anvil.app.bellows

import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.QualityState
import io.anvil.modules.bellows.BellowsRouter
import io.anvil.modules.bellows.ProviderAdapter
import io.anvil.modules.bellows.wire.OpenAiChatRequest
import io.anvil.modules.bellows.wire.OpenAiChatResponse
import io.anvil.modules.bellows.wire.OpenAiMessage
import io.anvil.modules.bellows.wire.OpenAiModelList
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun routerReturning(reply: String): BellowsRouter =
    BellowsRouter(
        listOf(
            object : ProviderAdapter {
                override val id = "fake"
                override val isLocal = true
                override val models = listOf("test-model")
                override fun handles(model: String?) = true
                override suspend fun route(request: ModelRequest) =
                    ModelResponse(content = reply, modelUsed = "fake:test-model", finishReason = "stop")
                override fun qualityState() = QualityState.STABLE
            },
        ),
    )

class GatewayServerTest {

    @Test
    fun chatCompletions_returnsOpenAiShape() = testApplication {
        application { bellowsGateway(routerReturning("Hallo Welt"), gatewayKey = null) }
        val client = createClient { install(ContentNegotiation) { json(gatewayJson) } }

        val resp = client.post("/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(OpenAiChatRequest(model = "test-model", messages = listOf(OpenAiMessage("user", "hi"))))
        }
        assertEquals(HttpStatusCode.OK, resp.status)
        val body = resp.body<OpenAiChatResponse>()
        assertEquals("chat.completion", body.objectType)
        assertEquals("Hallo Welt", body.choices.first().message?.content)
        assertEquals("fake:test-model", body.model)
    }

    @Test
    fun models_listsConfiguredModels() = testApplication {
        application { bellowsGateway(routerReturning("x"), gatewayKey = null) }
        val client = createClient { install(ContentNegotiation) { json(gatewayJson) } }

        val list = client.get("/v1/models").body<OpenAiModelList>()
        assertEquals(listOf("test-model"), list.data.map { it.id })
    }

    @Test
    fun streaming_emitsSseWithDone() = testApplication {
        application { bellowsGateway(routerReturning("gestreamt"), gatewayKey = null) }
        val client = createClient { install(ContentNegotiation) { json(gatewayJson) } }

        val text = client.post("/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(OpenAiChatRequest(model = "test-model", messages = listOf(OpenAiMessage("user", "hi")), stream = true))
        }.bodyAsText()
        assertTrue(text.contains("chat.completion.chunk"), "SSE muss Chunk-Objekte enthalten")
        assertTrue(text.contains("gestreamt"), "SSE muss den Inhalt enthalten")
        assertTrue(text.contains("[DONE]"), "SSE muss mit [DONE] enden")
    }

    @Test
    fun auth_rejectsMissingTokenWhenKeyConfigured() = testApplication {
        application { bellowsGateway(routerReturning("x"), gatewayKey = "geheim") }
        val client = createClient { install(ContentNegotiation) { json(gatewayJson) } }

        val unauth = client.post("/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(OpenAiChatRequest(model = "test-model", messages = listOf(OpenAiMessage("user", "hi"))))
        }
        assertEquals(HttpStatusCode.Unauthorized, unauth.status)

        val ok = client.post("/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer geheim")
            contentType(ContentType.Application.Json)
            setBody(OpenAiChatRequest(model = "test-model", messages = listOf(OpenAiMessage("user", "hi"))))
        }
        assertEquals(HttpStatusCode.OK, ok.status)
    }
}
