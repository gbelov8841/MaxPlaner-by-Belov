package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsV2Screen(store: PlannerStore, onOpenTasks: () -> Unit = {}, onOpenHabits: () -> Unit = {}, onAdd: () -> Unit = {}) {
    var days by rememberSaveable { mutableStateOf(7) }
    var detailDay by rememberSaveable { mutableStateOf<String?>(null) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val today = store.today
    val current = periodProgress(store.completionHistory, store.habits, store.focusByDay, today, days)
    val previous = periodProgress(store.completionHistory, store.habits, store.focusByDay, today.minusDays(days.toLong()), days)
    val daily = progressOverview(store.tasks, store.habits, store.trackers, today).today
    val dates = (days - 1 downTo 0).map { today.minusDays(it.toLong()) }
    val maxDaily = dates.maxOf { d -> periodProgress(store.completionHistory, store.habits, store.focusByDay, d, 1).let { it.taskCount + it.habitCount } }.coerceAtLeast(1)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Прогресс", style = MaterialTheme.typography.headlineLarge)
            Text("Результаты и история", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(days == 7, { days = 7 }, label = { Text("7 дней") })
                FilterChip(days == 30, { days = 30 }, label = { Text("30 дней") })
            }
        }
        item {
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
                PanelHeading("Сегодня", "Открыть дела", onOpenTasks)
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (daily.total == 0) "Пока нет плана" else "${daily.done} из ${daily.total} выполнено", style = MaterialTheme.typography.titleLarge)
                    if (daily.total > 0) PlannerProgressIndicator({ daily.fraction }, Modifier.fillMaxWidth()) else InlineAdd("Добавить дело", onAdd)
                }
            }
        }
        item {
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("За последние $days дней", style = MaterialTheme.typography.titleMedium)
                    Text("Завершено задач: ${current.taskCount}")
                    Text("Отметок привычек: ${current.habitCount}")
                    Text("Фокус: ${current.focusMinutes} мин")
                    Text("Предыдущие $days дней: ${previous.taskCount} задач · ${previous.habitCount} отметок · ${previous.focusMinutes} мин", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (current.taskCount + current.habitCount > previous.taskCount + previous.habitCount)
                        "Отметок больше, чем в предыдущем периоде." else if (current.taskCount + current.habitCount == 0)
                        "История появится после первых выполнений." else "Открой день ниже, чтобы посмотреть, что было сделано.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Text("По дням", style = MaterialTheme.typography.titleMedium) }
        items(dates.reversed(), key = { it.toString() }) { date ->
            val summary = periodProgress(store.completionHistory, store.habits, store.focusByDay, date, 1)
            PlannerSurface(modifier = Modifier.fillMaxWidth(), onClick = { detailDay = date.toString() }) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(date.format(DateTimeFormatter.ofPattern("dd.MM")), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        Text("${summary.taskCount} задач · ${summary.habitCount} привычек · ${summary.focusMinutes} мин", style = MaterialTheme.typography.labelSmall)
                    }
                    PlannerProgressIndicator({ (summary.taskCount + summary.habitCount).toFloat() / maxDaily }, Modifier.fillMaxWidth())
                }
            }
        }
        if (current.categories.isNotEmpty()) item { Text("Категории завершённых задач", style = MaterialTheme.typography.titleMedium) }
        items(current.categories.entries.toList(), key = { it.key }) { entry ->
            PlannerSurface(modifier = Modifier.fillMaxWidth(), onClick = { category = entry.key }) {
                Row(Modifier.padding(14.dp)) { Text(categoryLabel(entry.key), Modifier.weight(1f)); Text(entry.value.toString()) }
            }
        }
        item { TextButton(onClick = onOpenHabits, modifier = Modifier.fillMaxWidth()) { Text("Серии и история привычек") } }
        item { Text("История задач и фокуса записывается с этого обновления. Старые итоги сохранены, но их точные даты неизвестны. Отметки привычек учитываются по сохранённым датам.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (store.focusMinutes > 0) item { Text("Фокус за всё время: ${store.focusMinutes} мин", style = MaterialTheme.typography.bodySmall) }
    }
    if (detailDay != null || category != null) {
        val start = today.minusDays(days.toLong() - 1).toString()
        val records = store.completionHistory.filter { if (detailDay != null) it.date == detailDay else it.category == category && it.date >= start && it.date <= today.toString() }
        AlertDialog(onDismissRequest = { detailDay = null; category = null }, title = { Text(detailDay ?: categoryLabel(category.orEmpty())) },
            text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (records.isEmpty()) item { Text("Завершённых задач нет") }
                items(records, key = { it.taskId }) { Text(it.title) }
                if (detailDay != null) {
                    items(store.habits.filter { detailDay.orEmpty() in it.completedDates }, key = { "habit-${it.id}" }) { Text("Привычка · ${it.title}") }
                    item { Text("Фокус: ${store.focusByDay[detailDay.orEmpty()] ?: 0} мин") }
                }
            } }, confirmButton = { TextButton(onClick = { detailDay = null; category = null }) { Text("Закрыть") } })
    }
}
