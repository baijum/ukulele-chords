package com.baijum.ukufretboard.domain

import kotlin.math.abs

/**
 * Result of a successful pitch detection.
 *
 * @property frequencyHz Detected fundamental frequency in Hertz.
 * @property confidence  Confidence in the detection (0.0 = perfect periodicity,
 *   values below [PitchDetector.DEFAULT_THRESHOLD] are considered reliable).
 */
data class PitchResult(
    val frequencyHz: Double,
    val confidence: Double,
)

/**
 * Monophonic pitch detector using the YIN algorithm.
 *
 * The YIN algorithm (de Cheveigné & Kawahara, 2002) estimates the fundamental
 * frequency of a periodic signal. It works well for monophonic instruments
 * with complex harmonic content such as nylon-string ukuleles.
 *
 * Algorithm steps:
 * 1. Compute the difference function (auto-correlation variant).
 * 2. Compute the cumulative mean normalised difference function (CMND).
 * 3. Apply an absolute threshold to find the first dip below [threshold].
 * 4. Refine the lag estimate with parabolic interpolation.
 * 5. Convert the lag to a frequency: f = sampleRate / lag.
 *
 * This is a pure-Kotlin implementation with no external dependencies.
 */
object PitchDetector {

    /**
     * Default CMND threshold.
     *
     * Lower values require a cleaner periodic signal. 0.15 works well for
     * close-mic'd ukulele recordings; increase to 0.20 for noisier environments.
     */
    const val DEFAULT_THRESHOLD = 0.15

    /** Minimum detectable frequency in Hz (~D2, well below Baritone D3). */
    private const val MIN_FREQUENCY = 65.0

    /** Maximum detectable frequency in Hz (above the highest ukulele fret). */
    private const val MAX_FREQUENCY = 1100.0

    /** Minimum RMS amplitude to consider a buffer as containing a signal. */
    private const val SILENCE_THRESHOLD = 0.01f

    /**
     * Detects the fundamental frequency of the audio in [samples].
     *
     * @param samples    Normalised audio samples (−1.0 .. 1.0).
     * @param sampleRate Sample rate in Hz (e.g. 44100).
     * @param threshold  CMND threshold (default [DEFAULT_THRESHOLD]).
     * @return A [PitchResult] if a reliable pitch is found, or `null` if the
     *   buffer is silent or no periodic signal is detected.
     */
    fun detect(
        samples: FloatArray,
        sampleRate: Int,
        threshold: Double = DEFAULT_THRESHOLD,
    ): PitchResult? {
        // --- Silence gate ---------------------------------------------------
        if (rms(samples) < SILENCE_THRESHOLD) return null

        val halfLen = samples.size / 2
        if (halfLen < 2) return null

        // Lag limits derived from the frequency range we care about.
        val minLag = (sampleRate / MAX_FREQUENCY).toInt().coerceAtLeast(2)
        val maxLag = (sampleRate / MIN_FREQUENCY).toInt().coerceAtMost(halfLen)
        if (minLag >= maxLag) return null

        // --- Step 1: Difference function ------------------------------------
        val diff = FloatArray(maxLag + 1)
        for (tau in 1..maxLag) {
            var sum = 0.0f
            for (j in 0 until halfLen) {
                val delta = samples[j] - samples[j + tau]
                sum += delta * delta
            }
            diff[tau] = sum
        }

        // --- Step 2: Cumulative mean normalised difference (CMND) -----------
        val cmnd = FloatArray(maxLag + 1)
        cmnd[0] = 1.0f
        var runningSum = 0.0f
        for (tau in 1..maxLag) {
            runningSum += diff[tau]
            cmnd[tau] = if (runningSum > 0) diff[tau] * tau / runningSum else 1.0f
        }

        // --- Step 3: Absolute threshold -------------------------------------
        var bestTau = -1
        for (tau in minLag..maxLag) {
            if (cmnd[tau] < threshold) {
                // Walk forward while the CMND keeps decreasing to find the dip.
                bestTau = tau
                while (bestTau + 1 <= maxLag && cmnd[bestTau + 1] < cmnd[bestTau]) {
                    bestTau++
                }
                break
            }
        }

        if (bestTau < 0) return null

        // --- Step 4: Parabolic interpolation --------------------------------
        val refinedTau = parabolicInterpolation(cmnd, bestTau, maxLag)

        // --- Step 5: Convert lag → frequency --------------------------------
        val frequency = sampleRate.toDouble() / refinedTau
        if (frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) return null

        return PitchResult(
            frequencyHz = frequency,
            confidence = cmnd[bestTau].toDouble(),
        )
    }

    // --- Helpers ------------------------------------------------------------

    /**
     * Root-mean-square amplitude of the buffer.
     */
    private fun rms(samples: FloatArray): Float {
        var sumSq = 0.0f
        for (s in samples) {
            sumSq += s * s
        }
        return kotlin.math.sqrt(sumSq / samples.size)
    }

    /**
     * Refines an integer lag estimate using parabolic (quadratic) interpolation
     * around the minimum of the CMND function.
     *
     * @return The refined lag as a [Double].
     */
    private fun parabolicInterpolation(
        cmnd: FloatArray,
        tau: Int,
        maxLag: Int,
    ): Double {
        if (tau < 1 || tau >= maxLag) return tau.toDouble()

        val s0 = cmnd[tau - 1].toDouble()
        val s1 = cmnd[tau].toDouble()
        val s2 = cmnd[tau + 1].toDouble()

        val denominator = 2.0 * (2.0 * s1 - s0 - s2)
        return if (abs(denominator) > 1e-12) {
            tau + (s0 - s2) / denominator
        } else {
            tau.toDouble()
        }
    }
}
