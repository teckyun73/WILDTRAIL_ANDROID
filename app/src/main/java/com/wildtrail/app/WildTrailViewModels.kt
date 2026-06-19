package com.wildtrail.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wildtrail.app.feature.identify.IdentifyViewModel
import com.wildtrail.app.feature.records.RecordsViewModel
import com.wildtrail.app.feature.species.SpeciesViewModel
import com.wildtrail.app.feature.status.StatusViewModel
import com.wildtrail.app.feature.trips.TripsViewModel

data class WildTrailViewModels(
    val settings: AppSettingsViewModel,
    val identify: IdentifyViewModel,
    val records: RecordsViewModel,
    val status: StatusViewModel,
    val species: SpeciesViewModel,
    val trips: TripsViewModel,
)

@Composable
internal fun wildTrailViewModels(): WildTrailViewModels =
    WildTrailViewModels(
        settings = viewModel(),
        identify = viewModel(),
        records = viewModel(),
        status = viewModel(),
        species = viewModel(),
        trips = viewModel(),
    )
