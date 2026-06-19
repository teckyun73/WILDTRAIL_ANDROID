package com.wildtrail.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpeciesSummaryDto(
    val id: String,
    @SerialName("common_name") val commonName: String,
    @SerialName("scientific_name") val scientificName: String,
    val category: String,
    @SerialName("protection_grade") val protectionGrade: String? = null,
    @SerialName("best_months") val bestMonths: String,
    @SerialName("image_url") val imageUrl: String? = null,
)
