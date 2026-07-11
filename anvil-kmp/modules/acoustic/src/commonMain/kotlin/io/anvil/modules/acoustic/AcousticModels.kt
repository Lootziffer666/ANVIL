package io.anvil.modules.acoustic

import kotlinx.serialization.Serializable

@Serializable
enum class AcousticOperation { COMPILE, VALIDATE }

@JvmInline
@Serializable
value class AudioCueGraphId(val value: String)

@JvmInline
@Serializable
value class AudioCueId(val value: String)

@JvmInline
@Serializable
value class MusicThemeId(val value: String)

@JvmInline
@Serializable
value class MixSnapshotId(val value: String)

@JvmInline
@Serializable
value class AudioProofId(val value: String)

@Serializable
data class AudioIntent(
    val schema: String = SCHEMA,
    val intentId: String,
    val workspaceId: String,
    val runId: String,
    val creativeBriefRef: String,
    val gameplayPlanRef: String,
    val sceneBundleRef: String,
    val emotionalFunctions: List<String>,
    val worldStateInputs: List<String>,
    val requiredCues: List<String>,
    val voiceLineRefs: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.audio.intent/v1" }
}

@Serializable
data class AudioCueGraph(
    val schema: String = SCHEMA,
    val graphId: AudioCueGraphId,
    val creativeBriefRef: String,
    val gameplayPlanRef: String,
    val sceneBundleRef: String,
    val stateInputs: List<AudioStateInput>,
    val themes: List<MusicTheme>,
    val cues: List<AudioCue>,
    val mixSnapshots: List<MixSnapshot>,
    val voiceLines: List<VoiceLine>,
    val proofRequirements: List<AudioProofRequirement>,
    val acousticOwner: String = "anvil-acoustic-runtime",
) {
    companion object { const val SCHEMA = "anvil.audio-cue-graph/v1" }
}

@Serializable
data class AudioStateInput(
    val name: String,
    val range: String = "0..1",
    val sourceRef: String,
)

@Serializable
data class MusicTheme(
    val schema: String = SCHEMA,
    val id: MusicThemeId,
    val emotionalFunction: String,
    val stemSet: StemSet,
) {
    companion object { const val SCHEMA = "anvil.audio.music-theme/v1" }
}

@Serializable
data class StemSet(
    val schema: String = SCHEMA,
    val stems: List<AudioStem>,
) {
    companion object { const val SCHEMA = "anvil.audio.stem-set/v1" }
}

@Serializable
data class AudioStem(
    val id: String,
    val role: String,
    val gainExpression: String,
)

@Serializable
data class AudioCue(
    val schema: String = SCHEMA,
    val id: AudioCueId,
    val trigger: String,
    val cueType: AudioCueType,
    val mixSnapshotRef: MixSnapshotId,
) {
    companion object { const val SCHEMA = "anvil.audio-cue/v1" }
}

@Serializable
enum class AudioCueType { MUSIC_LAYER, SFX, AMBIENCE, STINGER, VOICE }

@Serializable
data class MixSnapshot(
    val schema: String = SCHEMA,
    val id: MixSnapshotId,
    val stateExpression: String,
    val ducking: List<String>,
    val spatialized: Boolean,
) {
    companion object { const val SCHEMA = "anvil.audio.mix-snapshot/v1" }
}

@Serializable
data class VoiceLine(
    val schema: String = SCHEMA,
    val id: String,
    val sourceRef: String,
    val timingRule: String,
    val interruptible: Boolean,
) {
    companion object { const val SCHEMA = "anvil.audio.voice-line/v1" }
}

@Serializable
data class AudioProofRequirement(
    val schema: String = SCHEMA,
    val id: AudioProofId,
    val cueRef: AudioCueId,
    val requiredEvidence: List<AudioEvidenceKind>,
) {
    companion object { const val SCHEMA = "anvil.audio-proof/v1" }
}

@Serializable
enum class AudioEvidenceKind { CUE_FIRED, LOOP_CONTINUITY, TRANSITION_TIMING, CLIPPING_CHECK, INTELLIGIBILITY, STATE_REACTION }

@Serializable
data class AudioValidationReport(
    val schema: String = SCHEMA,
    val graphRef: AudioCueGraphId,
    val passed: List<AudioValidationCheck>,
    val failed: List<AudioValidationCheck>,
) {
    companion object { const val SCHEMA = "anvil.audio.validation-report/v1" }
}

