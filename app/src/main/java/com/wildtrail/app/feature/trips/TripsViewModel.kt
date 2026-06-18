package com.wildtrail.app.feature.trips

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildtrail.app.data.dto.TripPlanRequestDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import com.wildtrail.app.data.dto.TripPreferencesDto
import com.wildtrail.app.data.network.ApiCallRunner
import com.wildtrail.app.data.network.toUserFacingMessage
import kotlinx.coroutines.launch

class TripsViewModel(
    private val createTripPlan: suspend (baseUrl: String, onBaseUrlFallback: (String) -> Unit, payload: TripPlanRequestDto) -> TripPlanResponseDto = { baseUrl, onBaseUrlFallback, payload ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback) { it.planTrip(payload) }
    },
) : ViewModel() {
    var tripState by mutableStateOf<TripUiState>(TripUiState.Empty)
        private set
    var tripSpeciesId by mutableStateOf("pica_pica")
        private set
    var tripOrigin by mutableStateOf("서울역")
        private set
    var tripDays by mutableStateOf("1")
        private set
    var tripBudget by mutableStateOf("150000")
        private set
    var tripTravelers by mutableStateOf("1")
        private set
    var tripMonth by mutableStateOf("")
        private set
    var tripTransport by mutableStateOf("public")
        private set
    var tripAccommodation by mutableStateOf("guesthouse")
        private set
    var tripDifficulty by mutableStateOf("easy")
        private set
    var nativeMapPlan by mutableStateOf<TripPlanResponseDto?>(null)
        private set

    fun updateTripSpeciesId(value: String) {
        tripSpeciesId = value
    }

    fun selectTripSpecies(speciesId: String) {
        tripSpeciesId = speciesId
        tripState = TripUiState.Empty
    }

    fun updateTripOrigin(value: String) {
        tripOrigin = value
    }

    fun updateTripDays(value: String) {
        tripDays = value.filter(Char::isDigit).take(1)
    }

    fun updateTripBudget(value: String) {
        tripBudget = value.filter(Char::isDigit).take(9)
    }

    fun updateTripTravelers(value: String) {
        tripTravelers = value.filter(Char::isDigit).take(2)
    }

    fun updateTripMonth(value: String) {
        tripMonth = value.filter(Char::isDigit).take(2)
    }

    fun updateTripTransport(value: String) {
        tripTransport = value
    }

    fun updateTripAccommodation(value: String) {
        tripAccommodation = value
    }

    fun updateTripDifficulty(value: String) {
        tripDifficulty = value
    }

    fun openNativeMap(plan: TripPlanResponseDto) {
        nativeMapPlan = plan
    }

    fun closeNativeMap() {
        nativeMapPlan = null
    }

    fun prepareTripForSpecies(speciesId: String) {
        tripSpeciesId = speciesId
        tripState = TripUiState.Empty
    }

    fun planTrip(
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        val payload = TripPlanRequestDto(
            speciesId = tripSpeciesId.ifBlank { "pica_pica" },
            origin = tripOrigin.ifBlank { "서울역" },
            days = tripDays.toIntOrNull()?.coerceIn(1, 7) ?: 1,
            budgetKrw = tripBudget.toIntOrNull()?.coerceAtLeast(30_000) ?: 150_000,
            travelers = tripTravelers.toIntOrNull()?.coerceIn(1, 10) ?: 1,
            month = tripMonth.toIntOrNull()?.coerceIn(1, 12),
            preferences = TripPreferencesDto(
                transport = tripTransport,
                accommodation = tripAccommodation,
                difficulty = tripDifficulty,
            ),
        )
        viewModelScope.launch {
            nativeMapPlan = null
            tripState = TripUiState.Loading
            tripState = try {
                TripUiState.Ready(createTripPlan(baseUrl, onBaseUrlFallback, payload))
            } catch (error: Exception) {
                TripUiState.Error(error.toUserFacingMessage("여행 계획을 생성할 수 없습니다."))
            }
        }
    }
}
