package com.wildtrail.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Forest = Color(0xFF24523D)
val Moss = Color(0xFF6D8B4D)
val Reed = Color(0xFFF7F5EF)
val Ink = Color(0xFF1C2420)
val Clay = Color(0xFFB05D3B)

private val LightColors: ColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    secondary = Moss,
    onSecondary = Color.White,
    tertiary = Clay,
    background = Reed,
    onBackground = Ink,
    surface = Color(0xFFFFFCF6),
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8E2D5),
    onSurfaceVariant = Color(0xFF4F5C54),
    outline = Color(0xFFC7BFAF),
)

@Composable
fun WildTrailTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}



