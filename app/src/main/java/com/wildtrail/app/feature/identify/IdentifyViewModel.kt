package com.wildtrail.app.feature.identify

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildtrail.app.data.dto.IdentificationResultDto
import com.wildtrail.app.data.network.ApiCallRunner
import com.wildtrail.app.data.network.toUserFacingMessage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class IdentifyViewModel(
    private val identifyImageRequest: suspend (
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
        filePart: MultipartBody.Part,
    ) -> IdentificationResultDto = { baseUrl, onBaseUrlFallback, filePart ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback) { it.identifyImage(filePart) }
    },
    private val identifyAudioRequest: suspend (
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
        filePart: MultipartBody.Part,
    ) -> IdentificationResultDto = { baseUrl, onBaseUrlFallback, filePart ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback) { it.identifyAudio(filePart) }
    },
) : ViewModel() {
    var identifyState by mutableStateOf<IdentifyUiState>(IdentifyUiState.Empty)
        private set
    var selectedImageName by mutableStateOf<String?>(null)
        private set
    var selectedAudioName by mutableStateOf<String?>(null)
        private set
    var recordingMessage by mutableStateOf<String?>(null)
        private set

    fun setRecordingStarted() {
        recordingMessage = "녹음 중입니다. 최소 1초 이상 녹음하세요."
    }

    fun setRecordingFileMissing() {
        recordingMessage = "녹음 파일을 찾을 수 없습니다."
    }

    fun setRecordingTooShort() {
        recordingMessage = "오디오는 최소 1초 이상 녹음해 주세요."
    }

    fun identifyImage(
        context: Context,
        uri: Uri,
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        selectedImageName = queryDisplayName(context, uri) ?: "선택한 이미지"
        selectedAudioName = null
        viewModelScope.launch {
            identifyState = IdentifyUiState.Loading
            identifyState =
                try {
                    val filePart = createImagePart(context, uri)
                    IdentifyUiState.Ready(identifyImageRequest(baseUrl, onBaseUrlFallback, filePart))
                } catch (error: Exception) {
                    IdentifyUiState.Error(error.toUserFacingMessage("이미지 식별에 실패했습니다."))
                }
        }
    }

    fun identifyAudio(
        context: Context,
        uri: Uri,
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        selectedAudioName = queryDisplayName(context, uri) ?: "선택한 오디오"
        selectedImageName = null
        viewModelScope.launch {
            identifyState = IdentifyUiState.Loading
            identifyState =
                try {
                    val filePart = createAudioPart(context, uri)
                    IdentifyUiState.Ready(identifyAudioRequest(baseUrl, onBaseUrlFallback, filePart))
                } catch (error: Exception) {
                    IdentifyUiState.Error(error.toUserFacingMessage("오디오 식별에 실패했습니다."))
                }
        }
    }

    fun identifyRecordedAudio(
        file: File,
        durationMillis: Long,
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        selectedAudioName = file.name
        selectedImageName = null
        recordingMessage = "${durationMillis / 1000.0}초 녹음 업로드 중..."
        viewModelScope.launch {
            identifyState = IdentifyUiState.Loading
            identifyState =
                try {
                    val filePart = createAudioPart(file)
                    recordingMessage = "녹음 파일 분석 완료"
                    IdentifyUiState.Ready(identifyAudioRequest(baseUrl, onBaseUrlFallback, filePart))
                } catch (error: Exception) {
                    recordingMessage = null
                    IdentifyUiState.Error(error.toUserFacingMessage("녹음 오디오 식별에 실패했습니다."))
                }
        }
    }
}

private fun createImagePart(
    context: Context,
    uri: Uri,
): MultipartBody.Part =
    createFilePart(
        context = context,
        uri = uri,
        fallbackMimeType = "image/jpeg",
        fallbackFileName = "wildtrail-upload.jpg",
        readError = "이미지 파일을 읽을 수 없습니다.",
    )

private fun createAudioPart(
    context: Context,
    uri: Uri,
): MultipartBody.Part =
    createFilePart(
        context = context,
        uri = uri,
        fallbackMimeType = "audio/mpeg",
        fallbackFileName = "wildtrail-audio.mp3",
        readError = "오디오 파일을 읽을 수 없습니다.",
    )

private fun createAudioPart(file: File): MultipartBody.Part {
    val body = file.readBytes().toRequestBody("audio/mp4".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", file.name, body)
}

private fun createFilePart(
    context: Context,
    uri: Uri,
    fallbackMimeType: String,
    fallbackFileName: String,
    readError: String,
): MultipartBody.Part {
    val resolver = context.contentResolver
    val bytes =
        resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error(readError)
    val mimeType = resolver.getType(uri) ?: fallbackMimeType
    val fileName = queryDisplayName(context, uri) ?: fallbackFileName
    val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", fileName, body)
}

private fun queryDisplayName(
    context: Context,
    uri: Uri,
): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}
