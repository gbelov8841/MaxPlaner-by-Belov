package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerProgressIndicator
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun AnalyticsV2Screen(store: PlannerStore) {
    val today = LocalDate.now()
    val todayKey = today.toString()

    val totalTasks = store.tasks.size
    val completedTasks = store.tasks.count { it.completed }
    val taskProgress = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks

    val scheduledToday = store.habits.filter { it.schedule.isScheduled(today) }
    val completedScheduledToday = scheduledToday.count { todayKey in it.completedDates }
    val todayHabitProgress = if (scheduledToday.isEmpty()) 0f else completedScheduledToday.toFloat() / scheduledToday.size

    val weeklyRates = store.habits.map { store.habitCompletionRate(it, 7) }
    val weeklyHabitRate = if (weeklyRates.isEmpty()) 0 else weeklyRates.average().roundToInt()
    val bestActiveStreak = store.habits.maxOfOrNull { store.streak(it) } ?: 0

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = LocalStyleTokens.current.sectionSpacing),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)) {
                Column {
                    Text("Прогресс", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Полезные цифры без давления",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ProgressSummaryCard(
                    title = "Задачи",
                    subtitle = if (totalTasks == 0) "Пока нет задач" else "$completedTasks из $totalTasks выполнено",
                    progress = taskProgress
                )

                ProgressSummaryCard(
                    title = "Привычки сегодня",
                    subtitle = if (scheduledToday.isEmpty()) {
                        "Сегодня нет запланированных привычек"
                    } else {
                        "$completedScheduledToday из ${scheduledToday.size} выполнено"
                    },
                    progress = todayHabitProgress
                )

                ProgressSummaryCard(
                    title = "Ритм привычек · 7 дней",
                    subtitle = if (store.habits.isEmpty()) "Добавь первую привычку" else "$weeklyHabitRate% по запланированным дням",
                    progress = weeklyHabitRate / 100f
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
                ) {
                    MetricCard(store.focusMinutes.toString(), "минут фокуса", Modifier.weight(1f))
                    MetricCard(bestActiveStreak.toString(), "лучшая активная серия", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ProgressSummaryCard(title: String, subtitle: String, progress: Float) {
    PlannerCard(shape = LocalStyleTokens.current.cardShape) {
        Column(
            Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding),
            verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PlannerProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    PlannerCard(modifier = modifier, shape = LocalStyleTokens.current.cardShape) {
        Column(
            Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding),
            verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
