package com.wildtrail.app.feature.identify

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

class RecordingSessionState internal constructor(
    private val recorder: AudioRecorderController,
    private val identifyViewModel: IdentifyViewModel,
    private val baseUrlProvider: () -> String,
    private val onBaseUrlFallbackProvider: () -> (String) -> Unit,
) {
    var isRecording by mutableStateOf(false)
        private set

    fun startRecording() {
        recorder.start()
        isRecording = true
        identifyViewModel.setRecordingStarted()
    }

    fun stopRecording() {
        isRecording = false
        when (val result = recorder.stop()) {
            RecordingStopResult.Missing -> identifyViewModel.setRecordingFileMissing()
            RecordingStopResult.TooShort -> identifyViewModel.setRecordingTooShort()
            is RecordingStopResult.Ready ->
                identifyViewModel.identifyRecordedAudio(
                    file = result.file,
                    durationMillis = result.durationMillis,
                    baseUrl = baseUrlProvider(),
                    onBaseUrlFallback = onBaseUrlFallbackProvider(),
                )
        }
    }

    fun release() {
        recorder.release()
        isRecording = false
    }
}

@Composable
fun rememberRecordingSessionState(
    context: Context,
    identifyViewModel: IdentifyViewModel,
    baseUrl: String,
    onBaseUrlFallback: (String) -> Unit,
): RecordingSessionState {
    val currentBaseUrl by rememberUpdatedState(baseUrl)
    val currentOnBaseUrlFallback by rememberUpdatedState(onBaseUrlFallback)
    val appContext = context.applicationContext
    val recorder = remember(appContext) { AudioRecorderController(appContext) }
    val state =
        remember(recorder, identifyViewModel) {
            RecordingSessionState(
                recorder = recorder,
                identifyViewModel = identifyViewModel,
                baseUrlProvider = { currentBaseUrl },
                onBaseUrlFallbackProvider = { currentOnBaseUrlFallback },
            )
        }

    DisposableEffect(state) {
        onDispose { state.release() }
    }

    return state
}
