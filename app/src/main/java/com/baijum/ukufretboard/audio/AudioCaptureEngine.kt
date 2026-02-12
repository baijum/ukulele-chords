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
            try {
                withContext(Dispatchers.Default) {
                    val shortBuf = ShortArray(FRAME_SIZE)
                    val floatBuf = FloatArray(FRAME_SIZE)

                    while (isActive) {
                        val read = try {
                            recorder.read(shortBuf, 0, FRAME_SIZE)
                        } catch (_: IllegalStateException) {
                            // Recorder was stopped/released — exit gracefully.
                            break
                        }
                        // Only emit complete frames.  When the recorder is
                        // stopped, read() may return a partial buffer whose
                        // length is not a power of two, which would crash the
                        // downstream FFT (requires power-of-2 input).
                        if (read == FRAME_SIZE) {
                            for (i in 0 until read) {
                                floatBuf[i] = shortBuf[i] * SHORT_TO_FLOAT
                            }
                            onBuffer(floatBuf)
                        }
                    }
                }
            } finally {
                recorder.release()
            }
        }
    }

    /**
     * Stops capturing and releases the [AudioRecord] resource.
     *
     * The [AudioRecord] is stopped here to unblock any pending [AudioRecord.read]
     * call in the capture coroutine. The actual [AudioRecord.release] is deferred
     * to the coroutine's `finally` block so it runs only **after** the capture
     * loop has exited, avoiding a race where `release()` frees native resources
     * while `read()` is still using them.
     */
    fun stop() {
        // Stop recording first to unblock any pending read() call in the
        // capture coroutine.  Without this, cancel() alone cannot interrupt
        // the blocking AudioRecord.read().
        audioRecord?.let { rec ->
            try {
                if (rec.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    rec.stop()
                }
            } catch (_: IllegalStateException) {
                // Already stopped — ignore.
            }
        }

        // Cancel the capture coroutine.  The finally block inside start()
        // will call recorder.release() once the coroutine finishes.
        captureJob?.cancel()
        captureJob = null
        audioRecord = null
    }
}
