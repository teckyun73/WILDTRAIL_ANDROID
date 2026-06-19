package com.wildtrail.app.feature.status

import androidx.compose.runtime.Composable
import com.wildtrail.app.AppSettingsViewModel

@Composable
internal fun StatusRoute(
    settingsViewModel: AppSettingsViewModel,
    statusViewModel: StatusViewModel,
) {
    StatusScreen(
        baseUrl = settingsViewModel.apiBaseUrl,
        onBaseUrlChange = { settingsViewModel.updateApiBaseUrl(it) },
        apiPresets = settingsViewModel.apiEnvironmentPresets,
        selectedEnvironmentLabel = settingsViewModel.selectedEnvironmentLabel,
        onPresetSelected = { settingsViewModel.selectApiEnvironment(it) },
        onResetBaseUrl = { settingsViewModel.resetApiBaseUrl() },
        healthState = statusViewModel.healthState,
        onCheckHealth = { statusViewModel.checkHealth(settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl) },
        isLoading = statusViewModel.healthState is HealthUiState.Loading,
    )
}




