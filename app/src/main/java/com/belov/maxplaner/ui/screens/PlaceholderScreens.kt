package com.belov.maxplaner.ui.screens

import com.belov.maxplaner.ui.components.TaskEditorDialog
import com.belov.maxplaner.ui.components.DayTimeline
import com.belov.maxplaner.ui.components.AgendaEntryDialog
import com.belov.maxplaner.data.AgendaItem
import com.belov.maxplaner.data.agendaItems
import com.belov.maxplaner.ui.components.CompletionButton
import androidx.compose.ui.text.style.TextDecoration

import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.components.PlannerProgressIndicator

import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.Habit
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.PlannerTask
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private enum class CalendarMode(val label: String) { Day("День"), Week("Неделя"), Month("Месяц") }

@Composable
fun TasksScreen(store: PlannerStore) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            Header("Задачи", "Все дела в одном месте")
            Button(onClick = { showAdd = true }, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Rounded.Add, null); Text("  Быстро добавить")
            }
        }
        val sorted = store.tasks.sortedWith(compareBy({ it.completed }, { -(it.priority) }))
        items(sorted, key = { it.id }) { task ->
            PlannerCard(shape = LocalStyleTokens.current.cardShape) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
                    Column(Modifier.weight(1f)) {
                        Text(task.title, fontWeight = FontWeight.Medium, textDecoration = if (task.completed) TextDecoration.LineThrough else null)
                        Text(task.dueDate ?: "Без даты", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { store.deleteTask(task.id) }) { Icon(Icons.Rounded.Delete, "Удалить") }
                }
            }
        }
    }
    if (showAdd) TaskEditorDialog(store = store, onDismiss = { showAdd = false })
}

@Composable
fun HabitsScreen(store: PlannerStore) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    val today = LocalDate.now().toString()
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            Header("Привычки", "Серии без давления и чувства вины")
            Button(onClick = { showAdd = true }, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Rounded.Add, null); Text("  Новая привычка")
            }
        }
        if (store.habits.isEmpty()) {
            item {
                PlannerCard {
                    Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding)) {
                        Text("Маленький шаг каждый день", style = MaterialTheme.typography.titleMedium)
                        Text("Добавь одну привычку, которую легко выполнить сегодня.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        items(store.habits, key = { it.id }) { habit -> HabitCard(store, habit, today) }
    }
    if (showAdd) SimpleNameDialog("Новая привычка", "Например: прогулка 30 минут", { showAdd = false }) {
        store.addHabit(it); showAdd = false
    }
}

@Composable
private fun HabitCard(store: PlannerStore, habit: Habit, today: String) {
    val completed = habit.completedDates.contains(today)
    val haptic = LocalHapticFeedback.current
    PlannerCard(
        modifier = Modifier.fillMaxWidth().toggleable(
            value = completed,
            role = Role.Checkbox,
            onValueChange = {
                store.toggleHabitToday(habit.id)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        ),
        shape = LocalStyleTokens.current.cardShape,
        selected = completed,
        colors = CardDefaults.cardColors(containerColor = if (completed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle, null)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(habit.title, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocalFireDepartment, null)
                    Text(" ${store.streak(habit)} дней подряд", style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = { store.deleteHabit(habit.id) }) { Icon(Icons.Rounded.Delete, "Удалить") }
        }
    }
}

@Composable
fun CalendarScreen(store: PlannerStore) {
    var mode by rememberSaveable { mutableStateOf(CalendarMode.Day) }
    var selectedDateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val selectedDate = LocalDate.parse(selectedDateText)
    var showAdd by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
        ) {
            item {
                CalendarHeader(selectedDate, mode) { selectedDateText = it.toString() }
                Spacer(Modifier.height(14.dp))
                CalendarModeSelector(mode) { mode = it }
            }
            when (mode) {
                CalendarMode.Day -> item { DayCalendar(store, selectedDate, onDateChange = { selectedDateText = it.toString() }) }
                CalendarMode.Week -> item { WeekCalendar(store, selectedDate, onDateChange = { selectedDateText = it.toString(); mode = CalendarMode.Day }) }
                CalendarMode.Month -> item { MonthCalendar(store, selectedDate, onDateChange = { selectedDateText = it.toString(); mode = CalendarMode.Day }) }
            }
        }
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(LocalStyleTokens.current.cardPadding),
            containerColor = MaterialTheme.colorScheme.primary
        ) { Icon(Icons.Rounded.Add, contentDescription = "Добавить") }
    }

    if (showAdd) TaskEditorDialog(
        store = store, initialDate = selectedDate, initialStartMinutes = 9 * 60,
        onDismiss = { showAdd = false },
        onSaved = { date -> if (date != null) selectedDateText = date.toString() }
    )
}

