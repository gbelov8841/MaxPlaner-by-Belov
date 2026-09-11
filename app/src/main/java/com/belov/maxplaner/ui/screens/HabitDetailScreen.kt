package com.belov.maxplaner.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.components.*
import java.time.LocalDate

@Composable
internal fun HabitDetailScreen(store: PlannerStore, habit: Habit, onBack: () -> Unit) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    var selectedDay by rememberSaveable { mutableStateOf(store.today.toString()) }
    val day = LocalDate.parse(selectedDay)
    BackHandler(enabled = !editing && !deleting, onBack = onBack)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Назад") }
                Text(habit.title, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = { editing = true }) { Icon(Icons.Rounded.Edit, "Изменить привычку") }
            }
        }
        item {
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Серия · ${store.streak(habit)}", style = MaterialTheme.typography.titleLarge)
                    Text("Лучшая серия · ${bestScheduledHabitStreak(habit.completedDates, habit.schedule)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Цель — выполнить в каждый выбранный день", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { editing = true }) { Text("Изменить цель и расписание") }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedDay = day.minusMonths(1).toString() }) { Icon(Icons.Rounded.ChevronLeft, "Предыдущий месяц") }
                Text("История выполнения", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { selectedDay = day.plusMonths(1).toString() }) { Icon(Icons.Rounded.ChevronRight, "Следующий месяц") }
            }
            PlannerMonth(day, store.today, count = { if (it.toString() in habit.completedDates) 1 else 0 }) { selectedDay = it.toString() }
        }
        item {
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(day.toString(), style = MaterialTheme.typography.titleMedium)
                    val scheduled = habit.schedule.isScheduled(day)
                    Text(if (!scheduled) "День отдыха" else if (selectedDay in habit.completedDates) "Выполнено" else "Не отмечено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (scheduled && day <= store.today) Button(onClick = { store.toggleHabitOn(habit.id, day) }) {
                        Text(if (selectedDay in habit.completedDates) "Снять отметку" else "Отметить выполнение")
                    }
                    if (day > store.today) Text("Будущие дни нельзя отметить заранее", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Text("По текущему расписанию: ${store.habitCompletionRate(habit, 7)}% за 7 дней · ${store.habitCompletionRate(habit, 30)}% за 30 дней",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { deleting = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Удалить привычку") }
        }
    }
    if (editing) HabitScheduleDialog("Изменить привычку", habit.schedule, true, { editing = false }, habit.startMinutes, habit.durationMinutes, habit.title) { name, schedule, start, duration ->
        store.updateHabit(habit.id, name, schedule, start, duration); editing = false
    }
    if (deleting) AlertDialog(onDismissRequest = { deleting = false }, title = { Text("Удалить привычку?") },
        text = { Text("История «${habit.title}» тоже будет удалена.") },
        confirmButton = { TextButton(onClick = { store.deleteHabit(habit.id); onBack() }) { Text("Удалить") } },
        dismissButton = { TextButton(onClick = { deleting = false }) { Text("Отмена") } })
}
