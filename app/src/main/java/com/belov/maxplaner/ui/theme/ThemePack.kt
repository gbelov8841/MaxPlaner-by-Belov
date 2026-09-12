package com.belov.maxplaner.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.R

@Immutable
data class ArtworkTokens(val resource: Int, val topScrim: Float, val middleScrim: Float, val bottomScrim: Float, val focalX: Float = .5f, val focalY: Float = .23f)

@Immutable
data class SurfaceTokens(val color: Color, val opacity: Float, val border: Color, val borderAlpha: Float,
    val modal: Color, val cardRadius: Dp = 12.dp, val compactRadius: Dp = 10.dp,
    val elevation: Dp = 0.dp, val glowAlpha: Float = 0f, val glowRadius: Dp = 8.dp,
    val backdropBlur: Dp = 0.dp)

@Immutable
data class StateTokens(val selectionAlpha: Float = .16f, val pressedAlpha: Float = .08f, val disabledAlpha: Float = .48f)

@Immutable
data class ThemePack(val id: String, val name: String, val description: String, val dark: Boolean,
    val background: Color, val text: Color, val secondaryText: Color, val accent: Color,
    val secondaryAccent: Color, val onAccent: Color, val artwork: ArtworkTokens,
    val surfaces: SurfaceTokens, val states: StateTokens = StateTokens(),
    val headingSerif: Boolean = false, val navigationHeight: Dp = 56.dp, val addSize: Dp = 40.dp,
    val progressHeight: Dp = 3.dp, val chartTask: Color = accent,
    val chartHabit: Color = secondaryAccent, val chartFocus: Color = secondaryText)

val ThemePacks = listOf(
    ThemePack("clean_minimal", "Clean Minimal", "Холодный пейзаж · спокойное стекло", true,
        Color(0xFF0B131C), Color(0xFFF2F5F7), Color(0xFFB8C4CF), Color(0xFFCABB9E), Color(0xFF93AFC7), Color(0xFF18202A),
        ArtworkTokens(R.drawable.theme_clean_minimal, .14f, .40f, .88f),
        SurfaceTokens(Color(0xFF182532), .78f, Color(0xFFD8E6F2), .16f, Color(0xFF17212C), glowAlpha = .03f)),
    ThemePack("light_glass", "Light Glass", "Жемчужный свет · матовое стекло", false,
        Color(0xFFEDF0F0), Color(0xFF202C36), Color(0xFF526371), Color(0xFF49687C), Color(0xFF647F90), Color.White,
        ArtworkTokens(R.drawable.theme_light_glass, .12f, .28f, .78f, .7f, .3f),
        SurfaceTokens(Color.White, .86f, Color(0xFF6D8393), .24f, Color(0xFFFAFBFB)),
        StateTokens(.13f, .06f)),
    ThemePack("dark_future", "Dark Future", "Ночное небо · холодное свечение", true,
        Color(0xFF070F1D), Color(0xFFF1F5FC), Color(0xFFB0C1D7), Color(0xFF81B9F4), Color(0xFF78D4E5), Color(0xFF0B1728),
        ArtworkTokens(R.drawable.theme_dark_future, .74f, .62f, .90f, .7f, .16f),
        SurfaceTokens(Color(0xFF111E32), .80f, Color(0xFFBAD9FF), .18f, Color(0xFF111C2C), glowAlpha = .09f, glowRadius = 12.dp), StateTokens(.18f)),
    ThemePack("warm_style", "Warm Style", "Тёплый пейзаж · дымчатая бронза", true,
        Color(0xFF1B130F), Color(0xFFF8F1E8), Color(0xFFD3C1B1), Color(0xFFD7B58C), Color(0xFFBF9472), Color(0xFF251B13),
        ArtworkTokens(R.drawable.theme_warm_style, .72f, .62f, .90f),
        SurfaceTokens(Color(0xFF34261F), .82f, Color(0xFFE9D5BB), .19f, Color(0xFF2B211C), 16.dp, 11.dp, 0.dp, .04f), StateTokens(.19f), headingSerif = true),
    ThemePack("neon_accent", "Neon Accent", "Глубокий фиолетовый · световые линии", true,
        Color(0xFF0C0B18), Color(0xFFF5F2FF), Color(0xFFC3BBD8), Color(0xFFB29BFF), Color(0xFF82B1FF), Color(0xFF19102E),
        ArtworkTokens(R.drawable.theme_neon_accent, .22f, .50f, .92f, .65f, .25f),
        SurfaceTokens(Color(0xFF18132C), .84f, Color(0xFFC4B4FF), .20f, Color(0xFF191427), glowAlpha = .12f, glowRadius = 14.dp), StateTokens(.20f))
)

val LocalThemePack = staticCompositionLocalOf { ThemePacks.first() }
