package io.anvil.core.domain

import io.anvil.core.contracts.CheckpointData
import kotlinx.serialization.Serializable

@Serializable
data class Snapshot(
    val id: String,
    val workspaceId: String,
    val runId: String? = null,
    val takenAt: String,
    val checkpoint: CheckpointData,
)
