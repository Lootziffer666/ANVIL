package io.anvil.core.contracts

import kotlinx.serialization.Serializable

interface ModuleSlotContract {
    val name: String
    val purpose: String
    fun qualityState(): QualityState
    suspend fun boot(ctx: ModuleContext): BootResult
    suspend fun handle(step: ModuleRunStep): StepResult
}

@Serializable
data class ModuleContext(
    val moduleId: String,
    val workspaceId: String,
    val runId: String,
    val artifactRoot: String,
    val privacyMode: PrivacyMode = PrivacyMode.LOCAL_ONLY,
)

@Serializable
data class BootResult(
    val moduleId: String,
    val qualityState: QualityState,
    val executionPhase: ExecutionPhase = ExecutionPhase.IDLE,
    val message: String,
)

@Serializable
data class ModuleRunStep(
    val operation: String,
    val payload: String,
    val context: ModuleContext,
)

@Serializable
sealed class StepResult {
    abstract val qualityState: QualityState
    abstract val executionPhase: ExecutionPhase

    @Serializable
    data class Completed(
        val artifact: ModuleArtifactRef,
        val payload: String,
        override val qualityState: QualityState = QualityState.STABLE,
        override val executionPhase: ExecutionPhase = ExecutionPhase.COMPLETE,
    ) : StepResult()

    @Serializable
    data class Rejected(
        val reason: String,
        override val qualityState: QualityState = QualityState.BLOCKED,
        override val executionPhase: ExecutionPhase = ExecutionPhase.WAITING_FOR_REVIEW,
    ) : StepResult()

    @Serializable
    data class Failed(
        val reason: String,
        override val qualityState: QualityState = QualityState.FAILED,
        override val executionPhase: ExecutionPhase = ExecutionPhase.COMPLETE,
    ) : StepResult()
}

@Serializable
data class ModuleArtifactRef(
    val id: String,
    val workspaceId: String,
    val runId: String,
    val moduleOrigin: String,
    val type: String,
    val uri: String,
    val sha256: String,
    val timestamp: String,
    val parentArtifactId: String? = null,
)
