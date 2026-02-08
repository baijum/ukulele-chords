package com.baijum.ukufretboard.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Captures audio from the device microphone and emits normalised sample buffers.
 *
 * Wraps Android's [AudioRecord] API, reading PCM 16-bit mono audio at 44.1 kHz
 * in a coroutine loop. Each buffer is normalised to the range -1.0..1.0 and
 * delivered via the [onBuffer] callback supplied to [start].
 *
 * Lifecycle mirrors [ToneGenerator]: call [start] to begin capture and [stop]
 * to release resources. The RECORD_AUDIO permission must be granted before
 * calling [start]; if not, the call is a no-op.
 */
object AudioCaptureEngine {

    /** Recording sample rate in Hz. */
    const val SAMPLE_RATE = 44100

    /**
     * Number of samples per analysis frame.
     *
     * 4096 samples at 44.1 kHz ≈ 93 ms — long enough to resolve the lowest
     * ukulele fundamental (D3 ≈ 147 Hz requires ≥ 2 periods ≈ 14 ms) while
     * keeping latency comfortable.
     */
    const val FRAME_SIZE = 4096

    /** Factor to convert a signed 16-bit PCM sample to the -1.0..1.0 range. */
    private const val SHORT_TO_FLOAT = 1.0f / Short.MAX_VALUE

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null

    /** Whether capture is currently active. */
    val isCapturing: Boolean get() = captureJob?.isActive == true

    /**
     * Begins capturing audio from the microphone.
     *
     * Each captured frame of [FRAME_SIZE] samples is normalised to floats and
     * passed to [onBuffer] on a background thread.
     *
     * @param scope   Coroutine scope that owns the capture lifetime.
     * @param onBuffer Callback receiving a [FloatArray] of normalised samples.
     */
    fun start(scope: CoroutineScope, onBuffer: (FloatArray) -> Unit) {
        if (isCapturing) return

        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val bufferSize = maxOf(minBuf, FRAME_SIZE * 2) // at least 2 frames

        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
            )
        } catch (_: SecurityException) {
            // Permission not granted — caller should check before starting.
            return
        }

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return
        }

        audioRecord = recorder
        recorder.startRecording()

        captureJob = scope.launch {
            withContext(Dispatchers.Default) {
                val shortBuf = ShortArray(FRAME_SIZE)
                val floatBuf = FloatArray(FRAME_SIZE)

                while (isActive) {
                    val read = recorder.read(shortBuf, 0, FRAME_SIZE)
                    if (read > 0) {
                        for (i in 0 until read) {
                            floatBuf[i] = shortBuf[i] * SHORT_TO_FLOAT
                        }
                        onBuffer(floatBuf.copyOf(read))
                    }
                }
            }
        }
    }

    /**
     * Stops capturing and releases the [AudioRecord] resource.
     */
    fun stop() {
        captureJob?.cancel()
        captureJob = null

        audioRecord?.let { rec ->
            try {
                if (rec.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    rec.stop()
                }
            } catch (_: IllegalStateException) {
                // Already stopped — ignore.
            }
            rec.release()
        }
        audioRecord = null
    }
}
