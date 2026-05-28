package io.anvil.surfaces.commander

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.anvil.surfaces.commander.components.DiffViewer
import io.anvil.surfaces.commander.components.QualityBadge
import io.anvil.surfaces.commander.components.RunLog
import io.anvil.surfaces.commander.components.WorkspaceBrowser

@Composable
fun CommanderApp(
    state: CommanderState,
    onEvent: (CommanderEvent) -> Unit,
) {
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            WorkspaceBrowser(
                modifier = Modifier.weight(0.25f).fillMaxHeight(),
                workspace = state.workspace,
                files = state.files,
                onFileClick = { onEvent(CommanderEvent.SelectFile(it)) },
            )
            DiffViewer(
                modifier = Modifier.weight(0.5f).fillMaxHeight(),
                diff = state.activeDiff,
                selectedFile = state.selectedFile,
            )
            Column(modifier = Modifier.weight(0.25f).fillMaxHeight()) {
                QualityBadge(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    knightQuality = state.knightQuality,
                    bellowsQuality = state.bellowsQuality,
                )
                RunLog(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    entries = state.runLog,
                )
            }
        }
        if (state.error != null) {
            AlertDialog(
                onDismissRequest = { onEvent(CommanderEvent.DismissError) },
                title = { Text("Error") },
                text = { Text(state.error) },
                confirmButton = {
                    TextButton(onClick = { onEvent(CommanderEvent.DismissError) }) {
                        Text("OK")
                    }
                },
            )
        }
    }
}
