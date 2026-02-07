package com.baijum.ukufretboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baijum.ukufretboard.audio.ToneGenerator
import com.baijum.ukufretboard.data.Notes
import com.baijum.ukufretboard.data.Scale
import com.baijum.ukufretboard.data.Scales
import com.baijum.ukufretboard.data.SoundSettings
import com.baijum.ukufretboard.data.TuningSettings
import com.baijum.ukufretboard.data.UkuleleTuning
import com.baijum.ukufretboard.domain.ChordDetector
import com.baijum.ukufretboard.domain.Note
import com.baijum.ukufretboard.domain.calculateNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents a single ukulele string with its tuning information.
 *
 * @property name The display name of the string (e.g., "G", "C", "E", "A").
 * @property openPitchClass The pitch class (0–11) of the string when played open (unfretted).
 * @property octave The octave number of the open string (e.g., 4 for G4, C4, E4, A4).
 */
data class UkuleleString(
    val name: String,
    val openPitchClass: Int,
    val octave: Int = 4,
)

/**
 * State for the scale note overlay on the fretboard.
 *
 * @property enabled Whether the scale overlay is currently visible.
 * @property root The pitch class (0–11) of the scale root.
 * @property scale The selected scale definition, or null if none.
 * @property scaleNotes The computed set of pitch classes in the scale (derived).
 */
data class ScaleOverlayState(
    val enabled: Boolean = false,
    val root: Int = 0,
    val scale: Scale? = null,
    val scaleNotes: Set<Int> = emptySet(),
)

/**
 * Immutable UI state for the fretboard screen.
 *
 * @property selections A map from string index (0–3) to the selected fret number (0–12),
 *   or null if no fret is selected on that string. Each string can have at most one
 *   selected fret, matching real ukulele behavior where each string sounds one note.
 * @property showNoteNames Whether to display note names on the fretboard cells.
 * @property detectionResult The current chord detection result based on selected frets.
 */
data class FretboardUiState(
    val selections: Map<Int, Int?> = mapOf(0 to null, 1 to null, 2 to null, 3 to null),
    val showNoteNames: Boolean = true,
    val detectionResult: ChordDetector.DetectionResult = ChordDetector.DetectionResult.NoSelection,
    val scaleOverlay: ScaleOverlayState = ScaleOverlayState(),
) {
    /**
     * Human-readable finger position string derived from the current selections.
     * Format: "0 - 0 - 0 - 3" (one entry per string, "x" for unselected strings).
     */
    val fingerPositions: String = selections.entries
        .sortedBy { it.key }
        .joinToString(" - ") { (_, fret) -> fret?.toString() ?: "x" }
}

/**
 * ViewModel that manages the fretboard UI state and chord detection logic.
 *
 * Handles user interactions (fret taps, reset, toggle note names) and automatically
 * recomputes chord detection whenever the fret selection changes. Uses [StateFlow]
 * for observable, immutable UI state that Compose can collect.
 */
class FretboardViewModel : ViewModel() {

    companion object {
        /** Number of fret positions on the ukulele (0 = open string through 12). */
        const val FRET_COUNT = 13

        /** Index of the first fret (open string). */
        const val OPEN_STRING_FRET = 0

        /** Index of the last fret. */
        const val LAST_FRET = 12

        /**
         * Standard ukulele tuning (high-G / re-entrant).
         *
         * In standard tuning, the strings are tuned to:
         * - String 0: G4 (pitch class 7)
         * - String 1: C4 (pitch class 0)
         * - String 2: E4 (pitch class 4)
         * - String 3: A4 (pitch class 9)
         *
         * Note: The G string is higher than the C string (re-entrant tuning),
         * which is the most common ukulele tuning.
         */
        val STANDARD_TUNING: List<UkuleleString> = listOf(
            UkuleleString(name = "G", openPitchClass = 7),
            UkuleleString(name = "C", openPitchClass = 0),
            UkuleleString(name = "E", openPitchClass = 4),
            UkuleleString(name = "A", openPitchClass = 9),
        )

        /**
         * Builds a tuning list for the given tuning variant.
         * Only the G string octave differs between High-G and Low-G.
         */
        fun buildTuning(variant: UkuleleTuning): List<UkuleleString> = listOf(
            UkuleleString(name = "G", openPitchClass = 7, octave = variant.gStringOctave),
            UkuleleString(name = "C", openPitchClass = 0, octave = 4),
            UkuleleString(name = "E", openPitchClass = 4, octave = 4),
            UkuleleString(name = "A", openPitchClass = 9, octave = 4),
        )
    }

