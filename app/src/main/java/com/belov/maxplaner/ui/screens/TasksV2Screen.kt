package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.PlannerTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TasksV2Screen(store: PlannerStore) {
    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    val selectedTask = store.tasks.firstOrNull { it.id == selectedTaskId }

    if (selectedTask != null) {
        TaskDetailScreen(
            store = store,
            task = selectedTask,
            onBack = { selectedTaskId = null }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Задачи", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
                Text("Фокус на том, что действительно важно", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { showAdd = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Rounded.Add, null)
                    Text("  Быстро добавить")
                }
            }
        }
        val sorted = store.tasks.sortedWith(compareBy({ it.completed }, { -it.priority }))
        items(sorted, key = { it.id }) { task ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { selectedTaskId = task.id },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (task.completed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { store.toggleTask(task.id) }) {
                        Icon(if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle, null)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            buildString {
                                append(task.dueDate ?: "Без даты")
                                task.startMinutes?.let { append(" • %02d:%02d".format(it / 60, it % 60)) }
                                append(" • ${task.category}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { store.deleteTask(task.id) }) { Icon(Icons.Rounded.Delete, null) }
                }
            }
        }
    }

    if (showAdd) {
        AddRichTaskDialog(
            onDismiss = { showAdd = false },
            onSave = { title, notes, category ->
                store.addTask(title = title, notes = notes, category = category)
                showAdd = false
            }
        )
    }
}

@Composable
private fun TaskDetailScreen(store: PlannerStore, task: PlannerTask, onBack: () -> Unit) {
    var showEdit by remember { mutableStateOf(false) }
    var newChecklistItem by remember { mutableStateOf("") }
    val done = task.checklist.count { it.completed }
    val progress = if (task.checklist.isEmpty()) 0f else done.toFloat() / task.checklist.size
    val dateText = task.dueDate?.let {
        runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))) }.getOrDefault(it)
    } ?: "Без даты"
    val timeText = task.startMinutes?.let { "%02d:%02d • %d мин".format(it / 60, it % 60, task.durationMinutes) } ?: "Время не задано"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Назад") }
                Text("Детали задачи", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("$dateText • $timeText", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(task.category)
                        InfoChip(priorityLabel(task.priority))
                    }
                }
            }
        }
        item { InfoSection("Повтор", task.recurrence) }
        item { InfoSection("Заметки", task.notes.ifBlank { "Заметок пока нет" }) }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Чек-лист", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("$done/${task.checklist.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (task.checklist.isNotEmpty()) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    task.checklist.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { store.toggleChecklistItem(task.id, item.id) }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(if (item.completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle, null)
                            Text("  ${item.title}", modifier = Modifier.weight(1f))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newChecklistItem,
                            onValueChange = { newChecklistItem = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Новый пункт") },
                            singleLine = true
                        )
                        IconButton(onClick = {
                            store.addChecklistItem(task.id, newChecklistItem)
                            newChecklistItem = ""
                        }) { Icon(Icons.Rounded.Add, "Добавить") }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showEdit = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Edit, null)
                    Text("  Изменить")
                }
                Button(onClick = { store.toggleTask(task.id) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.CheckCircle, null)
                    Text(if (task.completed) "  Вернуть" else "  Завершить")
                }
            }
        }
    }

    if (showEdit) {
        EditTaskDialog(task, onDismiss = { showEdit = false }) { updated ->
            store.updateTask(updated)
            showEdit = false
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun InfoSection(title: String, value: String) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun priorityLabel(priority: Int) = when (priority) {
    3 -> "Высокий приоритет"
    1 -> "Низкий приоритет"
    else -> "Средний приоритет"
}

@Composable
private fun AddRichTaskDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Личное") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Название") }, singleLine = true)
                OutlinedTextField(notes, { notes = it }, label = { Text("Заметки") })
                OutlinedTextField(category, { category = it }, label = { Text("Категория") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onSave(title, notes, category) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun EditTaskDialog(task: PlannerTask, onDismiss: () -> Unit, onSave: (PlannerTask) -> Unit) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var notes by remember(task.id) { mutableStateOf(task.notes) }
    var category by remember(task.id) { mutableStateOf(task.category) }
    var recurrence by remember(task.id) { mutableStateOf(task.recurrence) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить задачу") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Название") }, singleLine = true)
                OutlinedTextField(notes, { notes = it }, label = { Text("Заметки") })
                OutlinedTextField(category, { category = it }, label = { Text("Категория") }, singleLine = true)
                OutlinedTextField(recurrence, { recurrence = it }, label = { Text("Повтор") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) onSave(task.copy(title = title.trim(), notes = notes.trim(), category = category.ifBlank { "Личное" }, recurrence = recurrence.ifBlank { "Не повторять" }))
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
