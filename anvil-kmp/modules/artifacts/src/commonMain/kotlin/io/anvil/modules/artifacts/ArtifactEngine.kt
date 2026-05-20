package io.anvil.modules.artifacts

import io.anvil.core.contracts.FileIoContract
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState
import io.anvil.core.domain.Artifact
import io.anvil.core.domain.ArtifactId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

class ArtifactEngine(
    private val fileIo: FileIoContract,
) : ModuleSlotContract {

    override val name = "ArtifactEngine"
    override val purpose = "Schreibt Artifacts mit Manifest auf Disk (ARTIFACT_OUTPUT_LAYER.md)."

    fun store(artifact: Artifact, content: String): ArtifactId {
        val dir = "artifacts/${artifact.artifactId.value}"
        fileIo.write("$dir/${artifact.filename}", content)
        fileIo.write("$dir/manifest.json", json.encodeToString(artifact))
        return artifact.artifactId
    }

    fun exists(artifactId: ArtifactId): Boolean =
        fileIo.exists("artifacts/${artifactId.value}")

    override fun qualityState(): QualityState =
        if (fileIo.rootPath.isNotBlank()) QualityState.STABLE else QualityState.FAILED
}
