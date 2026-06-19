package com.wildtrail.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdentificationResultDto(
    @SerialName("media_type") val mediaType: String,
    val candidates: List<IdentificationCandidateDto>,
    val message: String = "",
    val source: String = "stub",
)

@Serializable
data class IdentificationCandidateDto(
    @SerialName("species_id") val speciesId: String,
    @SerialName("common_name") val commonName: String,
    @SerialName("scientific_name") val scientificName: String,
    val confidence: Double,
)
