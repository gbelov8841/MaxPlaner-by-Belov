package com.belov.maxplaner.ui.screens

import com.belov.maxplaner.ui.icons.PrimeIcons
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn

import com.belov.maxplaner.ui.components.TimeSlotFields
import com.belov.maxplaner.ui.components.EditTimeSlotDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.belov.maxplaner.data.timeRange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.Habit
import com.belov.maxplaner.data.HabitSchedule
import com.belov.maxplaner.data.HabitScheduleType
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerProgressIndicator
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import java.time.DayOfWeek
import java.time.LocalDate

private val habitDays = listOf(
    DayOfWeek.MONDAY to "Пн",
    DayOfWeek.TUESDAY to "Вт",
    DayOfWeek.WEDNESDAY to "Ср",
    DayOfWeek.THURSDAY to "Чт",
    DayOfWeek.FRIDAY to "Пт",
    DayOfWeek.SATURDAY to "Сб",
    DayOfWeek.SUNDAY to "Вс"
)

@Composable
fun HabitsV2Screen(store: PlannerStore) {
    var showCreate by rememberSaveable { mutableStateOf(false) }
    var editingHabitId by rememberSaveable { mutableStateOf<String?>(null) }
    val editingHabit = editingHabitId?.let { id -> store.habits.firstOrNull { it.id == id } }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            val scheduledToday = store.habits.filter { it.schedule.isScheduled(store.today) }
            val todayDone = scheduledToday.count { store.today.toString() in it.completedDates }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Привычки", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
                Text("Гибкий ритм без давления и чувства вины", color = MaterialTheme.colorScheme.onSurfaceVariant)
                PlannerCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                        Icon(PrimeIcons.Repeat, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Сегодня", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(if (store.habits.isEmpty()) "Добавь первую привычку" else if (scheduledToday.isEmpty()) "Сегодня день отдыха" else "$todayDone из ${scheduledToday.size} отмечено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Button(onClick = { showCreate = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = LocalStyleTokens.current.pillShape) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text("  Новая привычка", maxLines = 1)
                }
            }
        }

        if (store.habits.isEmpty()) {
            item {
                PlannerCard {
                    Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding)) {
                        Text("Начни с маленького шага", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Выбери удобные дни — PrimePlaner не будет ломать серию в дни отдыха.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(store.habits, key = { it.id }) { habit ->
            HabitScheduleCard(store, habit, onEdit = { editingHabitId = habit.id })
        }
    }

    if (showCreate) {
        HabitScheduleDialog(
            title = "Новая привычка",
            initialSchedule = HabitSchedule.Daily,
            allowName = true,
            onDismiss = { showCreate = false },
            onSave = { name, schedule, start, duration ->
                store.addHabit(name, schedule, start, duration)
                showCreate = false
            }
        )
    }

    if (editingHabit != null) {
        HabitScheduleDialog(
            title = editingHabit.title,
            initialSchedule = editingHabit.schedule,
            initialStart = editingHabit.startMinutes,
            initialDuration = editingHabit.durationMinutes,
            allowName = false,
            onDismiss = { editingHabitId = null },
            onSave = { _, schedule, start, duration ->
                store.updateHabitSchedule(editingHabit.id, schedule)
                store.updateHabitTime(editingHabit.id, start, duration)
                editingHabitId = null
            }
        )
    }
}

@Composable
private fun HabitScheduleCard(store: PlannerStore, habit: Habit, onEdit: () -> Unit) {
    var editTime by rememberSaveable(habit.id) { mutableStateOf(false) }
    val today = LocalDate.now()
    val todayKey = today.toString()
    val completed = todayKey in habit.completedDates
    val scheduledToday = store.isHabitScheduledToday(habit)
    val rate = store.habitCompletionRate(habit, 7)
    val haptic = LocalHapticFeedback.current
    val interaction = if (scheduledToday) {
        Modifier.toggleable(
            value = completed,
            role = Role.Checkbox,
            onValueChange = {
                store.toggleHabitToday(habit.id)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        )
    } else Modifier

    PlannerCard(
        modifier = Modifier.fillMaxWidth().then(interaction),
        shape = LocalStyleTokens.current.cardShape,
        selected = completed,
        colors = CardDefaults.cardColors(
            containerColor = if (completed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle,
                    contentDescription = if (scheduledToday) "Отметить привычку" else "Сегодня не запланировано"
                )
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(habit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(scheduleLabel(habit.schedule), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onEdit, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) { Icon(Icons.Rounded.Edit, "Изменить расписание") }
                IconButton(onClick = { store.deleteHabit(habit.id) }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) { Icon(Icons.Rounded.Delete, "Удалить") }
            }

            PlannerSurface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = LocalStyleTokens.current.compactShape,
                onClick = { editTime = true },
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(habit.startMinutes?.let { timeRange(it, habit.durationMinutes) } ?: "Назначить время", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text("Изменить", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(PrimeIcons.Repeat, contentDescription = null)
                Text(" ${store.streak(habit)} подряд", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (scheduledToday) {
                        if (completed) " · сегодня выполнено" else " · сегодня по плану"
                    } else " · сегодня выходной",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Text("Последние 7 дней", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                    Text("$rate%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
                PlannerProgressIndicator(progress = { rate / 100f }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
    if (editTime) EditTimeSlotDialog(habit.title, habit.startMinutes, habit.durationMinutes, { editTime = false }) { start, duration -> store.updateHabitTime(habit.id, start, duration) }
}

@Composable
private fun HabitScheduleDialog(
    title: String,
    initialSchedule: HabitSchedule,
    allowName: Boolean,
    onDismiss: () -> Unit,
    initialStart: Int? = null,
    initialDuration: Int = 30,
    onSave: (String, HabitSchedule, Int?, Int) -> Unit
) {
    var slotStart by rememberSaveable { mutableStateOf(initialStart) }
    var slotDuration by rememberSaveable { mutableStateOf(initialDuration.toString()) }
    val parsedDuration = slotDuration.toIntOrNull()
    var name by remember { mutableStateOf(if (allowName) "" else title) }
    var type by remember { mutableStateOf(initialSchedule.type) }
    var selectedDays by remember { mutableStateOf(initialSchedule.weekdays) }
    val valid = (slotStart == null || parsedDuration != null && parsedDuration in 15..720) && name.isNotBlank() && (type == HabitScheduleType.DAILY || selectedDays.isNotEmpty())
    val tokens = LocalStyleTokens.current

    AlertDialog(
        shape = tokens.heroShape,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (tokens.floatingGlass) .90f else 1f),
        tonalElevation = tokens.heroElevation,
        onDismissRequest = onDismiss,
        title = { Text(if (allowName) title else "Расписание · $title") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (allowName) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название") },
                        placeholder = { Text("Например: прогулка 30 минут") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = tokens.compactShape
                    )
                }

                TimeSlotFields(slotStart, slotDuration, { slotStart = it }, { slotDuration = it })
                Text("Повторять", fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScheduleModeButton(
                        label = "Каждый день",
                        selected = type == HabitScheduleType.DAILY,
                        modifier = Modifier.weight(1f)
                    ) { type = HabitScheduleType.DAILY }
                    ScheduleModeButton(
                        label = "По дням",
                        selected = type == HabitScheduleType.WEEKDAYS,
                        modifier = Modifier.weight(1f)
                    ) { type = HabitScheduleType.WEEKDAYS }
                }

                if (type == HabitScheduleType.WEEKDAYS) {
                    Text("Дни недели", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        habitDays.forEach { (day, label) ->
                            val selected = day in selectedDays
                            PlannerSurface(
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                shape = tokens.pillShape,
                                onClick = { selectedDays = if (selected) selectedDays - day else selectedDays + day },
                                selected = selected,
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    label,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (selectedDays.isEmpty()) {
                        Text("Выбери хотя бы один день", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                colors = ButtonDefaults.textButtonColors(disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = LocalStyleTokens.current.disabledAlpha)),
                onClick = {
                    val schedule = if (type == HabitScheduleType.DAILY) {
                        HabitSchedule.Daily
                    } else {
                        HabitSchedule(type = HabitScheduleType.WEEKDAYS, weekdays = selectedDays)
                    }
                    onSave(name.trim(), schedule, slotStart, parsedDuration ?: initialDuration)
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun ScheduleModeButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PlannerSurface(
        modifier = modifier.heightIn(min = 48.dp).selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = LocalStyleTokens.current.compactShape,
        selected = selected,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 13.dp),
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun scheduleLabel(schedule: HabitSchedule): String = when (schedule.type) {
    HabitScheduleType.DAILY -> "Каждый день"
    HabitScheduleType.WEEKDAYS -> habitDays
        .filter { (day, _) -> day in schedule.weekdays }
        .joinToString(" · ") { (_, label) -> label }
        .ifBlank { "Дни не выбраны" }
}
