package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Run(
    val runId: String,
    val workspaceId: String,
    val planId: String,
    val taskId: String,
    val status: RunStatus,
    val startedAt: String,
    val finishedAt: String? = null,
    val artifacts: List<Artifact> = emptyList(),
    val logs: List<String> = emptyList(),
    val failureState: String? = null,
    val recoveryStep: String? = null,
    // Default true per RUN_MODEL.md — must be explicitly overridden
    val humanReviewRequired: Boolean = true,
)

enum class RunStatus {
    STABLE,
    ADAPTING,
    ACT_NOW,
    FAILED,
}
