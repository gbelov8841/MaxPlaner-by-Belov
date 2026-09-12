package com.belov.maxplaner.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Existing preference files and legacy IDs are deliberately retained for rollback. */
class AppearanceStore(context: Context) {
    private val prefs = context.getSharedPreferences("primeplaner_appearance", Context.MODE_PRIVATE)
    var themeId by mutableStateOf(resolveThemeId(prefs.getString("theme_pack_id", null),
        prefs.getString("style_id", null), prefs.getString("palette_id", null)))
        private set
    var displayName by mutableStateOf(prefs.getString("display_name", "Максим") ?: "Максим")
        private set
    val theme: ThemePack get() = ThemePacks.firstOrNull { it.id == themeId } ?: ThemePacks.first()
    // Compatibility adapter until all old style consumers have migrated.
    val styleId: String get() = themeId
    fun selectTheme(id: String) {
        if (ThemePacks.none { it.id == id }) return
        themeId = id
        prefs.edit().putString("theme_pack_id", id).putInt("theme_pack_version", 2).apply()
    }
    fun rename(value: String) {
        displayName = value.trim().take(60)
        prefs.edit().putString("display_name", displayName).apply()
    }
}
