package com.wildtrail.app.feature.trips

import com.wildtrail.app.data.dto.TripPlanResponseDto

sealed interface TripUiState {
    data object Empty : TripUiState

    data object Loading : TripUiState

    data class Ready(
        val plan: TripPlanResponseDto,
    ) : TripUiState

    data class Error(
        val message: String,
    ) : TripUiState
}
