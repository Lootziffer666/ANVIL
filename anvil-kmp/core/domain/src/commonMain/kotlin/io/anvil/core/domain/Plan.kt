package io.anvil.core.domain

import kotlinx.serialization.Serializable

enum class PlanLifecycle { DRAFT, OPEN, LOCKED, EXECUTING, COMPLETED, FAILED, KILLED }

@Serializable
data class Plan(
    val planId: PlanId,
    val workspaceId: WorkspaceId,
    val goal: String,
    val currentState: String,
    val allowedFiles: List<String> = emptyList(),
    val forbiddenFiles: List<String> = emptyList(),
    val tasks: List<TaskId> = emptyList(),
    val expectedOutputs: List<String> = emptyList(),
    val killCriteria: List<String>,
    val testsRequired: List<String> = emptyList(),
    val status: PlanLifecycle = PlanLifecycle.DRAFT,
    val humanReviewRequired: Boolean = true,
)
