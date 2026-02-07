package com.baijum.ukufretboard.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A coroutine-based metronome engine that plays chord progressions at a steady tempo.
 *
 * The engine fires a callback on each beat, advancing to the next chord
 * after [beatsPerChord] beats. It loops continuously if [loop] is true.
 *
 * This uses `delay()` for timing, which provides sufficient accuracy
 * for a practice metronome (within ~10ms precision).
 */
class MetronomeEngine {

    private var job: Job? = null

    /** Whether the metronome is currently playing. */
    val isPlaying: Boolean get() = job?.isActive == true

    /**
     * Starts the metronome.
     *
     * @param scope The coroutine scope to launch in.
     * @param bpm Beats per minute (60–240).
     * @param beatsPerChord Number of beats before advancing to the next chord.
     * @param chordCount Total number of chords in the progression.
     * @param loop Whether to loop back to the first chord after the last.
     * @param onBeat Callback fired on each beat with (chordIndex, beatInChord).
     *   chordIndex is 0-based; beatInChord is 1-based.
     * @param onComplete Callback fired when playback finishes (only if not looping).
     */
    fun start(
        scope: CoroutineScope,
        bpm: Int,
        beatsPerChord: Int,
        chordCount: Int,
        loop: Boolean,
        onBeat: (chordIndex: Int, beatInChord: Int) -> Unit,
        onComplete: () -> Unit = {},
    ) {
        stop() // Cancel any existing playback

        val beatDurationMs = (60_000L / bpm.coerceIn(30, 300))

        job = scope.launch {
            var chordIndex = 0
            var beat = 1

            // Fire the first beat immediately
            onBeat(chordIndex, beat)

            while (isActive) {
                delay(beatDurationMs)
                if (!isActive) break

                beat++
                if (beat > beatsPerChord) {
                    beat = 1
                    chordIndex++
                    if (chordIndex >= chordCount) {
                        if (loop) {
                            chordIndex = 0
                        } else {
                            onComplete()
                            return@launch
                        }
                    }
                }
                onBeat(chordIndex, beat)
            }
        }
    }

    /**
     * Stops the metronome.
     */
    fun stop() {
        job?.cancel()
        job = null
    }
}
