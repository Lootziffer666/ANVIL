package io.anvil.core.run

import io.anvil.core.artifacts.ArtifactManifest
import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.contracts.QualityState
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class RunPlanId(val value: String)

@Serializable
data class RunPlan(
    val schema: String = SCHEMA,
    val planId: RunPlanId,
    val workspaceId: String,
    val runId: String,
    val artifactRoot: String,
    val steps: List<RunPlanStep>,
) {
    companion object { const val SCHEMA = "anvil.run.plan/v1" }
}

@Serializable
data class RunPlanStep(
    val id: String,
    val moduleId: String,
    val operation: String,
    val payload: String,
    val parentRefs: List<String> = emptyList(),
)

@Serializable
data class RunSummary(
    val schema: String = SCHEMA,
    val planRef: RunPlanId,
    val workspaceId: String,
    val runId: String,
    val status: RunStatus,
    val records: List<RunStepRecord>,
    val registry: ArtifactRegistry,
) {
    companion object { const val SCHEMA = "anvil.run.summary/v1" }
}

@Serializable
data class RunStepRecord(
    val stepId: String,
    val moduleId: String,
    val operation: String,
    val status: RunStepStatus,
    val qualityState: QualityState,
    val artifact: ArtifactManifest? = null,
    val message: String? = null,
)

@Serializable
enum class RunStatus { COMPLETE, BLOCKED, FAILED }

@Serializable
enum class RunStepStatus { COMPLETED, REJECTED, FAILED, MISSING_MODULE }
