package com.wildtrail.app.feature.trips

import com.wildtrail.app.data.dto.CostBreakdownDto
import com.wildtrail.app.data.dto.TripDayItemDto
import com.wildtrail.app.data.dto.TripDayPlanDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import com.wildtrail.app.data.dto.TripRouteStopDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteModelsTest {
    @Test
    fun tripRouteStops_usesServerRouteStopsWhenPresentAndTrimsNames() {
        val plan =
            tripPlanFixture(
                routeStops =
                    listOf(
                        TripRouteStopDto(name = "  서울역  ", role = "origin", latitude = 37.55, longitude = 126.97),
                        TripRouteStopDto(name = "", role = "blank"),
                        TripRouteStopDto(name = "DMZ 생태길", role = "", latitude = 38.1, longitude = 127.2),
                    ),
            )

        val stops = tripRouteStops(plan)

        assertEquals(2, stops.size)
        assertEquals(RouteStop("서울역", "출발", 37.55, 126.97), stops[0])
        assertEquals(RouteStop("DMZ 생태길", "경유", 38.1, 127.2), stops[1])
        assertTrue(stops[0].hasCoordinates)
        assertTrue(stops[1].hasCoordinates)
    }

    @Test
    fun tripRouteStops_buildsFallbackRouteFromOriginDistinctDayLocationsAndDestination() {
        val plan =
            tripPlanFixture(
                routeStops = emptyList(),
                daysPlan =
                    listOf(
                        dayPlan(
                            "서울역",
                            "철원 평화전망대",
                            "철원 평화전망대",
                            "두루미 관찰대",
                            "DMZ 생태길",
                            "",
                            "민통선 안내소",
                            "습지 탐방로",
                            "추가 경유지",
                        ),
                    ),
            )

        val stops = tripRouteStops(plan)

        assertEquals(
            listOf("서울역", "철원 평화전망대", "두루미 관찰대", "민통선 안내소", "습지 탐방로", "DMZ 생태길"),
            stops.map { it.name },
        )
        assertEquals(listOf("출발", "경유", "경유", "경유", "경유", "주요 관찰지"), stops.map { it.role })
        assertEquals(38.1, stops.last().latitude)
        assertEquals(127.2, stops.last().longitude)
    }

    @Test
    fun tripRouteStops_addsDestinationTwiceWhenOnlyOneNamedPlaceExistsToKeepRouteUsable() {
        val plan =
            tripPlanFixture(
                origin = "DMZ 생태길",
                hotspotName = "DMZ 생태길",
                routeStops = emptyList(),
                daysPlan = listOf(dayPlan("DMZ 생태길")),
            )

        val stops = tripRouteStops(plan)

        assertEquals(2, stops.size)
        assertEquals("DMZ 생태길", stops[0].name)
        assertEquals("출발", stops[0].role)
        assertEquals("DMZ 생태길", stops[1].name)
        assertEquals("주요 관찰지", stops[1].role)
    }

    @Test
    fun tripRouteStops_returnsDefaultRouteWhenPlanHasNoUsableNames() {
        val plan =
            tripPlanFixture(
                origin = " ",
                hotspotName = " ",
                routeStops = emptyList(),
                daysPlan = listOf(dayPlan(" ", "")),
            )

        val stops = tripRouteStops(plan)

        assertEquals(listOf("출발지", "관찰지"), stops.map { it.name })
        assertEquals(listOf("출발", "주요 관찰지"), stops.map { it.role })
        assertFalse(stops[0].hasCoordinates)
        assertFalse(stops[1].hasCoordinates)
    }

    @Test
    fun routeSummary_countsCoordinateStopsAndStraightLineDistance() {
        val stops =
            listOf(
                RouteStop("서울역", "출발", 37.55, 126.97),
                RouteStop("좌표 없는 경유지", "경유"),
                RouteStop("DMZ 생태길", "주요 관찰지", 38.1, 127.2),
            )

        val summary = routeSummary(stops)

        assertEquals(3, summary.stopCount)
        assertEquals(2, summary.coordinateStopCount)
        assertEquals(64.4, summary.straightLineDistanceKm ?: 0.0, 0.5)
        assertEquals("직선거리 64.4km", formatRouteDistance(summary.straightLineDistanceKm))
    }

    @Test
    fun routeSummary_usesUnknownDistanceWhenRouteHasFewerThanTwoCoordinateStops() {
        val summary = routeSummary(listOf(RouteStop("DMZ 생태길", "주요 관찰지", 38.1, 127.2)))

        assertEquals(1, summary.stopCount)
        assertEquals(1, summary.coordinateStopCount)
        assertNull(summary.straightLineDistanceKm)
        assertEquals("좌표 거리 미정", formatRouteDistance(summary.straightLineDistanceKm))
    }

    @Test
    fun buildRouteMapUri_encodesOriginDestinationAndWaypoints() {
        val stops =
            listOf(
                RouteStop("서울역", "출발"),
                RouteStop("철원 평화전망대", "경유"),
                RouteStop("두루미 관찰대", "경유"),
                RouteStop("DMZ 생태길", "주요 관찰지"),
            )

        val uri = buildRouteMapUri(stops)

        assertEquals(
            "https://www.google.com/maps/dir/?api=1" +
                "&origin=%EC%84%9C%EC%9A%B8%EC%97%AD%20%EB%8C%80%ED%95%9C%EB%AF%BC%EA%B5%AD" +
                "&destination=DMZ%20%EC%83%9D%ED%83%9C%EA%B8%B8%20%EB%8C%80%ED%95%9C%EB%AF%BC%EA%B5%AD" +
                "&waypoints=" +
                "%EC%B2%A0%EC%9B%90%20%ED%8F%89%ED%99%94%EC%A0%84%EB%A7%9D%EB%8C%80%20%EB%8C%80%ED%95%9C%EB%AF%BC%EA%B5%AD" +
                "%7C%EB%91%90%EB%A3%A8%EB%AF%B8%20%EA%B4%80%EC%B0%B0%EB%8C%80%20%EB%8C%80%ED%95%9C%EB%AF%BC%EA%B5%AD" +
                "&travelmode=transit",
            uri,
        )
    }

    @Test
    fun buildRouteMapUri_usesCoordinateDestinationAndSkipsWaypointsWhenDestinationHasCoordinates() {
        val stops =
            listOf(
                RouteStop("서울역", "출발"),
                RouteStop("철원 평화전망대", "경유"),
                RouteStop("DMZ 생태길", "주요 관찰지", 38.1, 127.2),
            )

        val uri = buildRouteMapUri(stops)

        assertEquals(
            "https://www.google.com/maps/dir/?api=1" +
                "&origin=%EC%84%9C%EC%9A%B8%EC%97%AD%20%EB%8C%80%ED%95%9C%EB%AF%BC%EA%B5%AD" +
                "&destination=38.1%2C127.2" +
                "&travelmode=transit",
            uri,
        )
    }

    @Test
    fun buildRouteMapUri_returnsNullWhenRouteHasInsufficientPlaces() {
        assertNull(buildRouteMapUri(emptyList()))
        assertNull(buildRouteMapUri(listOf(RouteStop("", "출발"), RouteStop("관찰지", "도착"))))
    }

    @Test
    fun buildPlaceMapUri_buildsGeoUriWhenCoordinatesExist() {
        val uri = buildPlaceMapUri(RouteStop("DMZ 생태길", "주요 관찰지", 38.1, 127.2))

        assertEquals(
            "geo:38.1,127.2?q=38.1,127.2(DMZ%20%EC%83%9D%ED%83%9C%EA%B8%B8)",
            uri,
        )
    }

    @Test
    fun buildPlaceMapUri_buildsGoogleSearchUriWhenCoordinatesAreMissing() {
        val uri = buildPlaceMapUri(RouteStop("철원 평화전망대", "경유"))

        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=%EC%B2%A0%EC%9B%90%20%ED%8F%89%ED%99%94%EC%A0%84%EB%A7%9D%EB%8C%80%20%EB%8C%80%ED%95%9C%EB%AF%BC%EA%B5%AD",
            uri,
        )
    }

    @Test
    fun buildPlaceMapUri_returnsNullWhenPlaceIsMissing() {
        assertNull(buildPlaceMapUri(null))
        assertNull(buildPlaceMapUri(RouteStop(" ", "경유")))
    }

    private fun dayPlan(vararg locations: String) =
        TripDayPlanDto(
            day = 1,
            title = "탐방",
            items =
                locations.mapIndexed { index, location ->
                    TripDayItemDto(
                        time = "%02d:00".format(9 + index),
                        activity = "이동",
                        location = location,
                    )
                },
        )

    private fun tripPlanFixture(
        origin: String = "서울역",
        hotspotName: String = "DMZ 생태길",
        routeStops: List<TripRouteStopDto> = emptyList(),
        daysPlan: List<TripDayPlanDto> = listOf(dayPlan("철원 평화전망대")),
    ) = TripPlanResponseDto(
        speciesId = "lynx",
        speciesName = "삵",
        origin = origin,
        days = 1,
        travelers = 1,
        hotspotName = hotspotName,
        hotspotLatitude = 38.1,
        hotspotLongitude = 127.2,
        region = "강원",
        summary = "하루 탐방 코스",
        checklist = listOf("쌍안경", "물"),
        routeStops = routeStops,
        daysPlan = daysPlan,
        costs =
            CostBreakdownDto(
                transport = 50_000,
                accommodation = 0,
                food = 20_000,
                entryFee = 0,
                misc = 10_000,
                total = 80_000,
                perPerson = 80_000,
            ),
        disclaimer = "실제 현장 상황을 확인하세요.",
        source = "test",
    )
}
