package com.belov.maxplaner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.PlannerTask
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

val LocalTaskCompletion = staticCompositionLocalOf<(String) -> Unit> {
    { error("Task completion must be provided by MaxPlanerApp") }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskQuickActions(store: PlannerStore, task: PlannerTask, onDismiss: () -> Unit, onOpen: () -> Unit) {
    var mode by rememberSaveable(task.id) { mutableStateOf("actions") }
    fun update(transform: (PlannerTask) -> PlannerTask) {
        store.tasks.firstOrNull { it.id == task.id }?.let { store.updateTask(transform(it)) }
        onDismiss()
    }
    when (mode) {
        "date" -> {
            val initial = task.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: store.today
            val picker = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
            DatePickerDialog(onDismissRequest = onDismiss,
                confirmButton = { TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                    picker.selectedDateMillis?.let { millis ->
                        val day = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        update { it.copy(dueDate = day.toString()) }
                    }
                }) { Text("Перенести") } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }) { DatePicker(picker) }
        }
        "time" -> EditTimeSlotDialog("Изменить время", task.startMinutes, task.durationMinutes, onDismiss) { start, duration ->
            update { it.copy(startMinutes = start, durationMinutes = duration) }
        }
        else -> AlertDialog(onDismissRequest = onDismiss, title = { Text(task.title) }, text = {
            Column(Modifier.fillMaxWidth()) {
                TextButton(onClick = { update { it.copy(dueDate = store.today.plusDays(1).toString()) } }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("На завтра") }
                TextButton(onClick = { mode = "date" }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Выбрать дату") }
                TextButton(onClick = { mode = "time" }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Изменить время") }
                TextButton(onClick = { onDismiss(); onOpen() }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Открыть дело") }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } })
    }
}
