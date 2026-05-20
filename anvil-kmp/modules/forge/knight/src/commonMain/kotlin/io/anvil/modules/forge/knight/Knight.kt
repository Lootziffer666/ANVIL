package io.anvil.modules.forge.knight

import io.anvil.core.contracts.FileIoContract
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState

class Knight(override val rootPath: String) : ModuleSlotContract, FileIoContract {

    override val name = "Knight"
    override val purpose = "Datei-I/O für Runs: lesen, schreiben, Diff-Tracking."

    val reader = KnightReader(rootPath)
    val writer = KnightWriter(rootPath)

    override fun read(relativePath: String): String = reader.read(relativePath)
    override fun exists(relativePath: String): Boolean = reader.exists(relativePath)
    override fun write(relativePath: String, content: String) { writer.write(relativePath, content) }
    override fun delete(relativePath: String) { writer.delete(relativePath) }

    override fun qualityState(): QualityState =
        if (rootPath.isNotBlank()) QualityState.STABLE else QualityState.FAILED
}
