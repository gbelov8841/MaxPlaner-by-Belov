package com.belov.maxplaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.belov.maxplaner.ui.MaxPlanerApp
import com.belov.maxplaner.ui.theme.MaxPlanerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaxPlanerTheme {
                MaxPlanerApp()
            }
        }
    }
}
