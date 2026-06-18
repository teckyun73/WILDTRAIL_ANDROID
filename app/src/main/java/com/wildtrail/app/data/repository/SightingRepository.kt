package com.wildtrail.app.data.repository

import com.wildtrail.app.data.dto.SightingCreateDto
import com.wildtrail.app.data.dto.SightingDto
import com.wildtrail.app.data.network.WildTrailApi

class SightingRepository(
    private val api: WildTrailApi,
) {
    suspend fun listSightings(): List<SightingDto> {
        return api.listSightings()
    }

    suspend fun createSighting(payload: SightingCreateDto): SightingDto {
        return api.createSighting(payload)
    }
}


