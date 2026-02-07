package com.baijum.ukufretboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baijum.ukufretboard.domain.ChordVoicing

/** Minimum number of fret columns so diagrams don't look too narrow. */
private const val MIN_FRET_COLUMNS = 3

/** Width of each fret column in the diagram. */
private val FRET_COL_WIDTH = 24.dp

/** Height of each string row in the diagram. */
private val STRING_ROW_HEIGHT = 22.dp

/** Size of the dot indicating a fretted position. */
private val DOT_SIZE = 14.dp

/** Size of the open-string circle indicator. */
private val OPEN_CIRCLE_SIZE = 10.dp

/** Width for the left label area (fret number or open indicator). */
private val LABEL_WIDTH = 20.dp

/**
 * A compact, tappable chord diagram card showing a single [ChordVoicing].
 *
 * Displays a mini fretboard with 4 strings and a dynamically sized number
 * of fret columns — just enough to fit every dot with no wasted space.
 * Dots at the fretted positions, and open string indicators are shown.
 * A start fret label (e.g., "3fr") appears when the voicing is above the nut.
 *
 * @param voicing The [ChordVoicing] to display.
 * @param onClick Callback invoked when the card is tapped.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
fun ChordDiagramPreview(
    voicing: ChordVoicing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Determine the fret range to display dynamically
    val startFret: Int
    val columnCount: Int

    if (voicing.maxFret == 0) {
        // All open strings — show a small diagram at the nut
        startFret = 0
        columnCount = MIN_FRET_COLUMNS
    } else if (voicing.maxFret <= MIN_FRET_COLUMNS) {
        // Fits near the nut starting from 0
        startFret = 0
        columnCount = maxOf(MIN_FRET_COLUMNS, voicing.maxFret + 1)
    } else {
        // Higher up the neck — start from the lowest fretted position
        startFret = maxOf(1, voicing.minFret)
        columnCount = maxOf(MIN_FRET_COLUMNS, voicing.maxFret - startFret + 1)
    }
    val isAtNut = startFret == 0

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Fret numbers header
            FretNumbersHeader(startFret = startFret, columnCount = columnCount)

            // Diagram body: strings with dots
            DiagramBody(
                voicing = voicing,
                startFret = startFret,
                columnCount = columnCount,
                isAtNut = isAtNut,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Note names below
            Text(
                text = voicing.notes.joinToString(" ") { it.name },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Header row showing fret numbers above the diagram columns.
 */
@Composable
private fun FretNumbersHeader(startFret: Int, columnCount: Int) {
    Row(modifier = Modifier.padding(start = LABEL_WIDTH)) {
        repeat(columnCount) { col ->
            val fretNum = startFret + col
            Box(
                modifier = Modifier
                    .width(FRET_COL_WIDTH)
                    .height(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$fretNum",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/**
 * The main diagram body: 4 string rows with fret grid lines and position dots.
 *
 * The number of fret columns is sized dynamically so every fretted position
 * is visible and no empty columns are wasted.
 */
@Composable
private fun DiagramBody(
    voicing: ChordVoicing,
    startFret: Int,
    columnCount: Int,
    isAtNut: Boolean,
) {
    val stringColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val fretColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val nutColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val dotColor = MaterialTheme.colorScheme.primary
    val openColor = MaterialTheme.colorScheme.outline

    Column {
        voicing.frets.forEachIndexed { stringIndex, fret ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Left label: open string indicator or start fret
                Box(
                    modifier = Modifier
                        .width(LABEL_WIDTH)
                        .height(STRING_ROW_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    if (fret == 0) {
                        // Open string circle
                        Box(
                            modifier = Modifier
                                .size(OPEN_CIRCLE_SIZE)
                                .drawBehind {
                                    drawCircle(
                                        color = openColor,
                                        radius = size.minDimension / 2,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 1.5.dp.toPx()
                                        ),
                                    )
                                },
                        )
                    } else if (stringIndex == 0 && !isAtNut) {
                        Text(
                            text = "${startFret}fr",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Fret cells
                repeat(columnCount) { col ->
                    val diagramFret = startFret + col
                    val isNutColumn = isAtNut && col == 0

                    Box(
                        modifier = Modifier
                            .width(FRET_COL_WIDTH)
                            .height(STRING_ROW_HEIGHT)
                            .drawBehind {
                                // String line
                                drawLine(
                                    color = stringColor,
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    strokeWidth = 1.5f,
                                )
                                // Fret wire on right edge
                                drawLine(
                                    color = if (isNutColumn) nutColor else fretColor,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = if (isNutColumn) 3f else 1f,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (fret == diagramFret && fret > 0) {
                            // Filled dot for fretted position
                            Box(
                                modifier = Modifier
                                    .size(DOT_SIZE)
                                    .background(dotColor, CircleShape),
                            )
                        }
                    }
                }
            }
        }
    }
}
