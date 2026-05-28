package io.anvil.surfaces.commander

import io.anvil.core.domain.Workspace
import io.anvil.modules.forge.knight.KnightContract
import io.anvil.modules.forge.knight.UnifiedDiff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommanderViewModel(
    private val knight: KnightContract,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(CommanderState())
    val state: StateFlow<CommanderState> = _state.asStateFlow()

    fun onEvent(event: CommanderEvent) {
        when (event) {
            is CommanderEvent.OpenWorkspace -> openWorkspace(event.rootPath)
            is CommanderEvent.LoadFiles -> _state.update { it.copy(files = event.files) }
            is CommanderEvent.SelectFile -> selectFile(event.path)
            is CommanderEvent.WriteFile -> writeFile(event.path, event.content)
            is CommanderEvent.ApplyPatch -> applyPatch(event.path, event.diff)
            CommanderEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun openWorkspace(rootPath: String) {
        val workspace = Workspace(
            id = "local",
            name = rootPath.substringAfterLast('/').ifEmpty { rootPath },
            rootPath = rootPath,
        )
        _state.update {
            it.copy(
                workspace = workspace,
                files = emptyList(),
                selectedFile = null,
                activeDiff = null,
                error = null,
            )
        }
    }

    private fun selectFile(path: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            try {
                val fc = knight.readFile(path)
                _state.update { s ->
                    s.copy(
                        isLoading = false,
                        selectedFile = CommanderState.SelectedFile(fc.path, fc.content),
                        activeDiff = null,
                        knightQuality = knight.qualityState(),
                    )
                }
            } catch (e: Exception) {
                _state.update { s ->
                    s.copy(isLoading = false, error = e.message, knightQuality = knight.qualityState())
                }
            }
        }
    }

    private fun writeFile(path: String, content: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            try {
                val result = knight.writeFile(path, content)
                _state.update { s ->
                    s.copy(
                        isLoading = false,
                        selectedFile = CommanderState.SelectedFile(result.path, content),
                        activeDiff = result.diff,
                        runLog = s.runLog + "Written: ${result.path}",
                        knightQuality = knight.qualityState(),
                    )
                }
            } catch (e: Exception) {
                _state.update { s ->
                    s.copy(isLoading = false, error = e.message, knightQuality = knight.qualityState())
                }
            }
        }
    }

    private fun applyPatch(path: String, diff: UnifiedDiff) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            try {
                val result = knight.applyPatch(path, diff)
                val entry = if (result.success)
                    "Patch applied: ${result.path} (${result.appliedHunks} hunks)"
                else
                    "Partial patch: ${result.path} (${result.appliedHunks} ok, ${result.rejectedHunks} rejected)"
                _state.update { s ->
                    s.copy(
                        isLoading = false,
                        activeDiff = null,
                        runLog = s.runLog + entry,
                        knightQuality = knight.qualityState(),
                    )
                }
            } catch (e: Exception) {
                _state.update { s ->
                    s.copy(isLoading = false, error = e.message, knightQuality = knight.qualityState())
                }
            }
        }
    }
}
