package com.wildtrail.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppSettingsViewModel : ViewModel() {
    var apiBaseUrl by mutableStateOf(BuildConfig.API_BASE_URL)
        private set

    fun updateApiBaseUrl(value: String) {
        apiBaseUrl = value
    }
}


