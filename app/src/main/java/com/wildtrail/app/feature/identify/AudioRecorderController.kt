package com.wildtrail.app.feature.identify

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import java.io.File

sealed interface RecordingStopResult {
    data object Missing : RecordingStopResult
    data object TooShort : RecordingStopResult
    data class Ready(
        val file: File,
        val durationMillis: Long,
    ) : RecordingStopResult
}

class AudioRecorderController(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val cacheDir = appContext.cacheDir
    private var recorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recordingStartedAt: Long? = null

    fun start() {
        release()
        val outputFile = File(cacheDir, "wildtrail-recording-${System.currentTimeMillis()}.m4a")
        val newRecorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44_100)
            setAudioEncodingBitRate(128_000)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
        recorder = newRecorder
        recordingFile = outputFile
        recordingStartedAt = SystemClock.elapsedRealtime()
    }

    fun stop(): RecordingStopResult {
        val startedAt = recordingStartedAt ?: SystemClock.elapsedRealtime()
        val durationMillis = SystemClock.elapsedRealtime() - startedAt
        val outputFile = recordingFile

        if (durationMillis < MIN_RECORDING_MILLIS) {
            release()
            outputFile?.delete()
            return RecordingStopResult.TooShort
        }

        try {
            recorder?.stop()
        } finally {
            release()
        }

        if (outputFile == null || !outputFile.exists()) {
            return RecordingStopResult.Missing
        }
        return RecordingStopResult.Ready(outputFile, durationMillis)
    }

    fun release() {
        recorder?.release()
        recorder = null
        recordingFile = null
        recordingStartedAt = null
    }

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(appContext)
        } else {
            createLegacyMediaRecorder()
        }
    }

    @Suppress("DEPRECATION")
    private fun createLegacyMediaRecorder(): MediaRecorder {
        return MediaRecorder()
    }

    private companion object {
        const val MIN_RECORDING_MILLIS = 1_000L
    }
}
