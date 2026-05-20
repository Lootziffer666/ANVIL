package io.anvil.modules.forge.knight

data class WriteResult(
    val path: String,
    val diff: UnifiedDiff,
)
