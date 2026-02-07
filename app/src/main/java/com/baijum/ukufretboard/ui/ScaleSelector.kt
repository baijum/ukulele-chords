package com.baijum.ukufretboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.data.Notes
import com.baijum.ukufretboard.data.Scale
import com.baijum.ukufretboard.data.Scales
import com.baijum.ukufretboard.viewmodel.ScaleOverlayState

/**
 * Collapsible scale selector that controls the fretboard scale overlay.
 *
 * Shows a root note selector and scale type selector when expanded.
 *
 * @param state The current scale overlay state.
 * @param useFlats Whether to use flat note names.
 * @param onRootChanged Callback when the scale root note is changed.
 * @param onScaleChanged Callback when the scale type is changed.
 * @param onToggle Callback to toggle the overlay on/off.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaleSelector(
    state: ScaleOverlayState,
    useFlats: Boolean = false,
    onRootChanged: (Int) -> Unit,
    onScaleChanged: (Scale) -> Unit,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        AnimatedVisibility(visible = state.enabled) {
            Column {
                // Root note selector
                Text(
                    text = "Scale Root",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                val noteNames = if (useFlats) Notes.NOTE_NAMES_FLAT else Notes.NOTE_NAMES_SHARP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    noteNames.forEachIndexed { index, name ->
                        FilterChip(
                            selected = index == state.root,
                            onClick = { onRootChanged(index) },
                            label = { Text(name, fontWeight = if (index == state.root) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scale type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Scales.ALL.forEach { scale ->
                        FilterChip(
                            selected = state.scale == scale,
                            onClick = { onScaleChanged(scale) },
                            label = { Text(scale.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
