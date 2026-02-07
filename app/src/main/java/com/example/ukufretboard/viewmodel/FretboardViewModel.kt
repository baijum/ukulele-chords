package com.example.ukufretboard.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ukufretboard.data.Notes
import com.example.ukufretboard.domain.ChordDetector
import com.example.ukufretboard.domain.Note
import com.example.ukufretboard.domain.calculateNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Represents a single ukulele string with its tuning information.
 *
 * @property name The display name of the string (e.g., "G", "C", "E", "A").
 * @property openPitchClass The pitch class (0–11) of the string when played open (unfretted).
 */
data class UkuleleString(
    val name: String,
    val openPitchClass: Int,
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
)

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
    }

    /**
     * The current tuning configuration.
     *
     * Exposed as a property to allow future extension to alternative tunings
     * (e.g., Low-G tuning where G4 is replaced with G3).
     */
    val tuning: List<UkuleleString> = STANDARD_TUNING

    private val _uiState = MutableStateFlow(FretboardUiState())

    /** Observable UI state for the fretboard screen. */
    val uiState: StateFlow<FretboardUiState> = _uiState.asStateFlow()

    /**
     * Toggles a fret selection on the specified string.
     *
     * - If the fret is already selected on that string, it is deselected (set to null).
     * - If a different fret (or no fret) is selected, the new fret becomes selected.
     * - Only one fret per string can be selected at a time, matching real ukulele behavior.
     *
     * After toggling, chord detection is automatically re-run on the new selection.
     *
     * @param stringIndex The index of the string (0 = G, 1 = C, 2 = E, 3 = A).
     * @param fret The fret number (0 = open string, 1–12 = fretted positions).
     */
    fun toggleFret(stringIndex: Int, fret: Int) {
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
     * Toggles the display of note names on the fretboard cells.
     */
    fun toggleNoteNames() {
        _uiState.update { it.copy(showNoteNames = !it.showNoteNames) }
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
        return calculateNote(openPitchClass, fret)
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
        return ChordDetector.detect(pitchClasses)
    }
}