@Serializable
data class AudioValidationCheck(
    val id: String,
    val passed: Boolean,
    val message: String,
)

// ── Gate F-01: Acoustic Production Lane — AudioGenerationRequest → AudioAssetManifest ──
// AcousticRuntimeModule above stays AudioIntent → AudioCueGraph only. Everything below
// belongs to the separate AcousticProducer (Gate F-05): it owns audio *material* and its
// provenance, never gameplay/world-state truth.

@Serializable
enum class AudioGenerationKind { MUSIC, SFX, VOICE, AMBIENCE }

@JvmInline
@Serializable
value class AudioProviderId(val value: String)

@JvmInline
@Serializable
value class AudioAssetId(val value: String)

@Serializable
data class AudioGenerationRequest(
    val schema: String = SCHEMA,
    val requestId: String,
    val kind: AudioGenerationKind,
    val provider: AudioProviderId,
    /** Never the raw BARD prompt verbatim in exported manifests — see [AudioAssetManifest.sourcePromptHash]. */
    val prompt: String,
    val durationMs: Long,
    val forceInstrumental: Boolean = false,
    val seed: Long? = null,
    val outputFormat: String = "mp3_44100_128",
) {
    companion object { const val SCHEMA = "anvil.acoustic.generation-request/v1" }
}

@Serializable
data class LoopMetadata(
    val loopable: Boolean,
    val loopStartMs: Long? = null,
    val loopEndMs: Long? = null,
)

@Serializable
data class StemBundle(
    val schema: String = SCHEMA,
    val stemRole: String,
    val assetRefs: List<String>,
) {
    companion object { const val SCHEMA = "anvil.acoustic.stem-bundle/v1" }
}

@Serializable
data class AudioLicenseInfo(
    val licenseName: String,
    val commercialUseAllowed: Boolean,
    val attribution: String? = null,
)

@Serializable
data class AudioGenerationCost(
    val estimatedCredits: Int,
    val actualCredits: Int? = null,
    val currency: String = "elevenlabs-credits",
)

@Serializable
data class AudioGenerationProvenance(
    val provider: AudioProviderId,
    val providerModel: String,
    val providerGenerationId: String? = null,
    val sourcePromptHash: String,
    val sourceInputRefs: List<String> = emptyList(),
)

@Serializable
data class AudioAsset(
    val schema: String = SCHEMA,
    val assetId: AudioAssetId,
    val generationKind: AudioGenerationKind,
    val durationMs: Long,
    val format: String,
    val sampleRate: Int,
    val channels: Int,
    val loopMetadata: LoopMetadata? = null,
    val stemRole: String? = null,
    val uri: String,
    val checksumSha256: String,
) {
    companion object { const val SCHEMA = "anvil.acoustic.audio-asset/v1" }
}

// ── Gate G-01: Web Audio Runtime — produced by Acoustic, consumed only as serialized ──
// JSON by Target (modules may not import other modules' types per CLAUDE.md §3).

@Serializable
data class WebAudioRuntimeBundle(
    val schema: String = SCHEMA,
    val assetManifestRef: String,
    val cueGraphRef: String,
    val preloadList: List<String>,
    val buses: List<String> = listOf("master", "music", "sfx", "voice"),
    val stems: List<String> = emptyList(),
    val loops: List<String> = emptyList(),
    val transitions: List<String> = emptyList(),
    val debugStateInputs: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.web-audio-runtime-bundle/v1" }
}

@Serializable
data class AudioAssetManifest(
    val schema: String = SCHEMA,
    val assetId: AudioAssetId,
    val provider: AudioProviderId,
    val providerModel: String,
    val generationKind: AudioGenerationKind,
    val sourcePromptHash: String,
    val sourceInputRefs: List<String> = emptyList(),
    val durationMs: Long,
    val format: String,
    val sampleRate: Int,
    val channels: Int,
    val loopMetadata: LoopMetadata? = null,
    val stemRole: String? = null,
    val license: AudioLicenseInfo,
    val commercialUseAllowed: Boolean,
    val attribution: String? = null,
    val providerGenerationId: String? = null,
    val estimatedCredits: Int,
    val actualCredits: Int? = null,
    val checksum: String,
    val createdAt: String,
    val parentRefs: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.audio-asset-manifest/v1" }
}
