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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.domain.CapoCalculator

/**
 * Displays capo calculator results for a single chord.
 *
 * Shows a ranked list of capo positions with the resulting chord shape,
 * a mini diagram of the easiest voicing, and a playability score bar.
 *
 * @param results List of [CapoCalculator.SingleChordResult] sorted by score.
 * @param onBack Callback to return to the previous view.
 * @param leftHanded Whether to mirror diagrams for left-handed players.
 * @param modifier Optional modifier.
 */
@Composable
fun CapoCalculatorSingleView(
    results: List<CapoCalculator.SingleChordResult>,
    onBack: () -> Unit,
    leftHanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("No capo positions available for this chord.")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        return
    }

    val bestScore = results.first().score

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Capo Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Sounding: ${results.first().soundingName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Tip
        Text(
            text = "Place a capo to play simpler shapes that sound the same.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        // Results list
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(results) { index, result ->
                CapoResultCard(
                    result = result,
                    isRecommended = index == 0,
                    maxScore = bestScore,
                    leftHanded = leftHanded,
                )
            }
        }
    }
}

/**
 * Displays capo calculator results for a full progression.
 *
 * Each capo position shows all chord shapes in the progression,
 * with a combined score and mini diagrams.
 *
 * @param results List of [CapoCalculator.ProgressionResult] sorted by total score.
 * @param onBack Callback to return to the previous view.
 * @param leftHanded Whether to mirror diagrams for left-handed players.
 * @param modifier Optional modifier.
 */
@Composable
fun CapoCalculatorProgressionView(
    results: List<CapoCalculator.ProgressionResult>,
    onBack: () -> Unit,
    leftHanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("No capo positions available for this progression.")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        return
    }

    val bestTotal = results.first().totalScore

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Capo Helper",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = "Find the easiest capo position for this progression.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        // Results list
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(results) { index, result ->
                CapoProgressionResultCard(
                    result = result,
                    isRecommended = index == 0,
                    maxScore = bestTotal,
                    leftHanded = leftHanded,
                )
            }
        }
    }
}

/**
 * A card showing a single capo position result for one chord.
 */
@Composable
private fun CapoResultCard(
    result: CapoCalculator.SingleChordResult,
    isRecommended: Boolean,
    maxScore: Int,
    leftHanded: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRecommended) 2.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Capo fret label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp),
            ) {
                Text(
                    text = if (result.capoFret == 0) "No" else "Fret",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (result.capoFret == 0) "capo" else "${result.capoFret}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecommended)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Shape info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Play: ${result.shapeName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (isRecommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recommended",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    text = "Frets: ${result.bestVoicing.frets.joinToString(" ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Simple score indicator
                val ease = if (maxScore > 0) (result.score * 100 / maxScore) else 0
                Text(
                    text = "Ease: $ease%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Mini diagram
            VerticalChordDiagram(
                voicing = result.bestVoicing,
                onClick = {},
                leftHanded = leftHanded,
                modifier = Modifier.width(120.dp),
            )
        }
    }
}

/**
 * A card showing a single capo position result for a full progression.
 */
@Composable
private fun CapoProgressionResultCard(
    result: CapoCalculator.ProgressionResult,
    isRecommended: Boolean,
    maxScore: Int,
    leftHanded: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRecommended) 2.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Header row: capo fret + score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(56.dp),
                ) {
                    Text(
                        text = if (result.capoFret == 0) "No" else "Fret",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (result.capoFret == 0) "capo" else "${result.capoFret}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecommended)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chord shapes summary
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Shapes: ${result.chordResults.joinToString(" \u2013 ") { it.shapeName }}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (isRecommended) {
                        Text(
                            text = "Recommended",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    val ease = if (maxScore > 0) (result.totalScore * 100 / maxScore) else 0
                    Text(
                        text = "Overall ease: $ease%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini diagrams row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(result.chordResults) { chordResult ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = chordResult.shapeName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        VerticalChordDiagram(
                            voicing = chordResult.bestVoicing,
                            onClick = {},
                            leftHanded = leftHanded,
                            modifier = Modifier.width(110.dp),
                        )
                    }
                }
            }
        }
    }
}
