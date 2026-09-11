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
import com.belov.maxplaner.ui.theme.LocalStyleTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotFields(start: Int?, durationText: String, onStart: (Int?) -> Unit, onDuration: (String) -> Unit) {
    var picker by rememberSaveable { mutableStateOf(false) }
    val tokens = LocalStyleTokens.current
    val duration = durationText.toIntOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Время выполнения", style = MaterialTheme.typography.titleSmall)
        Text(
            "Поставь время, чтобы действие появилось на шкале дня.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(
            onClick = { picker = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = tokens.compactShape
        ) {
            Text(start?.let { "Начать в " + timeOfDay(it) } ?: "Назначить время", maxLines = 1)
        }
        if (start != null) {
            TextButton(onClick = { onStart(null) }, modifier = Modifier.fillMaxWidth()) {
                Text("Оставить без времени", maxLines = 1)
            }
        }
        if (start != null) {
            OutlinedTextField(durationText, onDuration, label = { Text("Длительность, минут") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = tokens.compactShape, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = duration == null || duration !in 15..720,
                supportingText = { Text(if (duration != null && duration in 15..720) timeRange(start, duration) else "От 15 до 720 минут") })
            Text("Быстрый выбор", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(15, 30, 60, 90).forEach { minutes ->
                    FilterChip(
                        selected = duration == minutes,
                        onClick = { onDuration(minutes.toString()) },
                        label = { Text("$minutes мин", maxLines = 1) },
                        modifier = Modifier.heightIn(min = 44.dp),
                        shape = tokens.pillShape
                    )
                }
            }
        }
    }
    if (picker) {
        val time = rememberTimePickerState(initialHour = (start ?: 540) / 60, initialMinute = (start ?: 540) % 60, is24Hour = true)
        AlertDialog(onDismissRequest = { picker = false }, shape = tokens.heroShape,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (tokens.floatingGlass) .88f else 1f),
            tonalElevation = tokens.heroElevation, title = { Text("Время начала") }, text = { TimeInput(state = time) },
            confirmButton = { TextButton(onClick = { onStart(time.hour * 60 + time.minute); picker = false }) { Text("Выбрать") } },
            dismissButton = { TextButton(onClick = { picker = false }) { Text("Отмена") } })
    }
}

@Composable
fun EditTimeSlotDialog(title: String, start: Int?, duration: Int, onDismiss: () -> Unit, onSave: (Int?, Int) -> Unit) {
    var selectedStart by rememberSaveable { mutableStateOf(start) }
    var durationText by rememberSaveable { mutableStateOf(duration.toString()) }
    val parsed = durationText.toIntOrNull()
    val tokens = LocalStyleTokens.current
    AlertDialog(onDismissRequest = onDismiss, shape = tokens.heroShape,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (tokens.floatingGlass) .88f else 1f),
        tonalElevation = tokens.heroElevation, title = { Text(title) },
        text = { TimeSlotFields(selectedStart, durationText, { selectedStart = it }, { durationText = it }) },
        confirmButton = { TextButton(enabled = selectedStart == null || parsed != null && parsed in 15..720, onClick = {
            onSave(selectedStart, parsed?.coerceIn(15, 720) ?: duration); onDismiss()
        }) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}
