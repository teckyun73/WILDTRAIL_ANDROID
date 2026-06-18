package com.wildtrail.app.data.repository

import com.wildtrail.app.data.dto.TripPlanRequestDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import com.wildtrail.app.data.network.WildTrailApi

class TripRepository(
    private val api: WildTrailApi,
) {
    suspend fun planTrip(payload: TripPlanRequestDto): TripPlanResponseDto {
        return api.planTrip(payload)
    }
}


