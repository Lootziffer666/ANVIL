package io.anvil.modules.bellows

import io.anvil.core.contracts.ChatMessage
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.ToolCall
import io.anvil.core.contracts.ToolDefinition
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OpenAiCompatibleAdapterTest {

    private fun client(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Test
    fun mapsOpenAiResponseToModelResponse() = runTest {
        val engine = MockEngine {
            respond(
                content = """
                    {"model":"gpt-4o-mini",
                     "choices":[{"index":0,"message":{"role":"assistant","content":"Hallo"},"finish_reason":"stop"}],
                     "usage":{"prompt_tokens":3,"completion_tokens":2,"total_tokens":5}}
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val adapter = OpenAiCompatibleAdapter(
            id = "openai",
            baseUrl = "https://api.test/v1",
            apiKey = "sk-test",
            models = listOf("gpt-4o-mini"),
            isLocal = false,
            client = client(engine),
        )
        val res = adapter.route(ModelRequest(messages = listOf(ChatMessage("user", "hi")), model = "gpt-4o-mini"))
        assertEquals("Hallo", res.content)
        assertEquals("openai:gpt-4o-mini", res.modelUsed)
        assertEquals(5, res.usage?.totalTokens)
        assertEquals("stop", res.finishReason)
        assertEquals(QualityState.STABLE, adapter.qualityState())
    }

    @Test
    fun sendsBearerAuthAndChatCompletionsPath() = runTest {
        var seenAuth: String? = null
        var seenUrl: String? = null
        val engine = MockEngine { request ->
            seenAuth = request.headers[HttpHeaders.Authorization]
            seenUrl = request.url.toString()
            respond(
                content = """{"model":"m","choices":[{"message":{"role":"assistant","content":"ok"}}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val adapter = OpenAiCompatibleAdapter("p", "http://localhost:1234/v1", "key123", listOf("m"), true, client(engine))
        adapter.route(ModelRequest(messages = listOf(ChatMessage("user", "hi"))))
        assertEquals("Bearer key123", seenAuth)
        assertEquals("http://localhost:1234/v1/chat/completions", seenUrl)
    }

    @Test
    fun non2xx_throwsProviderExceptionAndDegrades() = runTest {
        val engine = MockEngine { respond(content = "rate limited", status = HttpStatusCode.TooManyRequests) }
        val adapter = OpenAiCompatibleAdapter("x", "https://api.test/v1", null, emptyList(), true, client(engine))
        assertFailsWith<ProviderException> {
            adapter.route(ModelRequest(messages = listOf(ChatMessage("user", "hi"))))
        }
        assertEquals(QualityState.DEGRADED, adapter.qualityState())
    }

    @Test
    fun tools_arePassedThroughToUpstreamRequestBody() = runTest {
        var seenBody: String? = null
        val engine = MockEngine { request ->
            seenBody = (request.body as io.ktor.http.content.TextContent).text
            respond(
                content = """{"model":"m","choices":[{"message":{"role":"assistant","content":"ok"}}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val adapter = OpenAiCompatibleAdapter("openai", "https://api.test/v1", "sk-test", listOf("m"), false, client(engine))
        adapter.route(
            ModelRequest(
                messages = listOf(ChatMessage("user", "find a desert asset")),
                model = "m",
                tools = listOf(ToolDefinition(name = "search_assets", description = "search the library")),
                toolChoice = "auto",
            ),
        )
        assertTrue(seenBody!!.contains("\"search_assets\""), "tools müssen im Request-Body an den Upstream landen")
        assertTrue(seenBody!!.contains("\"tool_choice\":\"auto\""))
    }

    @Test
    fun toolCalls_inResponse_areMappedToModelResponse() = runTest {
        val engine = MockEngine {
            respond(
                content = """
                    {"model":"m","choices":[{"message":{"role":"assistant","content":null,
                     "tool_calls":[{"id":"call_1","type":"function","function":{"name":"search_assets","arguments":"{\"query\":\"desert\"}"}}]},
                     "finish_reason":"tool_calls"}]}
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val adapter = OpenAiCompatibleAdapter("openai", "https://api.test/v1", "sk-test", listOf("m"), false, client(engine))
        val res = adapter.route(ModelRequest(messages = listOf(ChatMessage("user", "hi")), model = "m"))
        assertEquals("tool_calls", res.finishReason)
        assertEquals(1, res.toolCalls?.size)
        assertEquals(ToolCall(id = "call_1", name = "search_assets", arguments = "{\"query\":\"desert\"}"), res.toolCalls?.first())
    }
}
