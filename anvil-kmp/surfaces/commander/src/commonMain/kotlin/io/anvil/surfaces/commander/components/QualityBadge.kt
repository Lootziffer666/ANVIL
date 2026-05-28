package io.anvil.surfaces.commander.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.anvil.core.contracts.QualityState

@Composable
fun QualityBadge(
    modifier: Modifier = Modifier,
    knightQuality: QualityState,
    bellowsQuality: QualityState,
) {
    Row(modifier = modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QualityChip(label = "Knight", state = knightQuality)
        QualityChip(label = "Bellows", state = bellowsQuality)
    }
}

@Composable
private fun QualityChip(label: String, state: QualityState) {
    val color = when (state) {
        QualityState.STABLE   -> Color(0xFF4CAF50)
        QualityState.DEGRADED -> Color(0xFFFF9800)
        QualityState.BLOCKED  -> Color(0xFF9E9E9E)
        QualityState.FAILED   -> Color(0xFFF44336)
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            text = "$label: ${state.name}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
