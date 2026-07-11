package io.anvil.core.handoff

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class HandoffPackageId(val value: String)

enum class HandoffAudience {
    COMMANDER,
    CODEX,
    CLAUDE,
    WIZARD,
    CUE_AGENT,
    MANUAL,
}

enum class HandoffFormat {
    MARKDOWN,
    JSON,
}

@Serializable
data class HandoffExportRequest(
    val schema: String = SCHEMA,
    val packageId: HandoffPackageId,
    val workspaceId: String,
    val runId: String,
    val title: String,
    val goal: String,
    val audience: HandoffAudience,
    val format: HandoffFormat = HandoffFormat.MARKDOWN,
    val artifactRefs: List<String>,
    val nextGates: List<String> = emptyList(),
    val constraints: List<String> = emptyList(),
    val definitionOfDone: List<String> = emptyList(),
    val killCriteria: List<String> = emptyList(),
    val contextFiles: List<String> = emptyList(),
    val parentRefs: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.handoff.export-request/v1" }
}

@Serializable
data class HandoffArtifactRef(
    val artifactId: String,
    val type: String,
    val uri: String,
    val checksumSha256: String,
    val moduleOrigin: String,
    val createdAt: String,
)

@Serializable
data class HandoffPackage(
    val schema: String = SCHEMA,
    val packageId: HandoffPackageId,
    val workspaceId: String,
    val runId: String,
    val title: String,
    val goal: String,
    val audience: HandoffAudience,
    val format: HandoffFormat,
    val artifactRefs: List<HandoffArtifactRef>,
    val nextGates: List<String>,
    val constraints: List<String>,
    val definitionOfDone: List<String>,
    val killCriteria: List<String>,
    val contextFiles: List<String>,
    val parentRefs: List<String>,
    val body: String,
) {
    companion object { const val SCHEMA = "anvil.handoff.package/v1" }
}

@Serializable
data class HandoffValidationFinding(
    val id: String,
    val passed: Boolean,
    val message: String,
)
