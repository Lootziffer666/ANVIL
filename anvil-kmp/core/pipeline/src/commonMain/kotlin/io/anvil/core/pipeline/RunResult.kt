package io.anvil.core.pipeline

import io.anvil.core.domain.ArtifactId
import io.anvil.core.domain.TaskId
import kotlinx.serialization.Serializable

@Serializable
sealed class RunResult {
    @Serializable
    data class Success(val artifacts: List<ArtifactId>) : RunResult()

    @Serializable
    data class Failure(val error: String, val recoveryStep: String) : RunResult()

    @Serializable
    data class Blocked(val reason: String) : RunResult()

    @Serializable
    data class NeedsReview(val taskId: TaskId, val message: String) : RunResult()
}
