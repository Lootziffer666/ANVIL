package io.anvil.core.pipeline

import io.anvil.core.contracts.ModelRequest
import kotlinx.serialization.Serializable

@Serializable
sealed class RunStep {
    @Serializable data class ReadFile(val path: String) : RunStep()
    @Serializable data class WriteFile(val path: String, val content: String) : RunStep()
    @Serializable data class PromptLlm(val request: ModelRequest) : RunStep()
    @Serializable data class RunCommand(
        val command: String,
        val args: List<String> = emptyList(),
    ) : RunStep()
    @Serializable data object SaveCheckpoint : RunStep()
}
