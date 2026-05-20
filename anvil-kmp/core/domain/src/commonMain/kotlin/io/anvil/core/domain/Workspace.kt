package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val id: String,
    val name: String,
    val description: String,
    // All file mutations during a Run must stay within this path (SAFETY_POLICY.md)
    val rootPath: String,
    val buildTarget: BuildTarget,
    val status: WorkspaceStatus,
    val moduleIds: List<String> = emptyList(),
)

enum class BuildTarget {
    ANDROID_APK,
    DESKTOP_EXE,
    EXPORT_JSON,
    EXPORT_PDF,
}

enum class WorkspaceStatus {
    STABLE,
    ADAPTING,
    ACT_NOW,
    FAILED,
}
