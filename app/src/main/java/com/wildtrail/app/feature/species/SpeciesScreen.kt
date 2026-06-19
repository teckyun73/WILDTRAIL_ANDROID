package com.wildtrail.app.feature.species

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wildtrail.app.data.dto.HotspotDto
import com.wildtrail.app.data.dto.SpeciesDetailDto
import com.wildtrail.app.data.dto.SpeciesSummaryDto
import com.wildtrail.app.ui.components.OfflineErrorPanel
import com.wildtrail.app.ui.theme.Forest
@Composable
fun SpeciesScreen(
    speciesState: SpeciesUiState,
    speciesDetailState: SpeciesDetailUiState,
    hotspotState: HotspotUiState,
    search: String,
    onSearchChange: (String) -> Unit,
    selectedSpeciesId: String?,
    onSelectSpecies: (String) -> Unit,
    onPlanTripForSpecies: (String) -> Unit,
    onRefresh: () -> Unit,
    onRetrySelectedSpecies: () -> Unit,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "도감 목록",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "종을 선택하면 상세 도감 정보를 백엔드에서 불러옵니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            singleLine = true,
            label = { Text("종 이름 검색") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val countText = when (speciesState) {
                is SpeciesUiState.Ready -> "${filterSpecies(speciesState.species, search).size}종 표시"
                SpeciesUiState.Loading -> "불러오는 중"
                SpeciesUiState.Idle -> "대기"
                is SpeciesUiState.Error -> "오류"
            }
            Text(countText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onRefresh,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
            ) {
                Text(if (isLoading) "갱신 중" else "새로고침")
            }
        }
        SpeciesDetailPanel(
            state = speciesDetailState,
            onPlanTripForSpecies = onPlanTripForSpecies,
            onRetry = onRetrySelectedSpecies,
            isLoading = speciesDetailState is SpeciesDetailUiState.Loading || hotspotState is HotspotUiState.Loading,
        )
        HotspotPanel(
            state = hotspotState,
            onRetry = onRetrySelectedSpecies,
            isLoading = speciesDetailState is SpeciesDetailUiState.Loading || hotspotState is HotspotUiState.Loading,
        )
        AnimatedContent(targetState = speciesState, modifier = Modifier.weight(1f), label = "species-state") { current ->
            when (current) {
                SpeciesUiState.Idle -> SpeciesIdlePanel("도감 목록을 불러오세요.")
                SpeciesUiState.Loading -> SpeciesLoadingPanel()
                is SpeciesUiState.Error -> OfflineErrorPanel(
                    title = "도감 연결 실패",
                    message = current.message,
                    actionLabel = if (isLoading) "갱신 중" else "다시 불러오기",
                    onAction = onRefresh,
                    actionTestTag = "species-error-retry-button",
                    isActionEnabled = !isLoading,
                )
                is SpeciesUiState.Ready -> SpeciesList(
                    species = filterSpecies(current.species, search),
                    selectedSpeciesId = selectedSpeciesId,
                    onSelectSpecies = onSelectSpecies,
                )
            }
        }
    }
}

private fun filterSpecies(species: List<SpeciesSummaryDto>, search: String): List<SpeciesSummaryDto> {
    val query = search.trim().lowercase()
    if (query.isEmpty()) return species
    return species.filter { item ->
        item.commonName.lowercase().contains(query) ||
            item.scientificName.lowercase().contains(query) ||
            item.id.lowercase().contains(query)
    }
}

