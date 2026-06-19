package com.wildtrail.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wildtrail.app.ui.components.OfflineErrorPanel
import com.wildtrail.app.ui.theme.WildTrailTheme
import org.junit.Rule
import org.junit.Test

class OfflineErrorPanelTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsGuidanceAndRunsRetryAction() {
        var retryCount by mutableStateOf(0)

        composeRule.setContent {
            WildTrailTheme {
                OfflineErrorPanel(
                    title = "연결 실패",
                    message = "서버에 연결할 수 없습니다.",
                    actionLabel = "다시 시도",
                    onAction = { retryCount += 1 },
                    actionTestTag = "offline-error-retry",
                )
            }
        }

        composeRule.onNodeWithText("연결 실패").assertIsDisplayed()
        composeRule.onNodeWithText("서버에 연결할 수 없습니다.").assertIsDisplayed()
        composeRule.onNodeWithText("API 서버 주소와 네트워크 연결을 확인한 뒤 다시 시도하세요.", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("offline-error-retry").performClick()

        composeRule.runOnIdle {
            check(retryCount == 1) { "Expected retry action to run once, got $retryCount." }
        }
    }
}

