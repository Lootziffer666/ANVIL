package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Artifact(
    val id: String,
    val runId: String,
    val kind: ArtifactKind,
    val path: String,
    val sizeBytes: Long? = null,
    val producedAt: String,
)

enum class ArtifactKind {
    FILE,
    DIFF,
    LOG,
    REPORT,
}
