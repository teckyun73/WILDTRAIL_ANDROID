package com.wildtrail.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.wildtrail.app.data.dto.HealthResponseDto
import com.wildtrail.app.data.dto.ModelStatusDto
import com.wildtrail.app.feature.status.HealthUiState
import com.wildtrail.app.feature.status.StatusScreen
import com.wildtrail.app.ui.theme.WildTrailTheme
import org.junit.Rule
import org.junit.Test

class StatusScreenModelMetadataTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun statusScreenShowsAudioModelTrainingMetadataAndLabelScope() {
        composeRule.setContent {
            WildTrailTheme {
                StatusScreen(
                    baseUrl = "http://10.0.2.2:8000",
                    onBaseUrlChange = {},
                    apiPresets = emptyList(),
                    selectedEnvironmentLabel = "테스트",
                    onPresetSelected = {},
                    onResetBaseUrl = {},
                    healthState = HealthUiState.Ready(healthFixture()),
                    onCheckHealth = {},
                    isLoading = false,
                )
            }
        }

        composeRule
            .onNodeWithText("오디오 모델은 별도 학습 라벨 31개 기준입니다. 식별 후보 저장 전 도감 종과 매칭되는지 확인하세요.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("classes 31").assertIsDisplayed()
        composeRule.onNodeWithText("val 52%").assertIsDisplayed()
        composeRule.onNodeWithText("resnet18").assertIsDisplayed()
    }

    private fun healthFixture() =
        HealthResponseDto(
            status = "ok",
            service = "wildtrail-api",
            imageModel =
                ModelStatusDto(
                    modelLoaded = true,
                    modelClasses = 30,
                    modelValAcc = 0.835,
                    modelPath = "../models/checkpoints/best_model.pth",
                    modelVersion = "wildtrail-efficientnet_b0-30c",
                    trainedAt = "2026-06-19T02:22:57+09:00",
                    backbone = "efficientnet_b0",
                    preprocessVersion = "resize256_centercrop224",
                    datasetFingerprint = "c591d63fc5edd57c",
                ),
            audioModel =
                ModelStatusDto(
                    modelLoaded = true,
                    modelClasses = 31,
                    modelValAcc = 0.52,
                    modelPath = "../models/checkpoints/best_audio_model.pth",
                    backbone = "resnet18",
                ),
            speciesJsonCount = 30,
            speciesDbCount = 30,
            llmConfigured = true,
            llmProvider = "gemini",
            llmModel = "gemini-2.5-flash-lite",
        )
}
