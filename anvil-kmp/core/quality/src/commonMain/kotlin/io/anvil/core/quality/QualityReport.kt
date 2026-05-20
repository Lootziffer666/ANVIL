package io.anvil.core.quality

import io.anvil.core.contracts.QualityState

data class QualityReport(
    val module: String,
    val state: QualityState,
    val reason: String? = null,
)
