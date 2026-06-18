package com.wildtrail.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.wildtrail.app.feature.identify.rememberRecordingSessionState

@Composable
fun WildTrailApp() {
    WildTrailAppContent(wildTrailViewModels())
}

@Composable
private fun WildTrailAppContent(viewModels: WildTrailViewModels) {
    val settingsViewModel = viewModels.settings
    val identifyViewModel = viewModels.identify
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Identify) }
    val context = LocalContext.current
    val recordingSessionState = rememberRecordingSessionState(
        context = context,
        identifyViewModel = identifyViewModel,
        baseUrl = settingsViewModel.apiBaseUrl,
        onBaseUrlFallback = settingsViewModel::updateApiBaseUrl,
    )

    WildTrailDataEffects(
        selectedTab = selectedTab,
        viewModels = viewModels,
    )

    Scaffold(
        topBar = { WildTrailTopBar(selectedTab) },
        bottomBar = {
            WildTrailBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            WildTrailTabContent(
                selectedTab = selectedTab,
                context = context,
                viewModels = viewModels,
                isRecording = recordingSessionState.isRecording,
                onStartRecording = recordingSessionState::startRecording,
                onStopRecording = recordingSessionState::stopRecording,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}




