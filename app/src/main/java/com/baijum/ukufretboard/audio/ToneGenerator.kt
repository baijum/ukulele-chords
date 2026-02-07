package com.baijum.ukufretboard.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Synthesizes and plays musical tones using Android's [AudioTrack] API.
 *
 * Generates PCM sine waves at precise frequencies derived from pitch class and octave,
 * with support for playing single notes and strummed chords (notes staggered in time).
 *
 * All audio is generated mathematically — no sample files are needed, keeping the app
 * lightweight and fully offline.
 *
 * Thread-safe: uses a [Mutex] to ensure only one sound plays at a time.
 */
object ToneGenerator {

    /** Standard sample rate in Hz. CD quality. */
    private const val SAMPLE_RATE = 44100

    /** Default duration of a single note in seconds. */
    private const val DEFAULT_NOTE_DURATION_SECONDS = 0.6

    /** Default time between successive string plucks in a strum, in seconds. */
    private const val DEFAULT_STRUM_DELAY_SECONDS = 0.05

    /** Duration of the fade-in envelope in seconds, to avoid click artifacts. */
    private const val FADE_IN_SECONDS = 0.01

    /** Duration of the fade-out envelope in seconds, for a natural decay. */
    private const val FADE_OUT_SECONDS = 0.15

    /** Peak amplitude (0.0–1.0). Kept below 1.0 to prevent clipping when mixing. */
    private const val AMPLITUDE = 0.35

    /**
     * Reference frequency: A4 = 440 Hz (concert pitch).
     * All other frequencies are derived relative to this.
     */
    private const val A4_FREQUENCY = 440.0

    /**
     * MIDI note number for A4.
     * MIDI defines middle C (C4) as note 60; A4 is note 69.
     */
    private const val A4_MIDI = 69

    /** Mutex to prevent overlapping playback. */
    private val playbackMutex = Mutex()

    /**
     * Calculates the frequency in Hz for a given pitch class and octave.
     *
     * Uses the equal-temperament formula:
     *     frequency = 440 × 2^((midiNote - 69) / 12)
     *
     * where midiNote = (octave + 1) × 12 + pitchClass.
     *
     * @param pitchClass The pitch class (0 = C, 1 = C#, ... 11 = B).
     * @param octave The octave number (e.g., 4 for the octave containing A440).
     * @return The frequency in Hz.
     */
    fun frequencyOf(pitchClass: Int, octave: Int): Double {
        val midiNote = (octave + 1) * 12 + pitchClass
        return A4_FREQUENCY * 2.0.pow((midiNote - A4_MIDI).toDouble() / 12.0)
    }

    /**
     * Plays a single note at the given pitch class and octave.
     *
     * Runs on [Dispatchers.Default] to avoid blocking the main thread.
     * If another note/chord is already playing, the previous sound is stopped first.
     *
     * @param pitchClass The pitch class (0–11).
     * @param octave The octave (typically 3–5 for ukulele).
     * @param durationMs Duration of the note in milliseconds.
     */
    suspend fun playNote(
        pitchClass: Int,
        octave: Int,
        durationMs: Int = (DEFAULT_NOTE_DURATION_SECONDS * 1000).toInt(),
    ) {
        val frequency = frequencyOf(pitchClass, octave)
        val durationSeconds = durationMs / 1000.0
        val samples = generateTone(frequency, durationSeconds)
        playSamples(samples)
    }

