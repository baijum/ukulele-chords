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
     * Note names indexed by pitch class, using sharps for accidentals.
     *
     * Index 0 = C, 1 = C#, 2 = D, ... 11 = B.
     */
    val NOTE_NAMES_SHARP: List<String> = listOf(
        "C", "C#", "D", "D#", "E", "F",
        "F#", "G", "G#", "A", "A#", "B"
    )

    /**
     * Note names indexed by pitch class, using flats for accidentals.
     *
     * Index 0 = C, 1 = Db, 2 = D, ... 11 = B.
     */
    val NOTE_NAMES_FLAT: List<String> = listOf(
        "C", "Db", "D", "Eb", "E", "F",
        "Gb", "G", "Ab", "A", "Bb", "B"
    )

    /**
     * Alias for [NOTE_NAMES_SHARP] to maintain backward compatibility.
     */
    val NOTE_NAMES: List<String> = NOTE_NAMES_SHARP

    /** Total number of pitch classes in the chromatic scale. */
    const val PITCH_CLASS_COUNT = 12

    /**
     * Converts a pitch class integer to its human-readable note name.
     *
     * @param pitchClass An integer from 0 to 11 representing a pitch class.
     * @param useFlats When true, returns flat names (Db, Eb, etc.);
     *   when false, returns sharp names (C#, D#, etc.).
     * @return The note name (e.g., "C", "F#"/"Gb", "B").
     * @throws IndexOutOfBoundsException if [pitchClass] is not in 0..11.
     */
    fun pitchClassToName(pitchClass: Int, useFlats: Boolean = false): String =
        if (useFlats) NOTE_NAMES_FLAT[pitchClass] else NOTE_NAMES_SHARP[pitchClass]
}
