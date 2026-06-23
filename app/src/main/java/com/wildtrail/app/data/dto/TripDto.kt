package com.wildtrail.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripPreferencesDto(
    val transport: String = "public",
    val accommodation: String = "guesthouse",
    val difficulty: String = "easy",
)

@Serializable
data class TripPlanRequestDto(
    @SerialName("species_id") val speciesId: String,
    val origin: String = "서울역",
    val days: Int = 1,
    @SerialName("budget_krw") val budgetKrw: Int = 150000,
    val travelers: Int = 1,
    val month: Int? = null,
    val preferences: TripPreferencesDto = TripPreferencesDto(),
)

@Serializable
data class CostBreakdownDto(
    val transport: Int,
    val accommodation: Int,
    val food: Int,
    @SerialName("entry_fee") val entryFee: Int,
    val misc: Int,
    val total: Int,
    @SerialName("per_person") val perPerson: Int,
)

@Serializable
data class TripDayItemDto(
    val time: String,
    val activity: String,
    val location: String,
    val note: String = "",
)

@Serializable
data class TripDayPlanDto(
    val day: Int,
    val title: String,
    val items: List<TripDayItemDto>,
)

@Serializable
data class TripRouteStopDto(
    val name: String,
    val role: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class AccommodationOptionDto(
    val name: String,
    val type: String,
    val region: String = "",
    val address: String = "",
    @SerialName("distance_km") val distanceKm: Double? = null,
    @SerialName("price_min_krw") val priceMinKrw: Int? = null,
    @SerialName("price_max_krw") val priceMaxKrw: Int? = null,
    @SerialName("price_per_night_krw") val pricePerNightKrw: Int? = null,
    @SerialName("parking_available") val parkingAvailable: Boolean? = null,
    val phone: String = "",
    @SerialName("booking_url") val bookingUrl: String = "",
    val source: String = "",
    val note: String = "",
    val rating: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class TripPlanResponseDto(
    @SerialName("species_id") val speciesId: String,
    @SerialName("species_name") val speciesName: String,
    val origin: String,
    val days: Int,
    val travelers: Int,
    @SerialName("hotspot_name") val hotspotName: String,
    @SerialName("hotspot_latitude") val hotspotLatitude: Double? = null,
    @SerialName("hotspot_longitude") val hotspotLongitude: Double? = null,
    val region: String,
    val summary: String,
    val checklist: List<String>,
    @SerialName("route_stops") val routeStops: List<TripRouteStopDto> = emptyList(),
    @SerialName("accommodation_options") val accommodationOptions: List<AccommodationOptionDto> = emptyList(),
    @SerialName("days_plan") val daysPlan: List<TripDayPlanDto>,
    val costs: CostBreakdownDto,
    val disclaimer: String,
    val source: String,
)
