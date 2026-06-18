package com.wildtrail.app.data.network

import kotlinx.coroutines.delay
import java.io.IOException

object ApiCallRunner {
    suspend fun <T> run(
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
        retryTransient: Boolean = true,
        operation: suspend (api: WildTrailApi) -> T,
    ): T {
        return runWithFactory(
            baseUrl = baseUrl,
            onBaseUrlFallback = onBaseUrlFallback,
            retryTransient = retryTransient,
            apiFactory = ApiClient::create,
            retryDelayMillis = 250,
            operation = operation,
        )
    }

    internal suspend fun <T> runWithFactory(
        baseUrl: String,
        onBaseUrlFallback: (String) -> Unit,
        retryTransient: Boolean = true,
        apiFactory: (String) -> WildTrailApi,
        retryDelayMillis: Long = 250,
        operation: suspend (api: WildTrailApi) -> T,
    ): T {
        suspend fun runWithRetry(api: WildTrailApi): T {
            return try {
                operation(api)
            } catch (error: Exception) {
                if (!retryTransient || error !is IOException) {
                    throw error
                }
                if (retryDelayMillis > 0) {
                    delay(retryDelayMillis)
                }
                operation(api)
            }
        }

        return try {
            runWithRetry(apiFactory(baseUrl))
        } catch (error: Exception) {
            if (!baseUrl.contains("10.0.2.2")) {
                throw error
            }
            val fallbackBaseUrl = "http://127.0.0.1:8000"
            val result = runWithRetry(apiFactory(fallbackBaseUrl))
            onBaseUrlFallback(fallbackBaseUrl)
            result
        }
    }
}
