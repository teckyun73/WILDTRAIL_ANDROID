package com.wildtrail.app.feature.status

import com.wildtrail.app.data.dto.HealthResponseDto

sealed interface HealthUiState {
    data object Idle : HealthUiState

    data object Loading : HealthUiState

    data class Ready(
        val health: HealthResponseDto,
    ) : HealthUiState

    data class Error(
        val message: String,
    ) : HealthUiState
}
