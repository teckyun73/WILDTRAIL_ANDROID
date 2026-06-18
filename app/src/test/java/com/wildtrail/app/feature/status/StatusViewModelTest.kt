package com.wildtrail.app.feature.status

import com.wildtrail.app.test.MainDispatcherRule

import com.wildtrail.app.data.dto.HealthResponseDto
import com.wildtrail.app.data.dto.ModelStatusDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class StatusViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun checkHealth_setsReadyStateWhenRequestSucceeds() = runTest {
        val expected = healthFixture()
        var receivedBaseUrl: String? = null
        var fallbackUrl: String? = null
        val viewModel = StatusViewModel { baseUrl, onBaseUrlFallback ->
            receivedBaseUrl = baseUrl
            onBaseUrlFallback("http://127.0.0.1:8000")

            expected
        }

        viewModel.checkHealth("http://10.0.2.2:8000") { fallbackUrl = it }
        advanceUntilIdle()

        val state = viewModel.healthState as HealthUiState.Ready
        assertSame(expected, state.health)
        assertEquals("http://10.0.2.2:8000", receivedBaseUrl)
        assertEquals("http://127.0.0.1:8000", fallbackUrl)
    }

    @Test
    fun checkHealth_setsUserFacingErrorWhenRequestFails() = runTest {
        val viewModel = StatusViewModel { _, _ -> throw UnknownHostException() }

        viewModel.checkHealth("http://10.0.2.2:8000") {}
        advanceUntilIdle()

        val state = viewModel.healthState as HealthUiState.Error
        assertEquals("서버 주소를 찾을 수 없습니다. API 주소와 네트워크 연결을 확인해 주세요.", state.message)
    }

    private fun healthFixture(): HealthResponseDto {
        val model = ModelStatusDto(modelLoaded = true, modelClasses = 3, modelPath = "models/demo.pt")
        return HealthResponseDto(
            status = "ok",
            service = "wildtrail-api",
            imageModel = model,
            audioModel = model,
            speciesJsonCount = 12,
            speciesDbCount = 12,
            llmConfigured = true,
            llmProvider = "test",
            llmModel = "test-model",
        )
    }
}


