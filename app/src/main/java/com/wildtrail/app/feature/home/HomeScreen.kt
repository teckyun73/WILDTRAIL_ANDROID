package com.wildtrail.app.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wildtrail.app.ui.theme.Forest
import com.wildtrail.app.ui.theme.Ink
import com.wildtrail.app.ui.theme.Moss

@Composable
fun HomeScreen(
    onOpenIdentify: () -> Unit,
    onOpenSpecies: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenTrips: () -> Unit,
    onOpenRecords: () -> Unit,
) {
    val identify = HomeAction("식별", HomeIconType.Camera, Forest, onOpenIdentify)
    val species = HomeAction("도감", HomeIconType.Book, Color(0xFF2F607C), onOpenSpecies)
    val map = HomeAction("지도", HomeIconType.Map, Moss, onOpenMap)
    val trips = HomeAction("여행", HomeIconType.Calendar, Color(0xFF3B6F92), onOpenTrips)
    val records = HomeAction("기록", HomeIconType.Note, Color(0xFF806A45), onOpenRecords)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F7F1))
                .testTag("home-screen"),
    ) {
        HomeBackdrop()
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(14.dp))
            LeafMark()
            Text(
                "WildTrail",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Forest,
                textAlign = TextAlign.Center,
            )
            Text(
                "관찰을 시작할 섹터를 선택하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            HomeActionRow(identify, species)
            Spacer(Modifier.height(14.dp))
            HomeActionRow(map, trips)
            Spacer(Modifier.height(14.dp))
            HomeActionTile(
                action = records,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(136.dp),
                useAspectRatio = false,
            )
        }
        TrailWalker(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
        )
    }
}

@Immutable
private data class HomeAction(
    val label: String,
    val iconType: HomeIconType,
    val accent: Color,
    val onClick: () -> Unit,
)

private enum class HomeIconType {
    Camera,
    Book,
    Map,
    Calendar,
    Note,
}

@Composable
private fun HomeActionRow(
    left: HomeAction,
    right: HomeAction,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HomeActionTile(left, Modifier.weight(1f))
        HomeActionTile(right, Modifier.weight(1f))
    }
}

@Composable
private fun HomeActionTile(
    action: HomeAction,
    modifier: Modifier = Modifier,
    useAspectRatio: Boolean = true,
) {
    val tileModifier =
        if (useAspectRatio) {
            modifier.aspectRatio(0.92f)
        } else {
            modifier
        }
    Surface(
        modifier =
            tileModifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = action.onClick)
                .testTag("home-action-${action.label}"),
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(action.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                HomeIcon(action.iconType, action.accent)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                action.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Ink,
                maxLines = 1,
            )
            Spacer(Modifier.height(7.dp))
            Box(
                modifier =
                    Modifier
                        .width(28.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(action.accent.copy(alpha = 0.75f)),
            )
        }
    }
}

@Composable
private fun HomeIcon(
    iconType: HomeIconType,
    accent: Color,
) {
    Canvas(modifier = Modifier.size(76.dp)) {
        when (iconType) {
            HomeIconType.Camera -> drawCameraIcon(accent)
            HomeIconType.Book -> drawBookIcon(accent)
            HomeIconType.Map -> drawMapIcon(accent)
            HomeIconType.Calendar -> drawCalendarIcon(accent)
            HomeIconType.Note -> drawNoteIcon(accent)
        }
    }
}

private fun DrawScope.drawCameraIcon(accent: Color) {
    drawCircle(accent.copy(alpha = 0.20f), radius = size.minDimension * 0.46f)
    drawRoundRect(
        color = accent,
        topLeft = Offset(size.width * 0.18f, size.height * 0.34f),
        size = Size(size.width * 0.64f, size.height * 0.40f),
        cornerRadius = CornerRadius(8.dp.toPx()),
    )
    drawRoundRect(
        color = accent.copy(alpha = 0.85f),
        topLeft = Offset(size.width * 0.30f, size.height * 0.26f),
        size = Size(size.width * 0.25f, size.height * 0.12f),
        cornerRadius = CornerRadius(4.dp.toPx()),
    )
    drawCircle(Color.White.copy(alpha = 0.90f), radius = size.minDimension * 0.15f, center = center)
    drawCircle(accent.copy(alpha = 0.80f), radius = size.minDimension * 0.09f, center = center)
}

