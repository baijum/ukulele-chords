package com.baijum.ukufretboard.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository for tracking learning progress using SharedPreferences.
 *
 * Tracks which chords the user has marked as "learned" and maintains
 * a daily practice streak.
 */
class ProgressRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Returns the set of learned chord keys (e.g., "0|Major" for C Major).
     */
    fun getLearnedChords(): Set<String> {
        return prefs.getStringSet(KEY_LEARNED_CHORDS, emptySet()) ?: emptySet()
    }

    /**
     * Marks a chord as learned.
     *
     * @param rootPitchClass The pitch class of the chord root (0-11).
     * @param quality The chord quality symbol (e.g., "", "m", "7").
     */
    fun markLearned(rootPitchClass: Int, quality: String) {
        val key = "$rootPitchClass|$quality"
        val current = getLearnedChords().toMutableSet()
        current.add(key)
        prefs.edit().putStringSet(KEY_LEARNED_CHORDS, current).apply()
        recordPractice()
    }

    /**
     * Unmarks a chord as learned.
     */
    fun unmarkLearned(rootPitchClass: Int, quality: String) {
        val key = "$rootPitchClass|$quality"
        val current = getLearnedChords().toMutableSet()
        current.remove(key)
        prefs.edit().putStringSet(KEY_LEARNED_CHORDS, current).apply()
    }

    /**
     * Checks if a chord is marked as learned.
     */
    fun isLearned(rootPitchClass: Int, quality: String): Boolean {
        val key = "$rootPitchClass|$quality"
        return getLearnedChords().contains(key)
    }

    /**
     * Returns the current daily practice streak.
     */
    fun getDailyStreak(): Int {
        val lastDate = prefs.getString(KEY_LAST_PRACTICE_DATE, null) ?: return 0
        val today = todayString()
        val yesterday = yesterdayString()

        return when (lastDate) {
            today -> prefs.getInt(KEY_DAILY_STREAK, 0)
            yesterday -> prefs.getInt(KEY_DAILY_STREAK, 0) // streak still valid
            else -> 0 // streak broken
        }
    }

    /**
     * Records a practice session for today. Updates the streak.
     */
    fun recordPractice() {
        val today = todayString()
        val lastDate = prefs.getString(KEY_LAST_PRACTICE_DATE, null)
        val currentStreak = prefs.getInt(KEY_DAILY_STREAK, 0)

        val newStreak = when (lastDate) {
            today -> currentStreak // Already practiced today
            yesterdayString() -> currentStreak + 1 // Continue streak
            else -> 1 // Start new streak
        }

        prefs.edit()
            .putString(KEY_LAST_PRACTICE_DATE, today)
            .putInt(KEY_DAILY_STREAK, newStreak)
            .apply()
    }

    private fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun yesterdayString(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    /**
     * Exports chord progress data for backup.
     */
    fun exportData(): Triple<Set<String>, Int, String?> {
        return Triple(
            getLearnedChords(),
            prefs.getInt(KEY_DAILY_STREAK, 0),
            prefs.getString(KEY_LAST_PRACTICE_DATE, null),
        )
    }

    /**
     * Imports chord progress from backup. Merges learned chords (union)
     * and takes the higher streak.
     */
    fun importData(learnedChords: Set<String>, dailyStreak: Int, lastPracticeDate: String?) {
        val current = getLearnedChords().toMutableSet()
        current.addAll(learnedChords)
        val editor = prefs.edit()
            .putStringSet(KEY_LEARNED_CHORDS, current)
        if (dailyStreak > prefs.getInt(KEY_DAILY_STREAK, 0)) {
            editor.putInt(KEY_DAILY_STREAK, dailyStreak)
        }
        if (lastPracticeDate != null) {
            editor.putString(KEY_LAST_PRACTICE_DATE, lastPracticeDate)
        }
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "learning_progress"
        private const val KEY_LEARNED_CHORDS = "learned_chords"
        private const val KEY_DAILY_STREAK = "daily_streak"
        private const val KEY_LAST_PRACTICE_DATE = "last_practice_date"
    }
}
