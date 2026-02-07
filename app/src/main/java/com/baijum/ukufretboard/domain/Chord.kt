package com.baijum.ukufretboard.domain

import com.baijum.ukufretboard.data.ChordFormula

/**
 * Represents a successfully detected chord.
 *
 * @property name The full display name of the chord (e.g., "Am", "G7", "Cmaj7").
 *   Composed of the root note name plus the chord formula symbol.
 * @property quality A human-readable description of the chord quality
 *   (e.g., "Minor", "Dominant 7th", "Major").
 * @property root The root [Note] of the chord — the note that gives the chord its letter name.
 * @property notes All unique [Note]s that make up the chord.
 * @property matchedFormula The [ChordFormula] that was matched during detection,
 *   providing access to interval data for display purposes. Null when the chord
 *   was not detected via formula matching.
 */
data class ChordResult(
    val name: String,
    val quality: String,
    val root: Note,
    val notes: List<Note>,
    val matchedFormula: ChordFormula? = null,
)

/**
 * Represents a specific way to play a chord on the ukulele fretboard.
 *
 * A single chord (e.g., C major) can be played in multiple positions on the
 * fretboard. Each voicing specifies the exact fret for each of the 4 strings.
 *
 * @property frets A list of 4 fret numbers, one per string (G, C, E, A in order).
 *   All values are 0–12; every string is always played (ukulele rarely mutes strings).
 * @property notes The [Note] produced at each string position, in string order.
 * @property minFret The lowest fretted (non-open) position, or 0 if all strings are open.
 *   Used to determine the diagram display range.
 * @property maxFret The highest fret used in this voicing.
 */
data class ChordVoicing(
    val frets: List<Int>,
    val notes: List<Note>,
    val minFret: Int,
    val maxFret: Int,
)
