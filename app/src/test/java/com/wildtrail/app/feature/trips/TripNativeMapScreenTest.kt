package com.wildtrail.app.feature.trips

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.wildtrail.app.ui.theme.Forest
import org.junit.Assert.assertEquals
import org.junit.Test

class TripNativeMapScreenTest {
    @Test
    fun routeStopMarkerData_keepsOriginalRouteIndexAndFiltersStopsWithoutCoordinates() {
        val origin = RouteStop("서울역", "출발", 37.55, 126.97)
        val waypointWithoutCoordinates = RouteStop("철원 평화전망대", "경유")
        val waypoint = RouteStop("두루미 관찰대", "경유", 38.2, 127.1)
        val destination = RouteStop("DMZ 생태길", "주요 관찰지", 38.1, 127.2)

        val markerData = routeStopMarkerData(
            listOf(origin, waypointWithoutCoordinates, waypoint, destination),
        )

        assertEquals(
            listOf(
                RouteStopMarkerData(0, origin, 37.55, 126.97),
                RouteStopMarkerData(2, waypoint, 38.2, 127.1),
                RouteStopMarkerData(3, destination, 38.1, 127.2),
            ),
            markerData,
        )
    }

    @Test
    fun routeStopColor_mapsRouteRolesToUiColors() {
        assertEquals(Color(0xFF2563EB), routeStopColor(RouteStop("서울역", "출발")))
        assertEquals(Color(0xFFF59E0B), routeStopColor(RouteStop("중간 지점", "경유")))
        assertEquals(Forest, routeStopColor(RouteStop("관찰지", "주요 관찰지")))
        assertEquals(Color(0xFF6B7280), routeStopColor(RouteStop("기타", "휴식")))
    }

    @Test
    fun routeStopHue_mapsRouteRolesToMarkerHues() {
        assertEquals(BitmapDescriptorFactory.HUE_AZURE, routeStopHue(RouteStop("서울역", "출발")), 0.0f)
        assertEquals(BitmapDescriptorFactory.HUE_ORANGE, routeStopHue(RouteStop("중간 지점", "경유")), 0.0f)
        assertEquals(BitmapDescriptorFactory.HUE_GREEN, routeStopHue(RouteStop("관찰지", "주요 관찰지")), 0.0f)
        assertEquals(BitmapDescriptorFactory.HUE_RED, routeStopHue(RouteStop("기타", "휴식")), 0.0f)
    }
}
