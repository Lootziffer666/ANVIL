package io.anvil.core.pipeline

import io.anvil.core.contracts.ModelResponse
import kotlinx.serialization.Serializable

@Serializable
sealed class RunResult {
    @Serializable data class FileRead(val path: String, val content: String) : RunResult()
    @Serializable data class FileWritten(val path: String) : RunResult()
    @Serializable data class LlmResponse(val response: ModelResponse) : RunResult()
    @Serializable data class CommandExecuted(
        val exitCode: Int,
        val stdout: String,
    ) : RunResult()
    @Serializable data object CheckpointSaved : RunResult()
    @Serializable data class Failure(
        val message: String,
        val cause: String? = null,
    ) : RunResult()
}
