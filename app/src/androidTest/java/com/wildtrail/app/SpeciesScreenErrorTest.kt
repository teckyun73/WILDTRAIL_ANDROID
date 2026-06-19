package com.wildtrail.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.feature.species.HotspotUiState
import com.wildtrail.app.feature.species.SpeciesDetailUiState
import com.wildtrail.app.feature.species.SpeciesScreen
import com.wildtrail.app.feature.species.SpeciesUiState
import com.wildtrail.app.ui.theme.WildTrailTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SpeciesScreenErrorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun detailAndHotspotErrorsShowRetryActions() {
        var retryCount by mutableStateOf(0)
        var refreshCount by mutableStateOf(0)

        composeRule.setContent {
            WildTrailTheme {
                SpeciesScreen(
                    speciesState = SpeciesUiState.Ready(listOf(speciesFixture())),
                    speciesDetailState = SpeciesDetailUiState.Error("상세 정보를 불러올 수 없습니다."),
                    hotspotState = HotspotUiState.Error("관찰지를 불러올 수 없습니다."),
                    search = "",
                    onSearchChange = {},
                    selectedSpeciesId = "lynx",
                    onSelectSpecies = {},
                    onPlanTripForSpecies = {},
                    onRefresh = { refreshCount += 1 },
                    onRetrySelectedSpecies = { retryCount += 1 },
                    isLoading = false,
                )
            }
        }

        composeRule.onNodeWithText("상세 정보 오류").assertIsDisplayed()
        composeRule.onNodeWithText("상세 정보를 불러올 수 없습니다.").assertIsDisplayed()
        composeRule.onNodeWithText("관찰지 오류").assertIsDisplayed()
        composeRule.onNodeWithText("관찰지를 불러올 수 없습니다.").assertIsDisplayed()

        composeRule.onNodeWithTag("species-detail-error-retry-button").performClick()
        composeRule.onNodeWithTag("hotspot-error-retry-button").performClick()

        composeRule.runOnIdle {
            assertEquals(2, retryCount)
            assertEquals(0, refreshCount)
        }
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
