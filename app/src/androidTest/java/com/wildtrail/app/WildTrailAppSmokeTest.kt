package com.wildtrail.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class WildTrailAppSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsOnIdentifyTab() {
        composeRule.onNodeWithText("WildTrail").assertIsDisplayed()
        composeRule.onNodeWithText("사진 식별").assertIsDisplayed()
        composeRule.onNodeWithText("이미지 고르기").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationOpensPrimaryTabs() {
        selectTab("상태")
        composeRule.onNodeWithText("API 서버").assertIsDisplayed()

        selectTab("도감")
        composeRule.onNodeWithText("도감 목록").assertIsDisplayed()

        selectTab("기록")
        composeRule.onNodeWithText("관찰 기록").assertIsDisplayed()

        selectTab("여행")
        composeRule.onNodeWithText("여행 플래너").assertIsDisplayed()
    }

    @Test
    fun statusTabAllowsApiUrlEditingAndShowsHealthCheckAction() {
        selectTab("상태")
        composeRule.onNodeWithText("API 서버").assertIsDisplayed()
        composeRule.onNodeWithText("Base URL").assertIsDisplayed()

        val apiUrlField = composeRule.onNodeWithTag("status-base-url")
        apiUrlField.performTextClearance()
        apiUrlField.performTextInput("http://127.0.0.1:8000")

        composeRule.onNodeWithTag("status-check-button").assertIsDisplayed()
    }

    private fun selectTab(label: String) {
        composeRule.onNode(hasText(label) and hasClickAction()).performClick()
    }
}







