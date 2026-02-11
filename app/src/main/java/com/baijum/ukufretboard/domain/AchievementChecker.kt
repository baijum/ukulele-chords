package com.baijum.ukufretboard.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.baijum.ukufretboard.viewmodel.LearningProgressState

/**
 * Defines all achievements and checks whether they have been earned.
 *
 * Each achievement has an ID, display info, and a condition function
 * that takes the current [AchievementContext] and returns `true` when
 * the achievement should be unlocked.
 */
object AchievementChecker {

    /**
     * All available achievements.
     */
    val ALL: List<Achievement> = listOf(
        // ── Practice Milestones ──────────────────────────────────────
        Achievement(
            id = "streak_3",
            title = "Getting Started",
            description = "Maintain a 3-day practice streak",
            icon = Icons.Filled.Star,
            category = AchievementCategory.PRACTICE,
        ) { it.currentStreak >= 3 },

        Achievement(
            id = "streak_7",
            title = "Week Warrior",
            description = "Maintain a 7-day practice streak",
            icon = Icons.Filled.Star,
            category = AchievementCategory.PRACTICE,
        ) { it.currentStreak >= 7 },

        Achievement(
            id = "streak_30",
            title = "Monthly Master",
            description = "Maintain a 30-day practice streak",
            icon = Icons.Filled.Star,
            category = AchievementCategory.PRACTICE,
        ) { it.currentStreak >= 30 },

        // ── Theory Learning ──────────────────────────────────────────
        Achievement(
            id = "first_lesson",
            title = "First Steps",
            description = "Complete your first theory lesson",
            icon = Icons.Filled.Info,
            category = AchievementCategory.LEARNING,
        ) { it.completedLessons >= 1 },

        Achievement(
            id = "half_lessons",
            title = "Halfway There",
            description = "Complete half of all theory lessons",
            icon = Icons.Filled.Info,
            category = AchievementCategory.LEARNING,
        ) { it.totalLessons > 0 && it.completedLessons >= it.totalLessons / 2 },

        Achievement(
            id = "all_lessons",
            title = "Scholar",
            description = "Complete all theory lessons",
            icon = Icons.Filled.Info,
            category = AchievementCategory.LEARNING,
        ) { it.totalLessons > 0 && it.completedLessons >= it.totalLessons },

        // ── Quiz Achievements ────────────────────────────────────────
        Achievement(
            id = "quiz_10",
            title = "Quiz Starter",
            description = "Answer 10 quiz questions correctly",
            icon = Icons.Filled.Search,
            category = AchievementCategory.LEARNING,
        ) { it.quizCorrect >= 10 },

        Achievement(
            id = "quiz_50",
            title = "Quiz Whiz",
            description = "Answer 50 quiz questions correctly",
            icon = Icons.Filled.Search,
            category = AchievementCategory.LEARNING,
        ) { it.quizCorrect >= 50 },

        Achievement(
            id = "quiz_100",
            title = "Quiz Master",
            description = "Answer 100 quiz questions correctly",
            icon = Icons.Filled.Search,
            category = AchievementCategory.LEARNING,
        ) { it.quizCorrect >= 100 },

        Achievement(
            id = "perfect_streak_5",
            title = "Sharp Mind",
            description = "Get 5 quiz questions correct in a row",
            icon = Icons.Filled.Star,
            category = AchievementCategory.LEARNING,
        ) { it.quizBestStreak >= 5 },

        Achievement(
            id = "perfect_streak_10",
            title = "Perfect Score",
            description = "Get 10 quiz questions correct in a row",
            icon = Icons.Filled.Star,
            category = AchievementCategory.LEARNING,
        ) { it.quizBestStreak >= 10 },

        // ── Ear Training ─────────────────────────────────────────────
        Achievement(
            id = "ear_first",
            title = "Tuning In",
            description = "Complete your first ear training exercise",
            icon = Icons.Filled.PlayArrow,
            category = AchievementCategory.EAR,
        ) { it.intervalTotal >= 1 || it.chordEarTotal >= 1 },

        Achievement(
            id = "ear_50",
            title = "Good Ear",
            description = "Complete 50 ear training exercises",
            icon = Icons.Filled.PlayArrow,
            category = AchievementCategory.EAR,
        ) { (it.intervalTotal + it.chordEarTotal) >= 50 },

        Achievement(
            id = "ear_accuracy_80",
            title = "Golden Ear",
            description = "Achieve 80% accuracy in ear training (50+ exercises)",
            icon = Icons.Filled.PlayArrow,
            category = AchievementCategory.EAR,
        ) {
            val total = it.intervalTotal + it.chordEarTotal
            val correct = it.intervalCorrect + it.chordEarCorrect
            total >= 50 && correct * 100 / total >= 80
        },

        // ── Songbook ─────────────────────────────────────────────────
        Achievement(
            id = "first_song",
            title = "Songwriter",
            description = "Create your first song in the songbook",
            icon = Icons.Filled.Create,
            category = AchievementCategory.SONGS,
        ) { it.songsCount >= 1 },

        Achievement(
            id = "songs_5",
            title = "Setlist Builder",
            description = "Create 5 songs in the songbook",
            icon = Icons.Filled.Create,
            category = AchievementCategory.SONGS,
        ) { it.songsCount >= 5 },

        Achievement(
            id = "songs_10",
            title = "Prolific Writer",
            description = "Create 10 songs in the songbook",
            icon = Icons.Filled.Create,
            category = AchievementCategory.SONGS,
        ) { it.songsCount >= 10 },

        // ── Favorites ────────────────────────────────────────────────
        Achievement(
            id = "fav_5",
            title = "Collector",
            description = "Save 5 favorite chord voicings",
            icon = Icons.Filled.Favorite,
            category = AchievementCategory.CHORDS,
        ) { it.favoritesCount >= 5 },

        Achievement(
            id = "fav_25",
            title = "Chord Hoarder",
            description = "Save 25 favorite chord voicings",
            icon = Icons.Filled.Favorite,
            category = AchievementCategory.CHORDS,
        ) { it.favoritesCount >= 25 },

        // ── Scale Practice ───────────────────────────────────────────
        Achievement(
            id = "scale_first",
            title = "Scale Runner",
            description = "Complete your first scale practice session",
            icon = Icons.Filled.Home,
            category = AchievementCategory.PRACTICE,
        ) { it.scalePracticeTotal >= 1 },

        Achievement(
            id = "scale_25",
            title = "Scale Explorer",
            description = "Complete 25 scale practice exercises",
            icon = Icons.Filled.Home,
            category = AchievementCategory.PRACTICE,
        ) { it.scalePracticeTotal >= 25 },
    )

