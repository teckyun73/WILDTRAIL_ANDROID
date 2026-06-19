package com.wildtrail.app.feature.species

import com.wildtrail.app.data.dto.HotspotDto
import com.wildtrail.app.data.dto.SpeciesDetailDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto

sealed interface SpeciesUiState {
    data object Idle : SpeciesUiState

    data object Loading : SpeciesUiState

    data class Ready(
        val species: List<SpeciesSummaryDto>,
    ) : SpeciesUiState

    data class Error(
        val message: String,
    ) : SpeciesUiState
}

sealed interface SpeciesDetailUiState {
    data object Empty : SpeciesDetailUiState

    data object Loading : SpeciesDetailUiState

    data class Ready(
        val detail: SpeciesDetailDto,
    ) : SpeciesDetailUiState

    data class Error(
        val message: String,
    ) : SpeciesDetailUiState
}

sealed interface HotspotUiState {
    data object Empty : HotspotUiState

    data object Loading : HotspotUiState

    data class Ready(
        val hotspots: List<HotspotDto>,
    ) : HotspotUiState

    data class Error(
        val message: String,
    ) : HotspotUiState
}
