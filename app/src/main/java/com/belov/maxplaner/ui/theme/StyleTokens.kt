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
    val navigationElevation: Dp
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
    val shapes = shapesFor(styleId)
    return when (styleId) {
        "ultra_minimal" -> StyleTokens(
            shapes = shapes,
            cardElevation = 0.dp,
            heroElevation = 0.dp,
            borderWidth = .5.dp,
            borderAlpha = .65f,
            accentBorders = false,
            heroHighlightAlpha = 0f,
            insetSurfaceAlpha = 1f,
            cardPadding = 12.dp,
            screenPadding = 20.dp,
            sectionSpacing = 10.dp,
            progressHeight = 3.dp,
            motionDurationMillis = 140,
            navigationElevation = 0.dp
        )
        "luxe_dark" -> StyleTokens(
            shapes = shapes,
            cardElevation = 3.dp,
            heroElevation = 6.dp,
            borderWidth = .5.dp,
            borderAlpha = .65f,
            accentBorders = false,
            heroHighlightAlpha = .035f,
            insetSurfaceAlpha = .85f,
            cardPadding = 18.dp,
            screenPadding = 20.dp,
            sectionSpacing = 14.dp,
            progressHeight = 5.dp,
            motionDurationMillis = 240,
            navigationElevation = 3.dp
        )
        "midnight_neon" -> StyleTokens(
            shapes = shapes,
            cardElevation = 0.dp,
            heroElevation = 2.dp,
            borderWidth = 1.dp,
            borderAlpha = .25f,
            accentBorders = true,
            heroHighlightAlpha = .06f,
            insetSurfaceAlpha = .9f,
            cardPadding = 14.dp,
            screenPadding = 18.dp,
            sectionSpacing = 12.dp,
            progressHeight = 4.dp,
            motionDurationMillis = 170,
            navigationElevation = 0.dp
        )
        "rose_premium" -> StyleTokens(
            shapes = shapes,
            cardElevation = 1.dp,
            heroElevation = 3.dp,
            borderWidth = .5.dp,
            borderAlpha = .35f,
            accentBorders = false,
            heroHighlightAlpha = .1f,
            insetSurfaceAlpha = .75f,
            cardPadding = 20.dp,
            screenPadding = 20.dp,
            sectionSpacing = 18.dp,
            progressHeight = 8.dp,
            motionDurationMillis = 280,
            navigationElevation = 1.dp
        )
        "pure_white" -> StyleTokens(
            shapes = shapes,
            cardElevation = 0.dp,
            heroElevation = 0.dp,
            borderWidth = .5.dp,
            borderAlpha = 1f,
            accentBorders = false,
            heroHighlightAlpha = 0f,
            insetSurfaceAlpha = 1f,
            cardPadding = 20.dp,
            screenPadding = 20.dp,
            sectionSpacing = 18.dp,
            progressHeight = 2.dp,
            motionDurationMillis = 140,
            navigationElevation = 0.dp
        )
        else -> StyleTokens(
            shapes = shapes,
            cardElevation = 2.dp,
            heroElevation = 5.dp,
            borderWidth = .5.dp,
            borderAlpha = .75f,
            accentBorders = false,
            heroHighlightAlpha = .09f,
            insetSurfaceAlpha = .55f,
            cardPadding = 18.dp,
            screenPadding = 18.dp,
            sectionSpacing = 14.dp,
            progressHeight = 6.dp,
            motionDurationMillis = 220,
            navigationElevation = 2.dp
        )
    }
}

val LocalStyleTokens = staticCompositionLocalOf { styleTokensFor("executive_glass") }

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

