package com.belov.maxplaner.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

fun themeColorScheme(p: ThemePack): ColorScheme {
    val base = if (p.dark) darkColorScheme() else lightColorScheme()
    val inset = p.accent.copy(alpha = .10f).compositeOver(p.surfaces.modal)
    return base.copy(
        primary = p.accent, onPrimary = p.onAccent,
        primaryContainer = p.accent.copy(alpha = .18f).compositeOver(p.surfaces.modal), onPrimaryContainer = p.text,
        secondary = p.secondaryAccent, onSecondary = p.onAccent,
        secondaryContainer = inset, onSecondaryContainer = p.text,
        tertiary = p.secondaryAccent, onTertiary = p.onAccent, tertiaryContainer = inset, onTertiaryContainer = p.text,
        background = p.background, onBackground = p.text,
        surface = p.surfaces.modal, onSurface = p.text, surfaceVariant = inset, onSurfaceVariant = p.secondaryText,
        surfaceDim = p.surfaces.modal, surfaceBright = inset, surfaceContainerLowest = p.background,
        surfaceContainerLow = p.surfaces.modal, surfaceContainer = p.surfaces.modal,
        surfaceContainerHigh = p.surfaces.modal, surfaceContainerHighest = inset,
        surfaceTint = Color.Transparent, outline = p.secondaryText,
        outlineVariant = p.surfaces.border.copy(alpha = p.surfaces.borderAlpha).compositeOver(p.surfaces.modal),
        error = if (p.dark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A),
        onError = if (p.dark) Color(0xFF690005) else Color.White,
        errorContainer = if (p.dark) Color(0xFF4A2425) else Color(0xFFFFDAD6),
        onErrorContainer = if (p.dark) Color(0xFFFFDAD6) else Color(0xFF410002)
    )
}

@Composable
fun MaxPlanerTheme(appearance: AppearanceStore, content: @Composable () -> Unit) {
    val pack = appearance.theme
    val tokens = remember(pack.id) { styleTokensFor(pack.id) }
    CompositionLocalProvider(LocalThemePack provides pack, LocalStyleTokens provides tokens) {
        MaterialTheme(colorScheme = themeColorScheme(pack), typography = themeTypography(pack), shapes = tokens.shapes, content = content)
    }
}