    /**
     * The current tuning configuration, derived from the selected tuning variant.
     *
     * High-G: G4 (standard re-entrant). Low-G: G3.
     */
    var tuning: List<UkuleleString> = STANDARD_TUNING
        private set

    private val _uiState = MutableStateFlow(FretboardUiState())

    /** Observable UI state for the fretboard screen. */
    val uiState: StateFlow<FretboardUiState> = _uiState.asStateFlow()

    /** Current sound settings, updated from [SettingsViewModel] via the UI layer. */
    private var soundSettings: SoundSettings = SoundSettings()

    /** Whether to use flat note names, updated from [SettingsViewModel]. */
    private var useFlats: Boolean = false

    /**
     * Updates the sound settings used by playback methods.
     * Called by the UI layer when [SettingsViewModel.settings] changes.
     */
    fun setSoundSettings(settings: SoundSettings) {
        soundSettings = settings
    }

    /**
     * Updates the tuning based on settings. Only the G string octave changes
     * between High-G and Low-G; pitch classes remain the same.
     */
    fun setTuningSettings(settings: TuningSettings) {
        val newTuning = buildTuning(settings.tuning)
        if (tuning != newTuning) {
            tuning = newTuning
            // Re-detect with updated tuning (pitch classes unchanged, but re-emitting state)
            _uiState.update { current ->
                current.copy(detectionResult = detectChord(current.selections))
            }
        }
    }

    /**
     * Updates the note naming preference and re-detects the current chord.
     */
    fun setUseFlats(flats: Boolean) {
        if (useFlats != flats) {
            useFlats = flats
            // Re-detect with new note names
            _uiState.update { current ->
                current.copy(detectionResult = detectChord(current.selections))
            }
        }
    }

    /**
     * Toggles a fret selection on the specified string.
     *
     * - If the fret is already selected on that string, it is deselected (set to null).
     * - If a different fret (or no fret) is selected, the new fret becomes selected.
     * - Only one fret per string can be selected at a time, matching real ukulele behavior.
     *
     * After toggling, chord detection is automatically re-run on the new selection.
     * If play-on-tap is enabled and a fret was selected (not deselected), the note
     * is played immediately.
     *
     * @param stringIndex The index of the string (0 = G, 1 = C, 2 = E, 3 = A).
     * @param fret The fret number (0 = open string, 1–12 = fretted positions).
     */
    fun toggleFret(stringIndex: Int, fret: Int) {
        val wasSelected = _uiState.value.selections[stringIndex] == fret
        _uiState.update { current ->
            val currentFret = current.selections[stringIndex]
            val newFret = if (currentFret == fret) null else fret
            val newSelections = current.selections.toMutableMap().apply {
                this[stringIndex] = newFret
            }
            current.copy(
                selections = newSelections,
                detectionResult = detectChord(newSelections),
            )
        }

        // Play the tapped note if play-on-tap is enabled and we just selected (not deselected)
        if (!wasSelected && soundSettings.enabled && soundSettings.playOnTap) {
            playNoteAt(stringIndex, fret)
        }
    }

    /**
     * Clears all fret selections and resets the detection result.
     * Preserves the current note name visibility setting.
     */
    fun clearAll() {
        _uiState.update {
            FretboardUiState(showNoteNames = it.showNoteNames)
        }
    }

