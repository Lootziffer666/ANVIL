package io.anvil.core.artifacts

import io.anvil.core.contracts.ModuleArtifactRef
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ArtifactId(val value: String)

@JvmInline
@Serializable
value class WorkspaceId(val value: String)

@JvmInline
@Serializable
value class RunId(val value: String)

@Serializable
data class ArtifactManifest(
    val schema: String = SCHEMA,
    val artifactId: ArtifactId,
    val createdAt: String,
    val origin: ArtifactOrigin,
    val type: String,
    val uri: String,
    val sizeBytes: Int,
    val checksumSha256: String,
    val parentRefs: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.artifact.manifest/v1" }
}

@Serializable
data class ArtifactOrigin(
    val module: String,
    val workspaceId: WorkspaceId,
    val runId: RunId,
)

@Serializable
data class ArtifactEnvelope(
    val manifest: ArtifactManifest,
    val payload: String,
)

@Serializable
data class ArtifactRegistry(
    val schema: String = SCHEMA,
    val version: Int = 1,
    val artifacts: List<ArtifactManifest> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.artifact.registry/v1" }
}

@Serializable
data class ArtifactWriteRequest(
    val artifactRef: ModuleArtifactRef,
    val payload: String,
    val createdAt: String,
    val parentRefs: List<String> = emptyList(),
)

@Serializable
data class ArtifactWriteResult(
    val envelope: ArtifactEnvelope,
    val registry: ArtifactRegistry,
)
