package com.wildtrail.app.feature.records

import com.wildtrail.app.data.dto.SightingDto

sealed interface SightingUiState {
    data object Idle : SightingUiState
    data object Loading : SightingUiState
    data class Ready(val sightings: List<SightingDto>) : SightingUiState
    data class Error(val message: String) : SightingUiState
}

