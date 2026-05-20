package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Artifact(
    val artifactId: ArtifactId,
    val createdAt: String,
    val originModule: String,
    val originWorkspaceId: WorkspaceId,
    val originRunId: RunId,
    val type: String,
    val filename: String,
    val sizeBytes: Long,
    val checksumSha256: String,
)
