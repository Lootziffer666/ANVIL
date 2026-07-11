package io.anvil.modules.acoustic

import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.StepResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AcousticProducerModuleTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun ctx(privacy: PrivacyMode = PrivacyMode.OPEN) = ModuleContext(
        moduleId = "anvil-acoustic-producer",
        workspaceId = "ws",
        runId = "r1",
        artifactRoot = "root",
        createdAt = "2026-07-11T00:00:00Z",
        privacyMode = privacy,
    )

    private fun request(durationMs: Long = 5000) = AudioGenerationRequest(
        requestId = "REQ1",
        kind = AudioGenerationKind.MUSIC,
        provider = AudioProviderId("fake-acoustic"),
        prompt = "tense coop survival percussion layer",
        durationMs = durationMs,
    )

    @Test
    fun generate_withFakeProvider_producesManifest() = runTest {
        val module = AcousticProducerModule(providers = mapOf(AudioProviderId("fake-acoustic") to FakeAcousticProvider()))
        val result = module.handle(ModuleRunStep("GENERATE", json.encodeToString(request()), ctx()))
        assertIs<StepResult.Completed>(result)
        val manifest = json.decodeFromString<AudioAssetManifest>(result.payload)
        assertEquals(AudioAssetManifest.SCHEMA, manifest.schema)
        assertTrue(manifest.checksum.startsWith("sha256:"))
        assertTrue(module.validate(manifest).all { it.passed })
    }

    @Test
    fun generate_localOnly_blocksRemoteProviderBeforeGeneration() = runTest {
        val remote = object : AcousticProvider {
            override val id = AudioProviderId("remote-only")
            var called = false
            override suspend fun capabilities() = AcousticCapabilities(id, isLocal = false, supportedKinds = listOf(AudioGenerationKind.MUSIC), commercialUseAllowed = true)
            override suspend fun estimate(request: AudioGenerationRequest) = AudioGenerationCost(estimatedCredits = 10)
            override suspend fun generate(request: AudioGenerationRequest, privacyMode: PrivacyMode): AudioGenerationResult {
                called = true
                error("must not be called under LOCAL_ONLY")
            }
        }
        val module = AcousticProducerModule(providers = mapOf(remote.id to remote))
        val result = module.handle(
            ModuleRunStep("GENERATE", json.encodeToString(request().copy(provider = remote.id)), ctx(PrivacyMode.LOCAL_ONLY)),
        )
        assertIs<StepResult.Rejected>(result)
        assertTrue(!remote.called)
    }

    @Test
    fun generate_overBudget_isRejectedNotAutoRetried() = runTest {
        val hugeDurationMs = 10 * 60 * 1000L // 10 minutes of "music" at 15 credits/sec => way over 5400 cap
        val module = AcousticProducerModule(
            providers = mapOf(AudioProviderId("fake-acoustic") to FakeAcousticProvider()),
            budgetPolicy = AudioBudgetPolicy(),
        )
        val result = module.handle(ModuleRunStep("GENERATE", json.encodeToString(request(hugeDurationMs)), ctx()))
        assertIs<StepResult.Rejected>(result)
    }

    @Test
    fun registerExisting_validManifest_completes() = runTest {
        val manifest = AudioAssetManifest(
            assetId = AudioAssetId("AUD_CC0_1"),
            provider = AudioProviderId("freesound"),
            providerModel = "cc0-library",
            generationKind = AudioGenerationKind.SFX,
            sourcePromptHash = "sha256:" + "a".repeat(64),
            durationMs = 1200,
            format = "wav",
            sampleRate = 44100,
            channels = 1,
            license = AudioLicenseInfo("CC0", commercialUseAllowed = true),
            commercialUseAllowed = true,
            estimatedCredits = 0,
            checksum = "sha256:" + "b".repeat(64),
            createdAt = "2026-07-11T00:00:00Z",
        )
        val module = AcousticProducerModule(providers = emptyMap())
        val result = module.handle(
            ModuleRunStep("REGISTER_EXISTING", json.encodeToString(RegisterExistingRequest(manifest = manifest)), ctx()),
        )
        assertIs<StepResult.Completed>(result)
    }

    @Test
    fun validateManifest_rejectsRawPromptLeak() {
        val module = AcousticProducerModule(providers = emptyMap())
        val leaking = AudioAssetManifest(
            assetId = AudioAssetId("AUD_1"),
            provider = AudioProviderId("fake"),
            providerModel = "m",
            generationKind = AudioGenerationKind.MUSIC,
            sourcePromptHash = "this is a raw prompt not a hash",
            durationMs = 1000,
            format = "mp3",
            sampleRate = 44100,
            channels = 2,
            license = AudioLicenseInfo("x", true),
            commercialUseAllowed = true,
            estimatedCredits = 1,
            checksum = "sha256:" + "c".repeat(64),
            createdAt = "2026-07-11T00:00:00Z",
        )
        assertTrue(module.validate(leaking).any { it.id == "no-raw-prompt-leak" && !it.passed })
    }
}
