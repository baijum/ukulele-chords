package com.baijum.ukufretboard.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository for persisting favorite voicings using SharedPreferences.
 *
 * Each favorite is stored as a key-value pair where the key is derived from
 * the voicing's root, symbol, and frets, and the value is a serialized string.
 *
 * This is intentionally simple (no Room, no DataStore) to avoid adding
 * dependencies for what is essentially a small list.
 */
class FavoritesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Returns all saved favorites, sorted by time added (newest first).
     */
    fun getAll(): List<FavoriteVoicing> {
        return prefs.all.entries
            .mapNotNull { (_, value) -> deserialize(value as? String) }
            .sortedByDescending { it.addedAt }
    }

    /**
     * Adds a voicing to favorites. No-op if already saved.
     */
    fun add(voicing: FavoriteVoicing) {
        if (!contains(voicing)) {
            prefs.edit().putString(voicing.key, serialize(voicing)).apply()
        }
    }

    /**
     * Removes a voicing from favorites.
     */
    fun remove(voicing: FavoriteVoicing) {
        prefs.edit().remove(voicing.key).apply()
    }

    /**
     * Checks if a voicing is already in favorites.
     */
    fun contains(voicing: FavoriteVoicing): Boolean =
        prefs.contains(voicing.key)

    /**
     * Checks if a voicing with the given root, symbol, and frets is in favorites.
     */
    fun contains(rootPitchClass: Int, chordSymbol: String, frets: List<Int>): Boolean {
        val key = "$rootPitchClass|$chordSymbol|${frets.joinToString(",")}"
        return prefs.contains(key)
    }

    /**
     * Updates the folder assignment for a voicing.
     */
    fun setFolder(voicing: FavoriteVoicing, folderId: String?) {
        val updated = voicing.copy(folderId = folderId)
        prefs.edit().putString(updated.key, serialize(updated)).apply()
    }

    // ── Folder management ───────────────────────────────────────────

    private val folderPrefs: SharedPreferences =
        context.getSharedPreferences(FOLDER_PREFS_NAME, Context.MODE_PRIVATE)

    fun getAllFolders(): List<FavoriteFolder> {
        return folderPrefs.all.entries
            .mapNotNull { (_, value) -> deserializeFolder(value as? String) }
            .sortedBy { it.name }
    }

    fun saveFolder(folder: FavoriteFolder) {
        folderPrefs.edit().putString(folder.id, serializeFolder(folder)).apply()
    }

    fun deleteFolder(folderId: String) {
        folderPrefs.edit().remove(folderId).apply()
        // Move all voicings in this folder to unfiled
        getAll().filter { it.folderId == folderId }.forEach { voicing ->
            setFolder(voicing, null)
        }
    }

    private fun serializeFolder(folder: FavoriteFolder): String =
        "${folder.id}|||${folder.name}|||${folder.createdAt}"

    private fun deserializeFolder(value: String?): FavoriteFolder? {
        if (value == null) return null
        val parts = value.split("|||")
        if (parts.size < 3) return null
        return try {
            FavoriteFolder(id = parts[0], name = parts[1], createdAt = parts[2].toLong())
        } catch (e: Exception) { null }
    }

    // ── Serialization ───────────────────────────────────────────────

    private fun serialize(voicing: FavoriteVoicing): String =
        "${voicing.rootPitchClass}|${voicing.chordSymbol}|${voicing.frets.joinToString(",")}|${voicing.addedAt}|${voicing.folderId ?: ""}"

    private fun deserialize(value: String?): FavoriteVoicing? {
        if (value == null) return null
        val parts = value.split("|")
        if (parts.size < 4) return null
        return try {
            FavoriteVoicing(
                rootPitchClass = parts[0].toInt(),
                chordSymbol = parts[1],
                frets = parts[2].split(",").map { it.toInt() },
                addedAt = parts[3].toLong(),
                folderId = parts.getOrNull(4)?.ifEmpty { null },
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Merges the given list of favorites into local storage.
     * Only adds entries that are not already present (union merge).
     */
    fun importAll(favorites: List<FavoriteVoicing>) {
        val editor = prefs.edit()
        for (voicing in favorites) {
            if (!prefs.contains(voicing.key)) {
                editor.putString(voicing.key, serialize(voicing))
            }
        }
        editor.apply()
    }

    /**
     * Merges the given list of folders into local storage.
     * Only adds folders that are not already present.
     */
    fun importFolders(folders: List<FavoriteFolder>) {
        val editor = folderPrefs.edit()
        for (folder in folders) {
            if (!folderPrefs.contains(folder.id)) {
                editor.putString(folder.id, serializeFolder(folder))
            }
        }
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "chord_favorites"
        private const val FOLDER_PREFS_NAME = "favorite_folders"
    }
}
