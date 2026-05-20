package io.anvil.core.pipeline

import io.anvil.core.domain.PlanId
import io.anvil.core.domain.RunId
import io.anvil.core.domain.TaskId
import io.anvil.core.domain.WorkspaceId

data class RunContext(
    val workspaceId: WorkspaceId,
    val planId: PlanId,
    val taskId: TaskId,
    val runId: RunId,
    val rootPath: String,
    val humanReviewRequired: Boolean = true,
)
