package com.wildtrail.app.feature.species

import androidx.compose.runtime.Composable
import com.wildtrail.app.AppSettingsViewModel
import com.wildtrail.app.feature.trips.TripsViewModel

@Composable
internal fun SpeciesRoute(
    settingsViewModel: AppSettingsViewModel,
    speciesViewModel: SpeciesViewModel,
    tripsViewModel: TripsViewModel,
    onOpenTripsTab: () -> Unit,
) {
    SpeciesScreen(
        speciesState = speciesViewModel.speciesState,
        speciesDetailState = speciesViewModel.speciesDetailState,
        hotspotState = speciesViewModel.hotspotState,
        search = speciesViewModel.speciesSearch,
        onSearchChange = { speciesViewModel.updateSpeciesSearch(it) },
        selectedSpeciesId = speciesViewModel.selectedSpeciesId,
        onSelectSpecies = { speciesViewModel.loadSpeciesDetail(it, settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl) },
        onPlanTripForSpecies = { speciesId ->
            tripsViewModel.prepareTripForSpecies(speciesId)
            onOpenTripsTab()
        },
        onRefresh = { speciesViewModel.loadSpecies(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl) },
        onRetrySelectedSpecies = {
            speciesViewModel.selectedSpeciesId?.let {
                speciesViewModel.loadSpeciesDetail(it, settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl)
            }
        },
        isLoading = speciesViewModel.speciesState is SpeciesUiState.Loading,
    )
}
