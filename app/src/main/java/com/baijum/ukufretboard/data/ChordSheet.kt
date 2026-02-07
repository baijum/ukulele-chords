package com.baijum.ukufretboard.data

import java.util.UUID

/**
 * A chord sheet containing lyrics with inline chord markers.
 *
 * Chord names are embedded in the content using square brackets, e.g.,
 * `"Some[C]where over the [Em]rainbow"`.
 *
 * @property id Unique identifier.
 * @property title Song title.
 * @property artist Artist name (optional).
 * @property content Raw text with `[ChordName]` markers.
 * @property createdAt Timestamp when created.
 * @property updatedAt Timestamp when last modified.
 */
data class ChordSheet(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String = "",
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
