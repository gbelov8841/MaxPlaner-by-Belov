package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerProgressIndicator
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import kotlin.math.roundToInt

@Composable
fun AnalyticsV2Screen(store: PlannerStore, onOpenTasks: () -> Unit = {}, onOpenHabits: () -> Unit = {}, onAdd: () -> Unit = {}) {
    val summary = progressOverview(store.tasks, store.habits, store.trackers, store.today)
    val today = summary.today
    val tokens = LocalStyleTokens.current
    val bestStreak = store.habits.maxOfOrNull { bestScheduledHabitStreak(it.completedDates, it.schedule) } ?: 0
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = tokens.sectionSpacing),
        verticalArrangement = Arrangement.spacedBy(tokens.sectionSpacing)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Прогресс", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
                Text("Полезные цифры без давления", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            PlannerCard(hero = true, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(if (LocalDensity.current.fontScale > 1.2f) 16.dp else 12.dp)) {
                    Text("Сегодня выполнено", style = MaterialTheme.typography.titleMedium)
                    if (today.total > 0) {
                        Text("${today.done} из ${today.total}", style = MaterialTheme.typography.headlineLarge)
                        Text("${(today.fraction * 100).roundToInt()}% плана дня", color = MaterialTheme.colorScheme.onTertiaryContainer)
                        PlannerProgressIndicator(progress = { today.fraction }, modifier = Modifier.fillMaxWidth())
                        Text(if (today.done == today.total) "Всё запланированное выполнено" else "Каждое выполненное дело приближает к цели", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("На сегодня нет запланированных дел", style = MaterialTheme.typography.titleLarge)
                        Text("Добавь одно дело — здесь появится его результат.", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onAdd, modifier = Modifier.heightIn(min = 48.dp)) { Text("Добавить дело") }
                    }
                }
            }
        }
        if (today.total > 0 || summary.measurementsPlanned > 0) item { Text("Ключевые показатели", style = MaterialTheme.typography.titleLarge) }
        if (summary.tasks.total > 0) item { ProgressSummaryCard("Дела сегодня", summary.tasks, "Открыть дела", onOpenTasks) }
        if (summary.habits.total > 0) item { ProgressSummaryCard("Привычки сегодня", summary.habits, "Открыть привычки", onOpenHabits) }
        if (summary.actions.total > 0) item { ProgressSummaryCard("Полезные действия сегодня", summary.actions, "Открыть действия", onOpenTasks) }
        if (summary.measurementsPlanned > 0) item {
            PlannerCard {
                Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Мои показатели", style = MaterialTheme.typography.titleLarge)
                    Text("Записано ${summary.measurementsRecorded} из ${summary.measurementsPlanned}")
                    Text("Это число записей, а не оценка достижения целей или соблюдения лимитов.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onOpenTasks, modifier = Modifier.heightIn(min = 48.dp)) { Text("Открыть показатели") }
                }
            }
        }
        if (store.focusMinutes > 0 || bestStreak > 0) item {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val stack = maxWidth < 360.dp || LocalDensity.current.fontScale > 1.2f
                if (stack) Column(verticalArrangement = Arrangement.spacedBy(tokens.sectionSpacing)) {
                    if (store.focusMinutes > 0) MetricCard(store.focusMinutes.toString(), "Минут фокуса за всё время")
                    if (bestStreak > 0) MetricCard(bestStreak.toString(), "Лучшая серия привычки за всё время")
                } else Row(horizontalArrangement = Arrangement.spacedBy(tokens.sectionSpacing)) {
                    if (store.focusMinutes > 0) MetricCard(store.focusMinutes.toString(), "Минут фокуса за всё время", Modifier.weight(1f))
                    if (bestStreak > 0) MetricCard(bestStreak.toString(), "Лучшая серия привычки за всё время", Modifier.weight(1f))
                }
            }
        }
        if (summary.weeklyRhythm.total > 0) item {
            PlannerCard {
                Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ритм · последние 7 дней", style = MaterialTheme.typography.titleLarge)
                    Text("${summary.weeklyRhythm.done} из ${summary.weeklyRhythm.total} запланированных повторений выполнено")
                    PlannerProgressIndicator(progress = { summary.weeklyRhythm.fraction }, modifier = Modifier.fillMaxWidth())
                    Text("Привычки и повторяющиеся действия по текущему расписанию, включая сегодня. Измерения и лимиты считаются отдельно.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProgressSummaryCard(title: String, summary: CompletionSummary, action: String, onOpen: () -> Unit) {
    PlannerCard {
        Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("${summary.done} из ${summary.total} выполнено", color = MaterialTheme.colorScheme.onSurfaceVariant)
            PlannerProgressIndicator(progress = { summary.fraction }, modifier = Modifier.fillMaxWidth())
            TextButton(onClick = onOpen, modifier = Modifier.heightIn(min = 48.dp)) { Text(action) }
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    PlannerCard(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