private fun DrawScope.drawBookIcon(accent: Color) {
    val stroke = Stroke(width = 5.dp.toPx())
    drawLine(
        accent,
        Offset(size.width * 0.50f, size.height * 0.24f),
        Offset(size.width * 0.50f, size.height * 0.76f),
        stroke.width,
    )
    drawRoundRect(
        accent.copy(alpha = 0.18f),
        Offset(size.width * 0.12f, size.height * 0.28f),
        Size(size.width * 0.36f, size.height * 0.45f),
        CornerRadius(8.dp.toPx()),
    )
    drawRoundRect(
        accent.copy(alpha = 0.18f),
        Offset(size.width * 0.52f, size.height * 0.28f),
        Size(size.width * 0.36f, size.height * 0.45f),
        CornerRadius(8.dp.toPx()),
    )
    drawArc(
        accent,
        180f,
        100f,
        false,
        Offset(size.width * 0.10f, size.height * 0.24f),
        Size(size.width * 0.40f, size.height * 0.55f),
        style = stroke,
    )
    drawArc(
        accent,
        260f,
        100f,
        false,
        Offset(size.width * 0.50f, size.height * 0.24f),
        Size(size.width * 0.40f, size.height * 0.55f),
        style = stroke,
    )
    drawCircle(Color.White, radius = size.minDimension * 0.18f, center = Offset(size.width * 0.75f, size.height * 0.23f))
    drawCircle(
        accent,
        radius = size.minDimension * 0.16f,
        center = Offset(size.width * 0.75f, size.height * 0.23f),
        style = Stroke(3.dp.toPx()),
    )
}

private fun DrawScope.drawMapIcon(accent: Color) {
    val stroke = Stroke(width = 4.dp.toPx())
    val mapPath =
        Path().apply {
            moveTo(size.width * 0.12f, size.height * 0.70f)
            lineTo(size.width * 0.35f, size.height * 0.54f)
            lineTo(size.width * 0.62f, size.height * 0.68f)
            lineTo(size.width * 0.88f, size.height * 0.50f)
            lineTo(size.width * 0.88f, size.height * 0.82f)
            lineTo(size.width * 0.12f, size.height * 0.82f)
            close()
        }
    drawPath(mapPath, accent.copy(alpha = 0.14f))
    drawPath(mapPath, accent, style = stroke)
    drawCircle(accent, radius = size.minDimension * 0.17f, center = Offset(size.width * 0.55f, size.height * 0.28f))
    drawCircle(Color.White, radius = size.minDimension * 0.07f, center = Offset(size.width * 0.55f, size.height * 0.28f))
    drawLine(
        accent,
        Offset(size.width * 0.55f, size.height * 0.45f),
        Offset(size.width * 0.45f, size.height * 0.60f),
        stroke.width,
    )
}

private fun DrawScope.drawCalendarIcon(accent: Color) {
    drawRoundRect(
        accent.copy(alpha = 0.16f),
        Offset(size.width * 0.18f, size.height * 0.20f),
        Size(size.width * 0.64f, size.height * 0.62f),
        CornerRadius(8.dp.toPx()),
    )
    drawRoundRect(
        accent,
        Offset(size.width * 0.18f, size.height * 0.20f),
        Size(size.width * 0.64f, size.height * 0.62f),
        CornerRadius(8.dp.toPx()),
        style = Stroke(4.dp.toPx()),
    )
    drawLine(accent, Offset(size.width * 0.18f, size.height * 0.36f), Offset(size.width * 0.82f, size.height * 0.36f), 4.dp.toPx())
    listOf(0.34f, 0.50f, 0.66f).forEach { x ->
        listOf(0.48f, 0.62f, 0.74f).forEach { y ->
            drawRect(accent.copy(alpha = 0.65f), Offset(size.width * x, size.height * y), Size(7.dp.toPx(), 7.dp.toPx()))
        }
    }
}

private fun DrawScope.drawNoteIcon(accent: Color) {
    val stroke = Stroke(width = 4.dp.toPx())
    drawRoundRect(
        accent.copy(alpha = 0.16f),
        Offset(size.width * 0.22f, size.height * 0.16f),
        Size(size.width * 0.56f, size.height * 0.70f),
        CornerRadius(7.dp.toPx()),
    )
    drawRoundRect(
        accent,
        Offset(size.width * 0.22f, size.height * 0.16f),
        Size(size.width * 0.56f, size.height * 0.70f),
        CornerRadius(7.dp.toPx()),
        style = stroke,
    )
    repeat(4) { index ->
        val y = size.height * (0.34f + index * 0.12f)
        drawLine(accent, Offset(size.width * 0.34f, y), Offset(size.width * 0.68f, y), stroke.width)
    }
}

@Composable
private fun HomeBackdrop() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                colors =
                    listOf(
                        Color(0xFFF9FCFA),
                        Color(0xFFE7F1EB),
                        Color(0xFFD4E5D9),
                    ),
            ),
        )
        drawCircle(Color.White.copy(alpha = 0.70f), radius = size.width * 0.38f, center = Offset(size.width * 0.50f, size.height * 0.12f))
        drawMountainLayer(0.32f, Color(0xFFDCE9DE))
        drawMountainLayer(0.39f, Color(0xFFC9DEC9))
        drawMountainLayer(0.86f, Color(0xFF7FA181))
        drawHillForeground()
        drawBirds()
    }
}

