package com.wildtrail.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wildtrail.app.feature.home.HomeScreen
import com.wildtrail.app.ui.theme.WildTrailTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeScreenShowsSectorActions() {
        composeRule.setContent {
            WildTrailTheme {
                HomeScreen(
                    onOpenIdentify = {},
                    onOpenSpecies = {},
                    onOpenMap = {},
                    onOpenTrips = {},
                    onOpenRecords = {},
                )
            }
        }

        composeRule.onNodeWithText("WildTrail").assertIsDisplayed()
        composeRule.onNodeWithTag("home-action-식별").assertIsDisplayed()
        composeRule.onNodeWithTag("home-action-도감").assertIsDisplayed()
        composeRule.onNodeWithTag("home-action-지도").assertIsDisplayed()
        composeRule.onNodeWithTag("home-action-여행").assertIsDisplayed()
        composeRule.onNodeWithTag("home-action-기록").assertIsDisplayed()
    }

    @Test
    fun homeScreenSectorActionsInvokeCallbacks() {
        val opened = mutableListOf<String>()

        composeRule.setContent {
            WildTrailTheme {
                HomeScreen(
                    onOpenIdentify = { opened += "식별" },
                    onOpenSpecies = { opened += "도감" },
                    onOpenMap = { opened += "지도" },
                    onOpenTrips = { opened += "여행" },
                    onOpenRecords = { opened += "기록" },
                )
            }
        }

        composeRule.onNodeWithTag("home-action-식별").performClick()
        composeRule.onNodeWithTag("home-action-도감").performClick()
        composeRule.onNodeWithTag("home-action-지도").performClick()
        composeRule.onNodeWithTag("home-action-여행").performClick()
        composeRule.onNodeWithTag("home-action-기록").performClick()

        assertEquals(listOf("식별", "도감", "지도", "여행", "기록"), opened)
    }
}
