package com.baijum.ukufretboard.domain

import kotlin.math.roundToLong

/**
 * SM-2 (SuperMemo 2) based spaced repetition scheduler.
 *
 * Determines the next review interval for a card based on the user's
 * self-assessed quality of recall (0–5 scale).
 *
 * The algorithm adjusts the ease factor and interval to optimise
 * long-term retention of chord voicings.
 */
object SrsScheduler {

    /** Minimum ease factor to prevent intervals from collapsing too quickly. */
    const val MIN_EASE_FACTOR = 1.3f

    /** Default ease factor for new cards. */
    const val DEFAULT_EASE_FACTOR = 2.5f

    /**
     * Quality grades for self-assessment.
     *
     * @property value The numeric grade (0–5).
     * @property label User-friendly label.
     * @property description Hint for the user.
     */
    enum class Grade(val value: Int, val label: String, val description: String) {
        AGAIN(0, "Again", "Didn't remember at all"),
        HARD(1, "Hard", "Remembered with significant difficulty"),
        GOOD(3, "Good", "Remembered with some effort"),
        EASY(5, "Easy", "Remembered instantly"),
    }

    /**
     * Computes the next review scheduling for a card.
     *
     * @param currentInterval Current interval in days (0 for new/reset cards).
     * @param currentEaseFactor The card's current ease factor.
     * @param grade The user's self-assessed quality of recall.
     * @param repetitions Number of consecutive successful reviews.
     * @return A [ReviewResult] with the updated interval, ease factor, and repetition count.
     */
    fun schedule(
        currentInterval: Int,
        currentEaseFactor: Float,
        grade: Grade,
        repetitions: Int,
    ): ReviewResult {
        return when {
            grade.value < 3 -> {
                // Failed — reset to learning phase
                ReviewResult(
                    interval = 0, // Review again soon (within the session)
                    easeFactor = (currentEaseFactor - 0.2f).coerceAtLeast(MIN_EASE_FACTOR),
                    repetitions = 0,
                )
            }
            repetitions == 0 -> {
                // First successful review — 1 day
                ReviewResult(
                    interval = 1,
                    easeFactor = adjustEaseFactor(currentEaseFactor, grade),
                    repetitions = 1,
                )
            }
            repetitions == 1 -> {
                // Second successful review — 3 days
                ReviewResult(
                    interval = 3,
                    easeFactor = adjustEaseFactor(currentEaseFactor, grade),
                    repetitions = 2,
                )
            }
            else -> {
                // Subsequent reviews — interval * ease factor
                val newInterval = (currentInterval * currentEaseFactor).roundToLong().toInt()
                    .coerceAtLeast(currentInterval + 1) // always increase by at least 1 day
                ReviewResult(
                    interval = newInterval,
                    easeFactor = adjustEaseFactor(currentEaseFactor, grade),
                    repetitions = repetitions + 1,
                )
            }
        }
    }

    /**
     * Adjusts the ease factor based on the grade using the SM-2 formula.
     */
    private fun adjustEaseFactor(currentEF: Float, grade: Grade): Float {
        val q = grade.value.toFloat()
        val newEF = currentEF + (0.1f - (5f - q) * (0.08f + (5f - q) * 0.02f))
        return newEF.coerceAtLeast(MIN_EASE_FACTOR)
    }

    /**
     * Result of a review scheduling computation.
     *
     * @property interval Days until the next review (0 = review again this session).
     * @property easeFactor Updated ease factor.
     * @property repetitions Updated consecutive successful review count.
     */
    data class ReviewResult(
        val interval: Int,
        val easeFactor: Float,
        val repetitions: Int,
    )
}
