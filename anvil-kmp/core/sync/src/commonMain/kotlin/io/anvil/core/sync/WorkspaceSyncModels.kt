package io.anvil.core.sync

import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.run.RunStatus
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class WorkspaceSyncBundleId(val value: String)

@Serializable
data class WorkspaceSyncExportRequest(
    val schema: String = SCHEMA,
    val bundleId: WorkspaceSyncBundleId,
    val workspaceId: String,
    val runId: String,
    val exportedAt: String,
    val exportedFrom: String,
    val deviceId: String,
    val runRefs: List<String> = emptyList(),
    val parentRefs: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.workspace-sync.export-request/v1" }
}

@Serializable
data class WorkspaceSyncBundle(
    val schema: String = SCHEMA,
    val bundleId: WorkspaceSyncBundleId,
    val version: Int = 1,
    val workspaceId: String,
    val exportedAt: String,
    val exportedFrom: String,
    val deviceId: String,
    val registry: ArtifactRegistry,
    val runs: List<WorkspaceSyncRunRef>,
    val warnings: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.workspace-sync.bundle/v1" }
}

@Serializable
data class WorkspaceSyncRunRef(
    val planId: String,
    val runId: String,
    val status: RunStatus,
    val artifactRefs: List<String>,
)

@Serializable
data class WorkspaceSyncValidationFinding(
    val id: String,
    val passed: Boolean,
    val message: String,
)

@Serializable
data class WorkspaceSyncMergeReport(
    val schema: String = SCHEMA,
    val workspaceId: String,
    val artifactCountBefore: Int,
    val artifactCountAfter: Int,
    val importedArtifactRefs: List<String>,
    val skippedArtifactRefs: List<String>,
    val findings: List<WorkspaceSyncValidationFinding>,
) {
    companion object { const val SCHEMA = "anvil.workspace-sync.merge-report/v1" }
}
