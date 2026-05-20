package io.anvil.modules.forge.knight

import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState

class Knight(private val rootPath: String) : ModuleSlotContract {

    override val name = "Knight"
    override val purpose = "Datei-I/O für Runs: lesen, schreiben, Diff-Tracking."

    val reader = KnightReader(rootPath)
    val writer = KnightWriter(rootPath)

    override fun qualityState(): QualityState =
        if (rootPath.isNotBlank()) QualityState.STABLE else QualityState.FAILED
}
