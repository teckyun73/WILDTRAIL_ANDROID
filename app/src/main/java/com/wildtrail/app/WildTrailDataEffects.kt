package com.wildtrail.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.wildtrail.app.feature.records.SightingUiState
import com.wildtrail.app.feature.species.SpeciesUiState

@Composable
internal fun WildTrailDataEffects(
    selectedTab: AppTab,
    viewModels: WildTrailViewModels,
) {
    val settingsViewModel = viewModels.settings
    val recordsViewModel = viewModels.records
    val speciesViewModel = viewModels.species

    LaunchedEffect(Unit) {
        viewModels.status.checkHealth(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == AppTab.Species && speciesViewModel.speciesState is SpeciesUiState.Idle) {
            speciesViewModel.loadSpecies(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl)
        }
        if (selectedTab == AppTab.Trips && speciesViewModel.speciesState is SpeciesUiState.Idle) {
            speciesViewModel.loadSpecies(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl)
        }
        if (selectedTab == AppTab.Records && recordsViewModel.sightingState is SightingUiState.Idle) {
            recordsViewModel.loadSightings(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl)
        }
    }
}
