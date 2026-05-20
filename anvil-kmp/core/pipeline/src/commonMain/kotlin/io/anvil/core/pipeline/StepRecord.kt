package io.anvil.core.pipeline

import io.anvil.core.domain.RunId
import kotlinx.serialization.Serializable

@Serializable
data class StepRecord(
    val stepId: String,
    val runId: RunId,
    val stepType: String,
    val resultType: String,
    val timestamp: String,
    val durationMs: Long,
)
