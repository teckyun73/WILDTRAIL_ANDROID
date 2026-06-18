package com.wildtrail.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpeciesDetailDto(
    val id: String,
    @SerialName("common_name") val commonName: String,
    @SerialName("scientific_name") val scientificName: String,
    val category: String,
    @SerialName("protection_grade") val protectionGrade: String? = null,
    val habitat: String,
    val diet: String,
    @SerialName("breeding_season") val breedingSeason: String,
    @SerialName("active_time") val activeTime: String,
    @SerialName("observation_tips") val observationTips: String,
    @SerialName("best_months") val bestMonths: String,
    @SerialName("similar_species") val similarSpecies: String,
    val description: String,
    @SerialName("image_url") val imageUrl: String? = null,
)


