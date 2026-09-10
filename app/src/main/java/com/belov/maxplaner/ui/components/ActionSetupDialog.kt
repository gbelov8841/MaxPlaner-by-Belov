package com.belov.maxplaner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSetupDialog(store: PlannerStore, onDismiss: () -> Unit, template: ActionTemplate? = null, category: String = "Личное") {
    var title by rememberSaveable { mutableStateOf(template?.title ?: "") }
    var type by rememberSaveable { mutableStateOf(template?.type ?: TrackerType.CHECK) }
    var period by rememberSaveable { mutableStateOf(if (template?.type == TrackerType.REDUCTION_GOAL) "Повторять" else "Сегодня") }
    var startText by rememberSaveable { mutableStateOf(store.today.toString()) }
    var dayMask by rememberSaveable { mutableStateOf(127) }
    var showDate by rememberSaveable { mutableStateOf(false) }
    var targetText by rememberSaveable { mutableStateOf(template?.target?.let(::displayNumber) ?: "") }
    var unit by rememberSaveable { mutableStateOf(template?.unit ?: "") }
    var initialText by rememberSaveable { mutableStateOf("") }
    var stepText by rememberSaveable { mutableStateOf("") }
    var customOptions by rememberSaveable { mutableStateOf(false) }
    var upperLimit by rememberSaveable { mutableStateOf(template?.direction == GoalDirection.AT_MOST) }
    var saved by remember { mutableStateOf(false) }
    val start = LocalDate.parse(startText)
    val numeric = type !in listOf(TrackerType.CHECK, TrackerType.STREAK, TrackerType.SCALE)
    fun number(s: String) = s.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }
    val target = number(targetText)
    val gradual = type == TrackerType.REDUCTION_GOAL
    val initial = number(initialText)
    val step = number(stepText)
    val valid = title.isNotBlank() && (!numeric || targetText.isBlank() || target != null) &&
        (period != "По дням недели" || dayMask != 0) &&
        (!gradual || (target != null && initial != null && initial >= target && step != null && step > 0 && initial % 1.0 == 0.0 && step % 1.0 == 0.0))
    val days = DayOfWeek.entries.filter { dayMask and (1 shl (it.value - 1)) != 0 }.toSet()
    val periodOptions = listOf("Сегодня", "На дату", "На неделю", "Повторять", "По дням недели")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "Своё действие" else "Добавить в план") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
                if (template == null) {
                    TextButton(onClick = { customOptions = !customOptions }) { Text(if (customOptions) "Скрыть настройки" else "Что отслеживать?") }
                    if (customOptions) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(TrackerType.CHECK to "Выполнение", TrackerType.COUNTER to "Количество", TrackerType.NUMBER to "Значение",
                                TrackerType.DURATION to "Минуты", TrackerType.STREAK to "Дни подряд", TrackerType.SCALE to "Оценку от 1 до 5").forEach { (value, label) ->
                                FilterChip(type == value, onClick = { type = value; targetText = ""; unit = if (value == TrackerType.DURATION) "мин" else "" }, label = { Text(label) })
                            }
                        }
                    }
                }
                if (numeric) {
                    OutlinedTextField(targetText, { targetText = it }, label = { Text(if (upperLimit) "Лимит за день (необязательно)" else "Цель за день (необязательно)") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
                        isError = targetText.isNotBlank() && target == null)
                    OutlinedTextField(unit, { unit = it }, label = { Text("Единица измерения") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (!gradual && targetText.isNotBlank()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(!upperLimit, { upperLimit = false }, label = { Text("Не меньше") })
                            FilterChip(upperLimit, { upperLimit = true }, label = { Text("Не больше") })
                        }
                    }
                    if (type == TrackerType.DURATION) Text("Записывай потраченное время в минутах. Таймер на 25 минут — на главной.", style = MaterialTheme.typography.bodySmall)
                }
                if (gradual) {
                    Text("Твой план снижения", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(initialText, { initialText = it }, label = { Text("Начальный дневной лимит") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    OutlinedTextField(stepText, { stepText = it }, label = { Text("Снижать каждую неделю на") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    Text("Лимит меняется каждые 7 дней от даты начала до указанной цели.", style = MaterialTheme.typography.bodySmall)
                }
                Text("Когда выполнять", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    periodOptions.forEach { label ->
                        FilterChip(period == label, onClick = { period = label; if (label == "Сегодня") startText = store.today.toString(); if (label == "На дату") showDate = true }, label = { Text(label) })
                    }
                }
                if (period != "Сегодня") {
                    TextButton(onClick = { showDate = true }) { Text("С " + start.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))) }
                }
                if (period == "На неделю") Text("Каждый день в течение 7 дней, начиная с выбранной даты.", style = MaterialTheme.typography.bodySmall)
                if (period == "Повторять") Text("Каждый день, начиная с выбранной даты.", style = MaterialTheme.typography.bodySmall)
                if (period == "По дням недели") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEachIndexed { index, label ->
                            FilterChip(dayMask and (1 shl index) != 0, { dayMask = dayMask xor (1 shl index) }, label = { Text(label) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                    if (dayMask == 0) Text("Выбери хотя бы один день", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(enabled = valid && !saved, onClick = {
                if (!valid || saved) return@Button
                saved = true
                val actionPeriod = ActionPeriod(
                    start = start,
                    end = when (period) { "Сегодня", "На дату" -> start; "На неделю" -> start.plusDays(6); else -> null },
                    weekdays = if (period == "По дням недели") days else DayOfWeek.entries.toSet()
                )
                val actionCategory = template?.category?.title ?: category
                if (type == TrackerType.CHECK && actionPeriod.end == actionPeriod.start) {
                    store.addTask(title = title, dueDate = start.toString(), category = actionCategory, durationMinutes = template?.defaultMinutes ?: 30,
                        priority = template?.priority ?: 2, focus = template?.priority == 3,
                        checklist = if (template?.title == "3 главных дела") (1..3).map { ChecklistItem(title = "Главный результат $it") } else emptyList())
                } else {
                    store.addTracker(Tracker(title = title, category = actionCategory, type = type, unit = unit,
                        target = if (numeric) target else null,
                        direction = if (!numeric || target == null) GoalDirection.RECORD else if (upperLimit) GoalDirection.AT_MOST else GoalDirection.AT_LEAST,
                        period = actionPeriod, initialTarget = if (gradual) initial else null, weeklyStep = if (gradual) step!! else 0.0))
                }
                onDismiss()
            }) { Text("Добавить", maxLines = 1) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
    if (showDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = start.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = {
            TextButton(enabled = state.selectedDateMillis != null, onClick = {
                state.selectedDateMillis?.let { startText = Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate().toString() }
                showDate = false
            }) { Text("Выбрать") }
        }, dismissButton = { TextButton(onClick = { showDate = false }) { Text("Отмена") } }) { DatePicker(state = state) }
    }
}
