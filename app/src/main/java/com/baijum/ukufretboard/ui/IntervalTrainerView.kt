package com.baijum.ukufretboard.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.domain.IntervalTrainer

/**
 * Interactive interval trainer with visual note display and multiple-choice answers.
 *
 * Users identify intervals between two notes shown on screen.
 * Difficulty increases progressively as they get more correct answers.
 */
@Composable
fun IntervalTrainerView(
    modifier: Modifier = Modifier,
) {
    var level by remember { mutableIntStateOf(1) }
    var question by remember { mutableStateOf<IntervalTrainer.IntervalQuestion?>(null) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var totalCorrect by remember { mutableIntStateOf(0) }
    var totalAnswered by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Interval Trainer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Identify the interval between two notes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Level selector
        Text(
            text = "Difficulty",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val levelNames = listOf("Easy", "Medium", "Hard", "Expert")
            levelNames.forEachIndexed { index, name ->
                FilterChip(
                    selected = level == index + 1,
                    onClick = {
                        level = index + 1
                        question = null
                        selectedAnswer = null
                    },
                    label = { Text(name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Score display
        if (totalAnswered > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ScoreItem(label = "Correct", value = "$totalCorrect/$totalAnswered")
                ScoreItem(label = "Accuracy", value = "${(totalCorrect * 100 / totalAnswered)}%")
                ScoreItem(label = "Streak", value = "$streak")
                ScoreItem(label = "Best", value = "$bestStreak")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (question == null) {
            // Start button
            Button(
                onClick = {
                    question = IntervalTrainer.generateQuestion(level)
                    selectedAnswer = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                Text(if (totalAnswered == 0) "Start Training" else "Next Interval")
            }
        } else {
            // Note display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "What interval is this?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Two notes displayed prominently
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NoteCircle(
                            noteName = question!!.note1Name,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "\u2192", // arrow
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        NoteCircle(
                            noteName = question!!.note2Name,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Semitone count hint
                    Text(
                        text = "${question!!.intervalSemitones} semitone${if (question!!.intervalSemitones != 1) "s" else ""} apart",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Answer options
                    question!!.options.forEachIndexed { index, option ->
                        val isSelected = selectedAnswer == index
                        val isCorrect = index == question!!.correctIndex
                        val hasAnswered = selectedAnswer != null

                        val containerColor = when {
                            hasAnswered && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                            hasAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }

                        OutlinedButton(
                            onClick = {
                                if (selectedAnswer != null) return@OutlinedButton
                                selectedAnswer = index
                                totalAnswered++
                                if (index == question!!.correctIndex) {
                                    totalCorrect++
                                    streak++
                                    if (streak > bestStreak) bestStreak = streak
                                    // Auto-level up after 5 correct in a row
                                    if (streak % 5 == 0 && level < 4) level++
                                } else {
                                    streak = 0
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = containerColor,
                            ),
                            enabled = selectedAnswer == null,
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                            )
                        }
                    }

                    // Result and next button
                    if (selectedAnswer != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val isCorrect = selectedAnswer == question!!.correctIndex
                        Text(
                            text = if (isCorrect) "Correct!" else "The answer is ${question!!.correctAnswer}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                question = IntervalTrainer.generateQuestion(level)
                                selectedAnswer = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Next Interval")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCircle(noteName: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(color = color, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = noteName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun ScoreItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
