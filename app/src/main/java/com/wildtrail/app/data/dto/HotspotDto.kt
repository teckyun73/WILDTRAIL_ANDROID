package com.wildtrail.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HotspotDto(
    val id: Int,
    val name: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("species_id") val speciesId: String,
    @SerialName("species_name") val speciesName: String,
    @SerialName("best_months") val bestMonths: String,
    @SerialName("observation_score") val observationScore: Double,
    @SerialName("access_level") val accessLevel: String,
    @SerialName("transport_note") val transportNote: String,
    @SerialName("entry_fee") val entryFee: Int,
    val facilities: String,
    @SerialName("safety_note") val safetyNote: String,
)


