package com.baijum.ukufretboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.baijum.ukufretboard.domain.QuizGenerator

/**
 * Interactive theory quiz view with category selection, scoring, and streaks.
 *
 * Users select a category (or "All"), answer multiple-choice questions,
 * and see their score and current streak.
 */
@Composable
fun TheoryQuizView(
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf<QuizGenerator.QuizCategory?>(null) }
    var currentQuestion by remember { mutableStateOf<QuizGenerator.QuizQuestion?>(null) }
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
            text = "Theory Quiz",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Test your music theory knowledge!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category selector
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
            QuizGenerator.QuizCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label) },
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
                ScoreStat(label = "Score", value = "$totalCorrect/$totalAnswered")
                ScoreStat(label = "Accuracy", value = "${(totalCorrect * 100 / totalAnswered)}%")
                ScoreStat(label = "Streak", value = "$streak")
                ScoreStat(label = "Best", value = "$bestStreak")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Question card or start button
        if (currentQuestion == null) {
            Button(
                onClick = {
                    currentQuestion = QuizGenerator.generate(selectedCategory)
                    selectedAnswer = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                Text(if (totalAnswered == 0) "Start Quiz" else "Next Question")
            }
        } else {
            QuestionCard(
                question = currentQuestion!!,
                selectedAnswer = selectedAnswer,
                onAnswerSelected = { answerIndex ->
                    if (selectedAnswer != null) return@QuestionCard // Already answered
                    selectedAnswer = answerIndex
                    totalAnswered++
                    if (answerIndex == currentQuestion!!.correctIndex) {
                        totalCorrect++
                        streak++
                        if (streak > bestStreak) bestStreak = streak
                    } else {
                        streak = 0
                    }
                },
                onNext = {
                    currentQuestion = QuizGenerator.generate(selectedCategory)
                    selectedAnswer = null
                },
            )
        }
    }
}

@Composable
private fun ScoreStat(label: String, value: String) {
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

@Composable
private fun QuestionCard(
    question: QuizGenerator.QuizQuestion,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category badge
            Text(
                text = question.category.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Question text
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Answer options
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == index
                val isCorrect = index == question.correctIndex
                val hasAnswered = selectedAnswer != null

                val containerColor = when {
                    hasAnswered && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                    hasAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surface
                }
                val contentColor = when {
                    hasAnswered && isCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
                    hasAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }

                OutlinedButton(
                    onClick = { onAnswerSelected(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
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

            // Explanation (shown after answering)
            if (selectedAnswer != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Next Question")
                }
            }
        }
    }
}
