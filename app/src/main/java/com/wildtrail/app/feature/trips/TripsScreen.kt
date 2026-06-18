package com.wildtrail.app.feature.trips

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wildtrail.app.feature.species.SpeciesUiState
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.data.dto.TripPlanResponseDto
import com.wildtrail.app.ui.theme.Forest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TripsScreen(
    nativeMapPlan: TripPlanResponseDto?,
    onCloseNativeMap: () -> Unit,
    speciesId: String,
    onSpeciesIdChange: (String) -> Unit,
    speciesState: SpeciesUiState,
    onSelectSpecies: (String) -> Unit,
    onRefreshSpecies: () -> Unit,
    origin: String,
    onOriginChange: (String) -> Unit,
    days: String,
    onDaysChange: (String) -> Unit,
    budget: String,
    onBudgetChange: (String) -> Unit,
    travelers: String,
    onTravelersChange: (String) -> Unit,
    month: String,
    onMonthChange: (String) -> Unit,
    transport: String,
    onTransportChange: (String) -> Unit,
    accommodation: String,
    onAccommodationChange: (String) -> Unit,
    difficulty: String,
    onDifficultyChange: (String) -> Unit,
    tripState: TripUiState,
    onPlanTrip: () -> Unit,
    onOpenNativeMap: (TripPlanResponseDto) -> Unit,
    isLoading: Boolean,
) {
    val tripScrollState = rememberScrollState()
    val tripScreenScope = rememberCoroutineScope()

    if (nativeMapPlan != null) {
        TripNativeMapScreen(
            plan = nativeMapPlan,
            onClose = onCloseNativeMap,
        )
        return
    }

    LaunchedEffect(tripState) {
        if (tripState is TripUiState.Ready || tripState is TripUiState.Error) {
            delay(120)
            tripScrollState.animateScrollTo(tripScrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(tripScrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("여행 플래너", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "종과 출발 조건을 입력하면 관찰지, 일정, 비용을 백엔드에서 생성합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("계획 조건", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = speciesId,
                    onValueChange = onSpeciesIdChange,
                    label = { Text("Species ID") },
                    supportingText = { Text("예: pica_pica, grus_japonensis") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip-species-id"),
                )
                TripSpeciesChooser(
                    speciesState = speciesState,
                    speciesId = speciesId,
                    onSelectSpecies = onSelectSpecies,
                    onRefreshSpecies = onRefreshSpecies,
                )
                OutlinedTextField(
                    value = origin,
                    onValueChange = onOriginChange,
                    label = { Text("출발지") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip-origin"),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = days,
                        onValueChange = onDaysChange,
                        label = { Text("일수") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("trip-days"),
                    )
                    OutlinedTextField(
                        value = travelers,
                        onValueChange = onTravelersChange,
                        label = { Text("인원") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("trip-travelers"),
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = onMonthChange,
                        label = { Text("월") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("trip-month"),
                    )
                }
                TripMonthSuggestions(
                    speciesState = speciesState,
                    speciesId = speciesId,
                    month = month,
                    onMonthChange = onMonthChange,
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = onBudgetChange,
                    label = { Text("예산(원)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip-budget"),
                )
                TripPreferenceControls(
                    transport = transport,
                    onTransportChange = onTransportChange,
                    accommodation = accommodation,
                    onAccommodationChange = onAccommodationChange,
                    difficulty = difficulty,
                    onDifficultyChange = onDifficultyChange,
                )
                Button(
                    onClick = onPlanTrip,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip-plan-button"),
                ) {
                    Text(if (isLoading) "계획 생성 중..." else "여행 계획 생성")
                }
            }
        }
        AnimatedContent(targetState = tripState, label = "trip-state") { current ->
            when (current) {
                TripUiState.Empty -> TripIdlePanel("조건을 입력하고 여행 계획을 생성하세요.")
                TripUiState.Loading -> TripLoadingPanel()
                is TripUiState.Error -> TripErrorPanel(
                    message = current.message,
                    onRetry = onPlanTrip,
                    isLoading = isLoading,
                )
                is TripUiState.Ready -> TripPlanPanel(
                    plan = current.plan,
                    onReplan = onPlanTrip,
                    onOpenNativeMap = { onOpenNativeMap(current.plan) },
                    onEditConditions = {
                        tripScreenScope.launch {
                            tripScrollState.animateScrollTo(0)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TripErrorPanel(
    message: String,
    onRetry: () -> Unit,
    isLoading: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TripErrorContent("여행 계획 실패", message)
        Button(
            onClick = onRetry,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "다시 생성 중..." else "같은 조건으로 다시 생성")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TripMonthSuggestions(
    speciesState: SpeciesUiState,
    speciesId: String,
    month: String,
    onMonthChange: (String) -> Unit,
) {
    val selectedSpecies = (speciesState as? SpeciesUiState.Ready)
        ?.species
        ?.firstOrNull { it.id == speciesId }
    val months = selectedSpecies?.bestMonths?.let(::parseBestMonths).orEmpty()
    if (selectedSpecies == null || months.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("추천 관찰월", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            months.forEach { option ->
                PreferenceChip(
                    label = "${option}월",
                    selected = month.toIntOrNull() == option,
                    onClick = { onMonthChange(option.toString()) },
                )
            }
        }
    }
}

private fun parseBestMonths(value: String): List<Int> {
    return Regex("\\d+")
        .findAll(value)
        .mapNotNull { it.value.toIntOrNull() }
        .filter { it in 1..12 }
        .distinct()
        .sorted()
        .toList()
}

@Composable
private fun TripPreferenceControls(
    transport: String,
    onTransportChange: (String) -> Unit,
    accommodation: String,
    onAccommodationChange: (String) -> Unit,
    difficulty: String,
    onDifficultyChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("선호 조건", style = MaterialTheme.typography.labelLarge, color = Forest, fontWeight = FontWeight.SemiBold)
        PreferenceSelector(
            label = "이동",
            value = transport,
            options = listOf(
                "public" to "대중교통",
                "car" to "자가용",
            ),
            onValueChange = onTransportChange,
        )
        PreferenceSelector(
            label = "숙소",
            value = accommodation,
            options = listOf(
                "guesthouse" to "게스트하우스",
                "hotel" to "호텔",
                "camping" to "캠핑",
            ),
            onValueChange = onAccommodationChange,
        )
        PreferenceSelector(
            label = "난이도",
            value = difficulty,
            options = listOf(
                "easy" to "쉬움",
                "moderate" to "보통",
                "hard" to "도전",
            ),
            onValueChange = onDifficultyChange,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferenceSelector(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (optionValue, optionLabel) ->
                PreferenceChip(
                    label = optionLabel,
                    selected = value == optionValue,
                    onClick = { onValueChange(optionValue) },
                )
            }
        }
    }
}

@Composable
private fun PreferenceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Forest else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 0.dp else 1.dp,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun TripSpeciesChooser(
    speciesState: SpeciesUiState,
    speciesId: String,
    onSelectSpecies: (String) -> Unit,
    onRefreshSpecies: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("종 선택", style = MaterialTheme.typography.labelLarge, color = Forest, fontWeight = FontWeight.SemiBold)
        when (speciesState) {
            SpeciesUiState.Idle -> Button(
                onClick = onRefreshSpecies,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("도감 목록 불러오기")
            }
            SpeciesUiState.Loading -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Forest)
                Text(
                    "도감 목록을 불러오는 중",
                    modifier = Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            is SpeciesUiState.Error -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "종 목록을 불러오지 못했습니다.",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = onRefreshSpecies, colors = ButtonDefaults.buttonColors(containerColor = Forest)) {
                    Text("재시도")
                }
            }
            is SpeciesUiState.Ready -> {
                val query = speciesId.trim().lowercase()
                val matches = speciesState.species
                    .filter { item ->
                        query.isBlank() ||
                            item.id.lowercase().contains(query) ||
                            item.commonName.lowercase().contains(query) ||
                            item.scientificName.lowercase().contains(query)
                    }
                    .take(5)
                val selected = speciesState.species.firstOrNull { it.id == speciesId }
                Text(
                    selected?.let { "${it.commonName} · ${it.id}" } ?: "입력값과 가까운 종을 선택하세요.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (matches.isEmpty()) {
                    Text(
                        "일치하는 종이 없습니다. 도감 탭에서 정확한 ID를 확인하세요.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    matches.forEach { item ->
                        TripSpeciesOption(
                            species = item,
                            selected = item.id == speciesId,
                            onClick = { onSelectSpecies(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSpeciesOption(
    species: SpeciesSummaryDto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Forest.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(species.commonName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                species.id,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(species.category.uppercase(), color = Forest, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TripPlanPanel(
    plan: TripPlanResponseDto,
    onReplan: () -> Unit,
    onOpenNativeMap: () -> Unit,
    onEditConditions: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plan.speciesName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${plan.region} · ${plan.hotspotName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(plan.source.uppercase(), color = Forest, style = MaterialTheme.typography.labelLarge)
                }
                Text(plan.summary)
                Text(
                    "${plan.days}일 · ${plan.travelers}명 · ${formatKrw(plan.costs.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Forest,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onEditConditions,
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("조건 수정")
                    }
                    Button(
                        onClick = onReplan,
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("다시 생성")
                    }
                }
            }
        }
        RoutePreviewPanel(plan, onOpenNativeMap = onOpenNativeMap)
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("비용", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TripDetailField("1인 비용", formatKrw(plan.costs.perPerson))
                TripDetailField("교통", formatKrw(plan.costs.transport))
                TripDetailField("숙박", formatKrw(plan.costs.accommodation))
                TripDetailField("식비", formatKrw(plan.costs.food))
                TripDetailField("입장료", formatKrw(plan.costs.entryFee))
            }
        }
        plan.daysPlan.forEach { day ->
            Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(day.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    day.items.forEachIndexed { index, item ->
                        if (index > 0) HorizontalDivider()
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("${item.time} · ${item.activity}", fontWeight = FontWeight.SemiBold)
                            Text(item.location, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (item.note.isNotBlank()) {
                                Text(item.note, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("체크리스트", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                plan.checklist.forEach { Text("· $it") }
                Text(plan.disclaimer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RoutePreviewPanel(
    plan: TripPlanResponseDto,
    onOpenNativeMap: () -> Unit,
) {
    val context = LocalContext.current
    val stops = remember(plan) { tripRouteStops(plan) }
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("지도/동선", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${plan.origin}에서 ${plan.hotspotName}까지 주요 이동 지점",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Forest.copy(alpha = 0.08f)),
            ) {
                RouteCanvas(stops.size, modifier = Modifier.fillMaxSize())
            }
            Button(
                onClick = onOpenNativeMap,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("앱 지도 보기")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        openRouteInMapApp(
                            context = context,
                            stops = stops,
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("경로 열기")
                }
                Button(
                    onClick = {
                        openPlaceInMapApp(
                            context = context,
                            stop = stops.lastOrNull(),
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("장소 검색")
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stops.forEachIndexed { index, stop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                openPlaceInMapApp(
                                    context = context,
                                    stop = stop,
                                )
                            }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (index == stops.lastIndex) Forest else Forest.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${index + 1}",
                                color = if (index == stops.lastIndex) Color.White else Forest,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stop.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                stop.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteCanvas(stopCount: Int, modifier: Modifier = Modifier) {
    val routeColor = Forest
    val guideColor = Forest.copy(alpha = 0.16f)
    val pointColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier) {
        val count = stopCount.coerceAtLeast(2)
        val stepX = size.width / (count + 1)
        val points = List(count) { index ->
            val x = stepX * (index + 1)
            val yRatio = when (index % 4) {
                0 -> 0.68f
                1 -> 0.32f
                2 -> 0.44f
                else -> 0.22f
            }
            Offset(x, size.height * yRatio)
        }
        points.zipWithNext().forEach { (start, end) ->
            drawLine(
                color = guideColor,
                start = Offset(start.x, start.y + 10f),
                end = Offset(end.x, end.y + 10f),
                strokeWidth = 18f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = routeColor,
                start = start,
                end = end,
                strokeWidth = 8f,
                cap = StrokeCap.Round,
            )
        }
        points.forEachIndexed { index, point ->
            drawCircle(color = pointColor, radius = 17f, center = point)
            drawCircle(
                color = routeColor,
                radius = if (index == points.lastIndex) 13f else 9f,
                center = point,
                style = if (index == points.lastIndex) Stroke(width = 5f) else Stroke(width = 4f),
            )
            if (index == points.lastIndex) {
                drawCircle(color = routeColor, radius = 5f, center = point)
            }
        }
    }
}

@Composable
private fun TripIdlePanel(message: String) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Text(
            message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TripLoadingPanel() {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Forest)
            Text(
                "불러오는 중",
                modifier = Modifier.padding(start = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TripErrorContent(title: String, message: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        color = Color(0xFFFFF3E0),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF7A4B00))
            Text(message, color = Color(0xFF7A4B00))
        }
    }
}

@Composable
private fun TripDetailField(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Forest, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatKrw(value: Int): String = "%,d원".format(value)

