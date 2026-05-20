package io.anvil.surfaces.commander

import io.anvil.core.contracts.QualityState
import io.anvil.core.domain.Workspace
import io.anvil.modules.forge.knight.UnifiedDiff

data class CommanderState(
    val workspace: Workspace? = null,
    val files: List<String> = emptyList(),
    val selectedFile: SelectedFile? = null,
    val activeDiff: UnifiedDiff? = null,
    val runLog: List<String> = emptyList(),
    val knightQuality: QualityState = QualityState.STABLE,
    val bellowsQuality: QualityState = QualityState.STABLE,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    data class SelectedFile(val path: String, val content: String)
}
