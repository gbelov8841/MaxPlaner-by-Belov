package com.belov.maxplaner.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeMigrationTest {
    @Test fun explicitSelectionWinsOverLegacyPreferences() {
        assertEquals("warm_style", resolveThemeId("warm_style", "pure_white", "arctic_white"))
    }
    @Test fun unknownStoredPackFallsBackWithoutReadingLegacy() {
        assertEquals("clean_minimal", resolveThemeId("removed_pack", "pure_white", null))
    }
    @Test fun oldLightPaletteStaysLightInsideDarkStyle() {
        assertEquals("light_glass", resolveThemeId(null, "executive_glass", "light_minimal"))
    }
    @Test fun legacyStylesAndPalettesHaveDeterministicMappings() {
        assertEquals("neon_accent", resolveThemeId(null, "midnight_neon", "cyber_teal"))
        assertEquals("dark_future", resolveThemeId(null, "executive_glass", "midnight_blue"))
        assertEquals("warm_style", resolveThemeId(null, "luxe_dark", "royal_gold"))
        assertEquals("clean_minimal", resolveThemeId(null, null, null))
    }
}
