package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Run(
    val runId: String,
    val workspaceId: String,
    val planId: String? = null,
    val taskId: String? = null,
    val status: RunStatus,
    val artifacts: List<String> = emptyList(),
    val logs: List<String> = emptyList(),
    val humanReviewRequired: Boolean = true,
)

@Serializable
enum class RunStatus { PENDING, RUNNING, SUCCEEDED, FAILED, CANCELLED }
