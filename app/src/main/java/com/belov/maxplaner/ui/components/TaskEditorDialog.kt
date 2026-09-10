package com.belov.maxplaner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.PlannerTask
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One form for all entry points. Draft state is local; storage changes only on Save. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    store: PlannerStore,
    onDismiss: () -> Unit,
    task: PlannerTask? = null,
    initialDate: LocalDate? = LocalDate.now(),
    initialStartMinutes: Int? = null,
    onSaved: (LocalDate?) -> Unit = {}
) {
    var title by rememberSaveable(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var notes by rememberSaveable(task?.id) { mutableStateOf(task?.notes.orEmpty()) }
    var category by rememberSaveable(task?.id) { mutableStateOf(task?.category ?: "Личное") }
    var recurrence by rememberSaveable(task?.id) { mutableStateOf(task?.recurrence ?: "Не повторять") }
    var dateText by rememberSaveable(task?.id) {
        mutableStateOf(if (task != null) task.dueDate else initialDate?.toString())
    }
    var startMinutes by rememberSaveable(task?.id) {
        mutableStateOf(if (task != null) task.startMinutes else initialStartMinutes)
    }
    var durationText by rememberSaveable(task?.id) { mutableStateOf((task?.durationMinutes ?: 60).toString()) }
    var priority by rememberSaveable(task?.id) { mutableStateOf(task?.priority ?: 2) }
    var focus by rememberSaveable(task?.id) { mutableStateOf(task?.isFocus ?: false) }
    var expanded by rememberSaveable(task?.id) { mutableStateOf(task != null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val requester = remember { FocusRequester() }
    val tokens = LocalStyleTokens.current
    val today = LocalDate.now()
    val date = dateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val duration = durationText.toIntOrNull()
    val durationValid = duration != null && duration in 15..720
    val canSave = title.isNotBlank() && (startMinutes == null || durationValid) && !saving

    fun save() {
        if (!canSave || saving) return
        saving = true
        val savedDuration = if (startMinutes != null) duration!! else task?.durationMinutes ?: 60
        if (task == null) {
            store.addTask(
                title = title, dueDate = date?.toString(), priority = priority, focus = focus,
                startMinutes = startMinutes, durationMinutes = savedDuration,
                notes = notes, category = category, recurrence = recurrence
            )
        } else {
            // Copy only edited fields, preserving ID, completion and every checklist item.
            store.updateTask(task.copy(
                title = title.trim(), dueDate = date?.toString(), priority = priority, isFocus = focus,
                startMinutes = startMinutes, durationMinutes = savedDuration,
                notes = notes.trim(), category = category.ifBlank { "Личное" },
                recurrence = recurrence.ifBlank { "Не повторять" }
            ))
        }
        onSaved(date)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = tokens.heroShape,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = tokens.heroElevation,
        title = { Text(if (task == null) "Новая задача" else "Изменить задачу") },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(tokens.sectionSpacing)
            ) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Что нужно сделать?") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(requester),
                    shape = tokens.compactShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { save() })
                )
                LaunchedEffect(Unit) { if (task == null) requester.requestFocus() }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = date == today, onClick = { dateText = today.toString() }, label = { Text("Сегодня") }, shape = tokens.pillShape)
                    FilterChip(selected = date == today.plusDays(1), onClick = { dateText = today.plusDays(1).toString() }, label = { Text("Завтра") }, shape = tokens.pillShape)
                    FilterChip(selected = date == null, onClick = { dateText = null; startMinutes = null }, label = { Text("Без даты") }, shape = tokens.pillShape)
                }
                AssistChip(
                    onClick = { showDatePicker = true }, shape = tokens.compactShape,
                    label = { Text(date?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))) ?: "Выбрать дату") }
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = { showTimePicker = true }, enabled = date != null, shape = tokens.compactShape,
                        label = { Text(startMinutes?.let { "%02d:%02d".format(it / 60, it % 60) } ?: "Добавить время") }
                    )
                    if (startMinutes != null) TextButton(onClick = { startMinutes = null }) { Text("Без времени") }
                }
                if (startMinutes != null) {
                    OutlinedTextField(
                        value = durationText, onValueChange = { durationText = it },
                        label = { Text("Длительность, минут") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = tokens.compactShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !durationValid,
                        supportingText = { Text(if (durationValid) "От 15 минут до 12 часов" else "Укажи число от 15 до 720") }
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 60, 90).forEach { minutes ->
                            FilterChip(selected = duration == minutes, onClick = { durationText = minutes.toString() }, label = { Text("$minutes мин") }, shape = tokens.pillShape)
                        }
                    }
                }
                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Свернуть подробности" else "Подробнее") }
                if (expanded) {
                    OutlinedTextField(notes, { notes = it }, label = { Text("Заметки") }, modifier = Modifier.fillMaxWidth(), shape = tokens.compactShape)
                    OutlinedTextField(category, { category = it }, label = { Text("Категория") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = tokens.compactShape)
                    Text("Приоритет", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "Низкий", 2 to "Обычный", 3 to "Высокий").forEach { (value, label) ->
                            FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(label) }, shape = tokens.pillShape)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("В фокусе", modifier = Modifier.weight(1f))
                        Switch(checked = focus, onCheckedChange = { focus = it }, modifier = Modifier.semantics { contentDescription = "В фокусе" })
                    }
                    OutlinedTextField(recurrence, { recurrence = it }, label = { Text("Повтор") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = tokens.compactShape)
                }
            }
        },
        confirmButton = { Button(onClick = { save() }, enabled = canSave, shape = tokens.pillShape) { Text(if (task == null) "Добавить" else "Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    if (showDatePicker) {
        // Material date pickers use UTC midnight, independent of the device time zone.
        val state = rememberDatePickerState(initialSelectedDateMillis = (date ?: today).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false }, shape = tokens.heroShape,
            confirmButton = {
                TextButton(enabled = state.selectedDateMillis != null, onClick = {
                    state.selectedDateMillis?.let { dateText = Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate().toString() }
                    showDatePicker = false
                }) { Text("Выбрать") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface))
        }
    }
    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = (startMinutes ?: 540) / 60, initialMinute = (startMinutes ?: 540) % 60, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false }, shape = tokens.heroShape,
            containerColor = MaterialTheme.colorScheme.surface, tonalElevation = tokens.heroElevation,
            title = { Text("Время начала") },
            text = { TimeInput(state = state, modifier = Modifier.padding(top = 8.dp)) },
            confirmButton = { TextButton(onClick = { startMinutes = state.hour * 60 + state.minute; showTimePicker = false }) { Text("Выбрать") } },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Отмена") } }
        )
    }
}
