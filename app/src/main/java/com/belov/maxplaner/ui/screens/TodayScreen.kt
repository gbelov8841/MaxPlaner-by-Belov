package com.belov.maxplaner.ui.screens

import com.belov.maxplaner.ui.components.CompletionButton
import androidx.compose.ui.text.style.TextDecoration

import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.components.PlannerProgressIndicator

import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(store: PlannerStore) {
    val date = LocalDate.now()
    val today = date.toString()
    val todayTasks = store.tasks.filter { it.dueDate == today }
    val done = todayTasks.count { it.completed }
    val habitsDone = store.habits.count { it.completedDates.contains(today) }
    val totalHabits = store.habits.size
    val progressParts = todayTasks.size + totalHabits
    val progress = if (progressParts == 0) 0f else (done + habitsDone).toFloat() / progressParts
    var showAdd by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))

    LazyColumn(
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
                            Text("Сделай главное.", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
            PlannerCard(shape = LocalStyleTokens.current.cardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                    PlannerSurface(shape = LocalStyleTokens.current.iconShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(Icons.Rounded.TrackChanges, null, Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Сегодня в фокусе", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(focus?.title ?: "Выбери главный результат дня", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { showAdd = true }) { Icon(Icons.Rounded.Add, "Добавить") }
                }
            }
        }

        item { FocusTimer(onComplete = { store.addFocusMinutes(25) }) }

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
                PlannerCard(shape = LocalStyleTokens.current.compactShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp, horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
                        Column(Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.Medium, textDecoration = if (task.completed) TextDecoration.LineThrough else null)
                            Text(
                                when (task.priority) { 3 -> "Высокий приоритет"; 1 -> "Низкий приоритет"; else -> "Обычный приоритет" },
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

    if (showAdd) AddTaskDialog(onDismiss = { showAdd = false }, onSave = { store.addTask(it); showAdd = false })
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
private fun FocusTimer(onComplete: () -> Unit) {
    var seconds by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running, seconds) {
        if (running && seconds > 0) { delay(1000); seconds-- }
        else if (running && seconds == 0) { running = false; onComplete() }
    }
    PlannerCard(shape = LocalStyleTokens.current.cardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Timer, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Фокус", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%02d:%02d".format(seconds / 60, seconds % 60), style = MaterialTheme.typography.titleLarge)
            }
            FilledTonalButton(onClick = { if (seconds == 0) seconds = 25 * 60 else running = !running }) {
                Text(if (running) "Пауза" else if (seconds == 0) "Снова" else "Старт")
            }
        }
    }
}

@Composable
fun AddTaskDialog(title: String = "Новая задача", onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        shape = LocalStyleTokens.current.heroShape,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = LocalStyleTokens.current.heroElevation,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Что нужно сделать?") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (text.isNotBlank()) onSave(text) }) { Text("Добавить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
