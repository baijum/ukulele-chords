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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.domain.ChordDetector
import com.baijum.ukufretboard.domain.ChordInfo
import com.baijum.ukufretboard.viewmodel.FretboardViewModel
import com.baijum.ukufretboard.viewmodel.UkuleleString

/**
 * Displays the chord detection result below the fretboard.
 *
 * Adapts its content based on the detection state:
 * - **No selection**: instructional prompt to tap the fretboard.
 * - **Single note**: shows the note name with "Single note selected" label.
 * - **Interval (2 notes)**: shows both notes with "Incomplete chord" label.
 * - **Chord found**: shows chord name, quality, constituent notes, interval
 *   breakdown, formula, fingering, and difficulty.
 * - **No match**: shows "No exact chord match" with the notes played.
 *
 * @param detectionResult The current [ChordDetector.DetectionResult] to display.
 * @param fingerPositions A string like "0 - 0 - 0 - 3" showing fret positions per string.
 * @param onPlayChord Callback invoked when the user taps the play/strum button.
 * @param soundEnabled Whether sound playback is enabled. When false, the play button is hidden.
 * @param frets The raw fret list (4 values, one per string) for computing fingering
 *   and difficulty. Null when no chord is selected or frets are not available.
 * @param useFlats Whether to use flat note names for interval display.
 * @param tuning The current ukulele tuning, used for inversion detection.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
fun ChordResultView(
    detectionResult: ChordDetector.DetectionResult,
    fingerPositions: String,
    onPlayChord: () -> Unit,
    soundEnabled: Boolean = true,
    frets: List<Int>? = null,
    useFlats: Boolean = false,
    tuning: List<UkuleleString> = FretboardViewModel.STANDARD_TUNING,
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
                val result = detectionResult.result
                val formula = result.matchedFormula

                // Compute inversion if we have frets and a formula
                val inversion = if (frets != null && frets.size == 4 && formula != null) {
                    ChordInfo.determineInversion(frets, result.root.pitchClass, formula, tuning)
                } else {
                    null
                }

                // Show chord name with slash notation for inversions
                val displayName = if (inversion != null && inversion != ChordInfo.Inversion.ROOT) {
                    val bassPc = ChordInfo.bassPitchClass(frets!!, tuning)
                    ChordInfo.slashNotation(result.name, inversion, bassPc, useFlats)
                } else {
                    result.name
                }

                ChordHeadlineWithPlay(
                    text = displayName,
                    onPlay = onPlayChord,
                    showPlay = soundEnabled,
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Quality + inversion label
                val qualityText = if (inversion != null && inversion != ChordInfo.Inversion.ROOT) {
                    "${result.quality} (${inversion.label})"
                } else {
                    result.quality
                }
                Text(
                    text = qualityText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.notes.joinToString(" \u2013 ") { it.name },
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

        // Detailed chord info (only for ChordFound with a matched formula)
        if (detectionResult is ChordDetector.DetectionResult.ChordFound) {
            val result = detectionResult.result
            val formula = result.matchedFormula
            if (formula != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Intervals
                ChordInfoRow(
                    label = "Intervals",
                    value = ChordInfo.buildIntervalBreakdown(
                        root = result.root,
                        notes = result.notes,
                        useFlats = useFlats,
                    ),
                )

                // Formula
                ChordInfoRow(
                    label = "Formula",
                    value = ChordInfo.buildFormulaString(formula),
                )

                // Fingering and inversion (requires frets)
                if (frets != null && frets.size == 4) {
                    val fingering = ChordInfo.suggestFingering(frets)
                    ChordInfoRow(
                        label = "Fingering",
                        value = ChordInfo.formatFingering(fingering),
                    )

                    // Difficulty
                    ChordInfoRow(
                        label = "Difficulty",
                        value = ChordInfo.rateDifficulty(frets),
                    )

                    // Inversion
                    val detailedInversion = ChordInfo.determineInversion(
                        frets, result.root.pitchClass, formula, tuning,
                    )
                    val bassNoteName = com.baijum.ukufretboard.data.Notes.pitchClassToName(
                        ChordInfo.bassPitchClass(frets, tuning),
                        useFlats,
                    )
                    ChordInfoRow(
                        label = "Inversion",
                        value = "${detailedInversion.label} (bass: $bassNoteName)",
                    )
                }
            }
        }
    }
}

/**
 * A single row in the chord info section with a bold label and a value.
 */
@Composable
private fun ChordInfoRow(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
