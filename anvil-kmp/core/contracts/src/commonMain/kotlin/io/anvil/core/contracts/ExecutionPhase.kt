package io.anvil.core.contracts

import kotlinx.serialization.Serializable

@Serializable
enum class ExecutionPhase {
    IDLE,
    GENERATING,
    VALIDATING,
    WAITING_FOR_REVIEW,
    COMPLETE,
}
