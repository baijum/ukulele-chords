package com.baijum.ukufretboard.data

/**
 * Settings for sound playback (strum, note duration, etc.).
 *
 * @property enabled Master toggle — when false, no sound is produced.
 * @property noteDurationMs How long each note rings, in milliseconds (300–1200).
 * @property strumDelayMs Time between successive string plucks in a strum (0–150).
 *   0 means all notes play simultaneously; higher values produce a slower strum.
 * @property strumDown When true, strings are strummed top-to-bottom (G→C→E→A).
 *   When false, the strum order is reversed (A→E→C→G).
 * @property playOnTap When true, a single note is played each time a fret is tapped.
 */
data class SoundSettings(
    val enabled: Boolean = true,
    val volume: Float = DEFAULT_VOLUME,
    val noteDurationMs: Int = DEFAULT_NOTE_DURATION_MS,
    val strumDelayMs: Int = DEFAULT_STRUM_DELAY_MS,
    val strumDown: Boolean = true,
    val playOnTap: Boolean = false,
) {
    companion object {
        const val MIN_VOLUME = 0f
        const val MAX_VOLUME = 1f
        const val DEFAULT_VOLUME = 1f

        const val MIN_NOTE_DURATION_MS = 300
        const val MAX_NOTE_DURATION_MS = 1200
        const val DEFAULT_NOTE_DURATION_MS = 600

        const val MIN_STRUM_DELAY_MS = 0
        const val MAX_STRUM_DELAY_MS = 150
        const val DEFAULT_STRUM_DELAY_MS = 50
    }
}

/**
 * Theme mode for the app.
 */
enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System"),
    HIGH_CONTRAST("High Contrast"),
}

/**
 * Settings for display preferences (theme).
 *
 * @property themeMode The app color theme: Light, Dark, or follow the System setting.
 */
data class DisplaySettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

/**
 * Available ukulele tunings.
 *
 * @property label Human-readable name for the tuning.
 * @property stringNames Display names for each string (top to bottom).
 * @property pitchClasses Pitch class (0–11) of each open string.
 * @property octaves Octave number of each open string.
 */
enum class UkuleleTuning(
    val label: String,
    val stringNames: List<String>,
    val pitchClasses: List<Int>,
    val octaves: List<Int>,
) {
    HIGH_G("High-G (Standard)", listOf("G","C","E","A"), listOf(7,0,4,9), listOf(4,4,4,4)),
    LOW_G("Low-G", listOf("g","C","E","A"), listOf(7,0,4,9), listOf(3,4,4,4)),
    BARITONE("Baritone (DGBE)", listOf("D","G","B","E"), listOf(2,7,11,4), listOf(3,3,4,4)),
    D_TUNING("D-Tuning (ADF#B)", listOf("A","D","F#","B"), listOf(9,2,6,11), listOf(4,4,4,4));

    /**
     * Whether this tuning is re-entrant (string pitches are not monotonically ascending).
     *
     * In re-entrant tuning the first string is higher than the second, which limits
     * the variety of chord inversions because the second string (C4 in High-G)
     * is almost always the lowest-pitched note.
     */
    val isReentrant: Boolean
        get() {
            for (i in 0 until octaves.size - 1) {
                val pitchA = octaves[i] * 12 + pitchClasses[i]
                val pitchB = octaves[i + 1] * 12 + pitchClasses[i + 1]
                if (pitchA > pitchB) return true
            }
            return false
        }
}

/**
 * Settings for ukulele tuning.
 *
 * @property tuning The selected tuning variant.
 */
data class TuningSettings(
    val tuning: UkuleleTuning = UkuleleTuning.HIGH_G,
)

/**
 * Settings for fretboard display preferences.
 *
 * @property leftHanded When true, the fretboard is mirrored horizontally
 *   so the nut appears on the right (matching a left-handed player's view).
 * @property lastFret The highest fret shown on the fretboard (12–22, default 12).
 * @property showNoteNames When true, note names are displayed on fretboard cells.
 */
data class FretboardSettings(
    val leftHanded: Boolean = false,
    val lastFret: Int = DEFAULT_LAST_FRET,
    val showNoteNames: Boolean = true,
) {
    companion object {
        const val MIN_LAST_FRET = 12
        const val MAX_LAST_FRET = 22
        const val DEFAULT_LAST_FRET = 12
    }
}

/**
 * Settings for daily chord notification.
 *
 * @property chordOfDayEnabled Whether to show a daily chord notification.
 */
data class NotificationSettings(
    val chordOfDayEnabled: Boolean = false,
)

/**
 * Top-level container for all app settings, organized by section.
 *
 * Each section is a nested data class with its own defaults.
 * New sections are added here as the app grows.
 */
data class AppSettings(
    val sound: SoundSettings = SoundSettings(),
    val display: DisplaySettings = DisplaySettings(),
    val tuning: TuningSettings = TuningSettings(),
    val fretboard: FretboardSettings = FretboardSettings(),
    val notification: NotificationSettings = NotificationSettings(),
)
