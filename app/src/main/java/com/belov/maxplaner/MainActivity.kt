package com.belov.maxplaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
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
                val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                // Update AndroidX's API-35 protection views as well as icon appearance.
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            MaxPlanerTheme(appearance) {
                MaxPlanerApp(appearance)
            }
        }
    }
}
