package io.anvil.modules.forge.knight

data class PatchResult(
    val path: String,
    val success: Boolean,
    val appliedHunks: Int,
    val rejectedHunks: Int,
)
