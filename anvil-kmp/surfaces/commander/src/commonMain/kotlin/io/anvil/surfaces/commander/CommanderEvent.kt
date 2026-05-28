package io.anvil.surfaces.commander

import io.anvil.modules.forge.knight.UnifiedDiff

sealed class CommanderEvent {
    data class OpenWorkspace(val rootPath: String) : CommanderEvent()
    data class LoadFiles(val files: List<String>) : CommanderEvent()
    data class SelectFile(val path: String) : CommanderEvent()
    data class WriteFile(val path: String, val content: String) : CommanderEvent()
    data class ApplyPatch(val path: String, val diff: UnifiedDiff) : CommanderEvent()
    data object DismissError : CommanderEvent()
}
