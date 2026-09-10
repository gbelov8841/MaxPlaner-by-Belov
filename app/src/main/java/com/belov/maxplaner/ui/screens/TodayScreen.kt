package com.belov.maxplaner.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable
import com.belov.maxplaner.ui.components.ActionSetupDialog
import com.belov.maxplaner.ui.components.TrackerCard
import com.belov.maxplaner.data.TrackerType
import com.belov.maxplaner.ui.components.TaskEditorDialog
import com.belov.maxplaner.ui.components.CompletionButton
import androidx.compose.ui.text.style.TextDecoration

import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.components.PlannerProgressIndicator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.FocusMode
import com.belov.maxplaner.data.FOCUS_DURATION_MILLIS
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(store: PlannerStore) {
    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var showCatalog by rememberSaveable { mutableStateOf(false) }
    val selectedTask = store.tasks.firstOrNull { it.id == selectedTaskId }
    if (selectedTask != null) {
        TaskDetailScreen(store, selectedTask) { selectedTaskId = null }
        return
    }
    val today = store.today.toString()
    val tasks = store.tasks.filter { it.dueDate == today }
        .sortedWith(compareBy({ it.completed }, { !it.isFocus }, { -it.priority }, { it.startMinutes ?: Int.MAX_VALUE }))
    val habits = store.habits.filter { it.schedule.isScheduled(store.today) }
    val trackers = store.trackers.filter { it.period.includes(store.today) }
    val plannedActions = trackers.filter { it.type == TrackerType.CHECK || it.type == TrackerType.STREAK }
    val metrics = trackers.filter { it.type != TrackerType.CHECK && it.type != TrackerType.STREAK }
    val done = plannedActions.count { it.isComplete(store.today) } + tasks.count { it.completed } + habits.count { today in it.completedDates }
    val total = tasks.size + habits.size + plannedActions.size
    val progress = if (total == 0) 0f else done.toFloat() / total
    val keyTasks = tasks.filter { !it.completed }.take(3)
    val greeting = when (java.time.LocalTime.now().hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }
    val tokens = LocalStyleTokens.current
    LazyColumn(
        state = rememberLazyListState(), modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = tokens.screenPadding, end = tokens.screenPadding, top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.sectionSpacing)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(store.today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(greeting, style = MaterialTheme.typography.headlineMedium)
            }
        }
        item {
            PlannerCard(hero = true, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (total == 0) "Каким будет твой день?" else if (done == total) "Всё на сегодня выполнено" else "Сегодня выполнено",
                        style = MaterialTheme.typography.titleLarge)
                    if (total > 0) {
                        Text("$done из $total", style = MaterialTheme.typography.headlineLarge)
                        PlannerProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    } else Text("Выбери готовое действие или начни со своего дела.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (keyTasks.isNotEmpty()) {
            item { SectionTitle("Главное на сегодня") }
            items(keyTasks, key = { "key-${it.id}" }) { task ->
                TodayTaskRow(store, task, true) { selectedTaskId = task.id }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showCatalog = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Выбрать готовое", maxLines = 1)
                }
                OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Создать своё", maxLines = 1)
                }
            }
        }
        if (total > 0) {
            item { SectionTitle("Сегодня по плану") }
            items(tasks, key = { "task-${it.id}" }) { task ->
                TodayTaskRow(store, task, false) { selectedTaskId = task.id }
            }
            items(habits, key = { "habit-${it.id}" }) { habit ->
                PlannerCard(modifier = Modifier.fillMaxWidth(), shape = tokens.compactShape) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompletionButton(today in habit.completedDates, habit.title) { store.toggleHabitToday(habit.id) }
                        Column(Modifier.weight(1f)) {
                            Text(habit.title, style = MaterialTheme.typography.titleMedium)
                            Text("Привычка · серия ${store.streak(habit)}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        items(plannedActions, key = { "action-${it.id}" }) { TrackerCard(store, it) }
        item { SectionTitle("Фокус") }
        item { FocusTimer(store) }
        if (metrics.isNotEmpty()) {
            item { SectionTitle("Мои показатели") }
            items(metrics, key = { "metric-${it.id}" }) { TrackerCard(store, it) }
        }
        if (total > 0 || store.focusMinutes > 0) {
            item {
                PlannerCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Итог дня", style = MaterialTheme.typography.titleMedium)
                        if (total > 0) Text("Выполнено $done из $total")
                        val streak = habits.maxOfOrNull { store.streak(it) } ?: 0
                        if (streak > 0) Text("Самая длинная текущая серия: $streak")
                        if (store.focusMinutes > 0) Text("Фокус за всё время: ${store.focusMinutes} мин", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (showAdd) ActionSetupDialog(store, onDismiss = { showAdd = false })
    if (showCatalog) ActionCatalogDialog(store, onDismiss = { showCatalog = false })
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun TodayTaskRow(store: PlannerStore, task: com.belov.maxplaner.data.PlannerTask, prominent: Boolean, onOpen: () -> Unit) {
    PlannerCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen), shape = LocalStyleTokens.current.compactShape,
        colors = CardDefaults.cardColors(containerColor = if (prominent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null)
                Text(task.startMinutes?.let { "%02d:%02d · %d мин".format(it / 60, it % 60, task.durationMinutes) } ?: task.category,
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, "Открыть детали", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FocusTimer(store: PlannerStore) {
    val session = store.focusState.session
    val remaining = session.remainingMillis(store.focusClock)
    val seconds = (remaining + 999) / 1000
    val running = session.mode == FocusMode.Running
    val status = when (session.mode) {
        FocusMode.Idle -> "25 минут на главное"
        FocusMode.Running -> "Время фокуса"
        FocusMode.Paused -> "На паузе"
        FocusMode.Completed -> "Готово · +25 минут фокуса"
    }
    PlannerCard(shape = LocalStyleTokens.current.cardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timer, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%02d:%02d".format(seconds / 60, seconds % 60), style = MaterialTheme.typography.headlineMedium)
                }
            }
            PlannerProgressIndicator(
                progress = { 1f - remaining.toFloat() / FOCUS_DURATION_MILLIS },
                modifier = Modifier.fillMaxWidth()
            )
            FilledTonalButton(
                onClick = { if (running) store.pauseFocus() else store.startFocus() },
                modifier = Modifier.fillMaxWidth(),
                shape = LocalStyleTokens.current.pillShape
            ) {
                Text(when (session.mode) {
                    FocusMode.Running -> "Пауза"
                    FocusMode.Paused -> "Продолжить"
                    FocusMode.Completed -> "Ещё 25 минут"
                    FocusMode.Idle -> "Начать фокус"
                })
            }
        }
    }
}
