package com.wildtrail.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponseDto(
    val status: String,
    val service: String,
    @SerialName("image_model") val imageModel: ModelStatusDto,
    @SerialName("audio_model") val audioModel: ModelStatusDto,
    @SerialName("species_json_count") val speciesJsonCount: Int,
    @SerialName("species_db_count") val speciesDbCount: Int,
    @SerialName("llm_configured") val llmConfigured: Boolean,
    @SerialName("llm_provider") val llmProvider: String,
    @SerialName("llm_model") val llmModel: String,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class ModelStatusDto(
    @SerialName("model_loaded") val modelLoaded: Boolean,
    @SerialName("model_classes") val modelClasses: Int = 0,
    @SerialName("model_val_acc") val modelValAcc: Double? = null,
    @SerialName("model_path") val modelPath: String = "",
    @SerialName("model_version") val modelVersion: String? = null,
    @SerialName("trained_at") val trainedAt: String? = null,
    val backbone: String? = null,
    @SerialName("preprocess_version") val preprocessVersion: String? = null,
    @SerialName("dataset_fingerprint") val datasetFingerprint: String? = null,
)


