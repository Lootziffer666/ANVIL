package io.anvil.core.quality

import io.anvil.core.contracts.QualityState

object QualityGuard {
    fun require(state: QualityState, context: String) {
        check(state != QualityState.FAILED) { "ANVIL FAILED: $context" }
    }

    fun enforce(context: String, check: () -> Boolean): QualityState =
        if (check()) QualityState.STABLE else QualityState.DEGRADED
}
