package com.wildtrail.app.feature.status

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.OutlinedButton
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
import com.wildtrail.app.ApiEnvironmentPreset
import com.wildtrail.app.data.dto.HealthResponseDto
import com.wildtrail.app.data.dto.ModelStatusDto
import com.wildtrail.app.ui.components.OfflineErrorPanel
import com.wildtrail.app.ui.theme.Forest

@Composable
fun StatusScreen(
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    apiPresets: List<ApiEnvironmentPreset>,
    selectedEnvironmentLabel: String,
    onPresetSelected: (String) -> Unit,
    onResetBaseUrl: () -> Unit,
    healthState: HealthUiState,
    onCheckHealth: () -> Unit,
    isLoading: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Header(healthState)
        ConnectionPanel(
            baseUrl = baseUrl,
            onBaseUrlChange = onBaseUrlChange,
            apiPresets = apiPresets,
            selectedEnvironmentLabel = selectedEnvironmentLabel,
            onPresetSelected = onPresetSelected,
            onResetBaseUrl = onResetBaseUrl,
            onCheck = onCheckHealth,
            isLoading = isLoading,
        )
        AnimatedContent(targetState = healthState, label = "health-state") { current ->
            when (current) {
                HealthUiState.Idle -> IdlePanel("서버 상태를 확인하세요.")
                HealthUiState.Loading -> LoadingPanel()
                is HealthUiState.Ready -> HealthPanel(current.health)
                is HealthUiState.Error ->
                    OfflineErrorPanel(
                        title = "백엔드 연결 실패",
                        message = current.message,
                        actionLabel = if (isLoading) "확인 중..." else "다시 확인",
                        onAction = onCheckHealth,
                        actionTestTag = "status-error-retry-button",
                        isActionEnabled = !isLoading,
                    )
            }
        }
        NextStepsPanel()
    }
}

@Composable
private fun MetadataChip(text: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Header(state: HealthUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "야생동물 식별 앱의 첫 연결점",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "FastAPI 서버 상태를 확인한 뒤 사진 식별, 도감, 관찰지 기능을 순서대로 붙입니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatusPill(state)
    }
}

@Composable
private fun StatusPill(state: HealthUiState) {
    val (label, color) =
        when (state) {
            HealthUiState.Idle -> "대기" to MaterialTheme.colorScheme.onSurfaceVariant
            HealthUiState.Loading -> "확인 중" to MaterialTheme.colorScheme.tertiary
            is HealthUiState.Ready -> if (state.health.status == "ok") "정상" to Forest else "점검 필요" to MaterialTheme.colorScheme.tertiary
            is HealthUiState.Error -> "연결 실패" to Color(0xFFB3261E)
        }
    Row(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
        )
        Text(label, color = color, style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConnectionPanel(
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    apiPresets: List<ApiEnvironmentPreset>,
    selectedEnvironmentLabel: String,
    onPresetSelected: (String) -> Unit,
    onResetBaseUrl: () -> Unit,
    onCheck: () -> Unit,
    isLoading: Boolean,
) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
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
                    Text("API 서버", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "현재 환경: $selectedEnvironmentLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = onResetBaseUrl,
                    modifier = Modifier.testTag("status-api-reset-button"),
                ) {
                    Text("기본값")
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                apiPresets.forEach { preset ->
                    OutlinedButton(
                        onClick = { onPresetSelected(preset.id) },
                        modifier = Modifier.testTag("status-api-preset-${preset.id}"),
                    ) {
                        Text(preset.label)
                    }
                }
            }
            OutlinedTextField(
                value = baseUrl,
                onValueChange = onBaseUrlChange,
                singleLine = true,
                label = { Text("Base URL") },
                supportingText = { Text("프리셋을 선택하거나 직접 입력하세요. 실기기 LAN 프리셋은 PC IP로 수정해야 합니다.") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("status-base-url"),
            )
            Button(
                onClick = onCheck,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("status-check-button"),
            ) {
                Text(if (isLoading) "확인 중..." else "상태 확인")
            }
        }
    }
}

@Composable
private fun IdlePanel(message: String) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Text(
            message,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingPanel() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(color = Forest)
    }
}

@Composable
private fun HealthPanel(health: HealthResponseDto) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
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
                        Text(health.service, fontWeight = FontWeight.Bold)
                        Text(
                            "species ${health.speciesDbCount}/${health.speciesJsonCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        health.status.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (health.status == "ok") Forest else MaterialTheme.colorScheme.tertiary,
                    )
                }
                HorizontalDivider()
                ModelRow("이미지 모델", health.imageModel)
                ModelRow("오디오 모델", health.audioModel)
                LlmRow(health)
            }
        }
        AnimatedVisibility(visible = health.warnings.isNotEmpty()) {
            WarningPanel(health.warnings)
        }
    }
}

@Composable
private fun ModelRow(
    label: String,
    model: ModelStatusDto,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(
                text = model.modelVersion ?: model.modelPath.ifBlank { "checkpoint 정보 없음" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = if (model.modelLoaded) "MODEL" else "STUB",
            color = if (model.modelLoaded) Forest else MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun LlmRow(health: HealthResponseDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("LLM", fontWeight = FontWeight.SemiBold)
            Text(
                "${health.llmProvider} / ${health.llmModel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (health.llmConfigured) "READY" else "OFF",
            color = if (health.llmConfigured) Forest else MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun WarningPanel(warnings: List<String>) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF8E7)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("백엔드 경고", fontWeight = FontWeight.Bold, color = Color(0xFF7A4B00))
            warnings.forEach { warning ->
                Text("- $warning", style = MaterialTheme.typography.bodySmall, color = Color(0xFF7A4B00))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NextStepsPanel() {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("다음 구현 단위", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("관찰지 연결", "이미지 업로드", "기록 저장", "여행 플래너").forEach { item ->
                    MetadataChip(item)
                }
            }
            Text(
                "도감 상세가 연결되면 다음은 선택한 종의 관찰지 지도와 식별 결과 저장 흐름입니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
