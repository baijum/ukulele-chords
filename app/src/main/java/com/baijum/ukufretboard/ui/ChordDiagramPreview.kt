package com.baijum.ukufretboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baijum.ukufretboard.domain.ChordVoicing

/** Minimum number of fret columns to match the standard chord diagram convention. */
private const val MIN_FRET_COLUMNS = 5

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
 * @param inversionLabel Optional inversion label (e.g., "Root", "1st Inv") displayed below the note names.
 * @param bassStringIndex Optional index (0–3) of the bass note string, used to highlight it.
 * @param commonToneIndices Optional set of string indices (0–3) to highlight as common tones
 *   (used in voice leading view to show fingers that don't move between chords).
 * @param capoFret Optional capo fret position (1–11). When set, a thick bar is drawn across
 *   all strings at this fret position to visually represent a capo.
 * @param soundingNotes Optional note names to show below the diagram when a capo shifts pitch.
 * @param modifier Optional [Modifier] for layout customization.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChordDiagramPreview(
    voicing: ChordVoicing,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    leftHanded: Boolean = false,
    inversionLabel: String? = null,
    bassStringIndex: Int? = null,
    commonToneIndices: Set<Int>? = null,
    capoFret: Int? = null,
    soundingNotes: String? = null,
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
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
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
            FretNumbersHeader(
                startFret = startFret,
                columnCount = columnCount,
                leftHanded = leftHanded,
            )

            // Diagram body: strings with dots
            DiagramBody(
                voicing = voicing,
                startFret = startFret,
                columnCount = columnCount,
                isAtNut = isAtNut,
                leftHanded = leftHanded,
                bassStringIndex = bassStringIndex,
                commonToneIndices = commonToneIndices,
                capoFret = capoFret,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Note names below (show sounding notes if capo is active)
            Text(
                text = soundingNotes ?: voicing.notes.joinToString(" ") { it?.name ?: "x" },
                style = MaterialTheme.typography.labelSmall,
                color = if (soundingNotes != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Inversion label
            if (inversionLabel != null) {
                Text(
                    text = inversionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Header row showing fret numbers above the diagram columns.
 */
@Composable
private fun FretNumbersHeader(startFret: Int, columnCount: Int, leftHanded: Boolean = false) {
    val colRange = if (leftHanded) (columnCount - 1 downTo 0) else (0 until columnCount)
    Row(modifier = Modifier.padding(start = LABEL_WIDTH)) {
        colRange.forEach { col ->
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
 *
 * @param bassStringIndex Optional index of the bass note string. When provided,
 *   the bass note dot is drawn with a distinct outline to visually distinguish it.
 * @param commonToneIndices Optional set of string indices to highlight as common tones.
 *   When provided, dots on these strings are drawn in a distinct color to indicate
 *   fingers that stay in place during a voice leading transition.
 * @param capoFret Optional capo fret position. When set, a thick rounded bar is drawn
 *   across all strings at this fret column.
 */
@Composable
private fun DiagramBody(
    voicing: ChordVoicing,
    startFret: Int,
    columnCount: Int,
    isAtNut: Boolean,
    leftHanded: Boolean = false,
    bassStringIndex: Int? = null,
    commonToneIndices: Set<Int>? = null,
    capoFret: Int? = null,
) {
    val stringColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val fretColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val nutColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val dotColor = MaterialTheme.colorScheme.primary
    val bassDotColor = MaterialTheme.colorScheme.tertiary
    val commonToneColor = MaterialTheme.colorScheme.secondary
    val openColor = MaterialTheme.colorScheme.outline
    val capoColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)

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
                    if (fret == ChordVoicing.MUTED) {
                        // Muted string: draw "X" indicator
                        Box(
                            modifier = Modifier
                                .size(OPEN_CIRCLE_SIZE)
                                .drawBehind {
                                    val half = size.minDimension / 2 * 0.7f
                                    val cx = size.width / 2
                                    val cy = size.height / 2
                                    val strokeW = 1.5.dp.toPx()
                                    drawLine(
                                        color = openColor,
                                        start = Offset(cx - half, cy - half),
                                        end = Offset(cx + half, cy + half),
                                        strokeWidth = strokeW,
                                    )
                                    drawLine(
                                        color = openColor,
                                        start = Offset(cx - half, cy + half),
                                        end = Offset(cx + half, cy - half),
                                        strokeWidth = strokeW,
                                    )
                                },
                        )
                    } else if (fret == 0) {
                        // Open string circle — highlighted for common tones
                        val isCommonOpen = commonToneIndices?.contains(stringIndex) == true
                        Box(
                            modifier = Modifier
                                .size(OPEN_CIRCLE_SIZE)
                                .drawBehind {
                                    drawCircle(
                                        color = if (isCommonOpen) commonToneColor else openColor,
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
                val colRange = if (leftHanded) (columnCount - 1 downTo 0) else (0 until columnCount)
                colRange.forEach { col ->
                    val diagramFret = startFret + col
                    val isNutColumn = isAtNut && diagramFret == 0

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
                                if (isNutColumn) {
                                    // Nut: thick line on the side toward the fretted notes
                                    val nutX = if (leftHanded) 0f else size.width
                                    drawLine(
                                        color = nutColor,
                                        start = Offset(nutX, 0f),
                                        end = Offset(nutX, size.height),
                                        strokeWidth = 3f,
                                    )
                                } else {
                                    // Regular fret wire on right edge
                                    drawLine(
                                        color = fretColor,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1f,
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (capoFret != null && diagramFret == capoFret) {
                            // Capo bar: thick rounded rectangle spanning the full cell height
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(STRING_ROW_HEIGHT)
                                    .background(
                                        capoColor,
                                        RoundedCornerShape(3.dp),
                                    ),
                            )
                        } else if (fret == diagramFret && fret > 0) {
                            // Filled dot — colored by role
                            val isCommon = commonToneIndices?.contains(stringIndex) == true
                            val isBass = bassStringIndex == stringIndex
                            val filledColor = when {
                                isCommon -> commonToneColor
                                isBass -> bassDotColor
                                else -> dotColor
                            }
                            Box(
                                modifier = Modifier
                                    .size(DOT_SIZE)
                                    .background(filledColor, CircleShape),
                            )
                        }
                    }
                }
            }
        }
    }
}
