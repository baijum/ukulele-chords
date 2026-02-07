package com.baijum.ukufretboard.viewmodel

import androidx.lifecycle.ViewModel
import com.baijum.ukufretboard.data.AppSettings
import com.baijum.ukufretboard.data.DisplaySettings
import com.baijum.ukufretboard.data.FretboardSettings
import com.baijum.ukufretboard.data.SoundSettings
import com.baijum.ukufretboard.data.TuningSettings
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
 * Settings are currently held in memory and reset on app restart.
 * Persistence via DataStore can be added later without changing the public API.
 */
class SettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())

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
            current.copy(sound = transform(current.sound))
        }
    }

    /**
     * Updates the display settings by applying a transformation function.
     */
    fun updateDisplay(transform: (DisplaySettings) -> DisplaySettings) {
        _settings.update { current ->
            current.copy(display = transform(current.display))
        }
    }

    /**
     * Updates the tuning settings by applying a transformation function.
     */
    fun updateTuning(transform: (TuningSettings) -> TuningSettings) {
        _settings.update { current ->
            current.copy(tuning = transform(current.tuning))
        }
    }

    /**
     * Updates the fretboard settings by applying a transformation function.
     */
    fun updateFretboard(transform: (FretboardSettings) -> FretboardSettings) {
        _settings.update { current ->
            current.copy(fretboard = transform(current.fretboard))
        }
    }
}
