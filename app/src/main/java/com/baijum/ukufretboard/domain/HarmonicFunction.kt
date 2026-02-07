package com.baijum.ukufretboard.domain

import com.baijum.ukufretboard.data.ScaleType

/**
 * Harmonic functions describe the *role* a chord plays within a key.
 *
 * - **Tonic (T)**: Feels stable and resolved — the "home" chord.
 * - **Subdominant (S)**: Creates forward motion — moves away from home.
 * - **Dominant (D)**: Builds tension — wants to resolve back to Tonic.
 *
 * Understanding chord functions is the bridge between knowing *which*
 * chords go together and understanding *why* they go together.
 */
enum class HarmonicFunction(
    val label: String,
    val description: String,
) {
    TONIC("T", "Tonic — feels like home, stable and resolved"),
    SUBDOMINANT("S", "Subdominant — creates forward motion, moves away from home"),
    DOMINANT("D", "Dominant — builds tension, wants to resolve back to Tonic"),
}

/**
 * Determines the harmonic function of a chord degree based on its
 * Roman numeral and the scale type.
 *
 * **Major scale functions:**
 * - Tonic: I, iii, vi (share notes with I)
 * - Subdominant: ii, IV (share notes with IV)
 * - Dominant: V, vii° (share the leading tone)
 *
 * **Minor scale functions:**
 * - Tonic: i, III, VI
 * - Subdominant: ii°, iv
 * - Dominant: V, v, VII
 *
 * @param numeral The Roman numeral label of the chord degree.
 * @param scaleType The scale type context.
 * @return The [HarmonicFunction] of the chord degree.
 */
fun harmonicFunction(numeral: String, scaleType: ScaleType): HarmonicFunction = when {
    // ── Major scale ──
    scaleType == ScaleType.MAJOR && numeral in listOf("I", "iii", "vi") ->
        HarmonicFunction.TONIC
    scaleType == ScaleType.MAJOR && numeral in listOf("ii", "IV") ->
        HarmonicFunction.SUBDOMINANT
    scaleType == ScaleType.MAJOR && numeral in listOf("V", "vii\u00B0") ->
        HarmonicFunction.DOMINANT

    // ── Minor scale ──
    scaleType == ScaleType.MINOR && numeral in listOf("i", "III", "VI") ->
        HarmonicFunction.TONIC
    scaleType == ScaleType.MINOR && numeral in listOf("ii\u00B0", "iv") ->
        HarmonicFunction.SUBDOMINANT
    scaleType == ScaleType.MINOR && numeral in listOf("V", "v", "VII") ->
        HarmonicFunction.DOMINANT

    // Fallback
    else -> HarmonicFunction.TONIC
}
