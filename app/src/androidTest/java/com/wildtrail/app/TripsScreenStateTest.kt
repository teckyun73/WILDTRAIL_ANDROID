package com.wildtrail.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.wildtrail.app.data.dto.SpeciesSummaryDto
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
        composeRule.onNodeWithTag("trip-error-retry-button")
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


    private fun assertTextExists(text: String) {
        val nodes = composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes()
        check(nodes.isNotEmpty()) { "Expected text node '$text' to exist." }
    }
    private fun replaceText(tag: String, value: String) {
        val field = composeRule.onNodeWithTag(tag)
        field.performScrollTo()
        field.performTextClearance()
        field.performTextInput(value)
    }

    private fun speciesFixture() = SpeciesSummaryDto(
        id = "lynx",
        commonName = "삵",
        scientificName = "Prionailurus bengalensis",
        category = "mammal",
        protectionGrade = "II",
        bestMonths = "4-10",
    )
}
