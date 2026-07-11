package io.anvil.modules.acoustic

import io.anvil.core.contracts.PrivacyMode
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ElevenLabsAcousticProviderTest {

    private fun client(engine: MockEngine) = HttpClient(engine) { expectSuccess = false }

    private fun request(kind: AudioGenerationKind = AudioGenerationKind.MUSIC, durationMs: Long = 10_000) = AudioGenerationRequest(
        requestId = "REQ_EL_1",
        kind = kind,
        provider = AudioProviderId("elevenlabs"),
        prompt = "tense coop survival percussion layer, no lyrics",
        durationMs = durationMs,
    )

    @Test
    fun generate_correctRequest_hitsMusicEndpointWithAuthHeader() = runTest {
        var seenUrl: String? = null
        var seenAuth: String? = null
        var seenBody: String? = null
        val engine = MockEngine { req ->
            seenUrl = req.url.toString()
            seenAuth = req.headers["xi-api-key"]
            seenBody = (req.body as io.ktor.http.content.TextContent).text
            respond(content = "FAKE_MP3_BYTES", status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, "audio/mpeg"))
        }
        val provider = ElevenLabsAcousticProvider(apiKey = "sk_test_key", httpClient = client(engine))
        val result = provider.generate(request(), PrivacyMode.OPEN)
        assertEquals("https://api.elevenlabs.io/v1/music", seenUrl)
        assertEquals("sk_test_key", seenAuth)
        assertTrue(seenBody!!.contains("\"prompt\""))
        assertTrue(seenBody!!.contains("\"music_length_ms\":10000"))
        assertIs<AudioGenerationResult.Generated>(result)
    }

    @Test
    fun generate_missingApiKey_blockedBeforeAnyNetworkCall() = runTest {
        var called = false
        val engine = MockEngine { called = true; respond("", HttpStatusCode.OK) }
        val provider = ElevenLabsAcousticProvider(apiKey = null, httpClient = client(engine))
        val result = provider.generate(request(), PrivacyMode.OPEN)
        assertIs<AudioGenerationResult.Blocked>(result)
        assertFalse(called)
    }

    @Test
    fun generate_localOnly_blockedBeforeAnyNetworkCall() = runTest {
        var called = false
        val engine = MockEngine { called = true; respond("", HttpStatusCode.OK) }
        val provider = ElevenLabsAcousticProvider(apiKey = "sk_test_key", httpClient = client(engine))
        val result = provider.generate(request(), PrivacyMode.LOCAL_ONLY)
        assertIs<AudioGenerationResult.Blocked>(result)
        assertFalse(called)
    }

    @Test
    fun generate_overBudget_blockedBeforeAnyNetworkCall() = runTest {
        var called = false
        val engine = MockEngine { called = true; respond("", HttpStatusCode.OK) }
        // 10 minutes of music at the default 900 credits/minute => 9000 credits, over the 5400 cap.
        val provider = ElevenLabsAcousticProvider(apiKey = "sk_test_key", httpClient = client(engine))
        val result = provider.generate(request(durationMs = 10 * 60 * 1000L), PrivacyMode.OPEN)
        assertIs<AudioGenerationResult.Blocked>(result)
        assertFalse(called)
    }

    @Test
    fun generate_errorResponse_redactsApiKeyFromFailureReason() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"detail":"invalid key xi-api-key: sk_abcdefghijklmno123456 rejected"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val provider = ElevenLabsAcousticProvider(apiKey = "sk_test_key", httpClient = client(engine))
        val result = provider.generate(request(), PrivacyMode.OPEN)
        assertIs<AudioGenerationResult.Failed>(result)
        assertFalse(result.reason.contains("sk_test_key"))
        assertFalse(Regex("sk_[A-Za-z0-9]{10,}").containsMatchIn(result.reason))
    }

    @Test
    fun generate_success_producesManifestableAssetWithoutLeakingKey() = runTest {
        val engine = MockEngine { respond(content = "FAKE_MP3_BYTES", status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, "audio/mpeg")) }
        val provider = ElevenLabsAcousticProvider(apiKey = "sk_test_key", httpClient = client(engine))
        val result = provider.generate(request(), PrivacyMode.OPEN)
        assertIs<AudioGenerationResult.Generated>(result)
        assertTrue(result.asset.checksumSha256.startsWith("sha256:"))
        assertFalse(result.asset.uri.contains("sk_test_key"))
        assertFalse(result.provenance.sourcePromptHash.contains(" "), "sourcePromptHash must be a hash, not the raw prompt")
    }

    @Test
    fun estimate_sfx_usesFlatCreditEstimate() = runTest {
        val provider = ElevenLabsAcousticProvider(apiKey = "sk_test_key", httpClient = client(MockEngine { respond("") }))
        val cost = provider.estimate(request(kind = AudioGenerationKind.SFX, durationMs = 3000))
        assertEquals(200, cost.estimatedCredits)
    }
}
