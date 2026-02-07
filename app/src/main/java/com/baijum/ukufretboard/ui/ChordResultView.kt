package com.baijum.ukufretboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
 * @param onPlayChord Callback invoked when the user taps the play/strum button.
 * @param soundEnabled Whether sound playback is enabled. When false, the play button is hidden.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
fun ChordResultView(
    detectionResult: ChordDetector.DetectionResult,
    fingerPositions: String,
    onPlayChord: () -> Unit,
    soundEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val hasNotes = detectionResult !is ChordDetector.DetectionResult.NoSelection

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
                ChordHeadlineWithPlay(
                    text = detectionResult.note.name,
                    onPlay = onPlayChord,
                    showPlay = soundEnabled,
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
                ChordHeadlineWithPlay(
                    text = noteNames,
                    onPlay = onPlayChord,
                    showPlay = soundEnabled,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Incomplete chord",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is ChordDetector.DetectionResult.ChordFound -> {
                ChordHeadlineWithPlay(
                    text = detectionResult.result.name,
                    onPlay = onPlayChord,
                    showPlay = soundEnabled,
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
                ChordHeadlineWithPlay(
                    text = "No exact chord match",
                    onPlay = onPlayChord,
                    showPlay = soundEnabled,
                    isSmall = true,
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
        if (hasNotes) {
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

/**
 * Displays a chord/note headline with an optional play button to the right.
 *
 * @param text The chord or note name to display.
 * @param onPlay Callback when the play button is tapped.
 * @param showPlay Whether to show the play button (hidden when sound is disabled).
 * @param isSmall Whether to use a smaller text style (for "No match" state).
 */
@Composable
private fun ChordHeadlineWithPlay(
    text: String,
    onPlay: () -> Unit,
    showPlay: Boolean = true,
    isSmall: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = if (isSmall) MaterialTheme.typography.headlineSmall
            else MaterialTheme.typography.headlineLarge,
            color = if (isSmall) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.primary,
        )
        if (showPlay) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onPlay,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play sound",
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
