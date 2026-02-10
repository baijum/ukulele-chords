package com.baijum.ukufretboard.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.baijum.ukufretboard.data.AppSettings
import com.baijum.ukufretboard.data.DisplaySettings
import com.baijum.ukufretboard.data.FretboardSettings
import com.baijum.ukufretboard.data.NotificationSettings
import com.baijum.ukufretboard.data.SoundSettings
import com.baijum.ukufretboard.data.ThemeMode
import com.baijum.ukufretboard.data.TuningSettings
import com.baijum.ukufretboard.data.UkuleleTuning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel that manages all application settings.
 *
 * Provides a single [StateFlow] of [AppSettings] and section-specific update
 * methods. Designed to be shared across all screens so that settings changes
 * are immediately reflected everywhere.
 *
 * Settings are persisted to SharedPreferences and restored on app restart.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())

    /** Observable stream of the current application settings. */
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /**
     * Updates the sound settings by applying a transformation function.
     *
     * Example usage:
     * ```
     * updateSound { it.copy(enabled = false) }
     * updateSound { it.copy(strumDelayMs = 100) }
     * ```
     *
     * @param transform A function that receives the current [SoundSettings]
     *   and returns the updated [SoundSettings].
     */
    fun updateSound(transform: (SoundSettings) -> SoundSettings) {
        _settings.update { current ->
            current.copy(sound = transform(current.sound)).also { saveSettings(it) }
        }
    }

    /**
     * Updates the display settings by applying a transformation function.
     */
    fun updateDisplay(transform: (DisplaySettings) -> DisplaySettings) {
        _settings.update { current ->
            current.copy(display = transform(current.display)).also { saveSettings(it) }
        }
    }

    /**
     * Updates the tuning settings by applying a transformation function.
     */
    fun updateTuning(transform: (TuningSettings) -> TuningSettings) {
        _settings.update { current ->
            current.copy(tuning = transform(current.tuning)).also { saveSettings(it) }
        }
    }

    /**
     * Updates the fretboard settings by applying a transformation function.
     */
    fun updateFretboard(transform: (FretboardSettings) -> FretboardSettings) {
        _settings.update { current ->
            current.copy(fretboard = transform(current.fretboard)).also { saveSettings(it) }
        }
    }

    /**
     * Updates the notification settings by applying a transformation function.
     */
    fun updateNotification(transform: (NotificationSettings) -> NotificationSettings) {
        _settings.update { current ->
            current.copy(notification = transform(current.notification)).also { saveSettings(it) }
        }
    }

    /**
     * Replaces all settings with the given [AppSettings].
     * Used for sync/restore operations.
     */
    fun replaceAll(newSettings: AppSettings) {
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    /**
     * Returns a snapshot of the current settings for export.
     */
    fun exportSettings(): AppSettings = _settings.value

    // ── Persistence ─────────────────────────────────────────────────────

    private fun saveSettings(s: AppSettings) {
        prefs.edit()
            // Sound
            .putBoolean(KEY_SOUND_ENABLED, s.sound.enabled)
            .putFloat(KEY_VOLUME, s.sound.volume)
            .putInt(KEY_NOTE_DURATION, s.sound.noteDurationMs)
            .putInt(KEY_STRUM_DELAY, s.sound.strumDelayMs)
            .putBoolean(KEY_STRUM_DOWN, s.sound.strumDown)
            .putBoolean(KEY_PLAY_ON_TAP, s.sound.playOnTap)
            // Display
            .putString(KEY_THEME_MODE, s.display.themeMode.name)
            // Tuning
            .putString(KEY_TUNING, s.tuning.tuning.name)
            // Fretboard
            .putBoolean(KEY_LEFT_HANDED, s.fretboard.leftHanded)
            .putInt(KEY_LAST_FRET, s.fretboard.lastFret)
            // Notification
            .putBoolean(KEY_CHORD_OF_DAY_ENABLED, s.notification.chordOfDayEnabled)
            .apply()
    }

    private fun loadSettings(): AppSettings {
        if (!prefs.contains(KEY_SOUND_ENABLED)) return AppSettings()

        return AppSettings(
            sound = SoundSettings(
                enabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
                volume = prefs.getFloat(KEY_VOLUME, SoundSettings.DEFAULT_VOLUME),
                noteDurationMs = prefs.getInt(KEY_NOTE_DURATION, SoundSettings.DEFAULT_NOTE_DURATION_MS),
                strumDelayMs = prefs.getInt(KEY_STRUM_DELAY, SoundSettings.DEFAULT_STRUM_DELAY_MS),
                strumDown = prefs.getBoolean(KEY_STRUM_DOWN, true),
                playOnTap = prefs.getBoolean(KEY_PLAY_ON_TAP, false),
            ),
            display = DisplaySettings(
                themeMode = try {
                    ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)!!)
                } catch (_: Exception) {
                    ThemeMode.SYSTEM
                },
            ),
            tuning = TuningSettings(
                tuning = try {
                    UkuleleTuning.valueOf(prefs.getString(KEY_TUNING, UkuleleTuning.HIGH_G.name)!!)
                } catch (_: Exception) {
                    UkuleleTuning.HIGH_G
                },
            ),
            fretboard = FretboardSettings(
                leftHanded = prefs.getBoolean(KEY_LEFT_HANDED, false),
                lastFret = prefs.getInt(KEY_LAST_FRET, FretboardSettings.DEFAULT_LAST_FRET),
            ),
            notification = NotificationSettings(
                chordOfDayEnabled = prefs.getBoolean(KEY_CHORD_OF_DAY_ENABLED, false),
            ),
        )
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VOLUME = "sound_volume"
        private const val KEY_NOTE_DURATION = "note_duration_ms"
        private const val KEY_STRUM_DELAY = "strum_delay_ms"
        private const val KEY_STRUM_DOWN = "strum_down"
        private const val KEY_PLAY_ON_TAP = "play_on_tap"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TUNING = "tuning"
        private const val KEY_LEFT_HANDED = "left_handed"
        private const val KEY_LAST_FRET = "last_fret"
        private const val KEY_CHORD_OF_DAY_ENABLED = "chord_of_day_enabled"
    }
}
