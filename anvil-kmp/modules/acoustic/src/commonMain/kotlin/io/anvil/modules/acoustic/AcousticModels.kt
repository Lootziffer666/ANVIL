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
