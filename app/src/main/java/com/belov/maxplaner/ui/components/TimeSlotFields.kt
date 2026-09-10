package com.belov.maxplaner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.timeRange
import com.belov.maxplaner.data.timeOfDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotFields(start: Int?, durationText: String, onStart: (Int?) -> Unit, onDuration: (String) -> Unit) {
    var picker by rememberSaveable { mutableStateOf(false) }
    val duration = durationText.toIntOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Время в течение дня", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { picker = true }, label = { Text(start?.let(::timeOfDay) ?: "Назначить время") }, modifier = Modifier.heightIn(min = 48.dp))
            if (start != null) TextButton(onClick = { onStart(null) }) { Text("Без времени") }
        }
        if (start != null) {
            OutlinedTextField(durationText, onDuration, label = { Text("Длительность, минут") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = duration == null || duration !in 15..720,
                supportingText = { Text(if (duration != null && duration in 15..720) timeRange(start, duration) else "От 15 до 720 минут") })
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(15, 30, 60, 90).forEach { minutes -> FilterChip(duration == minutes, { onDuration(minutes.toString()) }, label = { Text("$minutes мин") }) }
            }
        }
    }
    if (picker) {
        val time = rememberTimePickerState(initialHour = (start ?: 540) / 60, initialMinute = (start ?: 540) % 60, is24Hour = true)
        AlertDialog(onDismissRequest = { picker = false }, title = { Text("Время начала") }, text = { TimeInput(state = time) },
            confirmButton = { TextButton(onClick = { onStart(time.hour * 60 + time.minute); picker = false }) { Text("Выбрать") } },
            dismissButton = { TextButton(onClick = { picker = false }) { Text("Отмена") } })
    }
}

@Composable
fun EditTimeSlotDialog(title: String, start: Int?, duration: Int, onDismiss: () -> Unit, onSave: (Int?, Int) -> Unit) {
    var selectedStart by rememberSaveable { mutableStateOf(start) }
    var durationText by rememberSaveable { mutableStateOf(duration.toString()) }
    val parsed = durationText.toIntOrNull()
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = { TimeSlotFields(selectedStart, durationText, { selectedStart = it }, { durationText = it }) },
        confirmButton = { TextButton(enabled = selectedStart == null || parsed != null && parsed in 15..720, onClick = {
            onSave(selectedStart, parsed?.coerceIn(15, 720) ?: duration); onDismiss()
        }) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}
