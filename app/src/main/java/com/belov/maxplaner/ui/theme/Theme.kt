package com.belov.maxplaner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun darkScheme(p: PaletteOption) = darkColorScheme(
    primary = p.primary,
    onPrimary = Color(0xFF06110B),
    primaryContainer = p.primary.copy(alpha = 0.22f).compositeOver(p.background),
    onPrimaryContainer = Color(0xFFF4F6F4),
    secondary = p.accent,
    secondaryContainer = p.accent.copy(alpha = 0.20f).compositeOver(p.background),
    background = p.background,
    surface = p.surface,
    surfaceVariant = blend(p.surface, p.primary, 0.08f),
    onBackground = Color(0xFFF3F5F4),
    onSurface = Color(0xFFF3F5F4),
    onSurfaceVariant = Color(0xFFB8C0BC),
    outline = Color(0xFF5B6460),
    outlineVariant = Color(0xFF2B3330)
)

private fun lightScheme(p: PaletteOption) = lightColorScheme(
    primary = p.primary,
    onPrimary = Color.White,
    primaryContainer = blend(p.background, p.primary, 0.13f),
    onPrimaryContainer = Color(0xFF1A1B1C),
    secondary = p.accent,
    secondaryContainer = blend(p.background, p.accent, 0.18f),
    background = p.background,
    surface = p.surface,
    surfaceVariant = blend(p.background, Color(0xFF7B7F84), 0.08f),
    onBackground = Color(0xFF16181A),
    onSurface = Color(0xFF16181A),
    onSurfaceVariant = Color(0xFF60666B),
    outline = Color(0xFF8A9095),
    outlineVariant = Color(0xFFD9DEE2)
)

private fun blend(base: Color, tint: Color, amount: Float): Color = Color(
    red = base.red * (1f - amount) + tint.red * amount,
    green = base.green * (1f - amount) + tint.green * amount,
    blue = base.blue * (1f - amount) + tint.blue * amount,
    alpha = 1f
)

@Composable
fun MaxPlanerTheme(appearance: AppearanceStore, content: @Composable () -> Unit) {
    val palette = appearance.palette
    val tokens = remember(appearance.styleId) { styleTokensFor(appearance.styleId) }
    val typography = if (appearance.styleId == "executive_glass") FloatingGlassTypography else MaxPlanerTypography
    CompositionLocalProvider(LocalStyleTokens provides tokens) {
        MaterialTheme(
            colorScheme = if (palette.isDark) darkScheme(palette) else lightScheme(palette),
            typography = typography,
            shapes = tokens.shapes,
            content = content
        )
    }
}
