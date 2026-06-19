package com.wildtrail.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wildtrail.app.data.dto.IdentificationCandidateDto
import com.wildtrail.app.data.dto.IdentificationResultDto
import com.wildtrail.app.feature.identify.IdentifyScreen
import com.wildtrail.app.feature.identify.IdentifyUiState
import com.wildtrail.app.ui.theme.WildTrailTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IdentifyScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun readyResultSavesTopCandidateAndOpensCandidateDetail() {
        val candidate =
            IdentificationCandidateDto(
                speciesId = "lynx",
                commonName = "삵",
                scientificName = "Prionailurus bengalensis",
                confidence = 0.91,
            )
        var savedCandidate: IdentificationCandidateDto? = null
        var savedMediaType: String? = null
        var selectedSpeciesId: String? = null
        var saveMessage by mutableStateOf<String?>(null)

        composeRule.setContent {
            WildTrailTheme {
                IdentifyScreen(
                    identifyState =
                        IdentifyUiState.Ready(
                            IdentificationResultDto(
                                mediaType = "image",
                                candidates = listOf(candidate),
                                message = "식별 완료",
                                source = "stub",
                            ),
                        ),
                    selectedImageName = "lynx.jpg",
                    selectedAudioName = null,
                    isRecording = false,
                    recordingMessage = null,
                    onImageSelected = {},
                    onAudioSelected = {},
                    onStartRecording = {},
                    onStopRecording = {},
                    onCandidateSelected = { selectedSpeciesId = it },
                    onSaveCandidate = { selectedCandidate, mediaType ->
                        savedCandidate = selectedCandidate
                        savedMediaType = mediaType
                        saveMessage = "${selectedCandidate.commonName} 관찰 기록을 저장했습니다."
                    },
                    knownSpeciesIds = setOf("lynx"),
                    saveMessage = saveMessage,
                )
            }
        }

        composeRule.onNodeWithText("식별 결과").assertIsDisplayed()
        composeRule.onNodeWithText("삵").assertIsDisplayed()

        composeRule.onNodeWithTag("identify-save-top-candidate-button").performClick()
        assertEquals(candidate, savedCandidate)
        assertEquals("image", savedMediaType)
        composeRule.onNodeWithText("삵 관찰 기록을 저장했습니다.").assertIsDisplayed()

        composeRule.onNodeWithTag("identify-candidate-lynx").performClick()
        assertEquals("lynx", selectedSpeciesId)
    }

    @Test
    fun audioCandidateOutsideKnownSpeciesDisablesSaveAction() {
        val candidate =
            IdentificationCandidateDto(
                speciesId = "pica_serica",
                commonName = "어치",
                scientificName = "Pica serica",
                confidence = 0.77,
            )
        val mismatchMessage = "오디오 모델 후보 pica_serica는 현재 도감 목록에 없습니다. 기록 저장 전 백엔드 라벨과 도감 종 매핑을 확인하세요."

        composeRule.setContent {
            WildTrailTheme {
                IdentifyScreen(
                    identifyState =
                        IdentifyUiState.Ready(
                            IdentificationResultDto(
                                mediaType = "audio",
                                candidates = listOf(candidate),
                                message = "오디오 모델 식별 완료",
                                source = "model",
                            ),
                        ),
                    selectedImageName = null,
                    selectedAudioName = "jay.m4a",
                    isRecording = false,
                    recordingMessage = null,
                    onImageSelected = {},
                    onAudioSelected = {},
                    onStartRecording = {},
                    onStopRecording = {},
                    onCandidateSelected = {},
                    onSaveCandidate = { _, _ -> },
                    knownSpeciesIds = setOf("pica_pica"),
                    saveMessage = null,
                )
            }
        }

        composeRule.onNodeWithTag("identify-save-top-candidate-button").assertIsNotEnabled()
        composeRule.onNodeWithText(mismatchMessage).assertIsDisplayed()
    }
}
