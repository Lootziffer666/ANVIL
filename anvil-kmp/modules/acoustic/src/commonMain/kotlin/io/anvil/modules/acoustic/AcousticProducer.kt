package io.anvil.modules.acoustic

import io.anvil.core.contracts.BootResult
import io.anvil.core.contracts.ExecutionPhase
import io.anvil.core.contracts.ModuleArtifactRef
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class AcousticProducerOperation { ESTIMATE, GENERATE, REGISTER_EXISTING, VALIDATE_MANIFEST }

@Serializable
data class RegisterExistingRequest(
    val schema: String = SCHEMA,
    val manifest: AudioAssetManifest,
) {
    companion object { const val SCHEMA = "anvil.acoustic.register-existing-request/v1" }
}

/**
 * Produces audio *material* (AudioGenerationRequest → AudioAssetManifest). Deliberately
 * separate from [AcousticRuntimeModule] (AudioIntent → AudioCueGraph, Gate B15) — this
 * module owns provenance/cost/licensing, never state-reactive mixing.
 */
class AcousticProducerModule(
    private val providers: Map<AudioProviderId, AcousticProvider>,
    private val budgetPolicy: AudioBudgetPolicy = AudioBudgetPolicy(),
) : ModuleSlotContract {
    override val name: String = "anvil-acoustic-producer"
    override val purpose: String =
        "Generates or registers audio material and its provenance/cost/license, without owning gameplay or mix truth."

    private var quality = QualityState.STABLE
    private var spentCreditsThisRun = 0
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override fun qualityState(): QualityState = quality

    override suspend fun boot(ctx: ModuleContext): BootResult = BootResult(
        moduleId = name,
        qualityState = quality,
        executionPhase = ExecutionPhase.IDLE,
        message = "Acoustic Producer booted; LOCAL_ONLY blocks every non-local provider before any network call.",
    )

    override suspend fun handle(step: ModuleRunStep): StepResult = when (AcousticProducerOperation.valueOf(step.operation)) {
        AcousticProducerOperation.ESTIMATE -> estimate(step)
        AcousticProducerOperation.GENERATE -> generate(step)
        AcousticProducerOperation.REGISTER_EXISTING -> registerExisting(step)
        AcousticProducerOperation.VALIDATE_MANIFEST -> validateManifestStep(step)
    }

    private suspend fun estimate(step: ModuleRunStep): StepResult {
        val request = json.decodeFromString<AudioGenerationRequest>(step.payload)
        val provider = providers[request.provider]
            ?: return rejected("No provider registered for '${request.provider.value}'.")
        val cost = provider.estimate(request)
        return complete(step.context, "anvil.acoustic.generation-cost/v1", json.encodeToString(cost))
    }

    private suspend fun generate(step: ModuleRunStep): StepResult {
        val request = json.decodeFromString<AudioGenerationRequest>(step.payload)
        val provider = providers[request.provider]
            ?: return rejected("No provider registered for '${request.provider.value}'.")

        val capabilities = provider.capabilities()
        if (step.context.privacyMode == PrivacyMode.LOCAL_ONLY && !capabilities.isLocal) {
            return rejected("LOCAL_ONLY blocks remote provider '${request.provider.value}' before any network call.")
        }

        val cost = provider.estimate(request)
        val verdict = budgetPolicy.evaluate(request.kind, cost.estimatedCredits, spentCreditsThisRun)
        if (verdict != AudioBudgetVerdict.ALLOWED) {
            return rejected("Audio budget verdict for '${request.requestId}' was $verdict (estimated ${cost.estimatedCredits} credits); no automatic paid generation without approval.")
        }

        val result = provider.generate(request, step.context.privacyMode)
        return when (result) {
            is AudioGenerationResult.Blocked -> rejected(result.reason)
            is AudioGenerationResult.Failed -> StepResult.Failed(reason = result.reason, qualityState = QualityState.FAILED)
            is AudioGenerationResult.Generated -> {
                spentCreditsThisRun += result.cost.actualCredits ?: result.cost.estimatedCredits
                val manifest = AudioAssetManifest(
                    assetId = result.asset.assetId,
                    provider = result.provenance.provider,
                    providerModel = result.provenance.providerModel,
                    generationKind = result.asset.generationKind,
                    sourcePromptHash = result.provenance.sourcePromptHash,
                    sourceInputRefs = result.provenance.sourceInputRefs,
                    durationMs = result.asset.durationMs,
                    format = result.asset.format,
                    sampleRate = result.asset.sampleRate,
                    channels = result.asset.channels,
                    loopMetadata = result.asset.loopMetadata,
                    stemRole = result.asset.stemRole,
                    license = AudioLicenseInfo(
                        licenseName = if (capabilities.commercialUseAllowed) "commercial-generation-license" else "non-commercial-only",
                        commercialUseAllowed = capabilities.commercialUseAllowed,
                    ),
                    commercialUseAllowed = capabilities.commercialUseAllowed,
                    providerGenerationId = result.provenance.providerGenerationId,
                    estimatedCredits = result.cost.estimatedCredits,
                    actualCredits = result.cost.actualCredits,
                    checksum = result.asset.checksumSha256,
                    createdAt = step.context.createdAt,
                )
                complete(step.context, AudioAssetManifest.SCHEMA, json.encodeToString(manifest))
            }
        }
    }

    private fun registerExisting(step: ModuleRunStep): StepResult {
        val request = json.decodeFromString<RegisterExistingRequest>(step.payload)
        val findings = validate(request.manifest)
        if (findings.any { !it.passed }) {
            return rejected("Manifest failed validation: ${findings.filterNot { it.passed }.joinToString { it.message }}")
        }
        return complete(step.context, AudioAssetManifest.SCHEMA, json.encodeToString(request.manifest))
    }

    private fun validateManifestStep(step: ModuleRunStep): StepResult {
        val manifest = json.decodeFromString<AudioAssetManifest>(step.payload)
        val findings = validate(manifest)
        return complete(step.context, "anvil.acoustic.manifest-validation-report/v1", json.encodeToString(findings))
    }

    fun validate(manifest: AudioAssetManifest): List<AudioValidationCheck> = listOf(
        check("has-asset-id", manifest.assetId.value.isNotBlank(), "AudioAssetManifest requires an assetId."),
        check("has-checksum", manifest.checksum.startsWith("sha256:"), "AudioAssetManifest checksum must be a sha256 digest."),
        check("no-raw-prompt-leak", !manifest.sourcePromptHash.contains(" "), "sourcePromptHash must be a hash, not a raw prompt string."),
        check("duration-positive", manifest.durationMs > 0, "AudioAssetManifest durationMs must be positive."),
        check("license-consistent", manifest.license.commercialUseAllowed == manifest.commercialUseAllowed, "license.commercialUseAllowed must match commercialUseAllowed."),
    )

    private fun check(id: String, passed: Boolean, message: String) = AudioValidationCheck(id, passed, message)

    private fun rejected(reason: String) = StepResult.Rejected(reason = reason, qualityState = QualityState.BLOCKED)

    private fun complete(context: ModuleContext, type: String, payload: String): StepResult.Completed {
        val hash = stableHash(payload)
        return StepResult.Completed(
            artifact = ModuleArtifactRef(
                id = "ART_ACOUSTIC_PRODUCER_$hash",
                workspaceId = context.workspaceId,
                runId = context.runId,
                moduleOrigin = name,
                type = type,
                uri = "${context.artifactRoot}/acoustic-producer/$hash.json",
                sha256 = "sha256:$hash",
                timestamp = context.createdAt,
            ),
            payload = payload,
            executionPhase = ExecutionPhase.COMPLETE,
        )
    }

    private fun stableHash(text: String): String = text.fold(0) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }.toString(16)
}
