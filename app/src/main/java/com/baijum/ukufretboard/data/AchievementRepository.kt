package com.baijum.ukufretboard.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for persisting unlocked achievements using SharedPreferences.
 *
 * Stores a JSON map of achievement ID to unlock timestamp.
 */
class AchievementRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Returns the set of unlocked achievement IDs with their unlock timestamps.
     */
    fun getUnlocked(): Map<String, Long> {
        val raw = prefs.getString(KEY_UNLOCKED, null) ?: return emptyMap()
        return try {
            json.decodeFromString<Map<String, Long>>(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    /**
     * Marks an achievement as unlocked.
     *
     * @param id The achievement ID.
     * @return `true` if this was newly unlocked, `false` if already unlocked.
     */
    fun unlock(id: String): Boolean {
        val current = getUnlocked().toMutableMap()
        if (id in current) return false
        current[id] = System.currentTimeMillis()
        prefs.edit().putString(KEY_UNLOCKED, json.encodeToString(current)).apply()
        return true
    }

    /** Returns whether an achievement is unlocked. */
    fun isUnlocked(id: String): Boolean = id in getUnlocked()

    /** Returns the number of unlocked achievements. */
    fun unlockedCount(): Int = getUnlocked().size

    /** Exports all achievement data for backup. */
    fun exportAll(): Map<String, Long> = getUnlocked()

    /** Imports achievement data from backup, merging with existing. */
    fun importAll(data: Map<String, Long>) {
        val current = getUnlocked().toMutableMap()
        data.forEach { (id, timestamp) ->
            if (id !in current) {
                current[id] = timestamp
            }
        }
        prefs.edit().putString(KEY_UNLOCKED, json.encodeToString(current)).apply()
    }

    /** Clears all unlocked achievements. */
    fun clearAll() {
        prefs.edit().remove(KEY_UNLOCKED).apply()
    }

    companion object {
        private const val PREFS_NAME = "achievements"
        private const val KEY_UNLOCKED = "unlocked"
    }
}