    /**
     * Plays a chord as a strum — notes are triggered with a slight delay between
     * successive strings, simulating a strum.
     *
     * The total duration is the note duration plus strum delays for all strings.
     * All notes are mixed into a single PCM buffer before playback.
     *
     * @param notes A list of (pitchClass, octave) pairs, one per string, in strum order.
     * @param noteDurationMs Duration of each note in milliseconds.
     * @param strumDelayMs Delay between successive string plucks in milliseconds.
     */
    suspend fun playChord(
        notes: List<Pair<Int, Int>>,
        noteDurationMs: Int = (DEFAULT_NOTE_DURATION_SECONDS * 1000).toInt(),
        strumDelayMs: Int = (DEFAULT_STRUM_DELAY_SECONDS * 1000).toInt(),
    ) {
        if (notes.isEmpty()) return

        val noteDurationSeconds = noteDurationMs / 1000.0
        val strumDelaySeconds = strumDelayMs / 1000.0

        val totalDuration = noteDurationSeconds + strumDelaySeconds * (notes.size - 1)
        val totalSamples = (totalDuration * SAMPLE_RATE).toInt()
        val mixed = FloatArray(totalSamples)

        notes.forEachIndexed { index, (pitchClass, octave) ->
            val freq = frequencyOf(pitchClass, octave)
            val tone = generateTone(freq, noteDurationSeconds)
            val offsetSamples = (index * strumDelaySeconds * SAMPLE_RATE).toInt()

            for (i in tone.indices) {
                val destIndex = offsetSamples + i
                if (destIndex < mixed.size) {
                    mixed[destIndex] += tone[i]
                }
            }
        }

        // Normalize to prevent clipping when multiple notes overlap
        val maxAbs = mixed.maxOfOrNull { kotlin.math.abs(it) } ?: 1f
        if (maxAbs > 1f) {
            for (i in mixed.indices) {
                mixed[i] /= maxAbs
            }
        }

        playSamples(mixed)
    }

    /**
     * Generates a PCM sine wave tone with attack and release envelope.
     *
     * The envelope prevents audible clicks at the start and end of the tone:
     * - **Fade-in**: linear ramp from 0 to full amplitude over [FADE_IN_SECONDS].
     * - **Sustain**: full amplitude.
     * - **Fade-out**: linear ramp from full amplitude to 0 over [FADE_OUT_SECONDS].
     *
     * @param frequency The frequency in Hz.
     * @param durationSeconds The total duration of the tone in seconds.
     * @return A [FloatArray] of PCM samples in the range [-1.0, 1.0].
     */
    private fun generateTone(frequency: Double, durationSeconds: Double): FloatArray {
        val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
        val samples = FloatArray(numSamples)
        val fadeInSamples = (FADE_IN_SECONDS * SAMPLE_RATE).toInt()
        val fadeOutSamples = (FADE_OUT_SECONDS * SAMPLE_RATE).toInt()
        val fadeOutStart = numSamples - fadeOutSamples
        val angularFrequency = 2.0 * PI * frequency / SAMPLE_RATE

        for (i in 0 until numSamples) {
            val envelope = when {
                i < fadeInSamples -> i.toFloat() / fadeInSamples
                i >= fadeOutStart -> (numSamples - i).toFloat() / fadeOutSamples
                else -> 1f
            }
            samples[i] = (AMPLITUDE * envelope * sin(angularFrequency * i)).toFloat()
        }

        return samples
    }

    /**
     * Writes PCM float samples to an [AudioTrack] for playback.
     *
     * Acquires the [playbackMutex] to ensure only one sound plays at a time.
     * Any previously playing sound is implicitly stopped when the mutex is acquired
     * and a new AudioTrack is created.
     *
     * @param samples PCM float samples in the range [-1.0, 1.0].
     */
    private suspend fun playSamples(samples: FloatArray) {
        playbackMutex.withLock {
            withContext(Dispatchers.Default) {
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT,
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(bufferSize, samples.size * 4))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                try {
                    audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
                    audioTrack.play()

                    // Wait for playback to complete
                    val durationMs = (samples.size.toLong() * 1000) / SAMPLE_RATE
                    val startTime = System.currentTimeMillis()
                    while (isActive && System.currentTimeMillis() - startTime < durationMs) {
                        kotlinx.coroutines.delay(50)
                    }
                } finally {
                    audioTrack.stop()
                    audioTrack.release()
                }
            }
        }
    }
}
