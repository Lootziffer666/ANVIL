package io.anvil.core.artifacts

class ArtifactWriter {
    fun write(request: ArtifactWriteRequest, currentRegistry: ArtifactRegistry = ArtifactRegistry()): ArtifactWriteResult {
        require(request.artifactRef.id.isNotBlank()) { "Artifact id is required." }
        require(request.artifactRef.moduleOrigin.isNotBlank()) { "Artifact origin module is required." }
        require(request.artifactRef.workspaceId.isNotBlank()) { "Artifact workspace is required." }
        require(request.artifactRef.runId.isNotBlank()) { "Artifact run id is required." }
        require(request.artifactRef.type.isNotBlank()) { "Artifact type is required." }
        require(request.artifactRef.sha256.isNotBlank()) { "Artifact checksum is required." }
        require(request.createdAt.isNotBlank()) { "Artifact timestamp is required." }

        val payloadChecksum = Sha256.digestPrefixed(request.payload)
        val manifest = ArtifactManifest(
            artifactId = ArtifactId(request.artifactRef.id),
            createdAt = request.createdAt,
            origin = ArtifactOrigin(
                module = request.artifactRef.moduleOrigin,
                workspaceId = WorkspaceId(request.artifactRef.workspaceId),
                runId = RunId(request.artifactRef.runId),
            ),
            type = request.artifactRef.type,
            uri = request.artifactRef.uri,
            sizeBytes = request.payload.encodeToByteArray().size,
            checksumSha256 = payloadChecksum,
            parentRefs = request.parentRefs + listOfNotNull(request.artifactRef.parentArtifactId),
        )
        val registry = currentRegistry.upsert(manifest)
        return ArtifactWriteResult(
            envelope = ArtifactEnvelope(manifest = manifest, payload = request.payload),
            registry = registry,
        )
    }

    fun validate(manifest: ArtifactManifest): List<ArtifactValidationFinding> = listOf(
        ArtifactValidationFinding("has-id", manifest.artifactId.value.isNotBlank(), "Artifact id must be present."),
        ArtifactValidationFinding("has-origin-module", manifest.origin.module.isNotBlank(), "Origin module must be present."),
        ArtifactValidationFinding("has-workspace", manifest.origin.workspaceId.value.isNotBlank(), "Workspace id must be present."),
        ArtifactValidationFinding("has-run", manifest.origin.runId.value.isNotBlank(), "Run id must be present."),
        ArtifactValidationFinding("has-type", manifest.type.isNotBlank(), "Artifact type must be present."),
        ArtifactValidationFinding("has-timestamp", manifest.createdAt.isNotBlank(), "Timestamp must be present."),
        ArtifactValidationFinding("has-checksum", isSha256(manifest.checksumSha256), "Checksum must be sha256:<64 lowercase hex chars>."),
    )

    private fun isSha256(value: String): Boolean =
        value.length == 71 && value.startsWith("sha256:") && value.drop(7).all { it in '0'..'9' || it in 'a'..'f' }

    private fun ArtifactRegistry.upsert(manifest: ArtifactManifest): ArtifactRegistry = copy(
        artifacts = artifacts.filterNot { it.artifactId == manifest.artifactId } + manifest,
    )
}
