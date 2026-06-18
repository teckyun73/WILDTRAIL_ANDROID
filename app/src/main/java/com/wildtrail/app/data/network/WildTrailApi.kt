package com.wildtrail.app.data.network

import com.wildtrail.app.data.dto.HealthResponseDto
import com.wildtrail.app.data.dto.HotspotDto
import com.wildtrail.app.data.dto.IdentificationResultDto
import com.wildtrail.app.data.dto.SightingCreateDto
import com.wildtrail.app.data.dto.SightingDto
import com.wildtrail.app.data.dto.SpeciesDetailDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.data.dto.TripPlanRequestDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WildTrailApi {
    @GET("health")
    suspend fun health(): HealthResponseDto

    @GET("api/v1/species")
    suspend fun listSpecies(
        @Query("q") query: String? = null,
    ): List<SpeciesSummaryDto>

    @GET("api/v1/species/{species_id}")
    suspend fun getSpecies(
        @Path("species_id") speciesId: String,
    ): SpeciesDetailDto

    @GET("api/v1/locations")
    suspend fun listLocations(
        @Query("species_id") speciesId: String? = null,
    ): List<HotspotDto>

    @Multipart
    @POST("api/v1/identify/image")
    suspend fun identifyImage(
        @Part file: MultipartBody.Part,
    ): IdentificationResultDto

    @Multipart
    @POST("api/v1/identify/audio")
    suspend fun identifyAudio(
        @Part file: MultipartBody.Part,
    ): IdentificationResultDto

    @GET("api/v1/sightings")
    suspend fun listSightings(): List<SightingDto>

    @POST("api/v1/sightings")
    suspend fun createSighting(
        @Body payload: SightingCreateDto,
    ): SightingDto

    @POST("api/v1/trips/plan")
    suspend fun planTrip(
        @Body payload: TripPlanRequestDto,
    ): TripPlanResponseDto
}


