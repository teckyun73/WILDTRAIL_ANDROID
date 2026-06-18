package com.wildtrail.app.feature.trips

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.net.Uri
import com.wildtrail.app.data.dto.TripPlanResponseDto
import java.net.URLEncoder

data class RouteStop(
    val name: String,
    val role: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    val hasCoordinates: Boolean = latitude != null && longitude != null
}

fun tripRouteStops(plan: TripPlanResponseDto): List<RouteStop> {
    if (plan.routeStops.isNotEmpty()) {
        return plan.routeStops
            .filter { it.name.isNotBlank() }
            .map {
                RouteStop(
                    name = it.name.trim(),
                    role = it.role.ifBlank { "경유" },
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            }
    }

    val origin = plan.origin.trim()
    val destination = plan.hotspotName.trim()
    val middleStops = plan.daysPlan
        .flatMap { day -> day.items }
        .map { it.location.trim() }
        .filter { it.isNotBlank() && it != origin && it != destination }
        .distinct()
        .take(4)
    return buildList {
        if (origin.isNotBlank()) add(RouteStop(name = origin, role = "출발"))
        middleStops.forEach { add(RouteStop(name = it, role = "경유")) }
        if (destination.isNotBlank() && none { it.name == destination }) {
            add(
                RouteStop(
                    name = destination,
                    role = "주요 관찰지",
                    latitude = plan.hotspotLatitude,
                    longitude = plan.hotspotLongitude,
                )
            )
        }
        if (size < 2 && destination.isNotBlank()) {
            add(
                RouteStop(
                    name = destination,
                    role = "주요 관찰지",
                    latitude = plan.hotspotLatitude,
                    longitude = plan.hotspotLongitude,
                )
            )
        }
    }.ifEmpty {
        listOf(
            RouteStop(name = "출발지", role = "출발"),
            RouteStop(name = "관찰지", role = "주요 관찰지"),
        )
    }
}

fun openRouteInMapApp(
    context: Context,
    stops: List<RouteStop>,
) {
    val origin = stops.firstOrNull()
    val destination = stops.lastOrNull()
    if (origin == null || destination == null || origin.name.isBlank() || destination.name.isBlank()) {
        Toast.makeText(context, "경로를 열 장소 정보가 부족합니다.", Toast.LENGTH_SHORT).show()
        return
    }
    val routeUri = buildRouteMapUri(stops) ?: run {
        Toast.makeText(context, "경로를 열 장소 정보가 부족합니다.", Toast.LENGTH_SHORT).show()
        return
    }
    openMapUri(context, routeUri, "경로를 열 수 있는 지도 앱이 없습니다.")
}

fun openPlaceInMapApp(
    context: Context,
    stop: RouteStop?,
) {
    if (stop == null || stop.name.isBlank()) {
        Toast.makeText(context, "검색할 장소 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = buildPlaceMapUri(stop) ?: run {
        Toast.makeText(context, "검색할 장소 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        return
    }
    openMapUri(
        context = context,
        uri = uri,
        failureMessage = "장소를 검색할 수 있는 지도 앱이 없습니다.",
    )
}

internal fun buildRouteMapUri(stops: List<RouteStop>): String? {
    val origin = stops.firstOrNull()
    val destination = stops.lastOrNull()
    if (origin == null || destination == null || origin.name.isBlank() || destination.name.isBlank()) {
        return null
    }
    val waypoints = if (destination.hasCoordinates) emptyList() else stops.drop(1).dropLast(1)
    return buildString {
        append("https://www.google.com/maps/dir/?api=1")
        append("&origin=${urlEncode(mapQuery(origin.name))}")
        append("&destination=${urlEncode(mapDestination(destination))}")
        if (waypoints.isNotEmpty()) {
            append("&waypoints=${urlEncode(waypoints.joinToString("|") { mapQuery(it.name) })}")
        }
        append("&travelmode=transit")
    }
}

internal fun buildPlaceMapUri(stop: RouteStop?): String? {
    if (stop == null || stop.name.isBlank()) return null
    return if (stop.hasCoordinates) {
        "geo:${stop.latitude},${stop.longitude}?q=${stop.latitude},${stop.longitude}(${urlEncode(stop.name)})"
    } else {
        "https://www.google.com/maps/search/?api=1&query=${urlEncode(mapQuery(stop.name))}"
    }
}
private fun mapDestination(stop: RouteStop): String {
    return if (stop.hasCoordinates) {
        "${stop.latitude},${stop.longitude}"
    } else {
        mapQuery(stop.name)
    }
}

private fun mapQuery(place: String): String {
    val trimmed = place.trim()
    if (trimmed.isBlank()) return trimmed
    return if (trimmed.contains("대한민국") || trimmed.contains("Korea", ignoreCase = true)) {
        trimmed
    } else {
        "$trimmed 대한민국"
    }
}

private fun urlEncode(value: String): String {
    return URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
}
private fun openMapUri(context: Context, uri: String, failureMessage: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
    }
}





