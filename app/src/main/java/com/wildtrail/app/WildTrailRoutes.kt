package com.wildtrail.app

import android.content.Context
import androidx.compose.runtime.Composable
import com.wildtrail.app.feature.identify.IdentifyRoute
import com.wildtrail.app.feature.records.RecordsRoute
import com.wildtrail.app.feature.species.SpeciesRoute
import com.wildtrail.app.feature.status.StatusRoute
import com.wildtrail.app.feature.trips.TripsRoute

@Composable
internal fun WildTrailTabContent(
    selectedTab: AppTab,
    context: Context,
    viewModels: WildTrailViewModels,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onTabSelected: (AppTab) -> Unit,
) {
    when (selectedTab) {
        AppTab.Identify -> IdentifyRoute(
            context = context,
            settingsViewModel = viewModels.settings,
            identifyViewModel = viewModels.identify,
            recordsViewModel = viewModels.records,
            speciesViewModel = viewModels.species,
            isRecording = isRecording,
            onStartRecording = onStartRecording,
            onStopRecording = onStopRecording,
            onOpenSpeciesTab = { onTabSelected(AppTab.Species) },
        )
        AppTab.Status -> StatusRoute(
            settingsViewModel = viewModels.settings,
            statusViewModel = viewModels.status,
        )
        AppTab.Species -> SpeciesRoute(
            settingsViewModel = viewModels.settings,
            speciesViewModel = viewModels.species,
            tripsViewModel = viewModels.trips,
            onOpenTripsTab = { onTabSelected(AppTab.Trips) },
        )
        AppTab.Records -> RecordsRoute(
            settingsViewModel = viewModels.settings,
            recordsViewModel = viewModels.records,
        )
        AppTab.Trips -> TripsRoute(
            settingsViewModel = viewModels.settings,
            speciesViewModel = viewModels.species,
            tripsViewModel = viewModels.trips,
        )
    }
}


