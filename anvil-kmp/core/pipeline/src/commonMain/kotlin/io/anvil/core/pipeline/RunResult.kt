package io.anvil.core.pipeline

import io.anvil.core.contracts.ModelResponse
import kotlinx.serialization.Serializable

@Serializable
sealed class RunResult {
    abstract val stepId: String

    @Serializable
    data class FileRead(
        override val stepId: String,
        val content: String,
    ) : RunResult()

    @Serializable
    data class FileWritten(
        override val stepId: String,
        val path: String,
        val diffSummary: String,
    ) : RunResult()

    @Serializable
    data class LlmResponse(
        override val stepId: String,
        val response: ModelResponse,
    ) : RunResult()

    @Serializable
    data class CommandExecuted(
        override val stepId: String,
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
    ) : RunResult()

    @Serializable
    data class CheckpointSaved(
        override val stepId: String,
        val checkpointId: String,
    ) : RunResult()

    @Serializable
    data class Failure(
        override val stepId: String,
        val reason: String,
        val recoverable: Boolean,
    ) : RunResult()
}
