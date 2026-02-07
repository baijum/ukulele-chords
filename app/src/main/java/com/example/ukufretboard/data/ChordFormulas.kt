package com.example.ukufretboard.data

/**
 * Represents a chord type defined by its interval structure relative to the root note.
 *
 * In music theory, a chord is defined by the set of intervals (in semitones)
 * from its root note. For example, a major triad has intervals {0, 4, 7},
 * meaning: root, major third (4 semitones), perfect fifth (7 semitones).
 *
 * @property symbol The display suffix appended to the root note name (e.g., "" for major,
 *   "m" for minor, "7" for dominant seventh).
 * @property quality A human-readable description of the chord quality
 *   (e.g., "Major", "Minor 7th").
 * @property intervals The set of semitone intervals from the root that define this chord type.
 */
data class ChordFormula(
    val symbol: String,
    val quality: String,
    val intervals: Set<Int>,
)

/**
 * All supported chord formulas used for chord detection.
 *
 * The list is ordered by detection priority: simpler triads appear first,
 * followed by extended chords. When multiple formulas could match (e.g., a
 * dominant 7th chord contains a major triad), the first match in this list
 * is preferred during detection.
 */
object ChordFormulas {

    val ALL: List<ChordFormula> = listOf(
        // --- Triads ---
        ChordFormula(
            symbol = "",
            quality = "Major",
            intervals = setOf(0, 4, 7),
        ),
        ChordFormula(
            symbol = "m",
            quality = "Minor",
            intervals = setOf(0, 3, 7),
        ),
        ChordFormula(
            symbol = "dim",
            quality = "Diminished",
            intervals = setOf(0, 3, 6),
        ),
        ChordFormula(
            symbol = "aug",
            quality = "Augmented",
            intervals = setOf(0, 4, 8),
        ),
        ChordFormula(
            symbol = "sus2",
            quality = "Suspended 2nd",
            intervals = setOf(0, 2, 7),
        ),
        ChordFormula(
            symbol = "sus4",
            quality = "Suspended 4th",
            intervals = setOf(0, 5, 7),
        ),
        // --- Seventh chords ---
        ChordFormula(
            symbol = "7",
            quality = "Dominant 7th",
            intervals = setOf(0, 4, 7, 10),
        ),
        ChordFormula(
            symbol = "m7",
            quality = "Minor 7th",
            intervals = setOf(0, 3, 7, 10),
        ),
        ChordFormula(
            symbol = "maj7",
            quality = "Major 7th",
            intervals = setOf(0, 4, 7, 11),
        ),
    )
}
