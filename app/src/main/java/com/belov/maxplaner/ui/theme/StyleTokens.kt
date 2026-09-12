package com.belov.maxplaner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Visual decisions belong to a style; palettes and stored user data stay independent. */
@Immutable
data class StyleTokens(
    val shapes: Shapes,
    val cardElevation: Dp,
    val heroElevation: Dp,
    val borderWidth: Dp,
    val borderAlpha: Float,
    val accentBorders: Boolean,
    val heroHighlightAlpha: Float,
    val insetSurfaceAlpha: Float,
    val cardPadding: Dp,
    val screenPadding: Dp,
    val sectionSpacing: Dp,
    val progressHeight: Dp,
    val motionDurationMillis: Int,
    val navigationElevation: Dp,
    val floatingGlass: Boolean,
    val surfaceOpacity: Float,
    val pressedAlpha: Float,
    val disabledAlpha: Float
) {
    val heroShape: Shape get() = shapes.extraLarge
    val cardShape: Shape get() = shapes.large
    val compactShape: Shape get() = shapes.medium
    val iconShape: Shape get() = shapes.small
    val pillShape: Shape get() = shapes.small
    val progressShape: Shape get() = shapes.extraSmall
    val selectedBorderWidth: Dp get() = borderWidth * 2
}

fun styleTokensFor(styleId: String): StyleTokens {
    val pack = ThemePacks.firstOrNull { it.id == styleId } ?: ThemePacks.first()
    return StyleTokens(
        shapes = Shapes(RoundedCornerShape(4.dp), RoundedCornerShape(12.dp),
            RoundedCornerShape(pack.surfaces.compactRadius), RoundedCornerShape(pack.surfaces.cardRadius), RoundedCornerShape(20.dp)),
        cardElevation = pack.surfaces.elevation, heroElevation = pack.surfaces.elevation,
        borderWidth = .5.dp, borderAlpha = pack.surfaces.borderAlpha, accentBorders = false,
        heroHighlightAlpha = .025f, insetSurfaceAlpha = .9f, cardPadding = 14.dp,
        screenPadding = 16.dp, sectionSpacing = 12.dp, progressHeight = pack.progressHeight,
        motionDurationMillis = 160, navigationElevation = 0.dp, floatingGlass = true,
        surfaceOpacity = pack.surfaces.opacity, pressedAlpha = pack.states.pressedAlpha,
        disabledAlpha = pack.states.disabledAlpha
    )
}

val LocalStyleTokens = staticCompositionLocalOf { styleTokensFor("clean_minimal") }
