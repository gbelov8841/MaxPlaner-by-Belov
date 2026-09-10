package com.belov.maxplaner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private fun darkScheme(p: PaletteOption) = darkColorScheme(
    primary = p.primary,
    onPrimary = Color(0xFF06110B),
    primaryContainer = p.primary.copy(alpha = 0.22f),
    onPrimaryContainer = Color(0xFFF4F6F4),
    secondary = p.accent,
    secondaryContainer = p.accent.copy(alpha = 0.20f),
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

private fun shapesFor(styleId: String): Shapes = when (styleId) {
    "ultra_minimal" -> Shapes(
        extraSmall = RoundedCornerShape(6.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(14.dp),
        extraLarge = RoundedCornerShape(18.dp)
    )
    "luxe_dark" -> Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(14.dp),
        large = RoundedCornerShape(18.dp),
        extraLarge = RoundedCornerShape(24.dp)
    )
    "midnight_neon" -> Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(22.dp),
        extraLarge = RoundedCornerShape(28.dp)
    )
    "rose_premium" -> Shapes(
        extraSmall = RoundedCornerShape(12.dp),
        small = RoundedCornerShape(16.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(26.dp),
        extraLarge = RoundedCornerShape(32.dp)
    )
    "pure_white" -> Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(10.dp),
        extraLarge = RoundedCornerShape(14.dp)
    )
    else -> Shapes(
        extraSmall = RoundedCornerShape(10.dp),
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(18.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(30.dp)
    )
}

@Composable
fun MaxPlanerTheme(appearance: AppearanceStore, content: @Composable () -> Unit) {
    val palette = appearance.palette
    MaterialTheme(
        colorScheme = if (palette.isDark) darkScheme(palette) else lightScheme(palette),
        typography = MaxPlanerTypography,
        shapes = shapesFor(appearance.styleId),
        content = content
    )
}
