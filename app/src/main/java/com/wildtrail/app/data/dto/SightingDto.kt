package com.wildtrail.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SightingDto(
    val id: Int,
    @SerialName("species_id") val speciesId: String,
    @SerialName("common_name") val commonName: String,
    @SerialName("location_name") val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val confidence: Double = 0.0,
    @SerialName("media_type") val mediaType: String = "image",
    val note: String = "",
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class SightingCreateDto(
    @SerialName("species_id") val speciesId: String,
    @SerialName("location_name") val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val confidence: Double = 0.0,
    @SerialName("media_type") val mediaType: String = "image",
    val note: String = "",
)


