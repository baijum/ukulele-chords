package com.baijum.ukufretboard.domain

import com.baijum.ukufretboard.data.ChordFormula
import com.baijum.ukufretboard.data.Notes

/**
 * Utility functions for computing detailed chord information —
 * interval breakdowns, formula strings, fingering suggestions,
 * and difficulty ratings.
 *
 * All functions are pure and stateless.
 */
object ChordInfo {

    // ── Interval names (abbreviated) ────────────────────────────────────
    /** Maps a semitone interval (0–11) to its abbreviated interval name. */
    private val INTERVAL_NAMES = mapOf(
        0 to "Root",
        1 to "m2",
        2 to "M2",
        3 to "m3",
        4 to "M3",
        5 to "P4",
        6 to "d5",
        7 to "P5",
        8 to "m6",
        9 to "M6",
        10 to "m7",
        11 to "M7",
    )

    // ── Formula degree labels ───────────────────────────────────────────
    /** Maps a semitone interval (0–11) to its scale-degree formula symbol. */
    private val FORMULA_DEGREES = mapOf(
        0 to "1",
        1 to "b2",
        2 to "2",
        3 to "b3",
        4 to "3",
        5 to "4",
        6 to "b5",
        7 to "5",
        8 to "#5",
        9 to "6",
        10 to "b7",
        11 to "7",
    )

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Builds a human-readable interval breakdown string.
     *
     * Example output: `"C (Root)  Eb (m3)  G (P5)  Bb (m7)"`
     *
     * Notes are ordered by their interval from the root (ascending).
     *
     * @param root The root [Note] of the chord.
     * @param notes All unique notes in the chord.
     * @param useFlats Whether to use flat names for note display.
     * @return A formatted string showing each note with its interval role.
     */
    fun buildIntervalBreakdown(root: Note, notes: List<Note>, useFlats: Boolean = false): String {
        return notes
            .map { note ->
                val interval = (note.pitchClass - root.pitchClass + Notes.PITCH_CLASS_COUNT) %
                    Notes.PITCH_CLASS_COUNT
                val intervalName = INTERVAL_NAMES[interval] ?: "?"
                "${note.name} ($intervalName)"
            }
            .sortedBy { entry ->
                // Sort by interval value for consistent ordering
                val noteName = entry.substringBefore(" (")
                val pc = if (useFlats) {
                    Notes.NOTE_NAMES_FLAT.indexOf(noteName)
                } else {
                    Notes.NOTE_NAMES_SHARP.indexOf(noteName)
                }
                (pc - root.pitchClass + Notes.PITCH_CLASS_COUNT) % Notes.PITCH_CLASS_COUNT
            }
            .joinToString("  ")
    }

    /**
     * Builds a chord formula string from a [ChordFormula].
     *
     * Example output: `"1 b3 5 b7"`
     *
     * Intervals are sorted ascending and mapped to their scale-degree symbols.
     *
     * @param formula The chord formula containing the interval set.
     * @return A space-separated formula string.
     */
    fun buildFormulaString(formula: ChordFormula): String {
        return formula.intervals
            .sorted()
            .mapNotNull { FORMULA_DEGREES[it] }
            .joinToString("  ")
    }

    /**
     * Suggests which finger to use for each string based on fret positions.
     *
     * Returns a list of 4 integers (one per string, in G-C-E-A order):
     * - `0` = open string (no finger needed)
     * - `1` = index finger
     * - `2` = middle finger
     * - `3` = ring finger
     * - `4` = pinky finger
     *
     * The heuristic works as follows:
     * 1. Open strings get finger 0.
     * 2. If all fretted strings are on the same fret (barre), they all get finger 1.
     * 3. Otherwise, fretted positions are ranked by fret number and assigned
     *    ascending fingers starting from 1.
     *
     * @param frets A list of 4 fret numbers (one per string). All values 0–12.
     * @return A list of 4 finger assignments.
     */
    fun suggestFingering(frets: List<Int>): List<Int> {
        if (frets.size != 4) return frets.map { if (it == 0) 0 else 1 }

        // Collect the distinct fretted (non-zero) fret values, sorted ascending
        val frettedValues = frets.filter { it > 0 }.distinct().sorted()

        if (frettedValues.isEmpty()) {
            // All open strings
            return listOf(0, 0, 0, 0)
        }

        // Map each distinct fret value to a finger number (1-based)
        val fretToFinger = mutableMapOf<Int, Int>()
        frettedValues.forEachIndexed { index, fretValue ->
            fretToFinger[fretValue] = (index + 1).coerceAtMost(4)
        }

        return frets.map { fret ->
            if (fret == 0) 0 else fretToFinger[fret] ?: 1
        }
    }

    /**
     * Formats a fingering list as a display string.
     *
     * Example output: `"0  0  1  1"`
     *
     * @param fingering A list of 4 finger assignments from [suggestFingering].
     * @return A formatted string.
     */
    fun formatFingering(fingering: List<Int>): String {
        return fingering.joinToString("  ")
    }

    /**
     * Rates the difficulty of a chord voicing based on its fret positions.
     *
     * @param frets A list of 4 fret numbers (one per string). All values 0–12.
     * @return A human-readable difficulty label.
     */
    fun rateDifficulty(frets: List<Int>): String {
        val frettedPositions = frets.filter { it > 0 }

        // All open strings
        if (frettedPositions.isEmpty()) return "Very easy"

        val frettedCount = frettedPositions.size
        val minFret = frettedPositions.min()
        val maxFret = frettedPositions.max()
        val span = maxFret - minFret

        // Single fretted string at a low position
        if (frettedCount == 1 && maxFret <= 3) return "Very easy"

        // All fretted strings on the same fret (barre) at low position
        if (span == 0 && maxFret <= 3) return "Easy"

        // Small span, low frets
        if (span <= 2 && maxFret <= 5) return "Easy"

        // Moderate span or barre shapes
        if (span <= 3 && maxFret <= 7) return "Medium"

        // Wide span or high fret positions
        return "Hard"
    }
}
