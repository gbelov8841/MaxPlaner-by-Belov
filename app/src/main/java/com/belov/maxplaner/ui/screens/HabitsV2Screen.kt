package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Привычки", style = MaterialTheme.typography.headlineLarge)
                Text("Гибкий ритм без давления и чувства вины", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { showCreate = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text("  Новая привычка")
                }
            }
        }

        if (store.habits.isEmpty()) {
            item {
                PlannerCard {
                    Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding)) {
                        Text("Начни с маленького шага", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Выбери удобные дни — MaxPlaner не будет ломать серию в дни отдыха.",
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
            onSave = { name, schedule ->
                store.addHabit(name, schedule)
                showCreate = false
            }
        )
    }

    if (editingHabit != null) {
        HabitScheduleDialog(
            title = editingHabit.title,
            initialSchedule = editingHabit.schedule,
            allowName = false,
            onDismiss = { editingHabitId = null },
            onSave = { _, schedule ->
                store.updateHabitSchedule(editingHabit.id, schedule)
                editingHabitId = null
            }
        )
    }
}

@Composable
private fun HabitScheduleCard(store: PlannerStore, habit: Habit, onEdit: () -> Unit) {
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
                IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, "Изменить расписание") }
                IconButton(onClick = { store.deleteHabit(habit.id) }) { Icon(Icons.Rounded.Delete, "Удалить") }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null)
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
}

@Composable
private fun HabitScheduleDialog(
    title: String,
    initialSchedule: HabitSchedule,
    allowName: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, HabitSchedule) -> Unit
) {
    var name by remember { mutableStateOf(if (allowName) "" else title) }
    var type by remember { mutableStateOf(initialSchedule.type) }
    var selectedDays by remember { mutableStateOf(initialSchedule.weekdays) }
    val valid = name.isNotBlank() && (type == HabitScheduleType.DAILY || selectedDays.isNotEmpty())

    AlertDialog(
        shape = LocalStyleTokens.current.heroShape,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = LocalStyleTokens.current.heroElevation,
        onDismissRequest = onDismiss,
        title = { Text(if (allowName) title else "Расписание · $title") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (allowName) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название") },
                        placeholder = { Text("Например: прогулка 30 минут") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                                modifier = Modifier.weight(1f).heightIn(min = 44.dp).clickable {
                                    selectedDays = if (selected) selectedDays - day else selectedDays + day
                                },
                                shape = LocalStyleTokens.current.pillShape,
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
                onClick = {
                    val schedule = if (type == HabitScheduleType.DAILY) {
                        HabitSchedule.Daily
                    } else {
                        HabitSchedule(type = HabitScheduleType.WEEKDAYS, weekdays = selectedDays)
                    }
                    onSave(name.trim(), schedule)
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
