package io.anvil.surfaces.commander.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.anvil.modules.forge.knight.UnifiedDiff
import io.anvil.surfaces.commander.CommanderState

@Composable
fun DiffViewer(
    modifier: Modifier = Modifier,
    diff: UnifiedDiff?,
    selectedFile: CommanderState.SelectedFile?,
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.padding(8.dp)) {
        when {
            diff != null && !diff.isEmpty -> {
                Text("Diff", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = diff.text,
                    modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            selectedFile != null -> {
                Text(
                    text = selectedFile.path.substringAfterLast('/'),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedFile.content,
                    modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            else -> Text("No file selected", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
