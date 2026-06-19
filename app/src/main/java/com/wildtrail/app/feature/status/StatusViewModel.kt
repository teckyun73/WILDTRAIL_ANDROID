package com.wildtrail.app.feature.status

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildtrail.app.data.dto.HealthResponseDto
import com.wildtrail.app.data.network.ApiCallRunner
import com.wildtrail.app.data.network.toUserFacingMessage
import kotlinx.coroutines.launch

class StatusViewModel(
    private val runHealthCheck: suspend (
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) -> HealthResponseDto = { baseUrl, onBaseUrlFallback ->
        ApiCallRunner.run(baseUrl, onBaseUrlFallback) { it.health() }
    },
) : ViewModel() {
    var healthState by mutableStateOf<HealthUiState>(HealthUiState.Idle)
        private set

    fun checkHealth(
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
    ) {
        viewModelScope.launch {
            healthState = HealthUiState.Loading
            healthState =
                try {
                    HealthUiState.Ready(runHealthCheck(baseUrl, onBaseUrlFallback))
                } catch (error: Exception) {
                    HealthUiState.Error(error.toUserFacingMessage("백엔드에 연결할 수 없습니다."))
                }
        }
    }
}
