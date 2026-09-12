package com.belov.maxplaner.ui.theme

/** Pure migration; no writes and no dependency on Android/Compose. */
fun resolveThemeId(stored: String?, legacyStyle: String?, legacyPalette: String?): String {
    val ids = setOf("clean_minimal", "light_glass", "dark_future", "warm_style", "neon_accent")
    if (stored != null) return stored.takeIf { it in ids } ?: "clean_minimal"
    if (legacyStyle in setOf("ultra_minimal", "rose_premium", "pure_white") || legacyPalette in setOf("light_minimal", "arctic_white", "sand_beige", "rose_light", "pure_bw", "champagne", "blush_pink", "pearl_white", "sakura_bloom", "ivory_cream", "soft_rose", "pure_white")) return "light_glass"
    if (legacyStyle == "midnight_neon") return "neon_accent"
    if (legacyPalette in setOf("sunset_orange", "royal_gold", "rose_gold", "amber_glow")) return "warm_style"
    if (legacyPalette == "midnight_blue") return "dark_future"
    return "clean_minimal"
}
