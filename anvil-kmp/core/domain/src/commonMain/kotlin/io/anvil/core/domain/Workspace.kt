package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val id: String,
    val name: String,
    val description: String = "",
    val rootPath: String,
    val buildTarget: BuildTarget = BuildTarget.ANDROID,
    val status: WorkspaceStatus = WorkspaceStatus.ACTIVE,
    val moduleIds: List<String> = emptyList(),
)

@Serializable enum class BuildTarget { ANDROID, JVM, KMP }
@Serializable enum class WorkspaceStatus { ACTIVE, ARCHIVED }
