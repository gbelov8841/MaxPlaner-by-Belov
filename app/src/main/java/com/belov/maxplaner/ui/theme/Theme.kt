package com.belov.maxplaner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD7E7C6),
    onPrimary = Color(0xFF19321F),
    secondary = Color(0xFFD8C3A5),
    background = Color(0xFF0F1411),
    surface = Color(0xFF171D19),
    surfaceVariant = Color(0xFF232B25),
    onBackground = Color(0xFFF4F1E8),
    onSurface = Color(0xFFF4F1E8),
    outline = Color(0xFF829084)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F5A3A),
    onPrimary = Color.White,
    secondary = Color(0xFF8A6844),
    background = Color(0xFFF8F5EE),
    surface = Color(0xFFFFFCF6),
    surfaceVariant = Color(0xFFEDE9DE),
    onBackground = Color(0xFF172019),
    onSurface = Color(0xFF172019),
    outline = Color(0xFF6F786F)
)

@Composable
fun MaxPlanerTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = MaxPlanerTypography, content = content)
}
