package io.anvil.core.artifacts

/** commonMain reference implementation of [ArtifactStore], backed by an in-memory map. Used by tests and the Golden Run. */
class InMemoryArtifactStore : ArtifactStore {
    private val envelopes = mutableMapOf<String, ArtifactEnvelope>()

    override suspend fun write(envelope: ArtifactEnvelope) {
        val id = envelope.manifest.artifactId.value
        val recomputed = Sha256.digestPrefixed(envelope.payload)
        require(recomputed == envelope.manifest.checksumSha256) {
            "Artifact '$id' checksum mismatch: manifest declares ${envelope.manifest.checksumSha256} but payload hashes to $recomputed."
        }
        val existing = envelopes[id]
        if (existing != null) {
            if (existing.payload == envelope.payload) return // idempotent re-write of identical payload
            throw ArtifactStoreConflictException(
                "Artifact '$id' already exists with a different payload. Artifacts are immutable.",
            )
        }
        envelopes[id] = envelope
    }

    override suspend fun read(artifactId: ArtifactId): ArtifactEnvelope? = envelopes[artifactId.value]

    override suspend fun exists(artifactId: ArtifactId): Boolean = artifactId.value in envelopes

    override suspend fun verify(artifactId: ArtifactId): ArtifactVerificationResult {
        val envelope = envelopes[artifactId.value] ?: return ArtifactVerificationResult.NotFound(artifactId)
        val actual = Sha256.digestPrefixed(envelope.payload)
        return if (actual == envelope.manifest.checksumSha256) {
            ArtifactVerificationResult.Verified(artifactId)
        } else {
            ArtifactVerificationResult.ChecksumMismatch(artifactId, envelope.manifest.checksumSha256, actual)
        }
    }
}
