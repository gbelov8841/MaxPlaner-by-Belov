package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun Placeholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable fun CalendarScreen() = Placeholder("Календарь", "День, неделя и месяц с удобным time-blocking.")
@Composable fun TasksScreen() = Placeholder("Задачи", "Быстрый ввод, приоритеты, проекты и умные представления.")
@Composable fun HabitsScreen() = Placeholder("Привычки", "Серии, гибкие цели и спокойная статистика без давления.")
@Composable fun AnalyticsScreen() = Placeholder("Прогресс", "Тренды, серии, фокус-время и достижения без визуального шума.")
