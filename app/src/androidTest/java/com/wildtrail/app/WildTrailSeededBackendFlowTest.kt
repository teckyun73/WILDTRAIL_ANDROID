package com.wildtrail.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.wildtrail.app.data.dto.CostBreakdownDto
import com.wildtrail.app.data.dto.HealthResponseDto
import com.wildtrail.app.data.dto.HotspotDto
import com.wildtrail.app.data.dto.ModelStatusDto
import com.wildtrail.app.data.dto.SightingDto
import com.wildtrail.app.data.dto.SpeciesDetailDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.data.dto.TripDayItemDto
import com.wildtrail.app.data.dto.TripDayPlanDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import com.wildtrail.app.data.dto.TripRouteStopDto
import com.wildtrail.app.feature.identify.IdentifyViewModel
import com.wildtrail.app.feature.records.RecordsViewModel
import com.wildtrail.app.feature.species.SpeciesViewModel
import com.wildtrail.app.feature.status.StatusViewModel
import com.wildtrail.app.feature.trips.TripsViewModel
import com.wildtrail.app.ui.theme.WildTrailTheme
import org.junit.Rule
import org.junit.Test

class WildTrailSeededBackendFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun seededSpeciesFlowSearchesShowsDetailHotspotsAndPreparesTrip() {
        launchSeededApp()

        selectTab("도감")
        waitUntilText("삵")
        composeRule.onNodeWithText("2종 표시").assertIsDisplayed()

        replaceText("species-search-field", "두루미", scrollToField = false)
        composeRule.onNodeWithText("1종 표시").assertIsDisplayed()
        composeRule.onNodeWithTag("species-row-crane").assertIsDisplayed()

        replaceText("species-search-field", "삵", scrollToField = false)
        composeRule.onNodeWithText("1종 표시").assertIsDisplayed()
        composeRule.onNodeWithTag("species-row-lynx").performClick()

        waitUntilText("국내에 서식하는 야생 고양잇과 포유류")
        assertTextExists("DMZ 생태길")
        assertTextExists("추천 관찰지")

        composeRule.onNodeWithTag("species-plan-trip-button").performClick()

