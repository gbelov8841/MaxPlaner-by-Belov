package com.belov.maxplaner.ui.screens

import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.ChevronRight

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
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = store.habits.firstOrNull { it.id == selectedId }
    if (selected != null) { HabitDetailScreen(store, selected) { selectedId = null }; return }
    val today = store.today
    val scheduled = store.habits.filter { it.schedule.isScheduled(today) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Привычки", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = { showCreate = true }) { Icon(Icons.Rounded.Add, "Новая привычка") }
            }
            Text("Сегодня · ${scheduled.count { today.toString() in it.completedDates }} из ${scheduled.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (store.habits.isEmpty()) item {
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Начни с маленького шага", style = MaterialTheme.typography.titleMedium)
                    Text("Выбери привычку и удобные дни. Дни отдыха не прерывают серию.", style = MaterialTheme.typography.bodySmall)
                    com.belov.maxplaner.ui.components.InlineAdd("Новая привычка") { showCreate = true }
                }
            }
        }
        items(store.habits.sortedBy { !it.schedule.isScheduled(today) }, key = { it.id }) { habit ->
            PlannerCard(modifier = Modifier.fillMaxWidth(), onClick = { selectedId = habit.id }) {
                Row(Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (habit.schedule.isScheduled(today)) com.belov.maxplaner.ui.components.CompletionButton(today.toString() in habit.completedDates, habit.title) { store.toggleHabitToday(habit.id) }
                    else Icon(Icons.Rounded.Spa, null, Modifier.padding(14.dp).size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                        Text(habit.title, style = MaterialTheme.typography.bodyMedium)
                        Text(if (habit.schedule.isScheduled(today)) "Серия · ${store.streak(habit)}" else "Сегодня отдых", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Rounded.ChevronRight, null, Modifier.size(18.dp))
                }
            }
        }
    }
    if (showCreate) HabitScheduleDialog("Новая привычка", HabitSchedule.Daily, true, { showCreate = false }) { name, schedule, start, duration ->
        store.addHabit(name, schedule, start, duration); showCreate = false
    }
}

@Composable
internal fun HabitScheduleDialog(
    title: String,
    initialSchedule: HabitSchedule,
    allowName: Boolean,
    onDismiss: () -> Unit,
    initialStart: Int? = null,
    initialDuration: Int = 30,
    initialName: String = "",
    onSave: (String, HabitSchedule, Int?, Int) -> Unit
) {
    var slotStart by rememberSaveable { mutableStateOf(initialStart) }
    var slotDuration by rememberSaveable { mutableStateOf(initialDuration.toString()) }
    val parsedDuration = slotDuration.toIntOrNull()
    var name by rememberSaveable { mutableStateOf(if (allowName) initialName else title) }
    var type by remember { mutableStateOf(initialSchedule.type) }
    var selectedDays by remember { mutableStateOf(initialSchedule.weekdays) }
    val valid = (slotStart == null || parsedDuration != null && parsedDuration in 15..720) && name.isNotBlank() && (type == HabitScheduleType.DAILY || selectedDays.isNotEmpty())
    val tokens = LocalStyleTokens.current

    AlertDialog(
        shape = tokens.heroShape,
        containerColor = MaterialTheme.colorScheme.surface,
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