    /**
     * Applies a chord voicing from the chord library onto the fretboard.
     *
     * Sets each string's fret selection to the voicing's fret values and
     * re-runs chord detection on the resulting selection.
     *
     * @param voicing The [ChordVoicing] to apply (one fret per string).
     */
    fun applyVoicing(voicing: com.baijum.ukufretboard.domain.ChordVoicing) {
        _uiState.update { current ->
            val newSelections = voicing.frets.mapIndexed { i, fret ->
                i to (if (fret < 0) null else fret)
            }.toMap()
            current.copy(
                selections = newSelections,
                detectionResult = detectChord(newSelections),
            )
        }
    }

    /**
     * Toggles the display of note names on the fretboard cells.
     */
    fun toggleNoteNames() {
        _uiState.update { it.copy(showNoteNames = !it.showNoteNames) }
    }

    // ── Scale overlay controls ──

    /**
     * Toggles the scale overlay on/off.
     */
    fun toggleScaleOverlay() {
        _uiState.update { current ->
            val overlay = current.scaleOverlay
            current.copy(scaleOverlay = overlay.copy(enabled = !overlay.enabled))
        }
    }

    /**
     * Sets the scale overlay root note and recomputes scale notes.
     */
    fun setScaleRoot(root: Int) {
        _uiState.update { current ->
            val overlay = current.scaleOverlay
            val notes = overlay.scale?.let { Scales.scaleNotes(root, it) } ?: emptySet()
            current.copy(scaleOverlay = overlay.copy(root = root, scaleNotes = notes))
        }
    }

    /**
     * Sets the scale type and recomputes scale notes.
     */
    fun setScale(scale: Scale) {
        _uiState.update { current ->
            val overlay = current.scaleOverlay
            val notes = Scales.scaleNotes(overlay.root, scale)
            current.copy(
                scaleOverlay = overlay.copy(
                    scale = scale,
                    scaleNotes = notes,
                    enabled = true,
                )
            )
        }
    }

    /**
     * Computes the note at a given string and fret position using the current tuning.
     *
     * @param stringIndex The index of the string (0–3).
     * @param fret The fret number (0–12).
     * @return The [Note] at that position.
     */
    fun getNoteAt(stringIndex: Int, fret: Int): Note {
        val openPitchClass = tuning[stringIndex].openPitchClass
        return calculateNote(openPitchClass, fret, useFlats)
    }

    /**
     * Plays the currently selected notes as a strummed chord.
     *
     * Collects the pitch class and octave for each string that has a fret selected,
     * then delegates to [ToneGenerator.playChord] on a background coroutine.
     * Strum direction, note duration, and strum delay are taken from [soundSettings].
     *
     * If sound is disabled or no notes are selected, this method does nothing.
     */
    fun playChord() {
        if (!soundSettings.enabled) return

        val selections = _uiState.value.selections
        val notes = selections.entries
            .let { entries ->
                if (soundSettings.strumDown) entries.sortedBy { it.key }
                else entries.sortedByDescending { it.key }
            }
            .filter { it.value != null }
            .map { (stringIndex, fret) ->
                val string = tuning[stringIndex]
                val pitchClass = (string.openPitchClass + fret!!) % Notes.PITCH_CLASS_COUNT
                val octave = computeOctave(string.openPitchClass, string.octave, fret)
                pitchClass to octave
            }

        if (notes.isNotEmpty()) {
            viewModelScope.launch {
                ToneGenerator.playChord(
                    notes = notes,
                    noteDurationMs = soundSettings.noteDurationMs,
                    strumDelayMs = soundSettings.strumDelayMs,
                )
            }
        }
    }

    /**
     * Plays a specific [ChordVoicing] as a strummed chord.
     *
     * Used by the Chord Library's compare/preview features to audition
     * a voicing without applying it to the fretboard.
     *
     * @param voicing The voicing to play.
     */
    fun playVoicing(voicing: com.baijum.ukufretboard.domain.ChordVoicing) {
        if (!soundSettings.enabled) return

        val notes = voicing.frets.mapIndexed { index, fret ->
            val string = tuning[index]
            val pitchClass = (string.openPitchClass + fret) % Notes.PITCH_CLASS_COUNT
            val octave = computeOctave(string.openPitchClass, string.octave, fret)
            pitchClass to octave
        }

        viewModelScope.launch {
            ToneGenerator.playChord(
                notes = notes,
                noteDurationMs = soundSettings.noteDurationMs,
                strumDelayMs = soundSettings.strumDelayMs,
            )
        }
    }

