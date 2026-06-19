package com.wildtrail.app.feature.records

import com.wildtrail.app.data.dto.IdentificationCandidateDto
import com.wildtrail.app.data.dto.SightingCreateDto
import com.wildtrail.app.data.dto.SightingDto
import com.wildtrail.app.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadSightings_setsReadyStateWhenRequestSucceeds() =
        runTest {
            val expected = listOf(sightingFixture())
            val viewModel =
                RecordsViewModel(
                    loadSightingList = { _, _ -> expected },
                )

            viewModel.loadSightings("http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val state = viewModel.sightingState as SightingUiState.Ready
            assertSame(expected, state.sightings)
        }

    @Test
    fun loadSightings_setsUserFacingErrorWhenRequestFails() =
        runTest {
            val viewModel =
                RecordsViewModel(
                    loadSightingList = { _, _ -> throw SocketTimeoutException() },
                )

            viewModel.loadSightings("http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val state = viewModel.sightingState as SightingUiState.Error
            assertEquals("서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.", state.message)
        }

    @Test
    fun saveCandidate_createsSightingThenReloadsListAndSetsSuccessMessage() =
        runTest {
            val expectedList = listOf(sightingFixture())
            var capturedPayload: SightingCreateDto? = null
            var loadCount = 0
            val viewModel =
                RecordsViewModel(
                    loadSightingList = { _, _ ->
                        loadCount += 1
                        expectedList
                    },
                    createSightingRecord = { baseUrl, onBaseUrlFallback, payload ->
                        assertEquals("http://10.0.2.2:8000", baseUrl)
                        onBaseUrlFallback("http://127.0.0.1:8000")
                        capturedPayload = payload
                        sightingFixture(mediaType = payload.mediaType, note = payload.note)
                    },
                )
            var fallbackUrl: String? = null

            viewModel.saveCandidate(candidateFixture(), "audio", "http://10.0.2.2:8000") { fallbackUrl = it }
            advanceUntilIdle()

            val payload = capturedPayload ?: error("payload was not captured")
            val state = viewModel.sightingState as SightingUiState.Ready
            assertEquals("lynx", payload.speciesId)
            assertEquals(0.87, payload.confidence, 0.0)
            assertEquals("audio", payload.mediaType)
            assertEquals("Android 오디오 식별 기록", payload.note)
            assertEquals("http://127.0.0.1:8000", fallbackUrl)
            assertEquals(1, loadCount)
            assertSame(expectedList, state.sightings)
            assertEquals("삵 관찰 기록을 저장했습니다.", viewModel.saveSightingMessage)
        }

    @Test
    fun saveCandidate_usesImageNoteForNonAudioMedia() =
        runTest {
            var capturedPayload: SightingCreateDto? = null
            val viewModel =
                RecordsViewModel(
                    loadSightingList = { _, _ -> emptyList() },
                    createSightingRecord = { _, _, payload ->
                        capturedPayload = payload
                        sightingFixture(mediaType = payload.mediaType, note = payload.note)
                    },
                )

            viewModel.saveCandidate(candidateFixture(), "image", "http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val payload = capturedPayload ?: error("payload was not captured")
            assertEquals("Android 이미지 식별 기록", payload.note)
        }

    @Test
    fun saveCandidate_setsUserFacingErrorWhenCreateFailsAndDoesNotReloadList() =
        runTest {
            var loadCount = 0
            val viewModel =
                RecordsViewModel(
                    loadSightingList = { _, _ ->
                        loadCount += 1
                        emptyList()
                    },
                    createSightingRecord = { _, _, _ -> throw ConnectException() },
                )

            viewModel.saveCandidate(candidateFixture(), "image", "http://10.0.2.2:8000") {}
            advanceUntilIdle()

            assertEquals("서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.", viewModel.saveSightingMessage)
            assertEquals(0, loadCount)
            assertEquals(SightingUiState.Idle, viewModel.sightingState)
        }

    private fun candidateFixture() =
        IdentificationCandidateDto(
            speciesId = "lynx",
            commonName = "삵",
            scientificName = "Prionailurus bengalensis",
            confidence = 0.87,
        )

    private fun sightingFixture(
        mediaType: String = "image",
        note: String = "Android 이미지 식별 기록",
    ) = SightingDto(
        id = 1,
        speciesId = "lynx",
        commonName = "삵",
        locationName = "DMZ 생태길",
        latitude = 38.1,
        longitude = 127.2,
        confidence = 0.87,
        mediaType = mediaType,
        note = note,
        createdAt = "2026-06-18T09:00:00Z",
    )
}
