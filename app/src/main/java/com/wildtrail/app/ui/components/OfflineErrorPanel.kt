package com.wildtrail.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wildtrail.app.ui.theme.Forest

@Composable
fun OfflineErrorPanel(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    guidance: String = DEFAULT_OFFLINE_GUIDANCE,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionTestTag: String? = null,
    isActionEnabled: Boolean = true,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        color = Color(0xFFFFF3E0),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF7A4B00))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7A4B00),
            )
            Text(
                text = guidance,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7A4B00),
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    enabled = isActionEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .then(if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

private const val DEFAULT_OFFLINE_GUIDANCE =
    "API 서버 주소와 네트워크 연결을 확인한 뒤 다시 시도하세요. " +
        "에뮬레이터는 10.0.2.2, ADB reverse는 127.0.0.1, 실기기는 PC LAN IP를 사용합니다."
