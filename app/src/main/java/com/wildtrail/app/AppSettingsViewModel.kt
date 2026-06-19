package com.wildtrail.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class ApiEnvironmentPreset(
    val id: String,
    val label: String,
    val baseUrl: String,
    val description: String,
)

class AppSettingsViewModel : ViewModel() {
    val defaultApiBaseUrl: String = BuildConfig.API_BASE_URL

    val apiEnvironmentPresets: List<ApiEnvironmentPreset> =
        listOf(
            ApiEnvironmentPreset(
                id = "build-default",
                label = "빌드 기본값",
                baseUrl = defaultApiBaseUrl,
                description = "현재 빌드 변형에 주입된 기본 서버",
            ),
            ApiEnvironmentPreset(
                id = "emulator-local",
                label = "에뮬레이터",
                baseUrl = "http://10.0.2.2:8000",
                description = "Android Emulator에서 PC 로컬 백엔드 접속",
            ),
            ApiEnvironmentPreset(
                id = "adb-reverse",
                label = "ADB reverse",
                baseUrl = "http://127.0.0.1:8000",
                description = "adb reverse tcp:8000 tcp:8000 사용 시",
            ),
            ApiEnvironmentPreset(
                id = "device-lan",
                label = "실기기 LAN",
                baseUrl = "http://192.168.0.10:8000",
                description = "같은 Wi-Fi의 PC LAN IP로 교체",
            ),
            ApiEnvironmentPreset(
                id = "staging",
                label = "스테이징",
                baseUrl = BuildConfig.STAGING_API_BASE_URL,
                description = "검증 서버용 Gradle/local.properties 값",
            ),
            ApiEnvironmentPreset(
                id = "production",
                label = "운영",
                baseUrl = BuildConfig.PRODUCTION_API_BASE_URL,
                description = "운영 서버용 Gradle/local.properties 값",
            ),
        )

    var apiBaseUrl by mutableStateOf(defaultApiBaseUrl)
        private set

    private var selectedPresetId by mutableStateOf<String?>("build-default")

    val selectedEnvironmentLabel: String
        get() =
            selectedPresetId
                ?.let { presetId -> apiEnvironmentPresets.firstOrNull { it.id == presetId } }
                ?.label
                ?: "커스텀"

    fun updateApiBaseUrl(value: String) {
        apiBaseUrl = value
        selectedPresetId =
            apiEnvironmentPresets
                .firstOrNull { it.baseUrl.normalizedUrl() == value.normalizedUrl() }
                ?.id
    }

    fun selectApiEnvironment(presetId: String) {
        apiEnvironmentPresets
            .firstOrNull { it.id == presetId }
            ?.let {
                apiBaseUrl = it.baseUrl
                selectedPresetId = it.id
            }
    }

    fun resetApiBaseUrl() {
        apiBaseUrl = defaultApiBaseUrl
        selectedPresetId = "build-default"
    }
}

private fun String.normalizedUrl(): String = trim().trimEnd('/')
