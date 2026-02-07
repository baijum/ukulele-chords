package com.baijum.ukufretboard.data

import com.baijum.ukufretboard.domain.ChordVoicing
import com.baijum.ukufretboard.domain.Note
import com.baijum.ukufretboard.viewmodel.UkuleleString

/**
 * Generates all playable ukulele voicings for a given chord algorithmically.
 *
 * The algorithm:
 * 1. Computes the target pitch classes from the root and chord formula intervals.
 *    For chords with more notes than strings (e.g., 9th chords), generates subsets
 *    by dropping [ChordFormula.omittable] intervals.
 * 2. For each string, finds every fret (0–12) that produces one of the target
 *    pitch classes.
 * 3. Generates the cartesian product of valid frets across all 4 strings.
 * 4. Filters for playability: all required pitch classes must be present, and the
 *    fret span (ignoring open strings) must be at most [MAX_FRET_SPAN].
 * 5. Deduplicates, sorts by position and ease of play, and returns up to
 *    [MAX_VOICINGS] results.
 */
object VoicingGenerator {

    /** Maximum allowed span between the lowest and highest fretted (non-open) positions. */
    private const val MAX_FRET_SPAN = 4

    /** Maximum number of voicings to return per chord. */
    private const val MAX_VOICINGS = 10

    /** The highest fret to consider on the ukulele. */
    private const val LAST_FRET = 12

    /**
     * Generates playable voicings for a chord defined by [rootPitchClass] and [formula],
     * using the given [tuning].
     *
     * @param rootPitchClass The pitch class (0–11) of the chord's root note.
     * @param formula The [ChordFormula] defining the chord's interval structure.
     * @param tuning The ukulele string tuning to generate voicings for.
     * @return A list of [ChordVoicing]s sorted by playability (lower positions first,
     *   smaller spans preferred, more open strings preferred).
     */
    fun generate(
        rootPitchClass: Int,
        formula: ChordFormula,
        tuning: List<UkuleleString>,
        useFlats: Boolean = false,
    ): List<ChordVoicing> {
        val stringCount = tuning.size

        // Build the set of target pitch classes and the required subset
        val allTargetPCs = formula.intervals.map { interval ->
            (rootPitchClass + interval) % Notes.PITCH_CLASS_COUNT
        }.toSet()

        val requiredPCs = formula.intervals
            .subtract(formula.omittable)
            .map { interval -> (rootPitchClass + interval) % Notes.PITCH_CLASS_COUNT }
            .toSet()

        // Determine which pitch class sets to try.
        // For chords with ≤ stringCount notes, use the full set.
        // For chords with > stringCount notes, generate subsets by dropping omittable intervals.
        val pitchClassSets = if (allTargetPCs.size <= stringCount) {
            listOf(allTargetPCs)
        } else {
            buildSubsets(rootPitchClass, formula, stringCount)
        }

        val results = mutableSetOf<List<Int>>() // deduplicate by fret pattern

        for (targetPCs in pitchClassSets) {
            // For each string, find valid frets that produce a target pitch class
            val fretsPerString = tuning.map { string ->
                (0..LAST_FRET).filter { fret ->
                    (string.openPitchClass + fret) % Notes.PITCH_CLASS_COUNT in targetPCs
                }
            }

            // Skip if any string has no valid frets (shouldn't happen, but be safe)
            if (fretsPerString.any { it.isEmpty() }) continue

            // Cartesian product of valid frets across all strings
            cartesianProduct(fretsPerString) { combination ->
                if (combination in results) return@cartesianProduct

                // Check that all required pitch classes are covered
                val producedPCs = combination.mapIndexedTo(mutableSetOf()) { i, fret ->
                    (tuning[i].openPitchClass + fret) % Notes.PITCH_CLASS_COUNT
                }
                if (!producedPCs.containsAll(requiredPCs)) return@cartesianProduct

                // Check playability: fret span of fretted (non-open) positions
                val frettedPositions = combination.filter { it > 0 }
                if (frettedPositions.isNotEmpty()) {
                    val span = frettedPositions.max() - frettedPositions.min()
                    if (span > MAX_FRET_SPAN) return@cartesianProduct
                }

                results.add(combination.toList())
            }
        }

        // Convert to ChordVoicing objects and sort
        return results
            .map { frets -> toVoicing(frets, tuning, useFlats) }
            .sortedWith(voicingComparator())
            .take(MAX_VOICINGS)
    }

