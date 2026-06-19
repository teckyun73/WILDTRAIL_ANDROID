package com.wildtrail.app.data.repository

import com.wildtrail.app.data.dto.HotspotDto
import com.wildtrail.app.data.network.WildTrailApi

class LocationRepository(
    private val api: WildTrailApi,
) {
    suspend fun listLocations(speciesId: String? = null): List<HotspotDto> =
        api.listLocations(speciesId = speciesId?.takeIf { it.isNotBlank() })
}
