package com.baijum.ukufretboard.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository for persisting chord sheets using SharedPreferences.
 *
 * Each sheet is serialized as a pipe-delimited string.
 */
class ChordSheetRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): List<ChordSheet> {
        return prefs.all.entries
            .mapNotNull { (_, value) -> deserialize(value as? String) }
            .sortedByDescending { it.updatedAt }
    }

    fun get(id: String): ChordSheet? {
        val value = prefs.getString(id, null)
        return deserialize(value)
    }

    fun save(sheet: ChordSheet) {
        prefs.edit().putString(sheet.id, serialize(sheet)).apply()
    }

    fun delete(id: String) {
        prefs.edit().remove(id).apply()
    }

    private fun serialize(sheet: ChordSheet): String =
        listOf(
            sheet.id,
            sheet.title.replace("|", "\\|"),
            sheet.artist.replace("|", "\\|"),
            sheet.content.replace("|", "\\|"),
            sheet.createdAt.toString(),
            sheet.updatedAt.toString(),
        ).joinToString(SEPARATOR)

    private fun deserialize(value: String?): ChordSheet? {
        if (value == null) return null
        // Split on unescaped pipes
        val parts = value.split(SEPARATOR)
        if (parts.size < 6) return null
        return try {
            ChordSheet(
                id = parts[0],
                title = parts[1].replace("\\|", "|"),
                artist = parts[2].replace("\\|", "|"),
                content = parts[3].replace("\\|", "|"),
                createdAt = parts[4].toLong(),
                updatedAt = parts[5].toLong(),
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "chord_sheets"
        private const val SEPARATOR = "|||"
    }
}
