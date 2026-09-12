package com.belov.maxplaner.ui.screens

import com.belov.maxplaner.ui.components.LocalTaskCompletion

import com.belov.maxplaner.data.categoryLabel

import com.belov.maxplaner.ui.components.TaskEditorDialog
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.components.PlannerProgressIndicator
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.PlannerTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun TaskDetailScreen(store: PlannerStore, task: PlannerTask, onBack: () -> Unit) {
    val toggleTask = LocalTaskCompletion.current
    var showEdit by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    var newChecklistItem by rememberSaveable(task.id) { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    BackHandler(enabled = !showEdit && !showDelete, onBack = onBack)
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
                Text("Детали дела", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { showDelete = true }) { Icon(Icons.Rounded.Delete, "Удалить дело") }
            }
        }
        item {
            PlannerCard(hero = true, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("$dateText • $timeText", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(categoryLabel(task.category))
                        InfoChip(priorityLabel(task.priority))
                    }
                    TextButton(onClick = { store.updateTask(task.copy(isFocus = !task.isFocus)) }) {
                        Icon(if (task.isFocus) Icons.Rounded.Star else Icons.Rounded.StarBorder, null)
                        Text(if (task.isFocus) " Убрать из фокуса" else " Выбрать для фокуса")
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
                OutlinedButton(onClick = { showEdit = true }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Icon(Icons.Rounded.Edit, null)
                    Text("Изменить", maxLines = 1)
                }
                Button(onClick = {
                    toggleTask(task.id)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.CheckCircle, null)
                    Text(if (task.completed) "Вернуть" else "Готово", maxLines = 1)
                }
            }
        }
    }

    if (showDelete) AlertDialog(onDismissRequest = { showDelete = false }, title = { Text("Удалить дело?") },
        text = { Text(task.title) }, confirmButton = { TextButton(onClick = { store.deleteTask(task.id); onBack() }) { Text("Удалить") } },
        dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Отмена") } })
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
