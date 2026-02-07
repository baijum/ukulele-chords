package com.example.ukufretboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ukufretboard.domain.Note
import com.example.ukufretboard.viewmodel.FretboardViewModel
import com.example.ukufretboard.viewmodel.UkuleleString

/** Width of each fret cell — large enough for comfortable touch targets. */
private val CELL_WIDTH = 48.dp

/** Height of each fret cell — one per string row. */
private val CELL_HEIGHT = 48.dp

/** Width of the fixed string label column on the left. */
private val LABEL_WIDTH = 36.dp

/** Height of the fret number row at the top. */
private val FRET_NUMBER_HEIGHT = 24.dp

/** Height of the fret marker row (position dots). */
private val MARKER_HEIGHT = 16.dp

/** Size of the dot indicator for fret position markers (5, 7, 10, 12). */
private val MARKER_DOT_SIZE = 6.dp

/** Size of the filled circle for a selected fret position. */
private val SELECTED_DOT_SIZE = 36.dp

/** Size of the outlined circle for an open string indicator. */
private val OPEN_STRING_DOT_SIZE = 28.dp

/** Warm wood-tone background for the fretboard area. */
private val FretboardBackground = Color(0xFFF5E6D3)

/** Fret numbers where traditional position markers (dots) appear. */
private val SINGLE_MARKER_FRETS = setOf(5, 7, 10)

/** Fret number where a double position marker appears. */
private const val DOUBLE_MARKER_FRET = 12

/**
 * Interactive ukulele fretboard rendered as a horizontally scrollable grid.
 *
 * Layout structure:
 * - Fixed left column: string labels (G, C, E, A)
 * - Scrollable right section: fret numbers, position markers, and fret cells
 *
 * Each cell in the grid corresponds to a string/fret intersection and can be
 * tapped to toggle note selection for chord detection.
 *
 * @param tuning The current ukulele tuning (list of strings with their open pitch classes).
 * @param selections Map of string index to selected fret (null = no selection on that string).
 * @param showNoteNames Whether to display note names inside fret cells.
 * @param onFretTap Callback invoked when a fret cell is tapped, with (stringIndex, fret).
 * @param getNoteAt Function to compute the [Note] at a given string/fret position.
 * @param modifier Optional [Modifier] for the root layout.
 */
@Composable
fun FretboardView(
    tuning: List<UkuleleString>,
    selections: Map<Int, Int?>,
    showNoteNames: Boolean,
    onFretTap: (stringIndex: Int, fret: Int) -> Unit,
    getNoteAt: (stringIndex: Int, fret: Int) -> Note,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(modifier = modifier.padding(start = 4.dp)) {
        // Fixed string labels column (does not scroll)
        Column {
            // Spacer aligned with fret numbers row
            Spacer(modifier = Modifier.height(FRET_NUMBER_HEIGHT))
            // Spacer aligned with marker row
            Spacer(modifier = Modifier.height(MARKER_HEIGHT))
            // One label per string
            tuning.forEach { string ->
                Box(
                    modifier = Modifier
                        .width(LABEL_WIDTH)
                        .height(CELL_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = string.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Horizontally scrollable fretboard area
        Column(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .background(FretboardBackground, RoundedCornerShape(8.dp))
                .padding(end = 4.dp),
        ) {
            // Fret numbers row
            FretNumbersRow()

            // Position markers row (dots at frets 5, 7, 10, 12)
            FretMarkersRow()

            // String rows with fret cells
            tuning.forEachIndexed { stringIndex, _ ->
                Row {
                    repeat(FretboardViewModel.FRET_COUNT) { fret ->
                        FretCell(
                            note = getNoteAt(stringIndex, fret),
                            isSelected = selections[stringIndex] == fret,
                            isOpenString = fret == FretboardViewModel.OPEN_STRING_FRET,
                            showNoteNames = showNoteNames,
                            onClick = { onFretTap(stringIndex, fret) },
                            modifier = Modifier.size(CELL_WIDTH, CELL_HEIGHT),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Row of fret numbers (0–12) displayed above the fretboard grid.
 */
@Composable
private fun FretNumbersRow() {
    Row {
        repeat(FretboardViewModel.FRET_COUNT) { fret ->
            Box(
                modifier = Modifier
                    .width(CELL_WIDTH)
                    .height(FRET_NUMBER_HEIGHT),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = fret.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Row of position markers — small dots at traditional fretboard positions.
 *
 * Single dots appear at frets 5, 7, and 10.
 * A double dot appears at fret 12 (the octave).
 */
@Composable
private fun FretMarkersRow() {
    val markerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Row {
        repeat(FretboardViewModel.FRET_COUNT) { fret ->
            Box(
                modifier = Modifier
                    .width(CELL_WIDTH)
                    .height(MARKER_HEIGHT),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    fret in SINGLE_MARKER_FRETS -> {
                        Box(
                            modifier = Modifier
                                .size(MARKER_DOT_SIZE)
                                .background(markerColor, CircleShape),
                        )
                    }
                    fret == DOUBLE_MARKER_FRET -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(MARKER_DOT_SIZE)
                                    .background(markerColor, CircleShape),
                            )
                            Box(
                                modifier = Modifier
                                    .size(MARKER_DOT_SIZE)
                                    .background(markerColor, CircleShape),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single tappable cell on the fretboard grid.
 *
 * Visual states:
 * - **Selected**: large filled circle in primary color, with optional note name.
 * - **Open string (fret 0, unselected)**: outlined circle indicating the open string.
 * - **Unselected fretted position**: subtle note name text (if enabled) or empty.
 *
 * Draws the string (horizontal line) and fret wire (vertical line) behind the dot
 * to create the fretboard grid appearance. The fret-0 right edge is drawn thicker
 * to represent the nut.
 *
 * @param note The [Note] at this string/fret position.
 * @param isSelected Whether this fret is currently selected on its string.
 * @param isOpenString Whether this is fret 0 (open string position).
 * @param showNoteNames Whether to display the note name text.
 * @param onClick Callback invoked when this cell is tapped.
 * @param modifier Optional [Modifier] for sizing.
 */
@Composable
private fun FretCell(
    note: Note,
    isSelected: Boolean,
    isOpenString: Boolean,
    showNoteNames: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val outlineColor = MaterialTheme.colorScheme.outline
    val stringColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val fretColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    val nutColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .drawBehind {
                // Draw string — horizontal line through the vertical center
                drawLine(
                    color = stringColor,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2f,
                )
                // Draw fret wire on right edge; nut (fret 0) is thicker
                drawLine(
                    color = if (isOpenString) nutColor else fretColor,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = if (isOpenString) 4f else 1.5f,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        when {
            isSelected -> {
                // Filled circle for selected fret position
                Box(
                    modifier = Modifier
                        .size(SELECTED_DOT_SIZE)
                        .background(primaryColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showNoteNames) {
                        Text(
                            text = note.name,
                            color = onPrimaryColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            isOpenString -> {
                // Outlined circle for open string
                Box(
                    modifier = Modifier
                        .size(OPEN_STRING_DOT_SIZE)
                        .border(2.dp, outlineColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showNoteNames) {
                        Text(
                            text = note.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = outlineColor,
                        )
                    }
                }
            }
            else -> {
                // Subtle note name for unselected fretted positions
                if (showNoteNames) {
                    Text(
                        text = note.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = outlineColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
