package com.baijum.ukufretboard.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.data.ChordDegree
import com.baijum.ukufretboard.data.Notes
import com.baijum.ukufretboard.data.Progression
import com.baijum.ukufretboard.data.Progressions
import com.baijum.ukufretboard.data.ScaleType

/**
 * Tab showing common chord progressions for a selected key.
 *
 * Users select a key (root note) and scale (Major/Minor), then browse
 * common progressions. Tapping a chord chip calls [onChordTapped] with
 * the pitch class and quality so the caller can navigate to the fretboard.
 *
 * @param useFlats Whether to display note names using flats.
 * @param onChordTapped Callback with (rootPitchClass, qualitySymbol) when a chord is tapped.
 * @param modifier Optional modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressionsTab(
    useFlats: Boolean = false,
    onChordTapped: (rootPitchClass: Int, quality: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedRoot by remember { mutableIntStateOf(0) } // C
    var selectedScale by remember { mutableStateOf(ScaleType.MAJOR) }

    val progressions = Progressions.forScale(selectedScale)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp),
    ) {
        // Key selector
        Text(
            text = "Key",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
        )

        val noteNames = if (useFlats) Notes.NOTE_NAMES_FLAT else Notes.NOTE_NAMES_SHARP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            noteNames.forEachIndexed { index, name ->
                FilterChip(
                    selected = index == selectedRoot,
                    onClick = { selectedRoot = index },
                    label = {
                        Text(
                            text = name,
                            fontWeight = if (index == selectedRoot) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scale toggle
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ScaleType.entries.forEach { scale ->
                FilterChip(
                    selected = selectedScale == scale,
                    onClick = { selectedScale = scale },
                    label = { Text(scale.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progression cards
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(progressions) { progression ->
                ProgressionCard(
                    progression = progression,
                    keyRoot = selectedRoot,
                    useFlats = useFlats,
                    onChordTapped = onChordTapped,
                )
            }
        }
    }
}

/**
 * A card displaying a single chord progression.
 */
@Composable
private fun ProgressionCard(
    progression: Progression,
    keyRoot: Int,
    useFlats: Boolean,
    onChordTapped: (Int, String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Name
            Text(
                text = progression.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Numeral notation
            Text(
                text = progression.degrees.joinToString(" – ") { it.numeral },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Resolved chord chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                progression.degrees.forEach { degree ->
                    val chordRoot = (keyRoot + degree.interval) % Notes.PITCH_CLASS_COUNT
                    val chordName = Notes.pitchClassToName(chordRoot, useFlats) + degree.quality

                    SuggestionChip(
                        onClick = { onChordTapped(chordRoot, degree.quality) },
                        label = {
                            Text(
                                text = chordName,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = progression.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
