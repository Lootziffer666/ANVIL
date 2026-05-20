package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Snapshot(
    val id: SnapshotId,
    val workspaceId: WorkspaceId,
    val timestamp: String,
    val description: String,
    val checkpointPayload: String,
)
