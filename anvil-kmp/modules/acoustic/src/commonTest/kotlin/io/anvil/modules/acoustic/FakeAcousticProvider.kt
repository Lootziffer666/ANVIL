package io.anvil.modules.acoustic

import io.anvil.core.artifacts.Sha256
import io.anvil.core.contracts.PrivacyMode

/** Deterministic, offline stand-in for a real generative audio provider. Never makes network calls. */
class FakeAcousticProvider(
    override val id: AudioProviderId = AudioProviderId("fake-acoustic"),
    private val creditsPerSecond: Int = 15,
) : AcousticProvider {

    override suspend fun capabilities(): AcousticCapabilities = AcousticCapabilities(
        provider = id,
        isLocal = true,
        supportedKinds = listOf(AudioGenerationKind.MUSIC, AudioGenerationKind.SFX, AudioGenerationKind.AMBIENCE),
        commercialUseAllowed = true,
    )

    override suspend fun estimate(request: AudioGenerationRequest): AudioGenerationCost =
        AudioGenerationCost(estimatedCredits = creditsForDuration(request.durationMs))

    override suspend fun generate(request: AudioGenerationRequest, privacyMode: PrivacyMode): AudioGenerationResult {
        // Fixture is "local" but still respects the LOCAL_ONLY contract shape for parity with real providers.
        val promptHash = Sha256.digestPrefixed(request.prompt)
        val fakeBytes = "fixture-audio:${request.requestId}:$promptHash"
        val checksum = Sha256.digestPrefixed(fakeBytes)
        val credits = creditsForDuration(request.durationMs)
        val asset = AudioAsset(
            assetId = AudioAssetId("AUD_${request.requestId}"),
            generationKind = request.kind,
            durationMs = request.durationMs,
            format = request.outputFormat,
            sampleRate = 44100,
            channels = 2,
            loopMetadata = if (request.kind == AudioGenerationKind.MUSIC) LoopMetadata(loopable = true, loopStartMs = 0, loopEndMs = request.durationMs) else null,
            stemRole = if (request.kind == AudioGenerationKind.MUSIC) "full-mix" else null,
            uri = "fixture://acoustic/${request.requestId}.${extensionFor(request.outputFormat)}",
            checksumSha256 = checksum,
        )
        val provenance = AudioGenerationProvenance(
            provider = id,
            providerModel = "fixture-model-v1",
            providerGenerationId = "FIXGEN_${request.requestId}",
            sourcePromptHash = promptHash,
        )
        return AudioGenerationResult.Generated(asset, provenance, AudioGenerationCost(estimatedCredits = credits, actualCredits = credits))
    }

    private fun creditsForDuration(durationMs: Long): Int = ((durationMs / 1000.0) * creditsPerSecond).toInt().coerceAtLeast(1)

    private fun extensionFor(outputFormat: String): String = if (outputFormat.startsWith("mp3")) "mp3" else "wav"
}
