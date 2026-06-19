package com.wildtrail.app.feature.records

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wildtrail.app.data.dto.SightingDto
import com.wildtrail.app.ui.components.OfflineErrorPanel
import com.wildtrail.app.ui.theme.Forest
@Composable
fun RecordsScreen(
    sightingState: SightingUiState,
    onRefresh: () -> Unit,
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
                "관찰 기록",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "식별 결과로 저장한 관찰 기록을 시간순으로 확인합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val countText = when (sightingState) {
                is SightingUiState.Ready -> "${sightingState.sightings.size}건"
                SightingUiState.Loading -> "불러오는 중"
                SightingUiState.Idle -> "대기"
                is SightingUiState.Error -> "오류"
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
        AnimatedContent(targetState = sightingState, modifier = Modifier.weight(1f), label = "sighting-state") { current ->
            when (current) {
                SightingUiState.Idle -> RecordsIdlePanel("관찰 기록을 불러오세요.")
                SightingUiState.Loading -> RecordsLoadingPanel()
                is SightingUiState.Error -> OfflineErrorPanel(
                    title = "기록 연결 실패",
                    message = current.message,
                    actionLabel = if (isLoading) "갱신 중" else "다시 불러오기",
                    onAction = onRefresh,
                    actionTestTag = "records-error-retry-button",
                    isActionEnabled = !isLoading,
                )
                is SightingUiState.Ready -> SightingList(current.sightings)
            }
        }
    }
}

@Composable
private fun SightingList(sightings: List<SightingDto>) {
    if (sightings.isEmpty()) {
        RecordsIdlePanel("아직 저장된 관찰 기록이 없습니다.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(sightings, key = { it.id }) { sighting ->
            SightingRow(sighting)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SightingRow(sighting: SightingDto) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
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
                    Text(sighting.commonName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        sighting.createdAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    "${(sighting.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = confidenceColor(sighting.confidence),
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordsMetadataChip(sighting.mediaType.uppercase())
                if (sighting.locationName.isNotBlank()) RecordsMetadataChip(sighting.locationName)
            }
            if (sighting.note.isNotBlank()) {
                Text(
                    sighting.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
private fun confidenceColor(confidence: Double): Color {
    return when {
        confidence >= 0.8 -> Forest
        confidence >= 0.6 -> Color(0xFF8A6200)
        else -> Color(0xFFB3261E)
    }
}

@Composable
private fun RecordsIdlePanel(message: String) {
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
private fun RecordsLoadingPanel() {
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
private fun RecordsMetadataChip(text: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


