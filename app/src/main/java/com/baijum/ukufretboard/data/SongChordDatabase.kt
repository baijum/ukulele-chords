package com.baijum.ukufretboard.data

/**
 * Built-in database of popular ukulele songs with their chord sets.
 *
 * Used by the Song Finder feature to suggest songs based on chords
 * the user already knows.
 */
object SongChordDatabase {

    /**
     * A song entry with its chord requirements.
     *
     * @property title Song title.
     * @property artist Artist name.
     * @property chords Set of chord names needed to play the song.
     * @property difficulty Approximate difficulty level.
     * @property genre Musical genre.
     */
    data class SongEntry(
        val title: String,
        val artist: String,
        val chords: Set<String>,
        val difficulty: Difficulty,
        val genre: String,
    )

    enum class Difficulty(val label: String) {
        BEGINNER("Beginner"),
        INTERMEDIATE("Intermediate"),
        ADVANCED("Advanced"),
    }

    /**
     * Curated list of popular ukulele songs with their chords.
     */
    val SONGS: List<SongEntry> = listOf(
        // ── Beginner (2-3 chords) ──────────────────────────────────
        SongEntry("Riptide", "Vance Joy", setOf("Am", "G", "C", "F"), Difficulty.BEGINNER, "Pop"),
        SongEntry("I'm Yours", "Jason Mraz", setOf("C", "G", "Am", "F"), Difficulty.BEGINNER, "Pop"),
        SongEntry("Somewhere Over the Rainbow", "Israel Kamakawiwo'ole", setOf("C", "Em", "Am", "F", "G"), Difficulty.BEGINNER, "Hawaiian"),
        SongEntry("Three Little Birds", "Bob Marley", setOf("A", "D", "E"), Difficulty.BEGINNER, "Reggae"),
        SongEntry("You Are My Sunshine", "Traditional", setOf("C", "F", "G"), Difficulty.BEGINNER, "Folk"),
        SongEntry("Stand By Me", "Ben E. King", setOf("C", "Am", "F", "G"), Difficulty.BEGINNER, "Soul"),
        SongEntry("Let It Be", "The Beatles", setOf("C", "G", "Am", "F"), Difficulty.BEGINNER, "Rock"),
        SongEntry("Lean On Me", "Bill Withers", setOf("C", "F", "G"), Difficulty.BEGINNER, "Soul"),
        SongEntry("Leaving on a Jet Plane", "John Denver", setOf("C", "F", "G"), Difficulty.BEGINNER, "Folk"),
        SongEntry("Happy Birthday", "Traditional", setOf("C", "F", "G"), Difficulty.BEGINNER, "Traditional"),
        SongEntry("Love Me Do", "The Beatles", setOf("G", "C", "D"), Difficulty.BEGINNER, "Rock"),
        SongEntry("Bad Moon Rising", "CCR", setOf("G", "C", "D"), Difficulty.BEGINNER, "Rock"),
        SongEntry("Jambalaya", "Hank Williams", setOf("C", "G"), Difficulty.BEGINNER, "Country"),
        SongEntry("Achy Breaky Heart", "Billy Ray Cyrus", setOf("A", "E"), Difficulty.BEGINNER, "Country"),
        SongEntry("Twist and Shout", "The Beatles", setOf("D", "G", "A"), Difficulty.BEGINNER, "Rock"),
        SongEntry("La Bamba", "Ritchie Valens", setOf("C", "F", "G"), Difficulty.BEGINNER, "Latin"),
        SongEntry("What I Got", "Sublime", setOf("D", "G"), Difficulty.BEGINNER, "Rock"),
        SongEntry("Wagon Wheel", "Old Crow Medicine Show", setOf("G", "D", "Em", "C"), Difficulty.BEGINNER, "Folk"),
        SongEntry("Ho Hey", "The Lumineers", setOf("C", "F", "Am", "G"), Difficulty.BEGINNER, "Folk"),
        SongEntry("Count on Me", "Bruno Mars", setOf("C", "Em", "Am", "G", "F"), Difficulty.BEGINNER, "Pop"),

        // ── Intermediate (3-5 chords, some barre) ─────────────────
        SongEntry("Hey Soul Sister", "Train", setOf("C", "G", "Am", "F"), Difficulty.INTERMEDIATE, "Pop"),
        SongEntry("Hallelujah", "Leonard Cohen", setOf("C", "Am", "F", "G", "E"), Difficulty.INTERMEDIATE, "Folk"),
        SongEntry("Can't Help Falling in Love", "Elvis Presley", setOf("C", "Em", "Am", "F", "G", "A7"), Difficulty.INTERMEDIATE, "Pop"),
        SongEntry("House of the Rising Sun", "The Animals", setOf("Am", "C", "D", "F", "E"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Have You Ever Seen the Rain", "CCR", setOf("C", "G", "Am", "F"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Redemption Song", "Bob Marley", setOf("G", "Em", "C", "Am", "D"), Difficulty.INTERMEDIATE, "Reggae"),
        SongEntry("Perfect", "Ed Sheeran", setOf("G", "Em", "C", "D"), Difficulty.INTERMEDIATE, "Pop"),
        SongEntry("Someone Like You", "Adele", setOf("G", "D", "Em", "C"), Difficulty.INTERMEDIATE, "Pop"),
        SongEntry("Hey Jude", "The Beatles", setOf("C", "G", "F", "Am", "Dm"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Blowin' in the Wind", "Bob Dylan", setOf("C", "F", "G"), Difficulty.INTERMEDIATE, "Folk"),
        SongEntry("Peaceful Easy Feeling", "Eagles", setOf("E", "A", "B7"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Photograph", "Ed Sheeran", setOf("G", "C", "D", "Em"), Difficulty.INTERMEDIATE, "Pop"),
        SongEntry("Wish You Were Here", "Pink Floyd", setOf("G", "Em", "A", "C", "D"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Brown Eyed Girl", "Van Morrison", setOf("G", "C", "D", "Em"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Ring of Fire", "Johnny Cash", setOf("G", "C", "D"), Difficulty.INTERMEDIATE, "Country"),

        // ── More diverse ──────────────────────────────────────────
        SongEntry("Coconut", "Harry Nilsson", setOf("C", "G7"), Difficulty.BEGINNER, "Pop"),
        SongEntry("Pearly Shells", "Don Ho", setOf("C", "F", "G7"), Difficulty.BEGINNER, "Hawaiian"),
        SongEntry("Tiny Bubbles", "Don Ho", setOf("C", "F", "G7", "C7"), Difficulty.BEGINNER, "Hawaiian"),
        SongEntry("Over the Rainbow / What a Wonderful World", "Israel Kamakawiwo'ole", setOf("C", "Em", "Am", "F", "G"), Difficulty.BEGINNER, "Hawaiian"),
        SongEntry("Yellow Submarine", "The Beatles", setOf("G", "D", "C", "Em", "Am"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Octopus's Garden", "The Beatles", setOf("C", "Am", "F", "G"), Difficulty.BEGINNER, "Rock"),
        SongEntry("Creep", "Radiohead", setOf("G", "B", "C", "Cm"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Wonderwall", "Oasis", setOf("Em", "G", "D", "A7sus4", "C"), Difficulty.INTERMEDIATE, "Rock"),
        SongEntry("Budapest", "George Ezra", setOf("G", "C", "D"), Difficulty.BEGINNER, "Pop"),
        SongEntry("All Along the Watchtower", "Bob Dylan", setOf("Am", "G", "F"), Difficulty.BEGINNER, "Rock"),
        SongEntry("No Woman No Cry", "Bob Marley", setOf("C", "G", "Am", "F"), Difficulty.BEGINNER, "Reggae"),
        SongEntry("Africa", "Toto", setOf("A", "C#m", "F#m", "D", "E"), Difficulty.ADVANCED, "Pop"),
        SongEntry("Don't Worry Be Happy", "Bobby McFerrin", setOf("C", "Dm", "F"), Difficulty.BEGINNER, "Pop"),
        SongEntry("Moon River", "Andy Williams", setOf("C", "Am", "F", "G", "Em", "Dm"), Difficulty.INTERMEDIATE, "Jazz"),
        SongEntry("Take Me Home Country Roads", "John Denver", setOf("G", "Em", "D", "C"), Difficulty.BEGINNER, "Country"),
    )

    /**
     * Finds songs that the user can play with their known chords.
     *
     * @param knownChords Set of chord names the user knows.
     * @return Songs where all required chords are in the known set, sorted by chord count.
     */
    fun findPlayable(knownChords: Set<String>): List<SongEntry> =
        SONGS.filter { song ->
            song.chords.all { chord -> chord in knownChords }
        }.sortedBy { it.chords.size }

    /**
     * Finds songs where the user is missing just a few chords.
     *
     * @param knownChords Set of chord names the user knows.
     * @param maxMissing Maximum number of missing chords to allow (default 1).
     * @return Pairs of (song, missing chords), sorted by number of missing chords.
     */
    fun findAlmostPlayable(
        knownChords: Set<String>,
        maxMissing: Int = 1,
    ): List<Pair<SongEntry, Set<String>>> =
        SONGS.mapNotNull { song ->
            val missing = song.chords - knownChords
            if (missing.isNotEmpty() && missing.size <= maxMissing) {
                song to missing
            } else {
                null
            }
        }.sortedBy { it.second.size }
}
