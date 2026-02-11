package com.baijum.ukufretboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.baijum.ukufretboard.data.LearningProgressRepository
import com.baijum.ukufretboard.data.LearningStats
import com.baijum.ukufretboard.data.TheoryLessons
import com.baijum.ukufretboard.domain.QuizGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel managing Learn section persistence and progress tracking.
 *
 * Provides reactive state for lesson completion, quiz scores,
 * interval trainer scores, and daily streaks.
 */
class LearningProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LearningProgressRepository(application)

    private val _state = MutableStateFlow(buildState())
    val state: StateFlow<LearningProgressState> = _state.asStateFlow()

    // ── Theory Lessons ──────────────────────────────────────────────

    fun markLessonCompleted(lessonId: String) {
        repository.markLessonCompleted(lessonId)
        refresh()
    }

    fun isLessonCompleted(lessonId: String): Boolean =
        repository.isLessonCompleted(lessonId)

    fun markLessonQuizPassed(lessonId: String) {
        repository.markLessonQuizPassed(lessonId)
        refresh()
    }

    fun isLessonQuizPassed(lessonId: String): Boolean =
        repository.isLessonQuizPassed(lessonId)

    // ── Theory Quiz ─────────────────────────────────────────────────

    fun recordQuizAnswer(category: QuizGenerator.QuizCategory, correct: Boolean) {
        repository.recordQuizAnswer(category, correct)
        refresh()
    }

    fun quizStats(category: QuizGenerator.QuizCategory? = null): LearningStats =
        repository.quizStats(category)

    // ── Interval Trainer ────────────────────────────────────────────

    fun recordIntervalAnswer(level: Int, correct: Boolean) {
        repository.recordIntervalAnswer(level, correct)
        refresh()
    }

    fun intervalStats(level: Int? = null): LearningStats =
        repository.intervalStats(level)

    // ── Note Quiz ────────────────────────────────────────────────────

    fun recordNoteQuizAnswer(correct: Boolean) {
        repository.recordNoteQuizAnswer(correct)
        refresh()
    }

    fun noteQuizStats(): LearningStats = repository.noteQuizStats()

    // ── Chord Ear Training ──────────────────────────────────────────

    fun recordChordEarAnswer(level: Int, correct: Boolean) {
        repository.recordChordEarAnswer(level, correct)
        refresh()
    }

    fun chordEarStats(level: Int? = null): LearningStats =
        repository.chordEarStats(level)

    // ── Scale Practice ─────────────────────────────────────────────

    fun recordScalePracticeAnswer(mode: String, correct: Boolean) {
        repository.recordScalePracticeAnswer(mode, correct)
        refresh()
    }

    fun scalePracticeStats(mode: String? = null): LearningStats =
        repository.scalePracticeStats(mode)

    // ── Reset ───────────────────────────────────────────────────────

    fun clearAllProgress() {
        repository.clearAllProgress()
        refresh()
    }

    // ── State ───────────────────────────────────────────────────────

    private fun refresh() {
        _state.value = buildState()
    }

    private fun buildState(): LearningProgressState {
        val totalLessons = TheoryLessons.ALL.size
        val completedLessons = repository.completedLessonCount()
        val passedQuizzes = repository.passedQuizCount()
        return LearningProgressState(
            completedLessons = completedLessons,
            totalLessons = totalLessons,
            passedLessonQuizzes = passedQuizzes,
            quizStatsOverall = repository.quizStats(),
            quizStatsByCategory = QuizGenerator.QuizCategory.entries.associateWith {
                repository.quizStats(it)
            },
            intervalStatsOverall = repository.intervalStats(),
            intervalStatsByLevel = (1..4).associateWith { repository.intervalStats(it) },
            noteQuizStats = repository.noteQuizStats(),
            chordEarStatsOverall = repository.chordEarStats(),
            chordEarStatsByLevel = (1..4).associateWith { repository.chordEarStats(it) },
            scalePracticeStatsOverall = repository.scalePracticeStats(),
            scalePracticeStatsByMode = listOf("quiz", "ear").associateWith {
                repository.scalePracticeStats(it)
            },
            currentDayStreak = repository.currentDayStreak(),
            bestDayStreak = repository.bestDayStreak(),
        )
    }
}

/**
 * Snapshot of all learning progress for the UI.
 */
data class LearningProgressState(
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val passedLessonQuizzes: Int = 0,
    val quizStatsOverall: LearningStats = LearningStats(0, 0, 0),
    val quizStatsByCategory: Map<QuizGenerator.QuizCategory, LearningStats> = emptyMap(),
    val intervalStatsOverall: LearningStats = LearningStats(0, 0, 0),
    val intervalStatsByLevel: Map<Int, LearningStats> = emptyMap(),
    val noteQuizStats: LearningStats = LearningStats(0, 0, 0),
    val chordEarStatsOverall: LearningStats = LearningStats(0, 0, 0),
    val chordEarStatsByLevel: Map<Int, LearningStats> = emptyMap(),
    val scalePracticeStatsOverall: LearningStats = LearningStats(0, 0, 0),
    val scalePracticeStatsByMode: Map<String, LearningStats> = emptyMap(),
    val currentDayStreak: Int = 0,
    val bestDayStreak: Int = 0,
) {
    /** Lesson completion percentage (0–100). */
    val lessonCompletionPercent: Int
        get() = if (totalLessons > 0) (completedLessons * 100 / totalLessons) else 0
}
