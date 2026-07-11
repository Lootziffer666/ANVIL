package io.anvil.core.artifacts

import kotlinx.serialization.Serializable

/**
 * Immutable artifact persistence. Artifacts are never deleted or overwritten in this
 * gate — writing the same id twice with a different payload is an error, writing the
 * same id twice with the identical payload is a no-op (idempotent).
 */
interface ArtifactStore {
    suspend fun write(envelope: ArtifactEnvelope)
    suspend fun read(artifactId: ArtifactId): ArtifactEnvelope?
    suspend fun exists(artifactId: ArtifactId): Boolean
    suspend fun verify(artifactId: ArtifactId): ArtifactVerificationResult
}

@Serializable
sealed interface ArtifactVerificationResult {
    @Serializable
    data class Verified(val artifactId: ArtifactId) : ArtifactVerificationResult

    @Serializable
    data class ChecksumMismatch(val artifactId: ArtifactId, val expected: String, val actual: String) : ArtifactVerificationResult

    @Serializable
    data class NotFound(val artifactId: ArtifactId) : ArtifactVerificationResult
}

class ArtifactStoreConflictException(message: String) : IllegalStateException(message)
