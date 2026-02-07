package com.baijum.ukufretboard.data.sync

import com.baijum.ukufretboard.data.AppSettings
import com.baijum.ukufretboard.data.ChordSheet
import com.baijum.ukufretboard.data.ChordSheetRepository
import com.baijum.ukufretboard.data.DisplaySettings
import com.baijum.ukufretboard.data.FavoriteVoicing
import com.baijum.ukufretboard.data.FavoritesRepository
import com.baijum.ukufretboard.data.FretboardSettings
import com.baijum.ukufretboard.data.SoundSettings
import com.baijum.ukufretboard.data.ThemeMode
import com.baijum.ukufretboard.data.TuningSettings
import com.baijum.ukufretboard.data.UkuleleTuning
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.serialization.json.Json

/**
 * Orchestrates export and import of all user data to/from Google Drive.
 *
 * The sync flow is:
 * 1. **Download** the remote backup (if any) and merge into local data.
 * 2. **Upload** the merged local data back to Drive.
 *
 * This ensures both sides end up with the union of all data.
 */
class SyncManager(
    private val favoritesRepository: FavoritesRepository,
    private val chordSheetRepository: ChordSheetRepository,
    private val driveRepository: GoogleDriveRepository,
    private val getSettings: () -> AppSettings,
    private val setSettings: (AppSettings) -> Unit,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * Performs a full sync: download + merge + upload.
     *
     * @param account The signed-in Google account.
     * @throws Exception on network or API errors.
     */
    fun sync(account: GoogleSignInAccount) {
        // 1. Download and merge remote data (if any)
        val remoteJson = driveRepository.download(account)
        if (remoteJson != null) {
            val remoteData = json.decodeFromString<BackupData>(remoteJson)
            importRemoteData(remoteData)
        }

        // 2. Export local data and upload
        val localData = exportLocalData()
        val localJson = json.encodeToString(BackupData.serializer(), localData)
        driveRepository.upload(account, localJson)
    }

    // ── Export ───────────────────────────────────────────────────────────

    private fun exportLocalData(): BackupData {
        val favorites = favoritesRepository.getAll().map { fav ->
            BackupFavorite(
                rootPitchClass = fav.rootPitchClass,
                chordSymbol = fav.chordSymbol,
                frets = fav.frets,
                addedAt = fav.addedAt,
            )
        }

        val sheets = chordSheetRepository.getAll().map { sheet ->
            BackupChordSheet(
                id = sheet.id,
                title = sheet.title,
                artist = sheet.artist,
                content = sheet.content,
                createdAt = sheet.createdAt,
                updatedAt = sheet.updatedAt,
            )
        }

        val settings = getSettings()
        val backupSettings = BackupSettings(
            soundEnabled = settings.sound.enabled,
            noteDurationMs = settings.sound.noteDurationMs,
            strumDelayMs = settings.sound.strumDelayMs,
            strumDown = settings.sound.strumDown,
            playOnTap = settings.sound.playOnTap,
            useFlats = settings.display.useFlats,
            themeMode = settings.display.themeMode.name,
            tuning = settings.tuning.tuning.name,
            leftHanded = settings.fretboard.leftHanded,
        )

        return BackupData(
            favorites = favorites,
            chordSheets = sheets,
            settings = backupSettings,
        )
    }

    // ── Import ──────────────────────────────────────────────────────────

    private fun importRemoteData(data: BackupData) {
        // Favorites: union merge
        val remoteFavorites = data.favorites.map { fav ->
            FavoriteVoicing(
                rootPitchClass = fav.rootPitchClass,
                chordSymbol = fav.chordSymbol,
                frets = fav.frets,
                addedAt = fav.addedAt,
            )
        }
        favoritesRepository.importAll(remoteFavorites)

        // Chord sheets: merge by ID, latest updatedAt wins
        val remoteSheets = data.chordSheets.map { sheet ->
            ChordSheet(
                id = sheet.id,
                title = sheet.title,
                artist = sheet.artist,
                content = sheet.content,
                createdAt = sheet.createdAt,
                updatedAt = sheet.updatedAt,
            )
        }
        chordSheetRepository.importAll(remoteSheets)

        // Settings: replace local with remote
        val s = data.settings
        val newSettings = AppSettings(
            sound = SoundSettings(
                enabled = s.soundEnabled,
                noteDurationMs = s.noteDurationMs,
                strumDelayMs = s.strumDelayMs,
                strumDown = s.strumDown,
                playOnTap = s.playOnTap,
            ),
            display = DisplaySettings(
                useFlats = s.useFlats,
                themeMode = try {
                    ThemeMode.valueOf(s.themeMode)
                } catch (_: Exception) {
                    ThemeMode.SYSTEM
                },
            ),
            tuning = TuningSettings(
                tuning = try {
                    UkuleleTuning.valueOf(s.tuning)
                } catch (_: Exception) {
                    UkuleleTuning.HIGH_G
                },
            ),
            fretboard = FretboardSettings(
                leftHanded = s.leftHanded,
            ),
        )
        setSettings(newSettings)
    }
}
