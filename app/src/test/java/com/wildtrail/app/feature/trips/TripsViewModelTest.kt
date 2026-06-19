package com.wildtrail.app.feature.trips

import com.wildtrail.app.data.dto.CostBreakdownDto
import com.wildtrail.app.data.dto.TripDayItemDto
import com.wildtrail.app.data.dto.TripDayPlanDto
import com.wildtrail.app.data.dto.TripPlanRequestDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import com.wildtrail.app.data.dto.TripRouteStopDto
import com.wildtrail.app.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import java.net.ConnectException

@OptIn(ExperimentalCoroutinesApi::class)
class TripsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun updateTripInputs_filtersNumericFieldsAndStoresPreferences() {
        val viewModel = TripsViewModel()

        viewModel.updateTripSpeciesId("lynx")
        viewModel.updateTripOrigin(" 부산역 ")
        viewModel.updateTripDays("12일")
        viewModel.updateTripBudget("150000원")
        viewModel.updateTripTravelers("123명")
        viewModel.updateTripMonth("09월")
        viewModel.updateTripTransport("car")
        viewModel.updateTripAccommodation("hotel")
        viewModel.updateTripDifficulty("hard")

        assertEquals("lynx", viewModel.tripSpeciesId)
        assertEquals(" 부산역 ", viewModel.tripOrigin)
        assertEquals("1", viewModel.tripDays)
        assertEquals("150000", viewModel.tripBudget)
        assertEquals("12", viewModel.tripTravelers)
        assertEquals("09", viewModel.tripMonth)
        assertEquals("car", viewModel.tripTransport)
        assertEquals("hotel", viewModel.tripAccommodation)
        assertEquals("hard", viewModel.tripDifficulty)
    }

    @Test
    fun planTrip_buildsCoercedPayloadAndSetsReadyStateWhenRequestSucceeds() =
        runTest {
            val expected = tripPlanFixture()
            var capturedPayload: TripPlanRequestDto? = null
            var fallbackUrl: String? = null
            val viewModel =
                TripsViewModel { baseUrl, onBaseUrlFallback, payload ->
                    assertEquals("http://10.0.2.2:8000", baseUrl)
                    onBaseUrlFallback("http://127.0.0.1:8000")
                    capturedPayload = payload
                    expected
                }
            viewModel.updateTripSpeciesId("")
            viewModel.updateTripOrigin("")
            viewModel.updateTripDays("9")
            viewModel.updateTripBudget("100")
            viewModel.updateTripTravelers("99")
            viewModel.updateTripMonth("99")
            viewModel.updateTripTransport("car")
            viewModel.updateTripAccommodation("hotel")
            viewModel.updateTripDifficulty("medium")
            viewModel.openNativeMap(expected)

            viewModel.planTrip("http://10.0.2.2:8000") { fallbackUrl = it }
            advanceUntilIdle()

            val payload = capturedPayload ?: error("payload was not captured")
            val state = viewModel.tripState as TripUiState.Ready
            assertEquals("pica_pica", payload.speciesId)
            assertEquals("서울역", payload.origin)
            assertEquals(7, payload.days)
            assertEquals(30_000, payload.budgetKrw)
            assertEquals(10, payload.travelers)
            assertEquals(12, payload.month)
            assertEquals("car", payload.preferences.transport)
            assertEquals("hotel", payload.preferences.accommodation)
            assertEquals("medium", payload.preferences.difficulty)
            assertEquals("http://127.0.0.1:8000", fallbackUrl)
            assertSame(expected, state.plan)
            assertNull(viewModel.nativeMapPlan)
        }

    @Test
    fun planTrip_setsUserFacingErrorWhenRequestFails() =
        runTest {
            val viewModel = TripsViewModel { _, _, _ -> throw ConnectException() }

            viewModel.planTrip("http://10.0.2.2:8000") {}
            advanceUntilIdle()

            val state = viewModel.tripState as TripUiState.Error
            assertEquals("서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.", state.message)
        }

    @Test
    fun openAndCloseNativeMap_updatesNativeMapPlan() {
        val plan = tripPlanFixture()
        val viewModel = TripsViewModel()

        viewModel.openNativeMap(plan)
        assertSame(plan, viewModel.nativeMapPlan)

        viewModel.closeNativeMap()
        assertNull(viewModel.nativeMapPlan)
    }

    @Test
    fun selectingSpeciesResetsTripState() =
        runTest {
            val viewModel = TripsViewModel { _, _, _ -> tripPlanFixture() }
            viewModel.planTrip("http://10.0.2.2:8000") {}
            advanceUntilIdle()

            viewModel.selectTripSpecies("lynx")

            assertEquals("lynx", viewModel.tripSpeciesId)
            assertEquals(TripUiState.Empty, viewModel.tripState)
        }

    @Test
    fun prepareTripForSpecies_setsSpeciesAndClearsState() {
        val viewModel = TripsViewModel()

        viewModel.prepareTripForSpecies("lynx")

        assertEquals("lynx", viewModel.tripSpeciesId)
        assertEquals(TripUiState.Empty, viewModel.tripState)
    }

    private fun tripPlanFixture() =
        TripPlanResponseDto(
            speciesId = "lynx",
            speciesName = "삵",
            origin = "서울역",
            days = 1,
            travelers = 1,
            hotspotName = "DMZ 생태길",
            hotspotLatitude = 38.1,
            hotspotLongitude = 127.2,
            region = "강원",
            summary = "하루 탐방 코스",
            checklist = listOf("쌍안경", "물"),
            routeStops =
                listOf(
                    TripRouteStopDto(name = "서울역", role = "origin", latitude = 37.55, longitude = 126.97),
                    TripRouteStopDto(name = "DMZ 생태길", role = "hotspot", latitude = 38.1, longitude = 127.2),
                ),
            daysPlan =
                listOf(
                    TripDayPlanDto(
                        day = 1,
                        title = "탐방",
                        items = listOf(TripDayItemDto(time = "09:00", activity = "출발", location = "서울역")),
                    ),
                ),
            costs =
                CostBreakdownDto(
                    transport = 50_000,
                    accommodation = 0,
                    food = 20_000,
                    entryFee = 0,
                    misc = 10_000,
                    total = 80_000,
                    perPerson = 80_000,
                ),
            disclaimer = "실제 현장 상황을 확인하세요.",
            source = "test",
        )
}