@Composable
private fun SpeciesList(
    species: List<SpeciesSummaryDto>,
    selectedSpeciesId: String?,
    onSelectSpecies: (String) -> Unit,
) {
    if (species.isEmpty()) {
        SpeciesIdlePanel("검색 결과가 없습니다.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(species, key = { it.id }) { item ->
            SpeciesRow(
                species = item,
                selected = item.id == selectedSpeciesId,
                onClick = { onSelectSpecies(item.id) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeciesRow(
    species: SpeciesSummaryDto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("species-row-${species.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Forest.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 2.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(species.commonName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        species.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    species.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Forest,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SpeciesMetadataChip("적기 ${species.bestMonths.ifBlank { "미정" }}")
                species.protectionGrade?.takeIf { it.isNotBlank() }?.let { SpeciesMetadataChip(it) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeciesDetailPanel(
    state: SpeciesDetailUiState,
    onPlanTripForSpecies: (String) -> Unit,
    onRetry: () -> Unit,
    isLoading: Boolean,
) {
    AnimatedContent(targetState = state, label = "species-detail-state") { current ->
        when (current) {
            SpeciesDetailUiState.Empty -> Unit
            SpeciesDetailUiState.Loading -> Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Forest)
                    Text(
                        "상세 정보를 불러오는 중",
                        modifier = Modifier.padding(start = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            is SpeciesDetailUiState.Error -> OfflineErrorPanel(
                title = "상세 정보 오류",
                message = current.message,
                actionLabel = if (isLoading) "불러오는 중" else "선택 종 다시 불러오기",
                onAction = onRetry,
                actionTestTag = "species-detail-error-retry-button",
                isActionEnabled = !isLoading,
            )
            is SpeciesDetailUiState.Ready -> SpeciesDetailContent(current.detail, onPlanTripForSpecies)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeciesDetailContent(
    detail: SpeciesDetailDto,
    onPlanTripForSpecies: (String) -> Unit,
) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 2.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(detail.commonName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        detail.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(detail.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = Forest)
            }
            if (detail.description.isNotBlank()) {
                Text(detail.description, style = MaterialTheme.typography.bodyMedium)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpeciesMetadataChip("적기 ${detail.bestMonths.ifBlank { "미정" }}")
                detail.protectionGrade?.takeIf { it.isNotBlank() }?.let { SpeciesMetadataChip(it) }
                SpeciesMetadataChip(detail.activeTime.ifBlank { "활동 시간 미정" })
            }
            Button(
                onClick = { onPlanTripForSpecies(detail.id) },
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("이 종으로 여행 계획")
            }
            HorizontalDivider()
            SpeciesDetailField("서식지", detail.habitat)
            SpeciesDetailField("먹이", detail.diet)
            SpeciesDetailField("번식기", detail.breedingSeason)
            SpeciesDetailField("관찰 팁", detail.observationTips)
            SpeciesDetailField("유사 종", detail.similarSpecies)
        }
    }
}

@Composable
private fun SpeciesDetailField(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Forest, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
private fun HotspotPanel(
    state: HotspotUiState,
    onRetry: () -> Unit,
    isLoading: Boolean,
) {
    AnimatedContent(targetState = state, label = "hotspot-state") { current ->
        when (current) {
            HotspotUiState.Empty -> Unit
            HotspotUiState.Loading -> Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Forest)
                    Text(
                        "관찰지를 불러오는 중",
                        modifier = Modifier.padding(start = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            is HotspotUiState.Error -> OfflineErrorPanel(
                title = "관찰지 오류",
                message = current.message,
                actionLabel = if (isLoading) "불러오는 중" else "선택 종 다시 불러오기",
                onAction = onRetry,
                actionTestTag = "hotspot-error-retry-button",
                isActionEnabled = !isLoading,
            )
            is HotspotUiState.Ready -> HotspotContent(current.hotspots)
        }
    }
}

@Composable
private fun HotspotContent(hotspots: List<HotspotDto>) {
    if (hotspots.isEmpty()) {
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Text(
                "이 종에 연결된 관찰지가 아직 없습니다.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 2.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("추천 관찰지", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "선택한 종과 연결된 관찰 후보 ${hotspots.size}곳",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("TOP ${minOf(3, hotspots.size)}", style = MaterialTheme.typography.labelLarge, color = Forest)
            }
            hotspots.take(3).forEachIndexed { index, hotspot ->
                if (index > 0) HorizontalDivider()
                HotspotRow(hotspot)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HotspotRow(hotspot: HotspotDto) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(hotspot.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(
                    hotspot.region,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "${(hotspot.observationScore * 100).toInt()}점",
                style = MaterialTheme.typography.labelLarge,
                color = Forest,
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SpeciesMetadataChip(hotspot.accessLevel.ifBlank { "난이도 미정" })
            SpeciesMetadataChip(if (hotspot.entryFee > 0) "${hotspot.entryFee}원" else "무료")
            SpeciesMetadataChip("적기 ${hotspot.bestMonths.ifBlank { "미정" }}")
        }
        if (hotspot.transportNote.isNotBlank()) {
            Text(
                "교통: ${hotspot.transportNote}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (hotspot.safetyNote.isNotBlank()) {
            Text(
                "주의: ${hotspot.safetyNote}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}
@Composable
private fun SpeciesIdlePanel(message: String) {
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
private fun SpeciesLoadingPanel() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(color = Forest)
    }
}

@Composable
private fun SpeciesMetadataChip(text: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}




