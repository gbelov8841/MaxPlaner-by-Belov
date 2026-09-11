package com.belov.maxplaner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.theme.LocalStyleTokens

@Composable
fun TrackerCard(store: PlannerStore, tracker: Tracker, date: java.time.LocalDate = store.today) {
    val value = tracker.values[date.toString()]
    val goal = tracker.targetOn(date)
    val active = tracker.period.includes(date)
    val check = tracker.type == TrackerType.CHECK || tracker.type == TrackerType.STREAK
    var edit by rememberSaveable(tracker.id) { mutableStateOf(false) }
    var draft by rememberSaveable(tracker.id) { mutableStateOf("") }
    var editTime by rememberSaveable(tracker.id) { mutableStateOf(false) }
    var delete by rememberSaveable(tracker.id) { mutableStateOf(false) }
    val tokens = LocalStyleTokens.current
    PlannerCard(modifier = Modifier.fillMaxWidth().clickable { draft = value?.let(::displayNumber) ?: ""; edit = true }, shape = tokens.compactShape) {
        Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(tracker.title, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { editTime = true }) { Text(tracker.startMinutes?.let { timeRange(it, tracker.durationMinutes) } ?: "Назначить время") }
            if (!active) Text("На этот день не запланировано", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            if (check && active) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompletionButton(tracker.isComplete(date), tracker.title) { store.setTrackerValue(tracker.id, date, if (tracker.isComplete(date)) null else 1.0) }
                    Text(if (tracker.type == TrackerType.STREAK) "Серия: ${tracker.streak(date)}" else if (tracker.isComplete(date)) "Выполнено" else "Отметить выполненным")
                }
            } else if (active) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (value == null) "Нет записи" else displayNumber(value) + " " + tracker.unit,
                        style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    if (tracker.type in listOf(TrackerType.COUNTER, TrackerType.REDUCTION_GOAL)) {
                        IconButton(enabled = value != null && value > 0, onClick = { store.setTrackerValue(tracker.id, date, (value!! - 1).coerceAtLeast(0.0)) }) { Icon(Icons.Rounded.Remove, "Уменьшить: ${tracker.title}") }
                        IconButton(onClick = { store.setTrackerValue(tracker.id, date, (value ?: 0.0) + 1) }) { Icon(Icons.Rounded.Add, "Увеличить: ${tracker.title}") }
                    }
                }
                if (goal != null) {
                    Text((if (tracker.direction == GoalDirection.AT_MOST) "Лимит: " else "Цель: ") + displayNumber(goal) + " " + tracker.unit,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (tracker.direction == GoalDirection.AT_LEAST && goal > 0) PlannerProgressIndicator(
                        progress = { ((value ?: 0.0) / goal).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    if (tracker.direction == GoalDirection.AT_MOST && value != null) Text(if (value <= goal) "Пока в пределах лимита" else "Лимит превышен",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (tracker.type == TrackerType.SCALE) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { rating -> FilterChip(value == rating.toDouble(), { store.setTrackerValue(tracker.id, date, rating.toDouble()) }, label = { Text("$rating") }, modifier = Modifier.heightIn(min = 48.dp)) }
                    }
                }
            }
        }
    }
    if (edit) {
        val numeric = draft.replace(',', '.').toDoubleOrNull()
        val valid = numeric != null && tracker.accepts(numeric)
        AlertDialog(
            onDismissRequest = { edit = false },
            shape = tokens.heroShape,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = tokens.heroElevation,
            title = { Text(tracker.title) },
            text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (tracker.period.end == null) "С ${tracker.period.start} · повторяется" else "${tracker.period.start} — ${tracker.period.end}")
                if (active && !check) OutlinedTextField(draft, { draft = it }, label = { Text(if (tracker.unit.isBlank()) "Значение за $date" else "$date, ${tracker.unit}") }, shape = tokens.compactShape,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                    isError = draft.isNotBlank() && !valid, supportingText = { Text(if (tracker.type == TrackerType.SCALE) "Целое число от 1 до 5" else "Ноль — тоже запись. Пустое поле удаляет запись.") })
                val history = tracker.values.toSortedMap(compareByDescending { it }).entries.take(14)
                if (history.isNotEmpty()) {
                    Text("Последние записи", style = MaterialTheme.typography.titleSmall)
                    history.forEach { (day, amount) -> Text("$day · ${if (check) "Выполнено" else displayNumber(amount) + " " + tracker.unit}", style = MaterialTheme.typography.bodySmall) }
                }
                TextButton(onClick = { delete = true }) { Text("Удалить действие", color = MaterialTheme.colorScheme.error) }
            }
        }, confirmButton = {
            if (active && !check) TextButton(enabled = draft.isBlank() || valid, onClick = { store.setTrackerValue(tracker.id, date, if (draft.isBlank()) null else numeric); edit = false }) { Text("Сохранить") }
        }, dismissButton = { TextButton(onClick = { edit = false }) { Text("Закрыть") } })
    }
    if (editTime) EditTimeSlotDialog(tracker.title, tracker.startMinutes, tracker.durationMinutes, { editTime = false }) { start, duration -> store.updateTrackerTime(tracker.id, start, duration) }
    if (delete) AlertDialog(onDismissRequest = { delete = false }, shape = tokens.heroShape,
        containerColor = MaterialTheme.colorScheme.surface, tonalElevation = tokens.heroElevation,
        title = { Text("Удалить действие?") }, text = { Text("«${tracker.title}» и его записи будут удалены.") },
        confirmButton = { TextButton(onClick = { store.deleteTracker(tracker.id); delete = false; edit = false }) { Text("Удалить") } },
        dismissButton = { TextButton(onClick = { delete = false }) { Text("Отмена") } })
}
