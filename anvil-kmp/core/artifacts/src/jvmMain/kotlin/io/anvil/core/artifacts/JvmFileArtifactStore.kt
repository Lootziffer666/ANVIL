package io.anvil.core.artifacts

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * JVM filesystem-backed [ArtifactStore]. Every artifact is written as `<artifactRoot>/<id>.json`
 * (manifest + payload as [ArtifactEnvelope] JSON). Writes are atomic (temp file + move) and
 * every write is followed by a checksum re-read to catch silent filesystem corruption or
 * truncation.
 */
class JvmFileArtifactStore(
    private val artifactRoot: File,
    private val json: Json = Json { prettyPrint = true; encodeDefaults = true },
) : ArtifactStore {
    private fun envelopeJson(envelope: ArtifactEnvelope): String = json.encodeToString(envelope)
    private fun envelopeFromJson(text: String): ArtifactEnvelope = json.decodeFromString(text)

    init {
        artifactRoot.mkdirs()
        require(artifactRoot.exists() && artifactRoot.isDirectory) { "artifactRoot must be a directory: $artifactRoot" }
    }

    override suspend fun write(envelope: ArtifactEnvelope) {
        val recomputed = Sha256.digestPrefixed(envelope.payload)
        require(recomputed == envelope.manifest.checksumSha256) {
            "Artifact '${envelope.manifest.artifactId.value}' checksum mismatch: manifest declares " +
                "${envelope.manifest.checksumSha256} but payload hashes to $recomputed."
        }

        val target = resolveWithinRoot(envelope.manifest.artifactId)
        if (target.exists()) {
            val existing = envelopeFromJson(target.readText())
            if (existing.payload == envelope.payload) return // idempotent
            throw ArtifactStoreConflictException(
                "Artifact '${envelope.manifest.artifactId.value}' already exists at $target with a different payload. Artifacts are immutable.",
            )
        }

        val tempFile = File.createTempFile("artifact-", ".tmp", artifactRoot)
        try {
            tempFile.writeText(envelopeJson(envelope))
            Files.move(tempFile.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } finally {
            tempFile.delete() // no-op if already moved
        }

        // Post-write integrity check: re-read from disk and re-verify the checksum.
        val writtenBack = envelopeFromJson(target.readText())
        val writtenChecksum = Sha256.digestPrefixed(writtenBack.payload)
        if (writtenChecksum != envelope.manifest.checksumSha256) {
            target.delete()
            error("Post-write verification failed for artifact '${envelope.manifest.artifactId.value}': disk content does not match expected checksum.")
        }
    }

    override suspend fun read(artifactId: ArtifactId): ArtifactEnvelope? {
        val target = resolveWithinRoot(artifactId)
        if (!target.exists()) return null
        return envelopeFromJson(target.readText())
    }

    override suspend fun exists(artifactId: ArtifactId): Boolean = resolveWithinRoot(artifactId).exists()

    override suspend fun verify(artifactId: ArtifactId): ArtifactVerificationResult {
        val envelope = read(artifactId) ?: return ArtifactVerificationResult.NotFound(artifactId)
        val actual = Sha256.digestPrefixed(envelope.payload)
        return if (actual == envelope.manifest.checksumSha256) {
            ArtifactVerificationResult.Verified(artifactId)
        } else {
            ArtifactVerificationResult.ChecksumMismatch(artifactId, envelope.manifest.checksumSha256, actual)
        }
    }

    /** Resolves [artifactId] to a file strictly inside [artifactRoot], rejecting any path or symlink escape. */
    private fun resolveWithinRoot(artifactId: ArtifactId): File {
        require(artifactId.value.isNotBlank()) { "Artifact id must not be blank." }
        require(SAFE_ID.matches(artifactId.value)) {
            "Artifact id '${artifactId.value}' contains characters that are not safe for a filesystem path."
        }
        val rootCanonical = artifactRoot.canonicalFile
        val candidate = File(rootCanonical, "${artifactId.value}.json")
        val candidateCanonicalParent = candidate.parentFile.canonicalFile
        if (candidateCanonicalParent != rootCanonical) {
            throw IllegalArgumentException("Artifact id '${artifactId.value}' would escape artifactRoot.")
        }
        return File(rootCanonical, "${artifactId.value}.json")
    }

    private companion object {
        val SAFE_ID = Regex("^[A-Za-z0-9_-]+$")
    }
}