        waitUntilText("여행 플래너")
        composeRule.onNodeWithTag("trip-species-id").assertTextContains("lynx")
    }

    @Test
    fun seededTripFlowGeneratesPlanAndOpensNativeMap() {
        launchSeededApp()

        selectTab("여행")
        replaceText("trip-species-id", "lynx")
        replaceText("trip-origin", "서울역")
        replaceText("trip-days", "1")
        replaceText("trip-travelers", "1")
        replaceText("trip-month", "5")
        replaceText("trip-budget", "150000")

        composeRule.onNodeWithTag("trip-plan-button")
            .performScrollTo()
            .performClick()

        waitUntilText("하루 탐방 코스")
        composeRule.onNodeWithText("지점 2곳").assertIsDisplayed()
        composeRule.onNodeWithText("좌표 2곳").assertIsDisplayed()
        composeRule.onNodeWithText("직선거리", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("DMZ 생태길").assertIsDisplayed()
        composeRule.onNodeWithTag("trip-open-native-map-button")
            .performScrollTo()
            .performClick()

        waitUntilText("여행 지도")
        composeRule.onNodeWithText("지점 2곳").assertIsDisplayed()
        composeRule.onNodeWithText("동선 지점").assertIsDisplayed()
    }

    private fun launchSeededApp() {
        composeRule.setContent {
            WildTrailTheme {
                WildTrailAppContent(seededViewModels())
            }
        }
    }

    private fun seededViewModels() = WildTrailViewModels(
        settings = AppSettingsViewModel(),
        identify = IdentifyViewModel(),
        records = RecordsViewModel(
            loadSightingList = { _, _ -> listOf(sightingFixture()) },
        ),
        status = StatusViewModel { _, _ -> healthFixture() },
        species = SpeciesViewModel(
            loadSpeciesList = { _, _ -> speciesSummaryFixtures() },
            loadSpeciesDetailById = { speciesId, _, _ -> speciesDetailFixture(speciesId) },
            loadHotspotsBySpeciesId = { speciesId, _, _ -> listOf(hotspotFixture(speciesId)) },
        ),
        trips = TripsViewModel { _, _, _ -> tripPlanFixture() },
    )

    private fun selectTab(label: String) {
        composeRule.onNode(hasText(label) and hasClickAction()).performClick()
    }

    private fun replaceText(tag: String, value: String, scrollToField: Boolean = true) {
        val field = composeRule.onNodeWithTag(tag)
        if (scrollToField) {
            field.performScrollTo()
        }
        field.performTextClearance()
        field.performTextInput(value)
    }

    private fun waitUntilText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText(text))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun assertTextExists(text: String) {
        val nodes = composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes()
        check(nodes.isNotEmpty()) { "Expected text node '$text' to exist." }
    }

    private fun healthFixture() = HealthResponseDto(
        status = "ok",
        service = "wildtrail-test",
        imageModel = ModelStatusDto(modelLoaded = false, modelPath = "stub"),
        audioModel = ModelStatusDto(modelLoaded = false, modelPath = "stub"),
        speciesJsonCount = 2,
        speciesDbCount = 2,
        llmConfigured = false,
        llmProvider = "off",
        llmModel = "none",
    )

    private fun speciesSummaryFixtures() = listOf(
        SpeciesSummaryDto(
            id = "lynx",
            commonName = "삵",
            scientificName = "Prionailurus bengalensis",
            category = "mammal",
            protectionGrade = "II",
            bestMonths = "4-10",
        ),
        SpeciesSummaryDto(
            id = "crane",
            commonName = "두루미",
            scientificName = "Grus japonensis",
            category = "bird",
            protectionGrade = "I",
            bestMonths = "11-2",
        ),
    )

    private fun speciesDetailFixture(speciesId: String) = when (speciesId) {
        "crane" -> SpeciesDetailDto(
            id = speciesId,
            commonName = "두루미",
            scientificName = "Grus japonensis",
            category = "bird",
            protectionGrade = "I",
            habitat = "습지와 철새 도래지",
            diet = "곡물과 수생 생물",
            breedingSeason = "봄",
            activeTime = "주간",
            observationTips = "망원경으로 거리를 유지",
            bestMonths = "11-2",
            similarSpecies = "재두루미",
            description = "겨울 철원 평야에서 관찰 가능한 대형 조류",
        )
        else -> SpeciesDetailDto(
            id = speciesId,
            commonName = "삵",
            scientificName = "Prionailurus bengalensis",
            category = "mammal",
            protectionGrade = "II",
            habitat = "숲과 농경지 가장자리",
            diet = "설치류와 조류",
            breedingSeason = "봄",
            activeTime = "야행성",
            observationTips = "흔적과 배설물을 함께 확인",
            bestMonths = "4-10",
            similarSpecies = "고양이",
            description = "국내에 서식하는 야생 고양잇과 포유류",
        )
    }

    private fun hotspotFixture(speciesId: String) = if (speciesId == "crane") {
        HotspotDto(
            id = 2,
            name = "철원 평야",
            region = "강원",
            latitude = 38.2,
            longitude = 127.3,
            speciesId = speciesId,
            speciesName = "두루미",
            bestMonths = "11-2",
            observationScore = 0.91,
            accessLevel = "쉬움",
            transportNote = "탐조대 이용",
            entryFee = 0,
            facilities = "탐조대",
            safetyNote = "서식지 접근 금지",
        )
    } else {
        HotspotDto(
            id = 1,
            name = "DMZ 생태길",
            region = "강원",
            latitude = 38.1,
            longitude = 127.2,
            speciesId = speciesId,
            speciesName = "삵",
            bestMonths = "4-10",
            observationScore = 0.86,
            accessLevel = "보통",
            transportNote = "자차 권장",
            entryFee = 0,
            facilities = "전망대",
            safetyNote = "지정 탐방로 이용",
        )
    }

    private fun sightingFixture() = SightingDto(
        id = 1,
        speciesId = "lynx",
        commonName = "삵",
        confidence = 0.91,
        mediaType = "image",
        locationName = "DMZ 생태길",
        note = "seeded observation",
        createdAt = "2026-06-18T12:00:00",
    )

    private fun tripPlanFixture() = TripPlanResponseDto(
        speciesId = "lynx",
        speciesName = "삵",
        origin = "서울역",
        days = 1,
        travelers = 1,
        hotspotName = "DMZ 생태길",
        hotspotLatitude = 38.1,
        hotspotLongitude = 127.2,
        region = "강원",
        summary = "하루 탐방 코스",
        checklist = listOf("쌍안경", "물"),
        routeStops = listOf(
            TripRouteStopDto(name = "서울역", role = "출발", latitude = 37.55, longitude = 126.97),
            TripRouteStopDto(name = "DMZ 생태길", role = "관찰지", latitude = 38.1, longitude = 127.2),
        ),
        daysPlan = listOf(
            TripDayPlanDto(
                day = 1,
                title = "탐방",
                items = listOf(TripDayItemDto(time = "09:00", activity = "출발", location = "서울역")),
            ),
        ),
        costs = CostBreakdownDto(
            transport = 50_000,
            accommodation = 0,
            food = 20_000,
            entryFee = 0,
            misc = 10_000,
            total = 80_000,
            perPerson = 80_000,
        ),
        disclaimer = "실제 현장 상황을 확인하세요.",
        source = "seeded",
    )
}

