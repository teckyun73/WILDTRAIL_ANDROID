package com.wildtrail.app.feature.identify

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wildtrail.app.data.dto.IdentificationCandidateDto
import com.wildtrail.app.data.dto.IdentificationResultDto
import com.wildtrail.app.ui.components.OfflineErrorPanel
import com.wildtrail.app.ui.theme.Forest

@Composable
fun IdentifyScreen(
    identifyState: IdentifyUiState,
    selectedImageName: String?,
    selectedAudioName: String?,
    isRecording: Boolean,
    recordingMessage: String?,
    onImageSelected: (Uri) -> Unit,
    onAudioSelected: (Uri) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCandidateSelected: (String) -> Unit,
    onSaveCandidate: (IdentificationCandidateDto, String) -> Unit,
    saveMessage: String?,
) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) onImageSelected(uri)
        },
    )
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) onAudioSelected(uri)
        },
    )
    var audioPermissionMessage by remember { mutableStateOf<String?>(null) }
    val audioPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                audioPermissionMessage = null
                onStartRecording()
            } else {
                audioPermissionMessage = "마이크 권한이 필요합니다. Android 설정에서 WildTrail의 마이크 권한을 허용해 주세요."
            }
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "사진 식별",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "이미지나 오디오 파일을 선택하면 백엔드 모델 또는 스텁 결과로 후보 종을 보여줍니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("이미지 선택", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    selectedImageName ?: "아직 선택된 이미지가 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Button(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    enabled = identifyState !is IdentifyUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (identifyState is IdentifyUiState.Loading) "분석 중..." else "이미지 고르기")
                }
            }
        }
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("오디오 선택", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    selectedAudioName ?: "아직 선택된 오디오가 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "wav, mp3, flac, ogg, m4a 파일을 업로드할 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { audioPicker.launch("audio/*") },
                    enabled = identifyState !is IdentifyUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (identifyState is IdentifyUiState.Loading) "분석 중..." else "오디오 고르기")
                }
            }
        }
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("오디오 녹음", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    recordingMessage ?: audioPermissionMessage ?: "마이크로 최소 1초 이상 녹음한 뒤 바로 분석할 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRecording) Forest else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = {
                        if (isRecording) {
                            onStopRecording()
                        } else {
                            audioPermissionMessage = null
                            audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    enabled = identifyState !is IdentifyUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFB3261E) else Forest,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        when {
                            identifyState is IdentifyUiState.Loading -> "분석 중..."
                            isRecording -> "녹음 종료"
                            else -> "녹음 시작"
                        },
                    )
                }
            }
        }
        AnimatedContent(targetState = identifyState, label = "identify-state") { current ->
            when (current) {
                IdentifyUiState.Empty -> IdentifyIdlePanel("이미지나 오디오를 선택하면 식별 결과가 여기에 표시됩니다.")
                IdentifyUiState.Loading -> IdentifyLoadingPanel()
                is IdentifyUiState.Error -> OfflineErrorPanel(
                    title = "식별 실패",
                    message = current.message,
                    guidance = "API 서버와 네트워크 연결을 확인한 뒤 같은 파일을 다시 선택하거나 녹음을 다시 시작하세요.",
                )
                is IdentifyUiState.Ready -> IdentificationResultPanel(
                    result = current.result,
                    onCandidateSelected = onCandidateSelected,
                    onSaveCandidate = onSaveCandidate,
                    saveMessage = saveMessage,
                )
            }
        }
    }
}

@Composable
private fun IdentificationResultPanel(
    result: IdentificationResultDto,
    onCandidateSelected: (String) -> Unit,
    onSaveCandidate: (IdentificationCandidateDto, String) -> Unit,
    saveMessage: String?,
) {
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
                    Text("식별 결과", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        result.message.ifBlank { "후보 종을 확인하세요." },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "${result.mediaType.uppercase()} / ${result.source.uppercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (result.source == "model") Forest else MaterialTheme.colorScheme.tertiary,
                )
            }
            if (result.candidates.isEmpty()) {
                Text("후보 종이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Button(
                    onClick = { onSaveCandidate(result.candidates.first(), result.mediaType) },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("상위 후보 기록 저장")
                }
                saveMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                result.candidates.forEachIndexed { index, candidate ->
                    if (index > 0) HorizontalDivider()
                    IdentificationCandidateRow(candidate, onCandidateSelected)
                }
            }
        }
    }
}

@Composable
private fun IdentificationCandidateRow(
    candidate: IdentificationCandidateDto,
    onCandidateSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCandidateSelected(candidate.speciesId) }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(candidate.commonName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(
                candidate.scientificName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            "${(candidate.confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = confidenceColor(candidate.confidence),
        )
    }
}

@Composable
private fun IdentifyIdlePanel(message: String) {
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
private fun IdentifyLoadingPanel() {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp), color = Forest)
            Text("불러오는 중", color = MaterialTheme.colorScheme.onSurfaceVariant)
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




