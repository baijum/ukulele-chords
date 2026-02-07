package com.baijum.ukufretboard.data

/**
 * Scale type used for generating diatonic chord progressions.
 */
enum class ScaleType(val label: String) {
    MAJOR("Major"),
    MINOR("Minor"),
}

/**
 * A chord degree within a scale, defining the interval from the root
 * and the quality of the resulting chord.
 *
 * @property interval Semitones from the key root to this degree's root.
 * @property quality The chord quality symbol (e.g., "", "m", "dim").
 * @property numeral The Roman numeral label (e.g., "I", "vi", "vii°").
 */
data class ChordDegree(
    val interval: Int,
    val quality: String,
    val numeral: String,
)

/**
 * A named chord progression pattern.
 *
 * @property name Human-readable name (e.g., "Pop / Four Chords").
 * @property description Brief explanation of when/where this progression is used.
 * @property degrees The sequence of chord degrees in the progression.
 * @property scaleType The scale type this progression is derived from.
 */
data class Progression(
    val name: String,
    val description: String,
    val degrees: List<ChordDegree>,
    val scaleType: ScaleType = ScaleType.MAJOR,
)

/**
 * Library of common chord progressions.
 *
 * Each progression uses scale degree intervals. To resolve for a specific key,
 * add the key's pitch class to each degree's interval (mod 12), then apply
 * the degree's chord quality.
 */
object Progressions {

    // ── Major scale degrees ──
    // I=0, ii=2, iii=4, IV=5, V=7, vi=9, vii°=11
    private val I      = ChordDegree(0,  "",    "I")
    private val ii     = ChordDegree(2,  "m",   "ii")
    private val iii    = ChordDegree(4,  "m",   "iii")
    private val IV     = ChordDegree(5,  "",    "IV")
    private val V      = ChordDegree(7,  "",    "V")
    private val vi     = ChordDegree(9,  "m",   "vi")
    private val viiDim = ChordDegree(11, "dim", "vii\u00B0")

    // ── Minor scale degrees ──
    // i=0, ii°=2, III=3, iv=5, v=7, VI=8, VII=10
    private val i_m     = ChordDegree(0,  "m",   "i")
    private val iiDim_m = ChordDegree(2,  "dim", "ii\u00B0")
    private val III_m   = ChordDegree(3,  "",    "III")
    private val iv_m    = ChordDegree(5,  "m",   "iv")
    private val v_m     = ChordDegree(7,  "m",   "v")
    private val V_m     = ChordDegree(7,  "",    "V")   // harmonic minor dominant
    private val VI_m    = ChordDegree(8,  "",    "VI")
    private val VII_m   = ChordDegree(10, "",    "VII")

    val MAJOR_PROGRESSIONS: List<Progression> = listOf(
        Progression(
            name = "Pop / Four Chords",
            description = "The most popular progression in modern pop music. Thousands of hit songs use this exact pattern.",
            degrees = listOf(I, V, vi, IV),
        ),
        Progression(
            name = "Classic Rock",
            description = "The backbone of rock 'n' roll. Simple, powerful, and timeless.",
            degrees = listOf(I, IV, V),
        ),
        Progression(
            name = "50s / Doo-Wop",
            description = "The iconic progression from the 1950s. Still widely used in ballads and slow songs.",
            degrees = listOf(I, vi, IV, V),
        ),
        Progression(
            name = "Folk / Country",
            description = "A warm, comfortable pattern common in folk, country, and campfire songs.",
            degrees = listOf(I, IV, I, V),
        ),
        Progression(
            name = "Sad / Emotional",
            description = "Starts on the minor vi chord, creating a melancholic and emotional mood.",
            degrees = listOf(vi, IV, I, V),
        ),
        Progression(
            name = "Jazz ii-V-I",
            description = "The fundamental jazz cadence. Found in virtually every jazz standard.",
            degrees = listOf(ii, V, I),
        ),
        Progression(
            name = "Reggae",
            description = "A laid-back, rhythmic pattern common in reggae and island music.",
            degrees = listOf(I, IV, V, IV),
        ),
        Progression(
            name = "Pachelbel's Canon",
            description = "Based on the famous Canon in D. Used in countless wedding and pop songs.",
            degrees = listOf(I, V, vi, iii, IV, I, IV, V),
        ),
    )

    val MINOR_PROGRESSIONS: List<Progression> = listOf(
        Progression(
            name = "Natural Minor",
            description = "A simple descending minor progression. Dark and moody.",
            degrees = listOf(i_m, VII_m, VI_m, V_m),
            scaleType = ScaleType.MINOR,
        ),
        Progression(
            name = "Andalusian Cadence",
            description = "A descending flamenco-style progression. Dramatic and passionate.",
            degrees = listOf(i_m, VII_m, VI_m, V_m),
            scaleType = ScaleType.MINOR,
        ),
        Progression(
            name = "Minor Pop",
            description = "The minor-key version of the four-chord pop progression.",
            degrees = listOf(i_m, VI_m, III_m, VII_m),
            scaleType = ScaleType.MINOR,
        ),
        Progression(
            name = "Minor Blues",
            description = "A simplified minor blues feel. Raw and expressive.",
            degrees = listOf(i_m, iv_m, i_m, V_m),
            scaleType = ScaleType.MINOR,
        ),
    )

    /**
     * Returns progressions for the given scale type.
     */
    fun forScale(scaleType: ScaleType): List<Progression> = when (scaleType) {
        ScaleType.MAJOR -> MAJOR_PROGRESSIONS
        ScaleType.MINOR -> MINOR_PROGRESSIONS
    }

    /**
     * Returns the diatonic chord degrees for a scale type.
     * Used by the custom progression builder to show available chords.
     */
    fun diatonicDegrees(scaleType: ScaleType): List<ChordDegree> = when (scaleType) {
        ScaleType.MAJOR -> listOf(I, ii, iii, IV, V, vi, viiDim)
        ScaleType.MINOR -> listOf(i_m, iiDim_m, III_m, iv_m, v_m, V_m, VI_m, VII_m)
    }
}
