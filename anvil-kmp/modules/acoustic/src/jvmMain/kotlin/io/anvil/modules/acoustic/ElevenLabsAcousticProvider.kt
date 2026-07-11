package io.anvil.modules.acoustic

import io.anvil.core.artifacts.Sha256
import io.anvil.core.contracts.PrivacyMode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64
import kotlin.math.ceil

/**
 * Real ElevenLabs provider (Gate F-04). Endpoints verified against ElevenLabs' public API
 * docs at the time of writing (not guessed):
 *   - `POST /v1/music` for Eleven Music generation
 *   - `POST /v1/sound-generation` for sound effects
 * Auth header: `xi-api-key`. Base URL default `https://api.elevenlabs.io`.
 *
 * Credit-per-unit figures (`creditsPerMinuteMusic`, `creditsPerSfxGeneration`) are the
 * Starter-plan estimates quoted in the fix prompt at the time of writing — configurable
 * assumptions, not a hard-coded eternal truth; see `docs/GOLDEN_RUN_REPORT.md`.
 */
class ElevenLabsAcousticProvider(
    private val apiKey: String?,
    private val httpClient: HttpClient,
    private val budgetPolicy: AudioBudgetPolicy = AudioBudgetPolicy(),
    private val baseUrl: String = "https://api.elevenlabs.io",
    private val creditsPerMinuteMusic: Int = 900,
    private val creditsPerSfxGeneration: Int = 200,
    private val maxResponseBytes: Int = 25 * 1024 * 1024,
) : AcousticProvider {

    override val id: AudioProviderId = AudioProviderId("elevenlabs")

    override suspend fun capabilities(): AcousticCapabilities = AcousticCapabilities(
        provider = id,
        isLocal = false,
        supportedKinds = listOf(AudioGenerationKind.MUSIC, AudioGenerationKind.SFX),
        commercialUseAllowed = true, // ElevenLabs Starter plan includes commercial license for generated audio.
    )

    override suspend fun estimate(request: AudioGenerationRequest): AudioGenerationCost {
        val credits = when (request.kind) {
            AudioGenerationKind.MUSIC -> ceil((request.durationMs / 60000.0) * creditsPerMinuteMusic).toInt().coerceAtLeast(1)
            AudioGenerationKind.SFX -> creditsPerSfxGeneration
            AudioGenerationKind.VOICE, AudioGenerationKind.AMBIENCE -> creditsPerSfxGeneration
        }
        return AudioGenerationCost(estimatedCredits = credits)
    }

    override suspend fun generate(request: AudioGenerationRequest, privacyMode: PrivacyMode): AudioGenerationResult {
        if (privacyMode == PrivacyMode.LOCAL_ONLY) {
            return AudioGenerationResult.Blocked("LOCAL_ONLY blocks ElevenLabs (remote provider) before any network call.")
        }
        val key = apiKey
        if (key.isNullOrBlank()) {
            return AudioGenerationResult.Blocked("Missing ELEVENLABS_API_KEY; remote audio generation refused.")
        }

        val cost = estimate(request)
        val verdict = budgetPolicy.evaluate(request.kind, cost.estimatedCredits, alreadySpentCreditsThisRun = 0)
        if (verdict == AudioBudgetVerdict.BLOCKED) {
            return AudioGenerationResult.Blocked("Budget policy blocked this request before any network call (estimated ${cost.estimatedCredits} credits).")
        }

        return try {
            val response = when (request.kind) {
                AudioGenerationKind.MUSIC -> httpClient.post("$baseUrl/v1/music") {
                    header("xi-api-key", key)
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(MusicRequestBody(
                        prompt = request.prompt,
                        music_length_ms = request.durationMs,
                        force_instrumental = request.forceInstrumental,
                        seed = request.seed,
                    )))
                }
                AudioGenerationKind.SFX, AudioGenerationKind.VOICE, AudioGenerationKind.AMBIENCE -> httpClient.post("$baseUrl/v1/sound-generation") {
                    header("xi-api-key", key)
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(SoundGenerationRequestBody(
                        text = request.prompt,
                        duration_seconds = (request.durationMs / 1000.0).coerceIn(0.5, 30.0),
                    )))
                }
            }
            handleResponse(request, response, cost)
        } catch (e: Exception) {
            AudioGenerationResult.Failed("Network error calling ElevenLabs: ${redact(e.message ?: e.toString())}")
        }
    }

    private suspend fun handleResponse(request: AudioGenerationRequest, response: HttpResponse, cost: AudioGenerationCost): AudioGenerationResult {
        if (!response.status.isSuccess()) {
            val bodyText = runCatching { response.bodyAsText() }.getOrDefault("")
            return AudioGenerationResult.Failed("ElevenLabs request failed with HTTP ${response.status.value}: ${redact(bodyText)}")
        }
        val bytes = response.bodyAsBytes()
        if (bytes.size > maxResponseBytes) {
            return AudioGenerationResult.Failed("ElevenLabs response exceeded the ${maxResponseBytes}-byte size limit.")
        }
        val payloadBase64 = Base64.getEncoder().encodeToString(bytes)
        val checksum = Sha256.digestPrefixed(bytes)
        val promptHash = Sha256.digestPrefixed(request.prompt)

        val asset = AudioAsset(
            assetId = AudioAssetId("AUD_${request.requestId}"),
            generationKind = request.kind,
            durationMs = request.durationMs,
            format = request.outputFormat,
            sampleRate = 44100,
            channels = 2,
            loopMetadata = if (request.kind == AudioGenerationKind.MUSIC) LoopMetadata(loopable = true, loopStartMs = 0, loopEndMs = request.durationMs) else null,
            stemRole = if (request.kind == AudioGenerationKind.MUSIC) "full-mix" else null,
            uri = "data:audio;base64-artifact-payload", // caller persists payloadBase64 via ArtifactStore; this uri is a placeholder pointer, never the key material itself.
            checksumSha256 = checksum,
        )
        val provenance = AudioGenerationProvenance(
            provider = id,
            providerModel = if (request.kind == AudioGenerationKind.MUSIC) "music_v1" else "eleven_text_to_sound_v2",
            providerGenerationId = null, // Not populated: ElevenLabs' binary-audio responses were not verified to carry a stable generation-id header for this gate.
            sourcePromptHash = promptHash,
        )
        return AudioGenerationResult.Generated(asset, provenance, AudioGenerationCost(estimatedCredits = cost.estimatedCredits, actualCredits = null))
    }

    /** Strips anything that looks like it could be the API key or a bearer token from provider error text before it reaches logs/artifacts. */
    private fun redact(text: String): String = text
        .replace(Regex("xi-api-key\\s*[:=]\\s*\\S+", RegexOption.IGNORE_CASE), "xi-api-key: [redacted]")
        .replace(Regex("sk_[A-Za-z0-9]{10,}"), "[redacted-key]")
        .take(500)

    @Serializable
    private data class MusicRequestBody(
        val prompt: String,
        val music_length_ms: Long,
        val force_instrumental: Boolean,
        val seed: Long? = null,
    )

    @Serializable
    private data class SoundGenerationRequestBody(
        val text: String,
        val duration_seconds: Double,
    )

    private companion object {
        val json = Json { encodeDefaults = true }
    }
}

/** Production HTTP client factory: JVM Java engine, bounded timeout, no logging of bodies (avoids key leakage into logs). */
fun elevenLabsDefaultHttpClient(): HttpClient = HttpClient(Java) {
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 10_000
    }
}
