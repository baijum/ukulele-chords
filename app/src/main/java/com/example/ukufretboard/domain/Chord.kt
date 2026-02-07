package com.example.ukufretboard.domain

/**
 * Represents a successfully detected chord.
 *
 * @property name The full display name of the chord (e.g., "Am", "G7", "Cmaj7").
 *   Composed of the root note name plus the chord formula symbol.
 * @property quality A human-readable description of the chord quality
 *   (e.g., "Minor", "Dominant 7th", "Major").
 * @property root The root [Note] of the chord — the note that gives the chord its letter name.
 * @property notes All unique [Note]s that make up the chord.
 */
data class ChordResult(
    val name: String,
    val quality: String,
    val root: Note,
    val notes: List<Note>,
)