private fun DrawScope.drawMountainLayer(
    baseline: Float,
    color: Color,
) {
    val path =
        Path().apply {
            moveTo(0f, size.height * baseline)
            lineTo(size.width * 0.22f, size.height * (baseline - 0.08f))
            lineTo(size.width * 0.48f, size.height * (baseline + 0.03f))
            lineTo(size.width * 0.78f, size.height * (baseline - 0.10f))
            lineTo(size.width, size.height * baseline)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    drawPath(path, color.copy(alpha = 0.78f))
}

private fun DrawScope.drawHillForeground() {
    val path =
        Path().apply {
            moveTo(0f, size.height * 0.88f)
            cubicTo(size.width * 0.25f, size.height * 0.80f, size.width * 0.46f, size.height * 0.92f, size.width, size.height * 0.78f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    drawPath(path, Forest.copy(alpha = 0.88f))
    val trail =
        Path().apply {
            moveTo(size.width * 0.58f, size.height)
            cubicTo(
                size.width * 0.50f,
                size.height * 0.94f,
                size.width * 0.65f,
                size.height * 0.88f,
                size.width * 0.55f,
                size.height * 0.82f,
            )
            cubicTo(
                size.width * 0.50f,
                size.height * 0.78f,
                size.width * 0.56f,
                size.height * 0.75f,
                size.width * 0.62f,
                size.height * 0.72f,
            )
        }
    drawPath(trail, Color(0xFFDDE8D6), style = Stroke(16.dp.toPx()))
}

private fun DrawScope.drawBirds() {
    val stroke = Stroke(width = 2.dp.toPx())
    listOf(
        Offset(size.width * 0.82f, size.height * 0.11f),
        Offset(size.width * 0.88f, size.height * 0.08f),
    ).forEach { bird ->
        drawArc(
            Forest.copy(alpha = 0.18f),
            205f,
            70f,
            false,
            Offset(bird.x - 13.dp.toPx(), bird.y),
            Size(18.dp.toPx(), 10.dp.toPx()),
            style = stroke,
        )
        drawArc(
            Forest.copy(alpha = 0.18f),
            265f,
            70f,
            false,
            Offset(bird.x + 2.dp.toPx(), bird.y),
            Size(18.dp.toPx(), 10.dp.toPx()),
            style = stroke,
        )
    }
}

@Composable
private fun LeafMark() {
    Canvas(modifier = Modifier.size(42.dp)) {
        drawLine(Forest, Offset(size.width * 0.50f, size.height * 0.92f), Offset(size.width * 0.50f, size.height * 0.24f), 3.dp.toPx())
        listOf(
            Offset(size.width * 0.33f, size.height * 0.34f),
            Offset(size.width * 0.67f, size.height * 0.38f),
            Offset(size.width * 0.34f, size.height * 0.58f),
            Offset(size.width * 0.66f, size.height * 0.62f),
        ).forEachIndexed { index, offset ->
            drawOval(
                Forest.copy(alpha = if (index < 2) 0.82f else 0.65f),
                topLeft = Offset(offset.x - 8.dp.toPx(), offset.y - 5.dp.toPx()),
                size = Size(16.dp.toPx(), 11.dp.toPx()),
            )
        }
    }
}

@Composable
private fun TrailWalker(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(34.dp, 58.dp)) {
        val walker = Ink.copy(alpha = 0.88f)
        drawCircle(walker, radius = 4.dp.toPx(), center = Offset(size.width * 0.52f, size.height * 0.15f))
        drawLine(walker, Offset(size.width * 0.50f, size.height * 0.22f), Offset(size.width * 0.46f, size.height * 0.55f), 3.dp.toPx())
        drawLine(walker, Offset(size.width * 0.46f, size.height * 0.35f), Offset(size.width * 0.27f, size.height * 0.45f), 3.dp.toPx())
        drawLine(walker, Offset(size.width * 0.48f, size.height * 0.55f), Offset(size.width * 0.30f, size.height * 0.88f), 3.dp.toPx())
        drawLine(walker, Offset(size.width * 0.48f, size.height * 0.55f), Offset(size.width * 0.70f, size.height * 0.88f), 3.dp.toPx())
        drawRoundRect(
            Forest,
            Offset(size.width * 0.58f, size.height * 0.25f),
            Size(10.dp.toPx(), 16.dp.toPx()),
            CornerRadius(3.dp.toPx()),
        )
    }
}