    /**
     * Plays a list of voicings sequentially with a pause between each.
     *
     * Used by the "Play All Inversions" feature to let users hear
     * how the same chord sounds in different inversions.
     *
     * @param voicings The voicings to play in order.
     * @param pauseMs Pause between voicings in milliseconds.
     */
    fun playVoicingsSequentially(
        voicings: List<com.baijum.ukufretboard.domain.ChordVoicing>,
        pauseMs: Long = 1000L,
    ) {
        if (!soundSettings.enabled || voicings.isEmpty()) return

        viewModelScope.launch {
            voicings.forEach { voicing ->
                val notes = voicing.frets.mapIndexed { index, fret ->
                    val string = tuning[index]
                    val pitchClass = (string.openPitchClass + fret) % Notes.PITCH_CLASS_COUNT
                    val octave = computeOctave(string.openPitchClass, string.octave, fret)
                    pitchClass to octave
                }
                ToneGenerator.playChord(
                    notes = notes,
                    noteDurationMs = soundSettings.noteDurationMs,
                    strumDelayMs = soundSettings.strumDelayMs,
                )
                kotlinx.coroutines.delay(pauseMs)
            }
        }
    }

    /**
     * Plays a single note at the given string and fret position.
     *
     * Used by the play-on-tap feature. Note duration is taken from [soundSettings].
     *
     * @param stringIndex The index of the string (0–3).
     * @param fret The fret number (0–12).
     */
    private fun playNoteAt(stringIndex: Int, fret: Int) {
        val string = tuning[stringIndex]
        val pitchClass = (string.openPitchClass + fret) % Notes.PITCH_CLASS_COUNT
        val octave = computeOctave(string.openPitchClass, string.octave, fret)
        viewModelScope.launch {
            ToneGenerator.playNote(
                pitchClass = pitchClass,
                octave = octave,
                durationMs = soundSettings.noteDurationMs,
            )
        }
    }

    /**
     * Plays a single note by pitch class (for melody notepad and interval trainer).
     *
     * Uses octave 4 (middle C octave) by default.
     *
     * @param pitchClass The pitch class (0–11).
     * @param octave The octave (default 4).
     */
    fun playMelodyNote(pitchClass: Int, octave: Int = 4) {
        if (!soundSettings.enabled) return
        viewModelScope.launch {
            ToneGenerator.playNote(
                pitchClass = pitchClass,
                octave = octave,
                durationMs = soundSettings.noteDurationMs,
            )
        }
    }

    /**
     * Computes the actual octave of a note produced at a given fret on a string.
     *
     * When fretting raises the pitch class past B (11) back to C (0), the octave
     * increments by one. For example, A4 (pitch class 9) at fret 4 produces
     * C#5 (pitch class 1, octave 5).
     *
     * @param openPitchClass The pitch class of the open string.
     * @param openOctave The octave of the open string.
     * @param fret The fret number (0–12).
     * @return The octave of the fretted note.
     */
    private fun computeOctave(openPitchClass: Int, openOctave: Int, fret: Int): Int {
        return openOctave + (openPitchClass + fret) / Notes.PITCH_CLASS_COUNT
    }

    /**
     * Runs chord detection on the current set of selected fret positions.
     *
     * Collects the pitch class for each string that has a fret selected,
     * then delegates to [ChordDetector.detect] for interval-based matching.
     */
    private fun detectChord(selections: Map<Int, Int?>): ChordDetector.DetectionResult {
        val pitchClasses = selections.entries
            .filter { it.value != null }
            .map { (stringIndex, fret) ->
                val openPitchClass = tuning[stringIndex].openPitchClass
                (openPitchClass + fret!!) % Notes.PITCH_CLASS_COUNT
            }
        return ChordDetector.detect(pitchClasses, useFlats)
    }
}
