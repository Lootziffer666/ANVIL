package io.anvil.modules.forge.knight

import io.anvil.core.contracts.CheckpointCapable
import io.anvil.core.contracts.CheckpointData
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState
import io.anvil.core.domain.Workspace
import io.anvil.modules.forge.knight.diff.UnifiedDiff
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

class Knight(
    private val fileSystem: FileSystem,
    private val workspace: Workspace,
) : ModuleSlotContract, CheckpointCapable {

    override val name = "Knight"
    override val purpose = "File I/O + Diff layer for Anvil"

    private var _quality = QualityState.STABLE

    override fun qualityState(): QualityState = _quality

    suspend fun read(path: String): String {
        requireInScope(path)
        return try {
            fileSystem.read(path.toPath()) { readUtf8() }
                .also { _quality = QualityState.STABLE }
        } catch (e: Exception) {
            _quality = QualityState.DEGRADED; throw e
        }
    }

    suspend fun write(path: String, content: String): FileDiff {
        requireInScope(path)
        return try {
            val okioPath = path.toPath()
            val previous = if (fileSystem.exists(okioPath))
                fileSystem.read(okioPath) { readUtf8() } else ""
            fileSystem.write(okioPath) { writeUtf8(content) }
            _quality = QualityState.STABLE
            FileDiff(path = path, unified = UnifiedDiff.compute(previous, content))
        } catch (e: Exception) {
            _quality = QualityState.DEGRADED; throw e
        }
    }

    suspend fun delete(path: String) {
        requireInScope(path)
        try {
            fileSystem.delete(path.toPath())
            _quality = QualityState.STABLE
        } catch (e: Exception) {
            _quality = QualityState.DEGRADED; throw e
        }
    }

    fun diff(original: String, modified: String): String =
        UnifiedDiff.compute(original, modified)

    override suspend fun checkpoint(): CheckpointData {
        val payload = Json.encodeToString(
            KnightState(workspace.id, workspace.rootPath, _quality.name)
        )
        return CheckpointData(id = "knight-${workspace.id}", payload = payload)
    }

    override suspend fun restore(data: CheckpointData) {
        val state = Json.decodeFromString<KnightState>(data.payload)
        _quality = QualityState.valueOf(state.qualityState)
    }

    private fun requireInScope(path: String) {
        if (!path.startsWith(workspace.rootPath)) {
            _quality = QualityState.FAILED
            throw KnightScopeViolation("ANVIL Workspace Safety: Pfad außerhalb rootPath: $path")
        }
    }
}

@Serializable
private data class KnightState(
    val workspaceId: String,
    val rootPath: String,
    val qualityState: String,
)

class KnightScopeViolation(msg: String) : Exception(msg)
