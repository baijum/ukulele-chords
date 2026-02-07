package com.baijum.ukufretboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baijum.ukufretboard.data.Difficulty
import com.baijum.ukufretboard.data.StrumBeat
import com.baijum.ukufretboard.data.StrumDirection
import com.baijum.ukufretboard.data.StrumPattern
import com.baijum.ukufretboard.data.StrumPatterns

/**
 * Tab showing a reference list of common ukulele strumming patterns.
 *
 * Displays each pattern as a card with the name, difficulty badge,
 * visual beat arrows, notation, and description.
 */
@Composable
fun StrumPatternsTab(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
    ) {
        items(StrumPatterns.ALL) { pattern ->
            StrumPatternCard(pattern = pattern)
        }
    }
}

/**
 * A card displaying a single strumming pattern.
 */
@Composable
private fun StrumPatternCard(pattern: StrumPattern) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Header: name + difficulty badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pattern.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                DifficultyBadge(difficulty = pattern.difficulty)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visual beat display
            BeatDisplay(beats = pattern.beats)

            Spacer(modifier = Modifier.height(6.dp))

            // Notation text
            Text(
                text = pattern.notation,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = pattern.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tempo range
            Text(
                text = "${pattern.suggestedBpm.first}–${pattern.suggestedBpm.last} BPM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

/**
 * Visual display of beat arrows in a row.
 */
@Composable
private fun BeatDisplay(beats: List<StrumBeat>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        beats.forEach { beat ->
            BeatArrow(beat = beat)
        }
    }
}

/**
 * A single beat arrow indicator.
 */
@Composable
private fun BeatArrow(beat: StrumBeat) {
    val color = when (beat.direction) {
        StrumDirection.DOWN -> MaterialTheme.colorScheme.primary
        StrumDirection.UP -> MaterialTheme.colorScheme.secondary
        StrumDirection.MISS -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        StrumDirection.PAUSE -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    val fontWeight = if (beat.emphasis) FontWeight.ExtraBold else FontWeight.Normal
    val fontSize = if (beat.emphasis) 20.sp else 16.sp

    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = beat.direction.symbol,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * A small badge showing the difficulty level.
 */
@Composable
private fun DifficultyBadge(difficulty: Difficulty) {
    val bgColor = when (difficulty) {
        Difficulty.BEGINNER -> MaterialTheme.colorScheme.primaryContainer
        Difficulty.INTERMEDIATE -> MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = when (difficulty) {
        Difficulty.BEGINNER -> MaterialTheme.colorScheme.onPrimaryContainer
        Difficulty.INTERMEDIATE -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = difficulty.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}
