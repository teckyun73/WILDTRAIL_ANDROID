package com.wildtrail.app.feature.species

import com.wildtrail.app.data.dto.HotspotDto
import com.wildtrail.app.data.dto.SpeciesDetailDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto
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
class SpeciesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadSpecies_setsReadyStateWhenListRequestSucceeds() =
        runTest {
            val expected = listOf(speciesSummaryFixture())
            val viewModel =
                SpeciesViewModel(
                    loadSpeciesList = { _, _ -> expected },
                )

            viewModel.loadSpecies("http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val state = viewModel.speciesState as SpeciesUiState.Ready
            assertSame(expected, state.species)
        }

    @Test
    fun loadSpecies_setsUserFacingErrorWhenListRequestFails() =
        runTest {
            val viewModel =
                SpeciesViewModel(
                    loadSpeciesList = { _, _ -> throw SocketTimeoutException() },
                )

            viewModel.loadSpecies("http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val state = viewModel.speciesState as SpeciesUiState.Error
            assertEquals("서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.", state.message)
        }

    @Test
    fun loadSpeciesDetail_setsDetailAndHotspotReadyStatesWhenRequestsSucceed() =
        runTest {
            val detail = speciesDetailFixture()
            val hotspots = listOf(hotspotFixture())
            val viewModel =
                SpeciesViewModel(
                    loadSpeciesDetailById = { speciesId, _, _ ->
                        assertEquals("lynx", speciesId)
                        detail
                    },
                    loadHotspotsBySpeciesId = { speciesId, _, _ ->
                        assertEquals("lynx", speciesId)
                        hotspots
                    },
                )

            viewModel.loadSpeciesDetail("lynx", "http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val detailState = viewModel.speciesDetailState as SpeciesDetailUiState.Ready
            val hotspotState = viewModel.hotspotState as HotspotUiState.Ready
            assertEquals("lynx", viewModel.selectedSpeciesId)
            assertSame(detail, detailState.detail)
            assertSame(hotspots, hotspotState.hotspots)
        }

    @Test
    fun loadSpeciesDetail_setsIndependentErrorsWhenDetailAndHotspotRequestsFail() =
        runTest {
            val viewModel =
                SpeciesViewModel(
                    loadSpeciesDetailById = { _, _, _ -> throw ConnectException() },
                    loadHotspotsBySpeciesId = { _, _, _ -> throw SocketTimeoutException() },
                )

            viewModel.loadSpeciesDetail("lynx", "http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val detailState = viewModel.speciesDetailState as SpeciesDetailUiState.Error
            val hotspotState = viewModel.hotspotState as HotspotUiState.Error
            assertEquals("서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.", detailState.message)
            assertEquals("서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.", hotspotState.message)
        }

    @Test
    fun openCandidate_loadsListWhenSpeciesStateIsIdle() =
        runTest {
            var listLoadCount = 0
            val viewModel =
                SpeciesViewModel(
                    loadSpeciesList = { _, _ ->
                        listLoadCount += 1
                        listOf(speciesSummaryFixture())
                    },
                    loadSpeciesDetailById = { _, _, _ -> speciesDetailFixture() },
                    loadHotspotsBySpeciesId = { _, _, _ -> listOf(hotspotFixture()) },
                )

            viewModel.openCandidate("lynx", "http://10.0.2.2:8000") {}
            advanceUntilIdle()

            assertEquals(1, listLoadCount)
            assertEquals("lynx", viewModel.selectedSpeciesId)
            assertEquals(true, viewModel.speciesState is SpeciesUiState.Ready)
            assertEquals(true, viewModel.speciesDetailState is SpeciesDetailUiState.Ready)
            assertEquals(true, viewModel.hotspotState is HotspotUiState.Ready)
        }

    @Test
    fun updateSpeciesSearch_updatesSearchText() {
        val viewModel = SpeciesViewModel()

        viewModel.updateSpeciesSearch("삵")

        assertEquals("삵", viewModel.speciesSearch)
    }

    @Test
    fun refreshSpeciesList_clearsSearchSelectionAndDetailState() =
        runTest {
            val refreshed = listOf(speciesSummaryFixture(id = "falcon", commonName = "황조롱이"))
            val viewModel =
                SpeciesViewModel(
                    loadSpeciesList = { _, _ -> refreshed },
                    loadSpeciesDetailById = { _, _, _ -> speciesDetailFixture() },
                    loadHotspotsBySpeciesId = { _, _, _ -> listOf(hotspotFixture()) },
                )

            viewModel.updateSpeciesSearch("황조롱이")
            viewModel.loadSpeciesDetail("falcon", "http://10.0.2.2:8000") {}
            advanceUntilIdle()

            viewModel.refreshSpeciesList("http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val state = viewModel.speciesState as SpeciesUiState.Ready
            assertEquals("", viewModel.speciesSearch)
            assertEquals(null, viewModel.selectedSpeciesId)
            assertEquals(SpeciesDetailUiState.Empty, viewModel.speciesDetailState)
            assertEquals(HotspotUiState.Empty, viewModel.hotspotState)
            assertSame(refreshed, state.species)
        }

    private fun speciesSummaryFixture(
        id: String = "lynx",
        commonName: String = "삵",
    ) = SpeciesSummaryDto(
        id = id,
        commonName = commonName,
        scientificName = "Prionailurus bengalensis",
        category = "mammal",
        protectionGrade = "II",
        bestMonths = "4-10",
    )

    private fun speciesDetailFixture() =
        SpeciesDetailDto(
            id = "lynx",
            commonName = "삵",
            scientificName = "Prionailurus bengalensis",
            category = "mammal",
            protectionGrade = "II",
            habitat = "숲과 농경지 가장자리",
            diet = "설치류와 조류",
            breedingSeason = "봄",
            activeTime = "야행성",
            observationTips = "흔적과 배설물을 함께 확인",
            bestMonths = "4-10",
            similarSpecies = "고양이",
            description = "국내에 서식하는 야생 고양잇과 포유류",
        )

    private fun hotspotFixture() =
        HotspotDto(
            id = 1,
            name = "DMZ 생태길",
            region = "강원",
            latitude = 38.1,
            longitude = 127.2,
            speciesId = "lynx",
            speciesName = "삵",
            bestMonths = "4-10",
            observationScore = 0.86,
            accessLevel = "보통",
            transportNote = "자차 권장",
            entryFee = 0,
            facilities = "전망대",
            safetyNote = "지정 탐방로 이용",
        )
}
