package com.wildtrail.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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

    @Test
    fun statusTabSwitchesApiEnvironmentPresets() {
        selectTab("상태")

        composeRule.onNodeWithTag("status-api-preset-adb-reverse").performClick()
        composeRule.onNodeWithTag("status-base-url").assertTextContains("http://127.0.0.1:8000")
        composeRule.onNodeWithText("현재 환경: ADB reverse").assertIsDisplayed()

        composeRule.onNodeWithTag("status-api-reset-button").performClick()
        composeRule.onNodeWithText("현재 환경: 빌드 기본값").assertIsDisplayed()
    }

    @Test
    fun recordsTabShowsObservationRecordContext() {
        selectTab("기록")
        composeRule.onNodeWithText("관찰 기록").assertIsDisplayed()
        composeRule.onNodeWithText("식별 결과로 저장한 관찰 기록을 시간순으로 확인합니다.").assertIsDisplayed()
    }

    @Test
    fun tripsTabAllowsEditingPlanningFields() {
        selectTab("여행")
        composeRule.onNodeWithText("여행 플래너").assertIsDisplayed()
        composeRule.onNodeWithText("계획 조건").assertIsDisplayed()

        replaceText("trip-species-id", "grus_japonensis")
        replaceText("trip-origin", "Seoul")
        replaceText("trip-days", "2")
        replaceText("trip-travelers", "2")
        replaceText("trip-month", "10")
        replaceText("trip-budget", "300000")

        composeRule.onNodeWithTag("trip-plan-button")
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun replaceText(tag: String, value: String) {
        val field = composeRule.onNodeWithTag(tag)
        field.performScrollTo()
        field.performTextClearance()
        field.performTextInput(value)
    }

    private fun selectTab(label: String) {
        composeRule.onNode(hasText(label) and hasClickAction()).performClick()
    }
}

