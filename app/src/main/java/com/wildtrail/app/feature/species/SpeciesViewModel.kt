package com.wildtrail.app.feature.species

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildtrail.app.data.dto.HotspotDto
import com.wildtrail.app.data.dto.SpeciesDetailDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.data.network.ApiCallRunner
import com.wildtrail.app.data.network.toUserFacingMessage
import kotlinx.coroutines.launch

class SpeciesViewModel(
    private val loadSpeciesList: suspend (
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) -> List<SpeciesSummaryDto> = { baseUrl, onBaseUrlFallback ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback) { it.listSpecies() }
    },
    private val loadSpeciesDetailById: suspend (
        speciesId: String,
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) -> SpeciesDetailDto = { speciesId, baseUrl, onBaseUrlFallback ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback) { it.getSpecies(speciesId) }
    },
    private val loadHotspotsBySpeciesId: suspend (
        speciesId: String,
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) -> List<HotspotDto> = { speciesId, baseUrl, onBaseUrlFallback ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback) { it.listLocations(speciesId) }
    },
) : ViewModel() {
    var speciesState by mutableStateOf<SpeciesUiState>(SpeciesUiState.Idle)
        private set
    var speciesDetailState by mutableStateOf<SpeciesDetailUiState>(SpeciesDetailUiState.Empty)
        private set
    var hotspotState by mutableStateOf<HotspotUiState>(HotspotUiState.Empty)
        private set
    var speciesSearch by mutableStateOf("")
        private set
    var selectedSpeciesId by mutableStateOf<String?>(null)
        private set

    fun updateSpeciesSearch(value: String) {
        speciesSearch = value
    }

    fun loadSpecies(
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        viewModelScope.launch {
            speciesState = SpeciesUiState.Loading
            speciesState =
                try {
                    SpeciesUiState.Ready(loadSpeciesList(baseUrl, onBaseUrlFallback))
                } catch (error: Exception) {
                    SpeciesUiState.Error(error.toUserFacingMessage("도감 목록을 불러올 수 없습니다."))
                }
        }
    }

    fun refreshSpeciesList(
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        speciesSearch = ""
        selectedSpeciesId = null
        speciesDetailState = SpeciesDetailUiState.Empty
        hotspotState = HotspotUiState.Empty
        loadSpecies(baseUrl, onBaseUrlFallback)
    }

    fun loadSpeciesDetail(
        speciesId: String,
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        selectedSpeciesId = speciesId
        viewModelScope.launch {
            speciesDetailState = SpeciesDetailUiState.Loading
            speciesDetailState =
                try {
                    SpeciesDetailUiState.Ready(loadSpeciesDetailById(speciesId, baseUrl, onBaseUrlFallback))
                } catch (error: Exception) {
                    SpeciesDetailUiState.Error(error.toUserFacingMessage("도감 상세를 불러올 수 없습니다."))
                }
        }
        viewModelScope.launch {
            hotspotState = HotspotUiState.Loading
            hotspotState =
                try {
                    HotspotUiState.Ready(loadHotspotsBySpeciesId(speciesId, baseUrl, onBaseUrlFallback))
                } catch (error: Exception) {
                    HotspotUiState.Error(error.toUserFacingMessage("관찰지를 불러올 수 없습니다."))
                }
        }
    }

    fun openCandidate(
        speciesId: String,
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        if (speciesState is SpeciesUiState.Idle) {
            loadSpecies(baseUrl, onBaseUrlFallback)
        }
        loadSpeciesDetail(speciesId, baseUrl, onBaseUrlFallback)
    }
}
