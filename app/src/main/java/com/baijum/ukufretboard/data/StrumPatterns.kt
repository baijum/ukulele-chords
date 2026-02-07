package com.baijum.ukufretboard.data

/**
 * Direction of a single strum beat.
 */
enum class StrumDirection(val symbol: String) {
    DOWN("↓"),
    UP("↑"),
    MISS("×"),
    PAUSE("—"),
}

/**
 * Difficulty level for strumming patterns.
 */
enum class Difficulty(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
}

/**
 * A single beat in a strumming pattern.
 *
 * @property direction The strum direction for this beat.
 * @property emphasis Whether this beat is accented.
 */
data class StrumBeat(
    val direction: StrumDirection,
    val emphasis: Boolean = false,
)

/**
 * A complete strumming pattern with metadata.
 *
 * @property name Human-readable name of the pattern.
 * @property description Brief description of when/how to use the pattern.
 * @property difficulty Skill level required.
 * @property beatsPerMeasure Time signature numerator (e.g., 4 for 4/4 time).
 * @property beats The sequence of strum beats.
 * @property notation Compact text notation (e.g., "D - D U - U D U").
 * @property suggestedBpm Recommended tempo range in BPM.
 */
data class StrumPattern(
    val name: String,
    val description: String,
    val difficulty: Difficulty,
    val beatsPerMeasure: Int = 4,
    val beats: List<StrumBeat>,
    val notation: String,
    val suggestedBpm: IntRange,
)

/**
 * Library of common ukulele strumming patterns.
 */
object StrumPatterns {

    val ALL: List<StrumPattern> = listOf(
        StrumPattern(
            name = "All Downs",
            description = "The simplest strum — perfect for absolute beginners. One down strum per beat.",
            difficulty = Difficulty.BEGINNER,
            beats = listOf(
                StrumBeat(StrumDirection.DOWN, emphasis = true),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.DOWN),
            ),
            notation = "D  D  D  D",
            suggestedBpm = 80..120,
        ),
        StrumPattern(
            name = "Down-Up",
            description = "Alternating down and up strums on every eighth note. Builds right-hand fluency.",
            difficulty = Difficulty.BEGINNER,
            beats = listOf(
                StrumBeat(StrumDirection.DOWN, emphasis = true),
                StrumBeat(StrumDirection.UP),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.UP),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.UP),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.UP),
            ),
            notation = "D U D U D U D U",
            suggestedBpm = 80..120,
        ),
        StrumPattern(
            name = "Island Strum",
            description = "The classic ukulele strum heard in Hawaiian and tropical music. Syncopated and relaxed.",
            difficulty = Difficulty.BEGINNER,
            beats = listOf(
                StrumBeat(StrumDirection.DOWN, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.UP),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.UP),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.UP),
            ),
            notation = "D - D U - U D U",
            suggestedBpm = 90..130,
        ),
        StrumPattern(
            name = "Folk Strum",
            description = "A steady pattern common in folk and campfire songs. Easy to maintain at moderate tempos.",
            difficulty = Difficulty.BEGINNER,
            beats = listOf(
                StrumBeat(StrumDirection.DOWN, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.UP),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.UP),
            ),
            notation = "D - D U D - D U",
            suggestedBpm = 80..120,
        ),
        StrumPattern(
            name = "Reggae",
            description = "Emphasizes the offbeat — the signature reggae feel. Mute or pause on the downbeat.",
            difficulty = Difficulty.BEGINNER,
            beats = listOf(
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.DOWN, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.DOWN, emphasis = true),
            ),
            notation = "-  D  -  D",
            suggestedBpm = 70..100,
        ),
        StrumPattern(
            name = "Calypso",
            description = "A syncopated Caribbean pattern. Feels bouncy and upbeat once you get the hang of it.",
            difficulty = Difficulty.INTERMEDIATE,
            beats = listOf(
                StrumBeat(StrumDirection.DOWN, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.DOWN),
                StrumBeat(StrumDirection.UP),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.UP),
            ),
            notation = "D - D U - U",
            suggestedBpm = 100..140,
        ),
        StrumPattern(
            name = "Ska",
            description = "All upstrokes on the offbeat — creates a bright, choppy sound. Classic ska and punk feel.",
            difficulty = Difficulty.INTERMEDIATE,
            beats = listOf(
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.UP, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.UP, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.UP, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.UP, emphasis = true),
            ),
            notation = "- U - U - U - U",
            suggestedBpm = 120..160,
        ),
        StrumPattern(
            name = "Swing",
            description = "A shuffled down-up feel with a triplet groove. Great for jazz and blues.",
            difficulty = Difficulty.INTERMEDIATE,
            beats = listOf(
                StrumBeat(StrumDirection.DOWN, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.UP),
                StrumBeat(StrumDirection.DOWN, emphasis = true),
                StrumBeat(StrumDirection.PAUSE),
                StrumBeat(StrumDirection.UP),
            ),
            notation = "D - U D - U",
            suggestedBpm = 80..120,
        ),
    )
}
