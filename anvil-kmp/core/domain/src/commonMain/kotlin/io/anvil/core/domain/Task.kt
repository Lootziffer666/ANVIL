package io.anvil.core.domain

import kotlinx.serialization.Serializable

enum class TaskStatus { QUEUED, READY, RUNNING, BLOCKED, DONE, FAILED, NEEDS_REVIEW }
enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

@Serializable
data class Task(
    val taskId: TaskId,
    val planId: PlanId,
    val title: String,
    val purpose: String,
    val dependsOn: List<TaskId> = emptyList(),
    val allowedFiles: List<String> = emptyList(),
    val forbiddenFiles: List<String> = emptyList(),
    val expectedArtifacts: List<String> = emptyList(),
    val status: TaskStatus = TaskStatus.QUEUED,
    val riskLevel: RiskLevel = RiskLevel.MEDIUM,
    val humanReviewRequired: Boolean = true,
)