    /**
     * Builds pitch class subsets for chords with more notes than strings.
     *
     * Drops each combination of omittable intervals to produce subsets that
     * fit within [stringCount] strings while retaining all required intervals.
     */
    private fun buildSubsets(
        rootPitchClass: Int,
        formula: ChordFormula,
        stringCount: Int,
    ): List<Set<Int>> {
        val required = formula.intervals - formula.omittable
        val omittableList = formula.omittable.toList()
        val results = mutableListOf<Set<Int>>()
        val needToDrop = formula.intervals.size - stringCount

        // Generate all combinations of omittable intervals to drop
        fun dropCombinations(remaining: List<Int>, toDrop: Int): List<Set<Int>> {
            if (toDrop == 0) return listOf(emptySet())
            if (remaining.size < toDrop) return emptyList()
            val combos = mutableListOf<Set<Int>>()
            for (i in remaining.indices) {
                val sub = dropCombinations(remaining.subList(i + 1, remaining.size), toDrop - 1)
                for (s in sub) {
                    combos.add(s + remaining[i])
                }
            }
            return combos
        }

        for (dropped in dropCombinations(omittableList, needToDrop)) {
            val kept = formula.intervals - dropped
            val pcs = kept.map { (rootPitchClass + it) % Notes.PITCH_CLASS_COUNT }.toSet()
            results.add(pcs)
        }

        // Also include the full set (in case doubling a note works)
        val allPCs = formula.intervals.map { (rootPitchClass + it) % Notes.PITCH_CLASS_COUNT }.toSet()
        if (allPCs !in results) results.add(allPCs)

        return results
    }

    /**
     * Iterates over the cartesian product of lists of frets without materializing
     * the full product in memory. Calls [action] for each combination.
     */
    private inline fun cartesianProduct(
        fretsPerString: List<List<Int>>,
        action: (List<Int>) -> Unit,
    ) {
        val sizes = fretsPerString.map { it.size }
        val indices = IntArray(fretsPerString.size)
        val total = sizes.fold(1L) { acc, s -> acc * s }

        for (i in 0 until total) {
            val combination = fretsPerString.mapIndexed { j, frets -> frets[indices[j]] }
            action(combination)

            // Increment indices (odometer-style)
            for (j in indices.indices.reversed()) {
                indices[j]++
                if (indices[j] < sizes[j]) break
                indices[j] = 0
            }
        }
    }

    /**
     * Converts a fret pattern to a [ChordVoicing] with computed notes and fret range.
     */
    private fun toVoicing(
        frets: List<Int>,
        tuning: List<UkuleleString>,
        useFlats: Boolean = false,
    ): ChordVoicing {
        val notes = frets.mapIndexed { i, fret ->
            val pc = (tuning[i].openPitchClass + fret) % Notes.PITCH_CLASS_COUNT
            Note(pitchClass = pc, name = Notes.pitchClassToName(pc, useFlats))
        }
        val frettedPositions = frets.filter { it > 0 }
        return ChordVoicing(
            frets = frets,
            notes = notes,
            minFret = frettedPositions.minOrNull() ?: 0,
            maxFret = frettedPositions.maxOrNull() ?: 0,
        )
    }

    /**
     * Comparator that sorts voicings by playability:
     * 1. Lower position (minFret) first
     * 2. Smaller fret span preferred
     * 3. More open strings preferred (easier to play)
     */
    private fun voicingComparator(): Comparator<ChordVoicing> =
        compareBy<ChordVoicing> { it.minFret }
            .thenBy { it.maxFret - it.minFret }
            .thenByDescending { voicing -> voicing.frets.count { it == 0 } }
}
