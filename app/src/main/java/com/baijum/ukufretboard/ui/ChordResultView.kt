package com.baijum.ukufretboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.domain.ChordDetector

/**
 * Displays the chord detection result below the fretboard.
 *
 * Adapts its content based on the detection state:
 * - **No selection**: instructional prompt to tap the fretboard.
 * - **Single note**: shows the note name with "Single note selected" label.
 * - **Interval (2 notes)**: shows both notes with "Incomplete chord" label.
 * - **Chord found**: shows chord name, quality, and constituent notes.
 * - **No match**: shows "No exact chord match" with the notes played.
 *
 * @param detectionResult The current [ChordDetector.DetectionResult] to display.
 * @param fingerPositions A string like "0 - 0 - 0 - 3" showing fret positions per string.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
fun ChordResultView(
    detectionResult: ChordDetector.DetectionResult,
    fingerPositions: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (detectionResult) {
            is ChordDetector.DetectionResult.NoSelection -> {
                Text(
                    text = "Tap the fretboard to select notes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is ChordDetector.DetectionResult.SingleNote -> {
                Text(
                    text = detectionResult.note.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Single note selected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is ChordDetector.DetectionResult.Interval -> {
                val noteNames = detectionResult.notes.joinToString(" \u2013 ") { it.name }
                Text(
                    text = noteNames,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Incomplete chord",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is ChordDetector.DetectionResult.ChordFound -> {
                Text(
                    text = detectionResult.result.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detectionResult.result.quality,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detectionResult.result.notes.joinToString(" \u2013 ") { it.name },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is ChordDetector.DetectionResult.NoMatch -> {
                Text(
                    text = "No exact chord match",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detectionResult.notes.joinToString(" \u2013 ") { it.name },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Finger positions (shown for all states except NoSelection)
        if (detectionResult !is ChordDetector.DetectionResult.NoSelection) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = fingerPositions,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
