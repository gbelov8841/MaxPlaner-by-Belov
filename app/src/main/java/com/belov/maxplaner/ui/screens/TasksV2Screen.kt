package com.belov.maxplaner.ui.screens

import com.belov.maxplaner.ui.components.TaskEditorDialog
import com.belov.maxplaner.ui.components.CompletionButton
import androidx.compose.ui.text.style.TextDecoration

import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.components.PlannerProgressIndicator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
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
        modifier = Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
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
        if (store.tasks.isEmpty()) {
            item {
                PlannerCard {
                    Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding)) {
                        Text("Начни с одного важного дела", style = MaterialTheme.typography.titleMedium)
                        Text("Нажми «Быстро добавить», чтобы освободить голову и записать задачу.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        val sorted = store.tasks.sortedWith(compareBy({ it.completed }, { -it.priority }))
        items(sorted, key = { it.id }) { task ->
            PlannerCard(
                modifier = Modifier.fillMaxWidth().clickable { selectedTaskId = task.id },
                shape = LocalStyleTokens.current.cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (task.completed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                    CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold, textDecoration = if (task.completed) TextDecoration.LineThrough else null)
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
                    IconButton(onClick = { store.deleteTask(task.id) }) { Icon(Icons.Rounded.Delete, "Удалить") }
                }
            }
        }
    }

    if (showAdd) TaskEditorDialog(store = store, onDismiss = { showAdd = false })
}

@Composable
private fun TaskDetailScreen(store: PlannerStore, task: PlannerTask, onBack: () -> Unit) {
    var showEdit by rememberSaveable { mutableStateOf(false) }
    var newChecklistItem by rememberSaveable(task.id) { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    BackHandler(enabled = !showEdit, onBack = onBack)
    val done = task.checklist.count { it.completed }
    val progress = if (task.checklist.isEmpty()) 0f else done.toFloat() / task.checklist.size
    val dateText = task.dueDate?.let {
        runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))) }.getOrDefault(it)
    } ?: "Без даты"
    val timeText = task.startMinutes?.let { "%02d:%02d • %d мин".format(it / 60, it % 60, task.durationMinutes) } ?: "Время не задано"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Назад") }
                Text("Детали задачи", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            PlannerCard(hero = true, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("$dateText • $timeText", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(task.category)
                        InfoChip(priorityLabel(task.priority))
                    }
                }
            }
        }
        item { InfoSection("Повтор", task.recurrence) }
        item { InfoSection("Заметки", task.notes.ifBlank { "Заметок пока нет" }) }
        item {
            PlannerCard(shape = LocalStyleTokens.current.cardShape) {
                Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Чек-лист", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("$done/${task.checklist.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (task.checklist.isNotEmpty()) PlannerProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    task.checklist.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                                value = item.completed,
                                role = Role.Checkbox,
                                onValueChange = {
                                    store.toggleChecklistItem(task.id, item.id)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            ).padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(if (item.completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle, null)
                            Text("  ${item.title}", modifier = Modifier.weight(1f), textDecoration = if (item.completed) TextDecoration.LineThrough else null)
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
                        IconButton(enabled = newChecklistItem.isNotBlank(), onClick = {
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
                Button(onClick = {
                    store.toggleTask(task.id)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.CheckCircle, null)
                    Text(if (task.completed) "  Вернуть" else "  Завершить")
                }
            }
        }
    }

    if (showEdit) {
        TaskEditorDialog(store = store, task = task, onDismiss = { showEdit = false })
    }
}

@Composable
private fun InfoChip(text: String) {
    PlannerSurface(shape = LocalStyleTokens.current.pillShape, color = MaterialTheme.colorScheme.surface.copy(alpha = LocalStyleTokens.current.insetSurfaceAlpha)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun InfoSection(title: String, value: String) {
    PlannerCard(shape = LocalStyleTokens.current.cardShape) {
        Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
