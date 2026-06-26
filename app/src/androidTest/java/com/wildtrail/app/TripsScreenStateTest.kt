package com.wildtrail.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.wildtrail.app.data.dto.AccommodationOptionDto
import com.wildtrail.app.data.dto.CostBreakdownDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.data.dto.TripDayItemDto
import com.wildtrail.app.data.dto.TripDayPlanDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import com.wildtrail.app.feature.species.SpeciesUiState
import com.wildtrail.app.feature.trips.TripUiState
import com.wildtrail.app.feature.trips.TripsScreen
import com.wildtrail.app.ui.theme.WildTrailTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TripsScreenStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun speciesChooserIdleRefreshButtonRunsRefreshAction() {
        var refreshCount by mutableStateOf(0)

        composeRule.setContent {
            WildTrailTheme {
                TripsScreen(
                    nativeMapPlan = null,
                    onCloseNativeMap = {},
                    speciesId = "lynx",
                    onSpeciesIdChange = {},
                    speciesState = SpeciesUiState.Idle,
                    onSelectSpecies = {},
                    onRefreshSpecies = { refreshCount += 1 },
                    origin = "서울역",
                    onOriginChange = {},
                    days = "1",
                    onDaysChange = {},
                    budget = "150000",
                    onBudgetChange = {},
                    travelers = "1",
                    onTravelersChange = {},
                    month = "5",
                    onMonthChange = {},
                    transport = "public",
                    onTransportChange = {},
                    accommodation = "guesthouse",
                    onAccommodationChange = {},
                    difficulty = "easy",
                    onDifficultyChange = {},
                    tripState = TripUiState.Empty,
                    onPlanTrip = {},
                    onOpenNativeMap = {},
                    isLoading = false,
                )
            }
        }

        composeRule.onNodeWithTag("trip-species-refresh-button").performClick()

        composeRule.runOnIdle {
            assertEquals(1, refreshCount)
        }
    }

    @Test
    fun speciesChooserErrorRetryButtonRunsRefreshAction() {
        var refreshCount by mutableStateOf(0)

        composeRule.setContent {
            WildTrailTheme {
                TripsScreen(
                    nativeMapPlan = null,
                    onCloseNativeMap = {},
                    speciesId = "lynx",
                    onSpeciesIdChange = {},
                    speciesState = SpeciesUiState.Error("도감 목록을 불러올 수 없습니다."),
                    onSelectSpecies = {},
                    onRefreshSpecies = { refreshCount += 1 },
                    origin = "서울역",
                    onOriginChange = {},
                    days = "1",
                    onDaysChange = {},
                    budget = "150000",
                    onBudgetChange = {},
                    travelers = "1",
                    onTravelersChange = {},
                    month = "5",
                    onMonthChange = {},
                    transport = "public",
                    onTransportChange = {},
                    accommodation = "guesthouse",
                    onAccommodationChange = {},
                    difficulty = "easy",
                    onDifficultyChange = {},
                    tripState = TripUiState.Empty,
                    onPlanTrip = {},
                    onOpenNativeMap = {},
                    isLoading = false,
                )
            }
        }

        composeRule.onNodeWithText("종 목록을 불러오지 못했습니다.").assertIsDisplayed()
        composeRule.onNodeWithTag("trip-species-error-retry-button").performClick()

        composeRule.runOnIdle {
            assertEquals(1, refreshCount)
        }
    }

    @Test
    fun tripErrorShowsRetryAction() {
        var retryCount by mutableStateOf(0)

        composeRule.setContent {
            WildTrailTheme {
                TripsScreen(
                    nativeMapPlan = null,
                    onCloseNativeMap = {},
                    speciesId = "lynx",
                    onSpeciesIdChange = {},
                    speciesState = SpeciesUiState.Ready(listOf(speciesFixture())),
                    onSelectSpecies = {},
                    onRefreshSpecies = {},
                    origin = "서울역",
                    onOriginChange = {},
                    days = "1",
                    onDaysChange = {},
                    budget = "150000",
                    onBudgetChange = {},
                    travelers = "1",
                    onTravelersChange = {},
                    month = "5",
                    onMonthChange = {},
                    transport = "public",
                    onTransportChange = {},
                    accommodation = "guesthouse",
                    onAccommodationChange = {},
                    difficulty = "easy",
                    onDifficultyChange = {},
                    tripState = TripUiState.Error("여행 계획을 생성할 수 없습니다."),
                    onPlanTrip = { retryCount += 1 },
                    onOpenNativeMap = {},
                    isLoading = false,
                )
            }
        }

        assertTextExists("여행 계획 실패")
        assertTextExists("여행 계획을 생성할 수 없습니다.")
        composeRule
            .onNodeWithTag("trip-error-retry-button")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, retryCount)
        }
    }

    @Test
    fun unmatchedSpeciesShowsGuidance() {
        composeRule.setContent {
            WildTrailTheme {
                TripsScreen(
                    nativeMapPlan = null,
                    onCloseNativeMap = {},
                    speciesId = "unknown",
                    onSpeciesIdChange = {},
                    speciesState = SpeciesUiState.Ready(listOf(speciesFixture())),
                    onSelectSpecies = {},
                    onRefreshSpecies = {},
                    origin = "서울역",
                    onOriginChange = {},
                    days = "1",
                    onDaysChange = {},
                    budget = "150000",
                    onBudgetChange = {},
                    travelers = "1",
                    onTravelersChange = {},
                    month = "5",
                    onMonthChange = {},
                    transport = "public",
                    onTransportChange = {},
                    accommodation = "guesthouse",
                    onAccommodationChange = {},
                    difficulty = "easy",
                    onDifficultyChange = {},
                    tripState = TripUiState.Empty,
                    onPlanTrip = {},
                    onOpenNativeMap = {},
                    isLoading = false,
                )
            }
        }

        composeRule.onNodeWithText("입력값과 가까운 종을 선택하세요.").assertIsDisplayed()
        composeRule.onNodeWithText("일치하는 종이 없습니다. 도감 탭에서 정확한 ID를 확인하세요.").assertIsDisplayed()
    }

    @Test
    fun numericInputsUpdateThroughCallbacks() {
        var days by mutableStateOf("1")
        var travelers by mutableStateOf("1")
        var month by mutableStateOf("")
        var budget by mutableStateOf("150000")

        composeRule.setContent {
            WildTrailTheme {
                TripsScreen(
                    nativeMapPlan = null,
                    onCloseNativeMap = {},
                    speciesId = "lynx",
                    onSpeciesIdChange = {},
                    speciesState = SpeciesUiState.Ready(listOf(speciesFixture())),
                    onSelectSpecies = {},
                    onRefreshSpecies = {},
                    origin = "서울역",
                    onOriginChange = {},
                    days = days,
                    onDaysChange = { days = it.filter(Char::isDigit).take(1) },
                    budget = budget,
                    onBudgetChange = { budget = it.filter(Char::isDigit).take(9) },
                    travelers = travelers,
                    onTravelersChange = { travelers = it.filter(Char::isDigit).take(2) },
                    month = month,
                    onMonthChange = { month = it.filter(Char::isDigit).take(2) },
                    transport = "public",
                    onTransportChange = {},
                    accommodation = "guesthouse",
                    onAccommodationChange = {},
                    difficulty = "easy",
                    onDifficultyChange = {},
                    tripState = TripUiState.Empty,
                    onPlanTrip = {},
                    onOpenNativeMap = {},
                    isLoading = false,
                )
            }
        }

        replaceText("trip-days", "12일")
        replaceText("trip-travelers", "123명")
        replaceText("trip-month", "09월")
        replaceText("trip-budget", "150000원")

        composeRule.onNodeWithTag("trip-days").assertTextContains("1")
        composeRule.onNodeWithTag("trip-travelers").assertTextContains("12")
        composeRule.onNodeWithTag("trip-month").assertTextContains("09")
        composeRule.onNodeWithTag("trip-budget").assertTextContains("150000")
    }

    @Test
    fun tripPlanCostPanelShowsTotalPerPersonAndMisc() {
        composeRule.setContent {
            WildTrailTheme {
                TripsScreen(
                    nativeMapPlan = null,
                    onCloseNativeMap = {},
                    speciesId = "lynx",
                    onSpeciesIdChange = {},
                    speciesState = SpeciesUiState.Ready(listOf(speciesFixture())),
                    onSelectSpecies = {},
                    onRefreshSpecies = {},
                    origin = "서울역",
                    onOriginChange = {},
                    days = "2",
                    onDaysChange = {},
                    budget = "150000",
                    onBudgetChange = {},
                    travelers = "2",
                    onTravelersChange = {},
                    month = "5",
                    onMonthChange = {},
                    transport = "public",
                    onTransportChange = {},
                    accommodation = "guesthouse",
                    onAccommodationChange = {},
                    difficulty = "easy",
                    onDifficultyChange = {},
                    tripState = TripUiState.Ready(tripPlanFixture()),
                    onPlanTrip = {},
                    onOpenNativeMap = {},
                    isLoading = false,
                )
            }
        }

        assertTextExists("2일 · 2명 · 총 120,000원 · 1인 60,000원")
        composeRule.onNodeWithText("총 비용").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("1인 비용").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("기타").performScrollTo().assertIsDisplayed()
        assertTextExists("12,000원")
    }

    @Test
    fun tripPlanShowsAccommodationOptions() {
        composeRule.setContent {
            WildTrailTheme {
                TripsScreen(
                    nativeMapPlan = null,
                    onCloseNativeMap = {},
                    speciesId = "lynx",
                    onSpeciesIdChange = {},
                    speciesState = SpeciesUiState.Ready(listOf(speciesFixture())),
                    onSelectSpecies = {},
                    onRefreshSpecies = {},
                    origin = "서울역",
                    onOriginChange = {},
                    days = "2",
                    onDaysChange = {},
                    budget = "150000",
                    onBudgetChange = {},
                    travelers = "2",
                    onTravelersChange = {},
                    month = "5",
                    onMonthChange = {},
                    transport = "public",
                    onTransportChange = {},
                    accommodation = "guesthouse",
                    onAccommodationChange = {},
                    difficulty = "easy",
                    onDifficultyChange = {},
                    tripState = TripUiState.Ready(tripPlanFixture()),
                    onPlanTrip = {},
                    onOpenNativeMap = {},
                    isLoading = false,
                )
            }
        }

        composeRule.onNodeWithText("주변 숙박").performScrollTo().assertIsDisplayed()
        assertTextExists("DMZ 게스트하우스")
        assertTextExists("50,000원 ~ 70,000원")
        assertTextExists("가능")
        assertTextExists("033-000-0000")
        composeRule
            .onNode(hasText("https://example.com/dmz-guesthouse").and(hasClickAction()))
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun assertTextExists(text: String) {
        val nodes = composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes()
        check(nodes.isNotEmpty()) { "Expected text node '$text' to exist." }
    }

    private fun replaceText(
        tag: String,
        value: String,
    ) {
        val field = composeRule.onNodeWithTag(tag)
        field.performScrollTo()
        field.performTextClearance()
        field.performTextInput(value)
    }

    private fun speciesFixture() =
        SpeciesSummaryDto(
            id = "lynx",
            commonName = "삵",
            scientificName = "Prionailurus bengalensis",
            category = "mammal",
            protectionGrade = "II",
            bestMonths = "4-10",
        )

    private fun tripPlanFixture() =
        TripPlanResponseDto(
            speciesId = "lynx",
            speciesName = "삵",
            origin = "서울역",
            days = 2,
            travelers = 2,
            hotspotName = "DMZ 생태길",
            hotspotLatitude = 38.1,
            hotspotLongitude = 127.2,
            region = "강원",
            summary = "2인 기준 탐방 코스",
            checklist = listOf("쌍안경", "물"),
            accommodationOptions =
                listOf(
                    AccommodationOptionDto(
                        name = "DMZ 게스트하우스",
                        type = "guesthouse",
                        address = "강원 철원군 생태길 1",
                        distanceKm = 3.2,
                        priceMinKrw = 50_000,
                        priceMaxKrw = 70_000,
                        parkingAvailable = true,
                        phone = "033-000-0000",
                        bookingUrl = "https://example.com/dmz-guesthouse",
                        source = "test",
                    ),
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
                    accommodation = 30_000,
                    food = 20_000,
                    entryFee = 8_000,
                    misc = 12_000,
                    total = 120_000,
                    perPerson = 60_000,
                ),
            disclaimer = "실제 현장 상황을 확인하세요.",
            source = "test",
        )
}