    /**
     * Checks all achievements and returns any that are newly earned.
     *
     * @param context Current stats from across the app.
     * @param alreadyUnlocked Set of achievement IDs that are already unlocked.
     * @return List of newly earned achievement IDs.
     */
    fun checkNewlyEarned(
        context: AchievementContext,
        alreadyUnlocked: Set<String>,
    ): List<Achievement> {
        return ALL.filter { achievement ->
            achievement.id !in alreadyUnlocked && achievement.condition(context)
        }
    }

    /** Returns the total number of achievements available. */
    fun totalCount(): Int = ALL.size
}

/**
 * Snapshot of the user's current stats used to evaluate achievement conditions.
 */
data class AchievementContext(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val quizCorrect: Int = 0,
    val quizTotal: Int = 0,
    val quizBestStreak: Int = 0,
    val intervalTotal: Int = 0,
    val intervalCorrect: Int = 0,
    val chordEarTotal: Int = 0,
    val chordEarCorrect: Int = 0,
    val scalePracticeTotal: Int = 0,
    val songsCount: Int = 0,
    val favoritesCount: Int = 0,
)

/**
 * Builds an [AchievementContext] from a [LearningProgressState] and additional counts.
 */
fun LearningProgressState.toAchievementContext(
    songsCount: Int = 0,
    favoritesCount: Int = 0,
): AchievementContext = AchievementContext(
    currentStreak = currentDayStreak,
    bestStreak = bestDayStreak,
    completedLessons = completedLessons,
    totalLessons = totalLessons,
    quizCorrect = quizStatsOverall.correct,
    quizTotal = quizStatsOverall.total,
    quizBestStreak = quizStatsOverall.bestStreak,
    intervalTotal = intervalStatsOverall.total,
    intervalCorrect = intervalStatsOverall.correct,
    chordEarTotal = chordEarStatsOverall.total,
    chordEarCorrect = chordEarStatsOverall.correct,
    scalePracticeTotal = scalePracticeStatsOverall.total,
    songsCount = songsCount,
    favoritesCount = favoritesCount,
)

/**
 * Categories of achievements for display grouping.
 */
enum class AchievementCategory(val label: String) {
    PRACTICE("Practice"),
    LEARNING("Learning"),
    EAR("Ear Training"),
    CHORDS("Chords"),
    SONGS("Songs"),
}

/**
 * Definition of a single achievement.
 *
 * @property id Unique identifier.
 * @property title Display title.
 * @property description How to earn the achievement.
 * @property icon Material icon for display.
 * @property category Grouping category.
 * @property condition Predicate that returns `true` when earned.
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: AchievementCategory,
    val condition: (AchievementContext) -> Boolean,
)
