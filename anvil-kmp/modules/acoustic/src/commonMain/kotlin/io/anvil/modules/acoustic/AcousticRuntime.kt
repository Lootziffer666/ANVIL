package io.anvil.modules.acoustic

import io.anvil.core.contracts.BootResult
import io.anvil.core.contracts.ExecutionPhase
import io.anvil.core.contracts.ModuleArtifactRef
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AcousticRuntimeModule : ModuleSlotContract {
    override val name: String = "anvil-acoustic-runtime"
    override val purpose: String =
        "Compiles adaptive audio cue graphs from intent and state inputs without owning state truth."

    private var quality = QualityState.STABLE
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override fun qualityState(): QualityState = quality

    override suspend fun boot(ctx: ModuleContext): BootResult = BootResult(
        moduleId = name,
        qualityState = quality,
        executionPhase = ExecutionPhase.IDLE,
        message = "Acoustic Runtime booted; audio reacts to state inputs but does not create them.",
    )

    override suspend fun handle(step: ModuleRunStep): StepResult = when (AcousticOperation.valueOf(step.operation)) {
        AcousticOperation.COMPILE -> {
            val graph = compile(json.decodeFromString<AudioIntent>(step.payload))
            val payload = json.encodeToString(graph)
            complete(step.context, AudioCueGraph.SCHEMA, payload)
        }
        AcousticOperation.VALIDATE -> {
            val report = validate(json.decodeFromString<AudioCueGraph>(step.payload))
            val payload = json.encodeToString(report)
            complete(step.context, AudioValidationReport.SCHEMA, payload)
        }
    }

    fun compile(intent: AudioIntent): AudioCueGraph {
        require(intent.schema == AudioIntent.SCHEMA) { "Unsupported audio intent schema: ${intent.schema}" }
        val inputs = intent.worldStateInputs.ifEmpty { listOf("danger", "wonder", "exhaustion") }.map { input ->
            AudioStateInput(name = input, sourceRef = "${intent.gameplayPlanRef}#$input")
        }
        val primaryInput = inputs.first().name
        val snapshot = MixSnapshot(
            id = MixSnapshotId("MIX_PRIMARY"),
            stateExpression = "$primaryInput > 0.1",
            ducking = listOf("ui", "ambience-low"),
            spatialized = true,
        )
        val themes = intent.emotionalFunctions.ifEmpty { listOf("responsive tension") }.mapIndexed { index, function ->
            MusicTheme(
                id = MusicThemeId("THEME_${index + 1}"),
                emotionalFunction = function,
                stemSet = StemSet(
                    stems = listOf(
                        AudioStem("stem_${index + 1}_bed", "bed", "1 - $primaryInput"),
                        AudioStem("stem_${index + 1}_intensity", "intensity", primaryInput),
                    ),
                ),
            )
        }
        val cues = intent.requiredCues.ifEmpty { listOf("state-change") }.mapIndexed { index, cue ->
            AudioCue(
                id = AudioCueId("CUE_${cue.uppercase().replace('-', '_')}_${index + 1}"),
                trigger = cue,
                cueType = AudioCueType.STINGER,
                mixSnapshotRef = snapshot.id,
            )
        }
        return AudioCueGraph(
            graphId = AudioCueGraphId("ACG_${stableHash(intent.intentId + intent.gameplayPlanRef)}"),
            creativeBriefRef = intent.creativeBriefRef,
            gameplayPlanRef = intent.gameplayPlanRef,
            sceneBundleRef = intent.sceneBundleRef,
            stateInputs = inputs,
            themes = themes,
            cues = cues,
            mixSnapshots = listOf(snapshot),
            voiceLines = intent.voiceLineRefs.mapIndexed { index, ref ->
                VoiceLine(id = "VOICE_${index + 1}", sourceRef = ref, timingRule = "after-cue-and-before-next-state-transition", interruptible = true)
            },
            proofRequirements = cues.map { cue ->
                AudioProofRequirement(
                    id = AudioProofId("APR_${cue.id.value}"),
                    cueRef = cue.id,
                    requiredEvidence = listOf(
                        AudioEvidenceKind.CUE_FIRED,
                        AudioEvidenceKind.TRANSITION_TIMING,
                        AudioEvidenceKind.STATE_REACTION,
                        AudioEvidenceKind.CLIPPING_CHECK,
                    ),
                )
            },
        )
    }

    fun validate(graph: AudioCueGraph): AudioValidationReport {
        val snapshotIds = graph.mixSnapshots.map { it.id }.toSet()
        val checks = listOf(
            check("has-state-inputs", graph.stateInputs.isNotEmpty(), "AudioCueGraph must define state inputs."),
            check("has-cues", graph.cues.isNotEmpty(), "AudioCueGraph must define cues."),
            check("has-proof-requirements", graph.proofRequirements.isNotEmpty(), "AudioCueGraph must define CUE audio proof requirements."),
            check("cue-snapshots-exist", graph.cues.all { it.mixSnapshotRef in snapshotIds }, "Every cue must reference an existing mix snapshot."),
            check("acoustic-owner-only", graph.acousticOwner == name, "Acoustic Runtime owns audible state only, not gameplay truth."),
        )
        return AudioValidationReport(
            graphRef = graph.graphId,
            passed = checks.filter { it.passed },
            failed = checks.filterNot { it.passed },
        )
    }

    private fun complete(context: ModuleContext, type: String, payload: String): StepResult.Completed {
        val hash = stableHash(payload)
        return StepResult.Completed(
            artifact = ModuleArtifactRef(
                id = "ART_AUDIO_$hash",
                workspaceId = context.workspaceId,
                runId = context.runId,
                moduleOrigin = name,
                type = type,
                uri = "${context.artifactRoot}/acoustic/$hash.json",
                sha256 = "sha256:$hash",
                timestamp = context.createdAt,
            ),
            payload = payload,
            executionPhase = ExecutionPhase.COMPLETE,
        )
    }

    private fun check(id: String, passed: Boolean, message: String) = AudioValidationCheck(id, passed, message)

    private fun stableHash(text: String): String = text.fold(0) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }.toString(16)
}
