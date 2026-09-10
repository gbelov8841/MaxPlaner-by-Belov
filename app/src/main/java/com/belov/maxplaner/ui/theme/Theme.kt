package com.belov.maxplaner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF86D69A),
    onPrimary = Color(0xFF092B17),
    primaryContainer = Color(0xFF153A24),
    onPrimaryContainer = Color(0xFFD9F7DF),
    secondary = Color(0xFFD4BE98),
    secondaryContainer = Color(0xFF3A3022),
    background = Color(0xFF090E0B),
    surface = Color(0xFF101713),
    surfaceVariant = Color(0xFF18221C),
    onBackground = Color(0xFFF3F4EE),
    onSurface = Color(0xFFF3F4EE),
    onSurfaceVariant = Color(0xFFB8C3BA),
    outline = Color(0xFF536158),
    outlineVariant = Color(0xFF29352D)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF235C37),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEEDF),
    onPrimaryContainer = Color(0xFF12341F),
    secondary = Color(0xFF82633E),
    secondaryContainer = Color(0xFFF1E4CF),
    background = Color(0xFFF6F4ED),
    surface = Color(0xFFFFFDF8),
    surfaceVariant = Color(0xFFECEBE4),
    onBackground = Color(0xFF151C17),
    onSurface = Color(0xFF151C17),
    onSurfaceVariant = Color(0xFF5C665E),
    outline = Color(0xFF7B857D),
    outlineVariant = Color(0xFFD5DAD4)
)

@Composable
fun MaxPlanerTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = MaxPlanerTypography, content = content)
}