@Composable
private fun CalendarHeader(selectedDate: LocalDate, mode: CalendarMode, onDateChange: (LocalDate) -> Unit) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("План дня", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFE8C56A))
        Text(
            "Расписание · " + selectedDate.format(monthFormatter).replaceFirstChar { it.uppercase() },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val period = when (mode) {
            CalendarMode.Day -> "день"
            CalendarMode.Week -> "неделю"
            CalendarMode.Month -> "месяц"
        }
        fun move(amount: Long) {
            onDateChange(when (mode) {
                CalendarMode.Day -> selectedDate.plusDays(amount)
                CalendarMode.Week -> selectedDate.plusWeeks(amount)
                CalendarMode.Month -> selectedDate.plusMonths(amount)
            })
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { move(-1) }) {
                Icon(Icons.Rounded.ChevronLeft, "На $period назад")
            }
            TextButton(onClick = { onDateChange(LocalDate.now()) }, modifier = Modifier.weight(1f)) {
                Text("Сегодня")
            }
            IconButton(onClick = { move(1) }) {
                Icon(Icons.Rounded.ChevronRight, "На $period вперёд")
            }
        }
    }
}

@Composable
private fun CalendarModeSelector(selected: CalendarMode, onSelect: (CalendarMode) -> Unit) {
    Row(
        Modifier.fillMaxWidth().selectableGroup().clip(LocalStyleTokens.current.compactShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CalendarMode.entries.forEach { mode ->
            val active = mode == selected
            PlannerSurface(
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).selectable(selected = active, role = Role.Tab, onClick = { onSelect(mode) }),
                shape = LocalStyleTokens.current.pillShape,
                selected = active,
                color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    mode.label,
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayCalendar(store: PlannerStore, date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    val locale = Locale("ru")
    val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = (0L..6L).map { weekStart.plusDays(it) }
    val now = LocalTime.now()
    val today = LocalDate.now()

    Column(verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekDays.forEach { day ->
                val active = day == date
                PlannerSurface(
                    modifier = Modifier.weight(1f).clickable { onDateChange(day) },
                    shape = LocalStyleTokens.current.compactShape,
                    selected = active,
                    color = if (active) androidx.compose.ui.graphics.Color(0xFF1E3B31) else androidx.compose.ui.graphics.Color(0xFF0E1A27)
                ) {
                    Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).take(2).uppercase(), style = MaterialTheme.typography.labelSmall)
                        Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        var selectedKey by rememberSaveable(date.toString()) { mutableStateOf<String?>(null) }
        var addAt by rememberSaveable(date.toString()) { mutableStateOf<Int?>(null) }
        DayTimeline(store, date, onOpen = { selectedKey = it.key }, onAdd = { addAt = it })
        val selected = (agendaItems(store.tasks, store.habits, store.trackers, date) +
            agendaItems(store.tasks, store.habits, store.trackers, date.minusDays(1))).firstOrNull { it.key == selectedKey }
        if (selected != null) AgendaEntryDialog(store, selected) { selectedKey = null }
        if (addAt != null) TaskEditorDialog(store = store, initialDate = date, initialStartMinutes = addAt, onDismiss = { addAt = null })

    }
}

@Composable
private fun TimeBlock(task: PlannerTask, onToggle: () -> Unit) {
    val start = task.startMinutes ?: 0
    val end = start + task.durationMinutes
    val time = "%02d:%02d–%02d:%02d".format(start / 60, start % 60, (end / 60) % 24, end % 60) +
        if (end >= 24 * 60) " (+1 день)" else ""
    val container = when (task.priority) {
        3 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    PlannerSurface(shape = LocalStyleTokens.current.compactShape, color = container) {
        Row(Modifier.fillMaxWidth().padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            CompletionButton(task.completed, task.title, onToggle)
            Column(Modifier.weight(1f).padding(vertical = 5.dp)) {
                Text(task.title, fontWeight = FontWeight.SemiBold, textDecoration = if (task.completed) TextDecoration.LineThrough else null)
                Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CalendarTaskRow(task: PlannerTask, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CompletionButton(task.completed, task.title, onToggle)
        Column(Modifier.weight(1f)) {
            Text(task.title, textDecoration = if (task.completed) TextDecoration.LineThrough else null)
            task.startMinutes?.let { start ->
                Text("%02d:%02d · %d мин".format(start / 60, start % 60, task.durationMinutes),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WeekCalendar(store: PlannerStore, selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit) {
    val locale = Locale("ru")
    val monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = (0L..6L).map { monday.plusDays(it) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        days.forEach { day ->
            val tasks = store.tasks.filter { it.dueDate == day.toString() }.sortedBy { it.startMinutes ?: Int.MAX_VALUE }
            PlannerCard(
                modifier = Modifier.fillMaxWidth().clickable { onDateChange(day) },
                shape = LocalStyleTokens.current.cardShape,
                selected = day == selectedDate,
                colors = CardDefaults.cardColors(containerColor = if (day == selectedDate) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(day.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                    val otherCount = agendaItems(store.tasks, store.habits, store.trackers, day).size - tasks.size
                    if (otherCount > 0) Text("Привычки и действия: $otherCount · открыть день", style = MaterialTheme.typography.bodySmall)
                    if (tasks.isEmpty() && otherCount == 0) Text("Свободно", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    tasks.forEach { task ->
                        CalendarTaskRow(task) { store.toggleTask(task.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendar(store: PlannerStore, selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit) {
    val month = YearMonth.from(selectedDate)
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value - 1
    val cells = buildList<LocalDate?> {
        repeat(leading) { add(null) }
        for (d in 1..month.lengthOfMonth()) add(month.atDay(d))
        while (size % 7 != 0) add(null)
    }
    val locale = Locale("ru")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { dow ->
                Text(
                    dow.getDisplayName(TextStyle.SHORT, locale).take(2).uppercase(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f).height(56.dp))
                    } else {
                        val count = agendaItems(store.tasks, store.habits, store.trackers, day).size
                        val active = day == selectedDate
                        PlannerSurface(
                            modifier = Modifier.weight(1f).height(56.dp).clickable { onDateChange(day) },
                            shape = LocalStyleTokens.current.pillShape,
                            selected = active,
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(day.dayOfMonth.toString(), fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                                if (count > 0) Text("•".repeat(count.coerceAtMost(3)), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        val selectedTasks = store.tasks.filter { it.dueDate == selectedDate.toString() }.sortedBy { it.startMinutes ?: Int.MAX_VALUE }
        PlannerCard(shape = LocalStyleTokens.current.cardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("План на ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, locale)}", fontWeight = FontWeight.SemiBold)
                if (selectedTasks.isEmpty()) Text("На этот день ничего не запланировано", color = MaterialTheme.colorScheme.onSurfaceVariant)
                selectedTasks.forEach { CalendarTaskRow(it) { store.toggleTask(it.id) } }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(store: PlannerStore) {
    val total = store.tasks.size
    val completed = store.tasks.count { it.completed }
    val taskProgress = if (total == 0) 0f else completed.toFloat() / total
    val today = LocalDate.now().toString()
    val habitProgress = if (store.habits.isEmpty()) 0f else store.habits.count { it.completedDates.contains(today) }.toFloat() / store.habits.size
    val bestStreak = store.habits.maxOfOrNull { store.streak(it) } ?: 0

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item { Header("Прогресс", "Только полезные цифры, без визуального шума") }
        item { ProgressCard("Задачи", "$completed из $total выполнено", taskProgress) }
        item { ProgressCard("Привычки сегодня", "${(habitProgress * 100).toInt()}% выполнено", habitProgress) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)) {
                StatCard("${store.focusMinutes}", "минут фокуса", Modifier.weight(1f))
                StatCard("$bestStreak", "лучшая серия", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProgressCard(title: String, subtitle: String, progress: Float) {
    PlannerCard(shape = LocalStyleTokens.current.cardShape) {
        Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PlannerProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    PlannerCard(modifier, shape = LocalStyleTokens.current.cardShape) {
        Column(Modifier.padding(LocalStyleTokens.current.cardPadding)) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SimpleNameDialog(title: String, hint: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        shape = LocalStyleTokens.current.heroShape,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = LocalStyleTokens.current.heroElevation,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, label = { Text(hint) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (text.isNotBlank()) onSave(text) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
