package com.wildtrail.app.feature.records

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildtrail.app.data.dto.IdentificationCandidateDto
import com.wildtrail.app.data.dto.SightingCreateDto
import com.wildtrail.app.data.dto.SightingDto
import com.wildtrail.app.data.network.ApiCallRunner
import com.wildtrail.app.data.network.toUserFacingMessage
import kotlinx.coroutines.launch

class RecordsViewModel(
    private val loadSightingList: suspend (
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) -> List<SightingDto> = { baseUrl, onBaseUrlFallback ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback) { it.listSightings() }
    },
    private val createSightingRecord: suspend (
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
        payload: SightingCreateDto,
    ) -> SightingDto = { baseUrl, onBaseUrlFallback, payload ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback, retryTransient = false) { it.createSighting(payload) }
    },
) : ViewModel() {
    var sightingState by mutableStateOf<SightingUiState>(SightingUiState.Idle)
        private set
    var saveSightingMessage by mutableStateOf<String?>(null)
        private set

    fun loadSightings(
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        viewModelScope.launch {
            sightingState = SightingUiState.Loading
            sightingState =
                try {
                    SightingUiState.Ready(loadSightingList(baseUrl, onBaseUrlFallback))
                } catch (error: Exception) {
                    SightingUiState.Error(error.toUserFacingMessage("관찰 기록을 불러올 수 없습니다."))
                }
        }
    }

    fun saveCandidate(
        candidate: IdentificationCandidateDto,
        mediaType: String,
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        viewModelScope.launch {
            saveSightingMessage = "기록 저장 중..."
            saveSightingMessage =
                try {
                    createSightingRecord(
                        baseUrl,
                        onBaseUrlFallback,
                        SightingCreateDto(
                            speciesId = candidate.speciesId,
                            confidence = candidate.confidence,
                            mediaType = mediaType,
                            note = "Android ${if (mediaType == "audio") "오디오" else "이미지"} 식별 기록",
                        ),
                    )
                    loadSightings(baseUrl, onBaseUrlFallback)
                    "${candidate.commonName} 관찰 기록을 저장했습니다."
                } catch (error: Exception) {
                    error.toUserFacingMessage("관찰 기록 저장에 실패했습니다.")
                }
        }
    }
}
