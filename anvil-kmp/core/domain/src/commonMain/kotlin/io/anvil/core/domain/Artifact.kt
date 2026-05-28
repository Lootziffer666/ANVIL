package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Artifact(
    val id: String,
    val runId: String,
    val kind: ArtifactKind,
    val path: String,
    val sizeBytes: Long,
    val producedAt: Long,
)

@Serializable
enum class ArtifactKind { FILE, APK, ZIP, TEXT, JSON }
