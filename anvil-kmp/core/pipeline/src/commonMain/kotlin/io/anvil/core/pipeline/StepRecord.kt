package io.anvil.core.pipeline

import kotlinx.serialization.Serializable

@Serializable
data class StepRecord(
    val step: RunStep,
    val result: RunResult,
    val durationMs: Long,
    val timestamp: String,
)
