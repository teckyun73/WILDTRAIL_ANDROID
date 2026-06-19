package com.wildtrail.app.data.repository

import com.wildtrail.app.data.dto.IdentificationResultDto
import com.wildtrail.app.data.network.WildTrailApi
import okhttp3.MultipartBody

class IdentifyRepository(
    private val api: WildTrailApi,
) {
    suspend fun identifyImage(file: MultipartBody.Part): IdentificationResultDto = api.identifyImage(file)

    suspend fun identifyAudio(file: MultipartBody.Part): IdentificationResultDto = api.identifyAudio(file)
}
