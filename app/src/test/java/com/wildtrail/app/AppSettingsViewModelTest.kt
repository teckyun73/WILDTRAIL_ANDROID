package com.wildtrail.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsViewModelTest {
    @Test
    fun startsWithBuildDefaultEnvironment() {
        val viewModel = AppSettingsViewModel()

        assertEquals(BuildConfig.API_BASE_URL, viewModel.apiBaseUrl)
        assertEquals("빌드 기본값", viewModel.selectedEnvironmentLabel)
    }

    @Test
    fun selectingPresetUpdatesBaseUrlAndEnvironmentLabel() {
        val viewModel = AppSettingsViewModel()

        viewModel.selectApiEnvironment("adb-reverse")

        assertEquals("http://127.0.0.1:8000", viewModel.apiBaseUrl)
        assertEquals("ADB reverse", viewModel.selectedEnvironmentLabel)
    }

    @Test
    fun customUrlUsesCustomEnvironmentLabel() {
        val viewModel = AppSettingsViewModel()

        viewModel.updateApiBaseUrl("https://api.example.test")

        assertEquals("커스텀", viewModel.selectedEnvironmentLabel)
    }

    @Test
    fun resetRestoresBuildDefault() {
        val viewModel = AppSettingsViewModel()
        viewModel.selectApiEnvironment("adb-reverse")

        viewModel.resetApiBaseUrl()

        assertEquals(BuildConfig.API_BASE_URL, viewModel.apiBaseUrl)
        assertEquals("빌드 기본값", viewModel.selectedEnvironmentLabel)
    }

    @Test
    fun exposesStagingAndProductionPresets() {
        val viewModel = AppSettingsViewModel()
        val presetIds = viewModel.apiEnvironmentPresets.map { it.id }

        assertTrue("staging" in presetIds)
        assertTrue("production" in presetIds)
    }
}
