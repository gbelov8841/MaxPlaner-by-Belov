package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.PlannerStore
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun TodayScreen(store: PlannerStore) {
    val today = LocalDate.now().toString()
    val todayTasks = store.tasks.filter { it.dueDate == today }
    val done = todayTasks.count { it.completed }
    val habitsDone = store.habits.count { it.completedDates.contains(today) }
    var showAdd by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Сегодня", style = MaterialTheme.typography.headlineLarge)
            Text("Сделай важное, не перегружая день", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Главный фокус", style = MaterialTheme.typography.titleLarge)
                    val focus = todayTasks.firstOrNull { it.isFocus && !it.completed }
                    Text(focus?.title ?: "Выбери один результат, после которого день уже можно считать удачным.")
                    Button(onClick = { showAdd = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Text("  Добавить фокус")
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("$done/${todayTasks.size}", "задачи", Modifier.weight(1f))
                Metric("$habitsDone/${store.habits.size}", "привычки", Modifier.weight(1f))
                Metric("${store.focusMinutes}м", "фокус", Modifier.weight(1f))
            }
        }

        item { FocusTimer(onComplete = { store.addFocusMinutes(25) }) }

        item { Text("План дня", style = MaterialTheme.typography.headlineMedium) }

        if (todayTasks.isEmpty()) {
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Text("День пока свободен", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Добавь первую задачу и держи план коротким и реалистичным.")
                    }
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                Card(shape = RoundedCornerShape(20.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { store.toggleTask(task.id) }) {
                            Icon(if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle, contentDescription = null)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.Medium)
                            Text(
                                when (task.priority) { 3 -> "Высокий приоритет"; 1 -> "Низкий приоритет"; else -> "Обычный приоритет" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddTaskDialog(
        title = "Главный фокус",
        onDismiss = { showAdd = false },
        onSave = { store.addTask(it, focus = true, priority = 3); showAdd = false }
    )
}

@Composable
private fun FocusTimer(onComplete: () -> Unit) {
    var seconds by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running, seconds) {
        if (running && seconds > 0) {
            delay(1000)
            seconds--
        } else if (running && seconds == 0) {
            running = false
            onComplete()
        }
    }
    Card(shape = RoundedCornerShape(22.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Rounded.Timer, contentDescription = null)
            Column(Modifier.weight(1f)) {
                Text("Фокус-сессия", style = MaterialTheme.typography.titleMedium)
                Text("%02d:%02d".format(seconds / 60, seconds % 60), style = MaterialTheme.typography.titleLarge)
            }
            FilledTonalButton(onClick = {
                if (seconds == 0) seconds = 25 * 60 else running = !running
            }) { Text(if (running) "Пауза" else if (seconds == 0) "Снова" else "Старт") }
        }
    }
}

@Composable
private fun Metric(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AddTaskDialog(title: String = "Новая задача", onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Что нужно сделать?") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (text.isNotBlank()) onSave(text) }) { Text("Добавить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
