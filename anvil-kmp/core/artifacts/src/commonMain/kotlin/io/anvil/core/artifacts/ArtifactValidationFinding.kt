package io.anvil.core.artifacts

import kotlinx.serialization.Serializable

@Serializable
data class ArtifactValidationFinding(
    val id: String,
    val passed: Boolean,
    val message: String,
)
