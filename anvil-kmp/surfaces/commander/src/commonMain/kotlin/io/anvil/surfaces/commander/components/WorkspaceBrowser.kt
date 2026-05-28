package io.anvil.surfaces.commander.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.anvil.core.domain.Workspace

@Composable
fun WorkspaceBrowser(
    modifier: Modifier = Modifier,
    workspace: Workspace?,
    files: List<String>,
    onFileClick: (String) -> Unit,
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = workspace?.name ?: "No workspace",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(4.dp))
        if (files.isEmpty()) {
            Text("No files", style = MaterialTheme.typography.bodySmall)
        } else {
            LazyColumn {
                items(files) { path ->
                    Text(
                        text = path.substringAfterLast('/'),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFileClick(path) }
                            .padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
