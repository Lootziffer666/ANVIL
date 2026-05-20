package io.anvil.surfaces.commander.components

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

@Composable
fun RunLog(
    modifier: Modifier = Modifier,
    entries: List<String>,
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text("Run Log", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        if (entries.isEmpty()) {
            Text("No entries", style = MaterialTheme.typography.bodySmall)
        } else {
            LazyColumn {
                items(entries) { entry ->
                    Text(
                        text = entry,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
