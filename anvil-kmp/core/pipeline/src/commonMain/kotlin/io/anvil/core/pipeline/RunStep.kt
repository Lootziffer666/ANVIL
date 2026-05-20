package io.anvil.core.pipeline

import io.anvil.core.domain.TaskId
import kotlinx.serialization.Serializable

@Serializable
sealed class RunStep {
    @Serializable
    data class Plan(val goal: String, val killCriteria: List<String>) : RunStep()

    @Serializable
    data class Execute(val taskId: TaskId, val command: String) : RunStep()

    @Serializable
    data class Review(val taskId: TaskId, val message: String) : RunStep()

    @Serializable
    data object Finish : RunStep()
}
