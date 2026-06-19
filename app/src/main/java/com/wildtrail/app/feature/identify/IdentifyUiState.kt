package com.wildtrail.app.feature.identify

import com.wildtrail.app.data.dto.IdentificationResultDto

sealed interface IdentifyUiState {
    data object Empty : IdentifyUiState

    data object Loading : IdentifyUiState

    data class Ready(
        val result: IdentificationResultDto,
    ) : IdentifyUiState

    data class Error(
        val message: String,
    ) : IdentifyUiState
}
