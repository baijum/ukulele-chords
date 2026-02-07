package com.baijum.ukufretboard.data

/**
 * Mapping of pitch class integers (0–11) to their human-readable note names.
 *
 * Pitch classes follow the standard chromatic scale where C = 0.
 * This is the universal numbering used in music theory for the twelve
 * tones of the Western chromatic scale.
 */
object Notes {

    /**
     * Note names indexed by pitch class.
     *
     * Index 0 = C, 1 = C#, 2 = D, ... 11 = B.
     * Sharps are used (no flats) for simplicity and consistency.
     */
    val NOTE_NAMES: List<String> = listOf(
        "C", "C#", "D", "D#", "E", "F",
        "F#", "G", "G#", "A", "A#", "B"
    )

    /** Total number of pitch classes in the chromatic scale. */
    const val PITCH_CLASS_COUNT = 12

    /**
     * Converts a pitch class integer to its human-readable note name.
     *
     * @param pitchClass An integer from 0 to 11 representing a pitch class.
     * @return The note name (e.g., "C", "F#", "B").
     * @throws IndexOutOfBoundsException if [pitchClass] is not in 0..11.
     */
    fun pitchClassToName(pitchClass: Int): String = NOTE_NAMES[pitchClass]
}
