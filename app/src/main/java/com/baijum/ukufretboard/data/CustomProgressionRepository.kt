package com.baijum.ukufretboard.data

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Persisted custom progression with a unique identifier.
 *
 * Wraps a [Progression] with metadata for storage and management.
 *
 * @property id Unique identifier (UUID string).
 * @property progression The chord progression data.
 * @property createdAt Epoch millis when the progression was created.
 */
data class CustomProgression(
    val id: String = UUID.randomUUID().toString(),
    val progression: Progression,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Repository for persisting user-created chord progressions using SharedPreferences.
 *
 * Each progression is stored as a pipe-delimited string.
 * Follows the same pattern as [FavoritesRepository] — intentionally simple,
 * no Room or DataStore dependencies.
 *
 * Serialization format:
 * ```
 * id|||name|||description|||scaleType|||degree1;degree2;...|||createdAt
 * ```
 * Where each degree is: `interval:quality:numeral`
 */
class CustomProgressionRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Returns all custom progressions, sorted by creation time (newest first).
     */
    fun getAll(): List<CustomProgression> {
        return prefs.all.entries
            .mapNotNull { (_, value) -> deserialize(value as? String) }
            .sortedByDescending { it.createdAt }
    }

    /**
     * Saves a custom progression. Overwrites if the same ID exists.
     */
    fun save(custom: CustomProgression) {
        prefs.edit().putString(custom.id, serialize(custom)).apply()
    }

    /**
     * Deletes a custom progression by ID.
     */
    fun delete(id: String) {
        prefs.edit().remove(id).apply()
    }

    private fun serialize(custom: CustomProgression): String {
        val p = custom.progression
        val degreesStr = p.degrees.joinToString(";") { d ->
            "${d.interval}:${d.quality}:${d.numeral}"
        }
        // Escape pipes in name/description to avoid breaking the delimiter
        val safeName = p.name.replace("|", "\\|")
        val safeDesc = p.description.replace("|", "\\|")
        return "${custom.id}|||$safeName|||$safeDesc|||${p.scaleType.name}|||$degreesStr|||${custom.createdAt}"
    }

    private fun deserialize(value: String?): CustomProgression? {
        if (value == null) return null
        val parts = value.split("|||")
        if (parts.size < 6) return null
        return try {
            val id = parts[0]
            val name = parts[1].replace("\\|", "|")
            val description = parts[2].replace("\\|", "|")
            val scaleType = ScaleType.valueOf(parts[3])
            val degrees = parts[4].split(";").map { degreeStr ->
                val dp = degreeStr.split(":")
                ChordDegree(
                    interval = dp[0].toInt(),
                    quality = dp[1],
                    numeral = dp[2],
                )
            }
            val createdAt = parts[5].toLong()
            CustomProgression(
                id = id,
                progression = Progression(
                    name = name,
                    description = description,
                    degrees = degrees,
                    scaleType = scaleType,
                ),
                createdAt = createdAt,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Merges the given list of progressions into local storage.
     * Only adds entries that are not already present (by ID).
     */
    fun importAll(items: List<CustomProgression>) {
        val editor = prefs.edit()
        for (item in items) {
            if (!prefs.contains(item.id)) {
                editor.putString(item.id, serialize(item))
            }
        }
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "custom_progressions"
    }
}
