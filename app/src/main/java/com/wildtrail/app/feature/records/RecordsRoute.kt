package com.wildtrail.app.feature.records

import androidx.compose.runtime.Composable
import com.wildtrail.app.AppSettingsViewModel

@Composable
internal fun RecordsRoute(
    settingsViewModel: AppSettingsViewModel,
    recordsViewModel: RecordsViewModel,
) {
    RecordsScreen(
        sightingState = recordsViewModel.sightingState,
        onRefresh = { recordsViewModel.loadSightings(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl) },
        isLoading = recordsViewModel.sightingState is SightingUiState.Loading,
    )
}
