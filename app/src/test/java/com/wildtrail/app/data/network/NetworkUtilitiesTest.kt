package com.wildtrail.app.data.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkUtilitiesTest {
    @Test
    fun normalizeBaseUrl_trimsWhitespaceAddsSchemeAndTrailingSlash() {
        assertEquals("http://10.0.2.2:8000/", ApiClient.normalizeBaseUrl(" 10.0.2.2:8000 "))
    }

    @Test
    fun normalizeBaseUrl_preservesHttpsAndRemovesExtraTrailingSlashes() {
        assertEquals("https://api.example.com/v1/", ApiClient.normalizeBaseUrl("https://api.example.com/v1///"))
    }

    @Test
    fun toUserFacingMessage_mapsCommonNetworkFailures() {
        assertEquals(
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.",
            SocketTimeoutException().toUserFacingMessage("기본 오류"),
        )
        assertEquals(
            "서버 주소를 찾을 수 없습니다. API 주소와 네트워크 연결을 확인해 주세요.",
            UnknownHostException().toUserFacingMessage("기본 오류"),
        )
        assertEquals(
            "서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.",
            ConnectException().toUserFacingMessage("기본 오류"),
        )
        assertEquals(
            "네트워크 연결이 불안정합니다. 연결 상태를 확인한 뒤 다시 시도해 주세요.",
            IOException().toUserFacingMessage("기본 오류"),
        )
    }

    @Test
    fun toUserFacingMessage_mapsHttpStatusCodes() {
        assertEquals(
            "요청 형식이 올바르지 않습니다. 입력값을 확인해 주세요.",
            httpException(400).toUserFacingMessage("기본 오류"),
        )
        assertEquals(
            "요청 권한이 없습니다. 서버 인증 설정을 확인해 주세요.",
            httpException(403).toUserFacingMessage("기본 오류"),
        )
        assertEquals(
            "요청한 API를 찾을 수 없습니다. 서버 주소와 백엔드 버전을 확인해 주세요.",
            httpException(404).toUserFacingMessage("기본 오류"),
        )
        assertEquals(
            "서버 처리 중 오류가 발생했습니다. 백엔드 로그를 확인해 주세요. (HTTP 503)",
            httpException(503).toUserFacingMessage("기본 오류"),
        )
        assertEquals(
            "기본 오류 (HTTP 418)",
            httpException(418).toUserFacingMessage("기본 오류"),
        )
    }

    @Test
    fun toUserFacingMessage_usesThrowableMessageOrFallbackForUnknownErrors() {
        assertEquals("직접 오류", IllegalStateException("직접 오류").toUserFacingMessage("기본 오류"))
        assertEquals("기본 오류", IllegalStateException("").toUserFacingMessage("기본 오류"))
    }

    private fun httpException(code: Int): HttpException {
        val body = "{}".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Unit>(code, body))
    }
}
