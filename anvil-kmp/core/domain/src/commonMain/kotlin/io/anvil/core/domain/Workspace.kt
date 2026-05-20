package io.anvil.core.domain

import io.anvil.core.contracts.QualityState
import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val id: WorkspaceId,
    val name: String,
    val description: String,
    val rootPath: String,
    val modules: List<String> = emptyList(),
    val inputs: List<WorkspaceSlot> = emptyList(),
    val outputs: List<WorkspaceSlot> = emptyList(),
    val buildTarget: String,
    val status: QualityState,
)

@Serializable
data class WorkspaceSlot(
    val name: String,
    val type: String,
)
