package io.anvil.modules.bellows

import io.anvil.core.contracts.ChatMessage
import io.anvil.core.contracts.ModelRequest
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
import kotlin.test.assertTrue

/**
 * Proves the composition recipe documented in `modules/bellows/README.md`
 * ("Anvil-Bellows als Upstream-Provider"): a [BellowsConfig] entry pointing at the
 * standalone Anvil-Bellows proxy (Python/LiteLLM, separate repo, port 4000 — see
 * `ANVIL_CONCEPT_CONTRACT.md` "Anvil × Anvil-Bellows") round-trips through real JSON
 * parsing, [ProviderFactory] and [BellowsRouter.route] against a mock endpoint shaped
 * like Anvil-Bellows' real OpenAI-compatible response (same shape verified live against
 * the actual proxy while fixing its `config.yaml` env-var substitution bug) — not an
 * invented response shape.
 */
class ProviderFactoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun client(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
    }

    @Test
    fun configPointingAtStandaloneAnvilBellows_routesThroughOpenAiCompatibleAdapter() = runTest {
        var seenAuth: String? = null
        var seenUrl: String? = null
        val engine = MockEngine { request ->
            seenAuth = request.headers[HttpHeaders.Authorization]
            seenUrl = request.url.toString()
            respond(
                content = """
                    {"id":"chatcmpl-1","object":"chat.completion","model":"gpt-4o",
                     "choices":[{"index":0,"message":{"role":"assistant","content":"Hallo aus Anvil-Bellows"},"finish_reason":"stop"}],
                     "usage":{"prompt_tokens":4,"completion_tokens":3,"total_tokens":7}}
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        // Exactly the recipe documented in modules/bellows/README.md.
        val configJson = """
            {
              "providers": [
                {
                  "id": "anvil-bellows",
                  "baseUrl": "http://localhost:4000/v1",
                  "apiKeyEnv": "ANVIL_BELLOWS_MASTER_KEY",
                  "models": ["gpt-4o", "gpt-4o-mini", "refinery"],
                  "local": false
                }
              ]
            }
        """.trimIndent()
        val config = json.decodeFromString(BellowsConfig.serializer(), configJson)

        val factory = ProviderFactory(
            client = client(engine),
            envResolver = { name -> if (name == "ANVIL_BELLOWS_MASTER_KEY") "sk-anvil-safe-key" else null },
        )
        val router = BellowsRouter(factory.build(config))

        val response = router.route(ModelRequest(messages = listOf(ChatMessage("user", "hi")), model = "gpt-4o"))

        assertEquals("Hallo aus Anvil-Bellows", response.content)
        assertEquals("Bearer sk-anvil-safe-key", seenAuth)
        assertEquals("http://localhost:4000/v1/chat/completions", seenUrl)
        assertTrue(router.listModels().containsAll(listOf("gpt-4o", "gpt-4o-mini", "refinery")))
    }
}
