package io.anvil.core.run

import io.anvil.core.artifacts.ArtifactManifest
import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.QualityState
import kotlinx.serialization.SerialName
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
    /** Step ids that must complete before this step may run. Rückwärtskompatibel: default leer. */
    val dependsOn: List<String> = emptyList(),
    /** Contract this step's resolved input payload must satisfy, if any. */
    val inputContract: RunContractRef? = null,
    /** Contract this step's produced artifact must satisfy, if any. */
    val outputContract: RunContractRef? = null,
    /**
     * How to obtain the actual payload at execution time. `null` preserves the old
     * behaviour of using [payload] verbatim (fully backward compatible).
     */
    val inputSelector: RunInputSelector? = null,
)

@Serializable
data class RunContractRef(val id: ContractId, val version: Int)

@Serializable
sealed interface RunInputSelector {
    @Serializable
    @SerialName("inline-payload")
    data class InlinePayload(val payload: String) : RunInputSelector

    @Serializable
    @SerialName("artifact-by-step")
    data class ArtifactByStep(val stepId: String) : RunInputSelector

    @Serializable
    @SerialName("latest-artifact-by-contract")
    data class LatestArtifactByContract(val contractId: ContractId, val version: Int) : RunInputSelector
}

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
