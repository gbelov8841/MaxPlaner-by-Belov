package com.belov.maxplaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.belov.maxplaner.ui.MaxPlanerApp
import com.belov.maxplaner.ui.theme.AppearanceStore
import com.belov.maxplaner.ui.theme.MaxPlanerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearance = remember { AppearanceStore(applicationContext) }
            val dark = appearance.theme.dark
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
                if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
            }
            MaxPlanerTheme(appearance) {
                MaxPlanerApp(appearance)
            }
        }
    }
}
