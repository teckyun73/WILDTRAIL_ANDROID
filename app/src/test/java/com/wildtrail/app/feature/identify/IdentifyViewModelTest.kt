package com.wildtrail.app.feature.identify

import com.wildtrail.app.data.dto.IdentificationCandidateDto
import com.wildtrail.app.data.dto.IdentificationResultDto
import com.wildtrail.app.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.RandomAccessFile
import java.net.ConnectException

@OptIn(ExperimentalCoroutinesApi::class)
class IdentifyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun recordingMessageHelpers_updateRecordingMessage() {
        val viewModel = IdentifyViewModel()

        viewModel.setRecordingStarted()
        assertEquals("녹음 중입니다. 최소 1초 이상 녹음하세요.", viewModel.recordingMessage)

        viewModel.setRecordingFileMissing()
        assertEquals("녹음 파일을 찾을 수 없습니다.", viewModel.recordingMessage)

        viewModel.setRecordingTooShort()
        assertEquals("오디오는 최소 1초 이상 녹음해 주세요.", viewModel.recordingMessage)
    }

    @Test
    fun identifyRecordedAudio_setsReadyStateAndCompletionMessageWhenRequestSucceeds() =
        runTest {
            val expected = identificationResultFixture()
            var receivedBaseUrl: String? = null
            var receivedPartName: String? = null
            var fallbackUrl: String? = null
            val audioFile = createTempAudioFile()
            val viewModel =
                IdentifyViewModel(
                    identifyAudioRequest = { baseUrl, onBaseUrlFallback, filePart ->
                        receivedBaseUrl = baseUrl
                        receivedPartName = filePart.headers?.get("Content-Disposition")
                        onBaseUrlFallback("http://127.0.0.1:8000")
                        expected
                    },
                )

            viewModel.identifyRecordedAudio(audioFile, 2300, "http://10.0.2.2:8000") { fallbackUrl = it }
            advanceUntilIdle()

            val state = viewModel.identifyState as IdentifyUiState.Ready
            assertSame(expected, state.result)
            assertEquals("http://10.0.2.2:8000", receivedBaseUrl)
            assertEquals("http://127.0.0.1:8000", fallbackUrl)
            assertEquals(audioFile.name, viewModel.selectedAudioName)
            assertNull(viewModel.selectedImageName)
            assertEquals("녹음 파일 분석 완료", viewModel.recordingMessage)
            assertEquals(true, receivedPartName?.contains("filename=\"${audioFile.name}\"") == true)
        }

    @Test
    fun identifyRecordedAudio_setsErrorStateAndClearsRecordingMessageWhenRequestFails() =
        runTest {
            val audioFile = createTempAudioFile()
            val viewModel =
                IdentifyViewModel(
                    identifyAudioRequest = { _, _, _ -> throw ConnectException() },
                )

            viewModel.identifyRecordedAudio(audioFile, 1500, "http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val state = viewModel.identifyState as IdentifyUiState.Error
            assertEquals("서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.", state.message)
            assertEquals(audioFile.name, viewModel.selectedAudioName)
            assertNull(viewModel.selectedImageName)
            assertNull(viewModel.recordingMessage)
        }

    @Test
    fun identifyRecordedAudio_blocksFilesLargerThanBackendLimitBeforeRequest() =
        runTest {
            var requestCalled = false
            val audioFile = createLargeTempAudioFile()
            val viewModel =
                IdentifyViewModel(
                    identifyAudioRequest = { _, _, _ ->
                        requestCalled = true
                        identificationResultFixture()
                    },
                )

            viewModel.identifyRecordedAudio(audioFile, 1500, "http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val state = viewModel.identifyState as IdentifyUiState.Error
            assertEquals("오디오는 20MB 이하 파일을 선택해 주세요.", state.message)
            assertEquals(false, requestCalled)
            assertEquals(audioFile.name, viewModel.selectedAudioName)
            assertNull(viewModel.selectedImageName)
            assertNull(viewModel.recordingMessage)
        }

    private fun createTempAudioFile(): File =
        File.createTempFile("wildtrail-recording", ".m4a").apply {
            writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03))
            deleteOnExit()
        }

    private fun createLargeTempAudioFile(): File =
        File.createTempFile("wildtrail-large-recording", ".m4a").apply {
            RandomAccessFile(this, "rw").use { it.setLength(20L * 1024L * 1024L + 1L) }
            deleteOnExit()
        }

    private fun identificationResultFixture() =
        IdentificationResultDto(
            mediaType = "audio",
            candidates =
                listOf(
                    IdentificationCandidateDto(
                        speciesId = "lynx",
                        commonName = "삵",
                        scientificName = "Prionailurus bengalensis",
                        confidence = 0.91,
                    ),
                ),
            message = "식별 완료",
            source = "test",
        )
}
