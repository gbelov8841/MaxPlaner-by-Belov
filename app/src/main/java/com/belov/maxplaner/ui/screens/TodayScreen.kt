package com.belov.maxplaner.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable
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
    val date = store.today
    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val selectedTask = store.tasks.firstOrNull { it.id == selectedTaskId }
    if (selectedTask != null) {
        TaskDetailScreen(store, selectedTask, onBack = { selectedTaskId = null })
        return
    }
    val today = date.toString()
    val todayTasks = store.tasks.filter { it.dueDate == today }
        .sortedWith(compareBy({ it.completed }, { !it.isFocus }, { -it.priority }, { it.startMinutes ?: Int.MAX_VALUE }))
    val done = todayTasks.count { it.completed }
    val habitsDone = store.habits.count { it.completedDates.contains(today) }
    val totalHabits = store.habits.size
    val progressParts = todayTasks.size + totalHabits
    val progress = if (progressParts == 0) 0f else (done + habitsDone).toFloat() / progressParts
    var showAdd by rememberSaveable { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = LocalStyleTokens.current.screenPadding, end = LocalStyleTokens.current.screenPadding, top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MaxPlaner", style = MaterialTheme.typography.titleLarge)
                    Text(date.format(formatter).replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                PlannerSurface(shape = LocalStyleTokens.current.iconShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Icon(Icons.Rounded.Person, null, Modifier.padding(11.dp))
                }
            }
        }

        item {
            PlannerCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                hero = true
            ) {
                Column(Modifier.padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Добрый день", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(if (progressParts > 0 && done + habitsDone == progressParts) "Всё на сегодня." else "Сделай главное.", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        PlannerSurface(shape = LocalStyleTokens.current.cardShape, color = MaterialTheme.colorScheme.surface.copy(alpha = LocalStyleTokens.current.insetSurfaceAlpha)) {
                            Text("${(progress * 100).toInt()}%", Modifier.padding(horizontal = 15.dp, vertical = 10.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    PlannerProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Прогресс дня", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f))
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                PremiumMetric(Icons.Rounded.CheckCircle, "$done/${todayTasks.size}", "Задачи", Modifier.weight(1f))
                PremiumMetric(Icons.Rounded.Spa, "$habitsDone/$totalHabits", "Привычки", Modifier.weight(1f))
                PremiumMetric(Icons.Rounded.Timer, "${store.focusMinutes}м", "Фокус", Modifier.weight(1f))
            }
        }

        item {
            val focus = todayTasks.firstOrNull { it.isFocus && !it.completed }
                ?: todayTasks.firstOrNull { !it.completed }
            PlannerCard(
                modifier = Modifier.clickable { if (focus != null) selectedTaskId = focus.id else showAdd = true },
                shape = LocalStyleTokens.current.cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                    PlannerSurface(shape = LocalStyleTokens.current.iconShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(Icons.Rounded.TrackChanges, null, Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (focus?.isFocus == true) "Сегодня в фокусе" else "Следующая задача", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(focus?.title ?: "Выбери главный результат дня", style = MaterialTheme.typography.titleMedium)
                    }
                    Icon(if (focus == null) Icons.Rounded.Add else Icons.Rounded.ChevronRight, null)
                }
            }
        }

        item { FocusTimer(store) }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Сегодня", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                FilledTonalButton(onClick = { showAdd = true }, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                    Icon(Icons.Rounded.Add, null)
                    Text(" Добавить")
                }
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                PlannerCard(shape = LocalStyleTokens.current.cardShape) {
                    Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding)) {
                        Text("День пока свободен", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(5.dp))
                        Text("Добавь несколько действительно важных дел — MaxPlaner поможет не перегружать день.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                PlannerCard(
                    modifier = Modifier.clickable { selectedTaskId = task.id },
                    shape = LocalStyleTokens.current.compactShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp, horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
                        Column(Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.Medium, textDecoration = if (task.completed) TextDecoration.LineThrough else null)
                            Text(
                                buildString {
                                    task.startMinutes?.let { append("%02d:%02d · %d мин · ".format(it / 60, it % 60, task.durationMinutes)) }
                                    append(when (task.priority) { 3 -> "Высокий приоритет"; 1 -> "Низкий приоритет"; else -> "Обычный приоритет" })
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (task.priority == 3) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (task.isFocus) Icon(Icons.Rounded.Star, "Фокус", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }

    if (showAdd) TaskEditorDialog(store = store, onDismiss = { showAdd = false })
}

@Composable
private fun PremiumMetric(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    PlannerCard(modifier, shape = LocalStyleTokens.current.compactShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
