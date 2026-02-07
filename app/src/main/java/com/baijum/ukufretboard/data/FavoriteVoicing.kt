package com.baijum.ukufretboard.data

/**
 * A saved chord voicing in the favorites list.
 *
 * @property rootPitchClass The pitch class (0–11) of the chord root.
 * @property chordSymbol The chord quality symbol (e.g., "m7", "sus2", "").
 * @property frets The fret positions for each string (e.g., [0, 0, 0, 3]).
 * @property addedAt Timestamp when the voicing was saved.
 */
data class FavoriteVoicing(
    val rootPitchClass: Int,
    val chordSymbol: String,
    val frets: List<Int>,
    val addedAt: Long = System.currentTimeMillis(),
) {
    /**
     * A unique key for deduplication: root + symbol + frets.
     */
    val key: String get() = "$rootPitchClass|$chordSymbol|${frets.joinToString(",")}"
}
