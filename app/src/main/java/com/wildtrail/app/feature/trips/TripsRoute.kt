package com.wildtrail.app.feature.trips

import androidx.compose.runtime.Composable
import com.wildtrail.app.AppSettingsViewModel
import com.wildtrail.app.feature.species.SpeciesViewModel

@Composable
internal fun TripsRoute(
    settingsViewModel: AppSettingsViewModel,
    speciesViewModel: SpeciesViewModel,
    tripsViewModel: TripsViewModel,
) {
    TripsScreen(
        nativeMapPlan = tripsViewModel.nativeMapPlan,
        onCloseNativeMap = { tripsViewModel.closeNativeMap() },
        speciesId = tripsViewModel.tripSpeciesId,
        onSpeciesIdChange = { tripsViewModel.updateTripSpeciesId(it) },
        speciesState = speciesViewModel.speciesState,
        onSelectSpecies = { tripsViewModel.selectTripSpecies(it) },
        onRefreshSpecies = { speciesViewModel.loadSpecies(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl) },
        origin = tripsViewModel.tripOrigin,
        onOriginChange = { tripsViewModel.updateTripOrigin(it) },
        days = tripsViewModel.tripDays,
        onDaysChange = { tripsViewModel.updateTripDays(it) },
        budget = tripsViewModel.tripBudget,
        onBudgetChange = { tripsViewModel.updateTripBudget(it) },
        travelers = tripsViewModel.tripTravelers,
        onTravelersChange = { tripsViewModel.updateTripTravelers(it) },
        month = tripsViewModel.tripMonth,
        onMonthChange = { tripsViewModel.updateTripMonth(it) },
        transport = tripsViewModel.tripTransport,
        onTransportChange = { tripsViewModel.updateTripTransport(it) },
        accommodation = tripsViewModel.tripAccommodation,
        onAccommodationChange = { tripsViewModel.updateTripAccommodation(it) },
        difficulty = tripsViewModel.tripDifficulty,
        onDifficultyChange = { tripsViewModel.updateTripDifficulty(it) },
        tripState = tripsViewModel.tripState,
        onPlanTrip = { tripsViewModel.planTrip(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl) },
        onOpenNativeMap = { tripsViewModel.openNativeMap(it) },
        isLoading = tripsViewModel.tripState is TripUiState.Loading,
    )
}
