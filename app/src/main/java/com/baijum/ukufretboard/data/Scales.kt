package com.baijum.ukufretboard.data

/**
 * A musical scale defined by its interval pattern.
 *
 * @property name Human-readable name.
 * @property intervals List of semitone intervals from the root (always starts with 0).
 */
data class Scale(
    val name: String,
    val intervals: List<Int>,
)

/**
 * Library of common musical scales for fretboard overlay.
 */
object Scales {

    val ALL: List<Scale> = listOf(
        // ── Diatonic scales ──
        Scale("Major",             listOf(0, 2, 4, 5, 7, 9, 11)),
        Scale("Natural Minor",     listOf(0, 2, 3, 5, 7, 8, 10)),
        Scale("Harmonic Minor",    listOf(0, 2, 3, 5, 7, 8, 11)),
        Scale("Melodic Minor",     listOf(0, 2, 3, 5, 7, 9, 11)),
        // ── Pentatonic & Blues ──
        Scale("Pentatonic Major",  listOf(0, 2, 4, 7, 9)),
        Scale("Pentatonic Minor",  listOf(0, 3, 5, 7, 10)),
        Scale("Blues",             listOf(0, 3, 5, 6, 7, 10)),
        // ── Modes ──
        Scale("Dorian",            listOf(0, 2, 3, 5, 7, 9, 10)),
        Scale("Phrygian",          listOf(0, 1, 3, 5, 7, 8, 10)),
        Scale("Lydian",            listOf(0, 2, 4, 6, 7, 9, 11)),
        Scale("Mixolydian",        listOf(0, 2, 4, 5, 7, 9, 10)),
        Scale("Locrian",           listOf(0, 1, 3, 5, 6, 8, 10)),
        // ── Symmetric scales ──
        Scale("Whole Tone",        listOf(0, 2, 4, 6, 8, 10)),
        Scale("Chromatic",         listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
    )

    /**
     * Computes the set of pitch classes in a scale for a given root.
     *
     * @param root The root pitch class (0–11).
     * @param scale The scale definition.
     * @return A set of pitch classes (0–11) belonging to the scale.
     */
    fun scaleNotes(root: Int, scale: Scale): Set<Int> =
        scale.intervals.map { (root + it) % Notes.PITCH_CLASS_COUNT }.toSet()
}
