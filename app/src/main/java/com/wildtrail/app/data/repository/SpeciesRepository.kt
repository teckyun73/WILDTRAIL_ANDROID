package com.wildtrail.app.data.repository

import com.wildtrail.app.data.dto.SpeciesDetailDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.data.network.WildTrailApi

class SpeciesRepository(
    private val api: WildTrailApi,
) {
    suspend fun listSpecies(query: String? = null): List<SpeciesSummaryDto> {
        return api.listSpecies(query = query?.takeIf { it.isNotBlank() })
    }

    suspend fun getSpecies(speciesId: String): SpeciesDetailDto {
        return api.getSpecies(speciesId)
    }
}


