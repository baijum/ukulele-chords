package com.baijum.ukufretboard.data

import java.time.LocalDate

/**
 * A chord entry for the Chord of the Day feature.
 *
 * @property rootPitchClass The pitch class (0–11) of the chord root.
 * @property symbol The chord quality symbol (e.g., "", "m", "7").
 * @property frets A representative voicing as fret positions.
 */
data class ChordOfDayEntry(
    val rootPitchClass: Int,
    val symbol: String,
    val frets: List<Int>,
)

/**
 * Curated list of chords and date-based selection logic for
 * the Chord of the Day widget.
 */
object ChordOfDay {

    /**
     * Curated chord list ordered roughly by difficulty.
     * Cycles through all chords before repeating.
     */
    val CURATED_CHORDS: List<ChordOfDayEntry> = listOf(
        // Week 1–2: Open major chords
        ChordOfDayEntry(0, "", listOf(0, 0, 0, 3)),       // C
        ChordOfDayEntry(5, "", listOf(2, 0, 1, 0)),       // F
        ChordOfDayEntry(7, "", listOf(0, 2, 3, 2)),       // G
        ChordOfDayEntry(9, "", listOf(2, 1, 0, 0)),       // A
        ChordOfDayEntry(2, "", listOf(2, 2, 2, 0)),       // D

        // Week 3–4: Open minor chords
        ChordOfDayEntry(9, "m", listOf(2, 0, 0, 0)),      // Am
        ChordOfDayEntry(2, "m", listOf(2, 2, 1, 0)),      // Dm
        ChordOfDayEntry(4, "m", listOf(0, 4, 3, 2)),      // Em

        // Week 5–6: Seventh chords
        ChordOfDayEntry(7, "7", listOf(0, 2, 1, 2)),      // G7
        ChordOfDayEntry(0, "7", listOf(0, 0, 0, 1)),      // C7
        ChordOfDayEntry(9, "7", listOf(0, 1, 0, 0)),      // A7
        ChordOfDayEntry(2, "7", listOf(2, 2, 2, 3)),      // D7

        // Week 7–8: Minor 7ths and Major 7ths
        ChordOfDayEntry(9, "m7", listOf(0, 0, 0, 0)),     // Am7
        ChordOfDayEntry(2, "m7", listOf(2, 2, 1, 3)),     // Dm7
        ChordOfDayEntry(0, "maj7", listOf(0, 0, 0, 2)),   // Cmaj7
        ChordOfDayEntry(5, "maj7", listOf(2, 4, 1, 3)),   // Fmaj7

        // Week 9–10: Suspended chords
        ChordOfDayEntry(9, "sus2", listOf(2, 4, 5, 2)),   // Asus2
        ChordOfDayEntry(2, "sus4", listOf(0, 2, 3, 0)),   // Dsus4
        ChordOfDayEntry(0, "sus2", listOf(0, 2, 0, 3)),   // Csus2

        // Week 11–12: More variety
        ChordOfDayEntry(4, "", listOf(4, 4, 4, 7)),       // E
        ChordOfDayEntry(10, "", listOf(3, 2, 1, 1)),      // Bb
        ChordOfDayEntry(1, "", listOf(1, 1, 1, 4)),       // Db/C#

        // Week 13+: Diminished and augmented
        ChordOfDayEntry(11, "dim", listOf(1, 2, 1, 2)),   // Bdim
        ChordOfDayEntry(0, "aug", listOf(1, 0, 0, 3)),    // Caug
    )

    /**
     * Returns the chord of the day for a given date.
     *
     * Uses a deterministic date-based index to ensure:
     * - Same chord all day
     * - Different chord each day
     * - Wraps around after exhausting the list
     *
     * @param date The date to get the chord for.
     * @return The [ChordOfDayEntry] for the given date.
     */
    fun chordForDate(date: LocalDate = LocalDate.now()): ChordOfDayEntry {
        val daysSinceEpoch = date.toEpochDay()
        val index = (daysSinceEpoch % CURATED_CHORDS.size).toInt().let {
            if (it < 0) it + CURATED_CHORDS.size else it
        }
        return CURATED_CHORDS[index]
    }
}
