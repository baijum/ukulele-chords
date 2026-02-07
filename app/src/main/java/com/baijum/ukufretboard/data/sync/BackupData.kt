package com.baijum.ukufretboard.data.sync

import kotlinx.serialization.Serializable

/**
 * Top-level container for the backup JSON file stored on Google Drive.
 *
 * @property version Schema version for forward compatibility.
 * @property exportedAt Timestamp (epoch millis) when this backup was created.
 * @property favorites All saved favorite voicings.
 * @property chordSheets All saved chord sheets.
 * @property settings Application settings snapshot.
 */
@Serializable
data class BackupData(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val favorites: List<BackupFavorite> = emptyList(),
    val chordSheets: List<BackupChordSheet> = emptyList(),
    val settings: BackupSettings = BackupSettings(),
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/**
 * Serializable representation of a favorite voicing for backup.
 */
@Serializable
data class BackupFavorite(
    val rootPitchClass: Int,
    val chordSymbol: String,
    val frets: List<Int>,
    val addedAt: Long,
)

/**
 * Serializable representation of a chord sheet for backup.
 */
@Serializable
data class BackupChordSheet(
    val id: String,
    val title: String,
    val artist: String = "",
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * Serializable representation of app settings for backup.
 */
@Serializable
data class BackupSettings(
    val soundEnabled: Boolean = true,
    val noteDurationMs: Int = 600,
    val strumDelayMs: Int = 50,
    val strumDown: Boolean = true,
    val playOnTap: Boolean = false,
    val useFlats: Boolean = false,
    val themeMode: String = "SYSTEM",
    val tuning: String = "HIGH_G",
    val leftHanded: Boolean = false,
)
