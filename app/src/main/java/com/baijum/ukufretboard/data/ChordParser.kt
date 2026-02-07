package com.baijum.ukufretboard.data

/**
 * Parses chord names from text containing `[ChordName]` markers.
 *
 * Supported chord name formats:
 * - Root note: A-G, optionally followed by # or b
 * - Quality: m, maj, dim, aug, sus, 7, m7, maj7, 6, m6, 9, m9, add9, sus2, sus4, etc.
 */
object ChordParser {

    /** Regex matching `[ChordName]` markers in text. */
    private val CHORD_PATTERN = Regex("""\[([A-G][#b]?(?:m(?:aj)?|dim|aug|sus)?(?:7|9|6|add9|sus[24]|maj7|m7)?)\]""")

    /**
     * Extracts all unique chord names from the given text.
     *
     * @param text The text containing `[ChordName]` markers.
     * @return A list of unique chord name strings found in order.
     */
    fun extractChords(text: String): List<String> =
        CHORD_PATTERN.findAll(text)
            .map { it.groupValues[1] }
            .distinct()
            .toList()

    /**
     * Splits text into segments: plain text and chord markers.
     *
     * @param line A single line of text.
     * @return A list of [TextSegment] alternating between text and chord markers.
     */
    fun parseLine(line: String): List<TextSegment> {
        val segments = mutableListOf<TextSegment>()
        var lastEnd = 0

        CHORD_PATTERN.findAll(line).forEach { match ->
            // Add text before the chord
            if (match.range.first > lastEnd) {
                segments.add(TextSegment.PlainText(line.substring(lastEnd, match.range.first)))
            }
            // Add the chord
            segments.add(TextSegment.Chord(match.groupValues[1]))
            lastEnd = match.range.last + 1
        }

        // Add remaining text
        if (lastEnd < line.length) {
            segments.add(TextSegment.PlainText(line.substring(lastEnd)))
        }

        return segments
    }

    /**
     * A segment of parsed text — either plain text or a chord name.
     */
    sealed class TextSegment {
        data class PlainText(val text: String) : TextSegment()
        data class Chord(val name: String) : TextSegment()
    }
}
