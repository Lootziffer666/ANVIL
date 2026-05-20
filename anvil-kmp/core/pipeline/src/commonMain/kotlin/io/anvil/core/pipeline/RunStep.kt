package io.anvil.core.pipeline

import io.anvil.core.contracts.ModelRequest
import kotlinx.serialization.Serializable

@Serializable
sealed class RunStep {
    abstract val stepId: String

    @Serializable
    data class ReadFile(
        override val stepId: String,
        val path: String,
    ) : RunStep()

    @Serializable
    data class WriteFile(
        override val stepId: String,
        val path: String,
        val content: String,
    ) : RunStep()

    @Serializable
    data class PromptLlm(
        override val stepId: String,
        val request: ModelRequest,
    ) : RunStep()

    // command must be on the Command Guard allowlist (SAFETY_POLICY.md)
    @Serializable
    data class RunCommand(
        override val stepId: String,
        val command: String,
        val workingDir: String,
    ) : RunStep()

    @Serializable
    data class SaveCheckpoint(
        override val stepId: String,
        val label: String,
    ) : RunStep()
}
