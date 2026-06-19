package com.wildtrail.app.data.network

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUserFacingMessage(fallback: String): String =
    when (this) {
        is HttpException -> httpErrorMessage(code(), fallback)
        is SocketTimeoutException -> "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요."
        is UnknownHostException -> "서버 주소를 찾을 수 없습니다. API 주소와 네트워크 연결을 확인해 주세요."
        is ConnectException -> "서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요."
        is IOException -> "네트워크 연결이 불안정합니다. 연결 상태를 확인한 뒤 다시 시도해 주세요."
        else -> message?.takeIf { it.isNotBlank() } ?: fallback
    }

private fun httpErrorMessage(
    code: Int,
    fallback: String,
): String =
    when (code) {
        400 -> "요청 형식이 올바르지 않습니다. 입력값을 확인해 주세요."
        401, 403 -> "요청 권한이 없습니다. 서버 인증 설정을 확인해 주세요."
        404 -> "요청한 API를 찾을 수 없습니다. 서버 주소와 백엔드 버전을 확인해 주세요."
        413 -> "파일 크기가 너무 큽니다. 오디오는 20MB 이하 파일을 선택해 주세요."
        in 500..599 -> "서버 처리 중 오류가 발생했습니다. 백엔드 로그를 확인해 주세요. (HTTP $code)"
        else -> "$fallback (HTTP $code)"
    }
