package com.belov.maxplaner.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class PaletteOption(
    val id: String,
    val name: String,
    val primary: Color,
    val background: Color,
    val surface: Color,
    val accent: Color,
    val isDark: Boolean
)

data class AppStyleOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val palettes: List<PaletteOption>
)

val MaxPlanerStyles = listOf(
    AppStyleOption(
        id = "executive_glass",
        name = "Executive Glass",
        subtitle = "Премиальная глубина и стеклянные акценты",
        palettes = listOf(
            PaletteOption("executive_green", "Executive Green", Color(0xFF86D69A), Color(0xFF090E0B), Color(0xFF101713), Color(0xFFD4BE98), true),
            PaletteOption("graphite_black", "Graphite Black", Color(0xFFD6D9DC), Color(0xFF08090A), Color(0xFF131517), Color(0xFF8E989F), true),
            PaletteOption("midnight_blue", "Midnight Blue", Color(0xFF5BB8FF), Color(0xFF07101B), Color(0xFF0E1A28), Color(0xFF6D78FF), true),
            PaletteOption("sunset_orange", "Sunset Orange", Color(0xFFFFA34D), Color(0xFF120B08), Color(0xFF20130E), Color(0xFFFFC078), true),
            PaletteOption("forest_nature", "Forest Nature", Color(0xFF73C982), Color(0xFF081109), Color(0xFF102016), Color(0xFFB1D48A), true),
            PaletteOption("light_minimal", "Light Minimal", Color(0xFF1B73E8), Color(0xFFF7F9FC), Color(0xFFFFFFFF), Color(0xFF5C6773), false)
        )
    ),
    AppStyleOption(
        id = "ultra_minimal",
        name = "Ultra Minimal",
        subtitle = "Воздух, чистота и минимум отвлекающего",
        palettes = listOf(
            PaletteOption("arctic_white", "Arctic White", Color(0xFF3979D8), Color(0xFFF8FAFD), Color(0xFFFFFFFF), Color(0xFF758195), false),
            PaletteOption("sand_beige", "Sand Beige", Color(0xFFA6743A), Color(0xFFFBF6EE), Color(0xFFFFFBF5), Color(0xFFC49A64), false),
            PaletteOption("rose_light", "Rose Light", Color(0xFFD45E77), Color(0xFFFFF6F8), Color(0xFFFFFFFF), Color(0xFFECA0AF), false),
            PaletteOption("pure_bw", "Pure Black & White", Color(0xFF202225), Color(0xFFFAFAFA), Color(0xFFFFFFFF), Color(0xFF777B80), false)
        )
    ),
    AppStyleOption(
        id = "luxe_dark",
        name = "Luxe Dark",
        subtitle = "Строгий тёмный премиум",
        palettes = listOf(
            PaletteOption("royal_gold", "Royal Gold", Color(0xFFE7C06A), Color(0xFF090806), Color(0xFF15110B), Color(0xFFB98B35), true),
            PaletteOption("carbon_slate", "Carbon Slate", Color(0xFFD1D6DC), Color(0xFF080A0C), Color(0xFF12161A), Color(0xFF7F8A94), true),
            PaletteOption("rose_gold", "Rose Gold", Color(0xFFE5A5A0), Color(0xFF120B0C), Color(0xFF211315), Color(0xFFC47F79), true),
            PaletteOption("teal_prestige", "Teal Prestige", Color(0xFF63D4D0), Color(0xFF071112), Color(0xFF0F2021), Color(0xFF3CA8A6), true),
            PaletteOption("pure_monochrome", "Pure Monochrome", Color(0xFFF2F2F2), Color(0xFF080808), Color(0xFF151515), Color(0xFF909090), true)
        )
    ),
    AppStyleOption(
        id = "midnight_neon",
        name = "Midnight Neon",
        subtitle = "Технологичный минимализм со свечением",
        palettes = listOf(
            PaletteOption("cyber_teal", "Cyber Teal", Color(0xFF25E7EA), Color(0xFF041013), Color(0xFF081A1E), Color(0xFF00A9B7), true),
            PaletteOption("amber_glow", "Amber Glow", Color(0xFFFF9E32), Color(0xFF120A04), Color(0xFF211108), Color(0xFFFFC06B), true),
            PaletteOption("rose_cyber", "Rose Cyber", Color(0xFFFF8BAC), Color(0xFF13080E), Color(0xFF221018), Color(0xFFD9507A), true),
            PaletteOption("pure_neon_white", "Pure Neon White", Color(0xFFFFFFFF), Color(0xFF050607), Color(0xFF101214), Color(0xFFA6B0BA), true)
        )
    ),
    AppStyleOption(
        id = "rose_premium",
        name = "Rose Premium",
        subtitle = "Мягкая светлая премиальность",
        palettes = listOf(
            PaletteOption("champagne", "Champagne", Color(0xFFC99554), Color(0xFFFFF8EF), Color(0xFFFFFFFF), Color(0xFFE2BD8F), false),
            PaletteOption("blush_pink", "Blush Pink", Color(0xFFD85F78), Color(0xFFFFF4F6), Color(0xFFFFFFFF), Color(0xFFF0A7B5), false),
            PaletteOption("pearl_white", "Pearl White", Color(0xFF7B726B), Color(0xFFFAF8F5), Color(0xFFFFFFFF), Color(0xFFC7BEB6), false),
            PaletteOption("sakura_bloom", "Sakura Bloom", Color(0xFFD86578), Color(0xFFFFF5F8), Color(0xFFFFFFFF), Color(0xFFF2B1BE), false),
            PaletteOption("ivory_cream", "Ivory Cream", Color(0xFFB88642), Color(0xFFFFFAF0), Color(0xFFFFFFFF), Color(0xFFD7B778), false)
        )
    ),
    AppStyleOption(
        id = "pure_white",
        name = "Pure White",
        subtitle = "Архитектурный белый минимализм",
        palettes = listOf(
            PaletteOption("arctic_white", "Arctic White", Color(0xFF2F7DE1), Color(0xFFF8FAFD), Color(0xFFFFFFFF), Color(0xFF91A8C7), false),
            PaletteOption("soft_rose", "Soft Rose", Color(0xFFD85D77), Color(0xFFFFF8FA), Color(0xFFFFFFFF), Color(0xFFE7A1B0), false),
            PaletteOption("pearl_white", "Pearl White", Color(0xFF727272), Color(0xFFFAFAFA), Color(0xFFFFFFFF), Color(0xFFB9B9B9), false),
            PaletteOption("pure_white", "Pure White", Color(0xFF111111), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFF6D6D6D), false)
        )
    )
)

class AppearanceStore(context: Context) {
    private val prefs = context.getSharedPreferences("maxplaner_appearance", Context.MODE_PRIVATE)

    var styleId by mutableStateOf(prefs.getString("style_id", "executive_glass") ?: "executive_glass")
        private set
    var paletteId by mutableStateOf(prefs.getString("palette_id", "executive_green") ?: "executive_green")
        private set

    val style: AppStyleOption
        get() = MaxPlanerStyles.firstOrNull { it.id == styleId } ?: MaxPlanerStyles.first()

    val palette: PaletteOption
        get() = style.palettes.firstOrNull { it.id == paletteId } ?: style.palettes.first()

    fun selectStyle(id: String) {
        val next = MaxPlanerStyles.firstOrNull { it.id == id } ?: return
        styleId = next.id
        paletteId = next.palettes.first().id
        persist()
    }

    fun selectPalette(id: String) {
        if (style.palettes.none { it.id == id }) return
        paletteId = id
        persist()
    }

    private fun persist() {
        prefs.edit().putString("style_id", styleId).putString("palette_id", paletteId).apply()
    }
}
