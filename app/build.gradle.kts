import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
}

val localProperties =
    Properties().apply {
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localFile.inputStream().use(::load)
        }
    }

fun projectSetting(name: String): String? =
    localProperties
        .getProperty(name)
        ?.takeIf { it.isNotBlank() }
        ?: providers
            .gradleProperty(name)
            .orNull
            ?.takeIf { it.isNotBlank() }
        ?: providers
            .environmentVariable(name)
            .orNull
            ?.takeIf { it.isNotBlank() }

val mapsApiKey = projectSetting("MAPS_API_KEY") ?: ""
val defaultApiBaseUrl = projectSetting("API_BASE_URL") ?: "http://10.0.2.2:8000"
val debugApiBaseUrl = projectSetting("DEBUG_API_BASE_URL") ?: defaultApiBaseUrl
val releaseApiBaseUrl = projectSetting("RELEASE_API_BASE_URL") ?: defaultApiBaseUrl
val stagingApiBaseUrl = projectSetting("STAGING_API_BASE_URL") ?: debugApiBaseUrl
val productionApiBaseUrl = projectSetting("PRODUCTION_API_BASE_URL") ?: releaseApiBaseUrl
val appVersionCode = projectSetting("VERSION_CODE")?.toIntOrNull() ?: 1
val appVersionName = projectSetting("VERSION_NAME") ?: "0.1.0"
val releaseStoreFilePath = projectSetting("RELEASE_STORE_FILE")
val releaseStorePassword = projectSetting("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = projectSetting("RELEASE_KEY_ALIAS")
val releaseKeyPassword = projectSetting("RELEASE_KEY_PASSWORD")
val hasReleaseSigningConfig =
    listOf(
        releaseStoreFilePath,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    ).all { !it.isNullOrBlank() }

android {
    namespace = "com.wildtrail.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wildtrail.app"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["mapsApiKey"] = mapsApiKey
        buildConfigField("Boolean", "HAS_MAPS_API_KEY", mapsApiKey.isNotBlank().toString())
        buildConfigField("String", "STAGING_API_BASE_URL", "\"$stagingApiBaseUrl\"")
        buildConfigField("String", "PRODUCTION_API_BASE_URL", "\"$productionApiBaseUrl\"")
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                storeFile = file(requireNotNull(releaseStoreFilePath))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
            buildConfigField("Boolean", "ENABLE_HTTP_LOGGING", "true")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrl\"")
            buildConfigField("Boolean", "ENABLE_HTTP_LOGGING", "false")
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ktlint {
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
    }
}
dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")

    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:rules:1.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
