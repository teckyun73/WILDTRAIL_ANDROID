package com.wildtrail.app.data.network

import com.wildtrail.app.data.dto.HealthResponseDto
import com.wildtrail.app.data.dto.HotspotDto
import com.wildtrail.app.data.dto.IdentificationResultDto
import com.wildtrail.app.data.dto.SightingCreateDto
import com.wildtrail.app.data.dto.SightingDto
import com.wildtrail.app.data.dto.SpeciesDetailDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.data.dto.TripPlanRequestDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class ApiCallRunnerTest {
    @Test
    fun runWithFactory_retriesIOExceptionOnceWhenRetryTransientIsEnabled() =
        runTest {
            val api = FakeWildTrailApi("http://10.0.2.2:8000")
            var callCount = 0

            val result =
                ApiCallRunner.runWithFactory(
                    baseUrl = "http://10.0.2.2:8000",
                    onBaseUrlFallback = {},
                    apiFactory = { api },
                    retryDelayMillis = 0,
                ) {
                    callCount += 1
                    if (callCount == 1) throw IOException("temporary")
                    "ok"
                }

            assertEquals("ok", result)
            assertEquals(2, callCount)
        }

    @Test
    fun runWithFactory_doesNotRetryIOExceptionWhenRetryTransientIsDisabled() =
        runTest {
            var callCount = 0

            val error =
                runCatching {
                    ApiCallRunner.runWithFactory(
                        baseUrl = "https://api.example.com",
                        onBaseUrlFallback = {},
                        retryTransient = false,
                        apiFactory = { FakeWildTrailApi(it) },
                        retryDelayMillis = 0,
                    ) {
                        callCount += 1
                        throw IOException("no retry")
                    }
                }.exceptionOrNull()

            assertEquals(1, callCount)
            assertEquals(IOException::class, error!!::class)
        }

    @Test
    fun runWithFactory_fallsBackFromEmulatorHostToLocalhostAfterPrimaryFailure() =
        runTest {
            val createdBaseUrls = mutableListOf<String>()
            var fallbackUrl: String? = null

            val result =
                ApiCallRunner.runWithFactory(
                    baseUrl = "http://10.0.2.2:8000",
                    onBaseUrlFallback = { fallbackUrl = it },
                    retryTransient = false,
                    apiFactory = { baseUrl ->
                        createdBaseUrls += baseUrl
                        FakeWildTrailApi(baseUrl)
                    },
                    retryDelayMillis = 0,
                ) { api ->
                    val fakeApi = api as FakeWildTrailApi
                    if (fakeApi.baseUrl.contains("10.0.2.2")) throw IOException("primary unavailable")
                    "fallback-ok"
                }

            assertEquals("fallback-ok", result)
            assertEquals(listOf("http://10.0.2.2:8000", "http://127.0.0.1:8000"), createdBaseUrls)
            assertEquals("http://127.0.0.1:8000", fallbackUrl)
        }

    @Test
    fun runWithFactory_doesNotFallbackWhenBaseUrlIsNotEmulatorHost() =
        runTest {
            var fallbackCalled = false
            val original = IOException("remote unavailable")

            val error =
                runCatching {
                    ApiCallRunner.runWithFactory(
                        baseUrl = "https://api.example.com",
                        onBaseUrlFallback = { fallbackCalled = true },
                        retryTransient = false,
                        apiFactory = { FakeWildTrailApi(it) },
                        retryDelayMillis = 0,
                    ) {
                        throw original
                    }
                }.exceptionOrNull()

            assertSame(original, error)
            assertEquals(false, fallbackCalled)
        }
}

private class FakeWildTrailApi(
    val baseUrl: String,
) : WildTrailApi {
    override suspend fun health(): HealthResponseDto = unsupported()

    override suspend fun listSpecies(query: String?): List<SpeciesSummaryDto> = unsupported()

    override suspend fun getSpecies(speciesId: String): SpeciesDetailDto = unsupported()

    override suspend fun listLocations(speciesId: String?): List<HotspotDto> = unsupported()

    override suspend fun identifyImage(file: MultipartBody.Part): IdentificationResultDto = unsupported()

    override suspend fun identifyAudio(file: MultipartBody.Part): IdentificationResultDto = unsupported()

    override suspend fun listSightings(): List<SightingDto> = unsupported()

    override suspend fun createSighting(payload: SightingCreateDto): SightingDto = unsupported()

    override suspend fun planTrip(payload: TripPlanRequestDto): TripPlanResponseDto = unsupported()

    private fun unsupported(): Nothing = error("Fake API method should not be called directly")
}
