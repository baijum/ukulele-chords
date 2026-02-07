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
    val noteDurationMs: Int = DEFAULT_NOTE_DURATION_MS,
    val strumDelayMs: Int = DEFAULT_STRUM_DELAY_MS,
    val strumDown: Boolean = true,
    val playOnTap: Boolean = false,
) {
    companion object {
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
}

/**
 * Settings for display preferences (note naming, theme).
 *
 * @property useFlats When true, note names use flats (Db, Eb, Gb, Ab, Bb)
 *   instead of sharps (C#, D#, F#, G#, A#).
 * @property themeMode The app color theme: Light, Dark, or follow the System setting.
 */
data class DisplaySettings(
    val useFlats: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

/**
 * Available ukulele tunings.
 *
 * @property label Human-readable name for the tuning.
 * @property gStringOctave The octave of the G string (4 for high-G, 3 for low-G).
 */
enum class UkuleleTuning(val label: String, val gStringOctave: Int) {
    HIGH_G("High-G (Standard)", 4),
    LOW_G("Low-G", 3),
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
 */
data class FretboardSettings(
    val leftHanded: Boolean = false,
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
)
