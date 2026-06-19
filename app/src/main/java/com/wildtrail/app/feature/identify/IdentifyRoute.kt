package com.wildtrail.app.feature.identify

import android.content.Context
import androidx.compose.runtime.Composable
import com.wildtrail.app.AppSettingsViewModel
import com.wildtrail.app.feature.records.RecordsViewModel
import com.wildtrail.app.feature.species.SpeciesUiState
import com.wildtrail.app.feature.species.SpeciesViewModel

@Composable
internal fun IdentifyRoute(
    context: Context,
    settingsViewModel: AppSettingsViewModel,
    identifyViewModel: IdentifyViewModel,
    recordsViewModel: RecordsViewModel,
    speciesViewModel: SpeciesViewModel,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onOpenSpeciesTab: () -> Unit,
) {
    IdentifyScreen(
        identifyState = identifyViewModel.identifyState,
        selectedImageName = identifyViewModel.selectedImageName,
        selectedAudioName = identifyViewModel.selectedAudioName,
        isRecording = isRecording,
        recordingMessage = identifyViewModel.recordingMessage,
        onImageSelected = {
            identifyViewModel.identifyImage(
                context,
                it,
                settingsViewModel.apiBaseUrl,
                settingsViewModel::updateApiBaseUrl,
            )
        },
        onAudioSelected = {
            identifyViewModel.identifyAudio(
                context,
                it,
                settingsViewModel.apiBaseUrl,
                settingsViewModel::updateApiBaseUrl,
            )
        },
        onStartRecording = onStartRecording,
        onStopRecording = onStopRecording,
        onCandidateSelected = {
            onOpenSpeciesTab()
            speciesViewModel.openCandidate(it, settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl)
        },
        onSaveCandidate = {
            candidate,
            mediaType,
            ->
            recordsViewModel.saveCandidate(candidate, mediaType, settingsViewModel.apiBaseUrl, settingsViewModel::updateApiBaseUrl)
        },
        knownSpeciesIds = speciesViewModel.knownSpeciesIds(),
        saveMessage = recordsViewModel.saveSightingMessage,
    )
}

private fun SpeciesViewModel.knownSpeciesIds(): Set<String>? =
    (speciesState as? SpeciesUiState.Ready)
        ?.species
        ?.map { it.id }
        ?.toSet()
