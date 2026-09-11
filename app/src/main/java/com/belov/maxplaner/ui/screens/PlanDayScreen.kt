package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.components.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun PlanDayScreen(store: PlannerStore, dateText: String, onDateChange: (LocalDate) -> Unit) {
    val date = runCatching { LocalDate.parse(dateText) }.getOrDefault(store.today)
    var mode by rememberSaveable { mutableStateOf("День") }
    var add by rememberSaveable { mutableStateOf(false) }
    var addTime by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedTask = store.tasks.firstOrNull { it.id == selectedTaskId }
    if (selectedTask != null) { TaskDetailScreen(store, selectedTask) { selectedTaskId = null }; return }
    val entries = agendaItems(store.tasks, store.habits, store.trackers, date)
    val previous = agendaItems(store.tasks, store.habits, store.trackers, date.minusDays(1))
    val segments = (entries + previous).mapNotNull { segmentOn(it, date) }.sortedBy { it.start }
    val selected = (entries + previous).firstOrNull { it.key == selectedKey }
    fun open(item: AgendaItem) {
        if (item.kind == AgendaKind.TASK) selectedTaskId = item.id else selectedKey = item.key
    }
    fun addAt(time: Int?) { addTime = time; add = true }
    fun step(direction: Long) {
        onDateChange(when (mode) { "Неделя" -> date.plusWeeks(direction); "Месяц" -> date.plusMonths(direction); else -> date.plusDays(direction) })
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("План дня", style = MaterialTheme.typography.headlineLarge)
                    Text(date.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru"))), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onDateChange(store.today); mode = "День" }) { Text("Сегодня") }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { step(-1) }) { Icon(Icons.Rounded.ChevronLeft, "Предыдущий период") }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("День", "Неделя", "Месяц").forEach { option ->
                        PlannerSurface(modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { this.selected = mode == option },
                            selected = mode == option, onClick = { mode = option }) {
                            Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(option, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                IconButton(onClick = { step(1) }) { Icon(Icons.Rounded.ChevronRight, "Следующий период") }
            }
        }
        if (mode == "День") {
            item { PlannerWeekStrip(date, store.today, onDateChange) }
            item {
                PlannerCard(modifier = Modifier.fillMaxWidth()) {
                    PanelHeading("Без времени")
                    val untimed = entries.filter { it.startMinutes == null }
                    if (untimed.isEmpty()) Text("Нет дел без времени", Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    untimed.forEach { item -> AgendaCompactRow(store, item, date) { open(item) } }
                    InlineAdd { addAt(null) }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Расписание", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { addAt(9 * 60) }) { Icon(Icons.Rounded.Add, null, Modifier.size(18.dp)); Text("Добавить") }
                }
                Text("Дела по времени · свободные промежутки сокращены", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (segments.isEmpty()) item {
                PlannerCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Расписание свободно", Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                    InlineAdd("Запланировать дело") { addAt(9 * 60) }
                }
            }
            items(segments, key = { it.item.key }) { segment ->
                val conflicts = segments.filter { it.item.key != segment.item.key && it.start < segment.end && segment.start < it.end }
                Row(verticalAlignment = Alignment.Top) {
                    Text(timeOfDay(segment.start), Modifier.width(48.dp).padding(top = 20.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    PlannerCard(modifier = Modifier.weight(1f)) {
                        AgendaCompactRow(store, segment.item, date) { open(segment.item) }
                        if (segment.item.occurrenceDate < date) Text("Продолжение со вчера", Modifier.padding(horizontal = 14.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                        if (conflicts.isNotEmpty()) Text("Пересечение: " + conflicts.joinToString { it.item.title },
                            Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        } else if (mode == "Неделя") {
            val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            items((0L..6L).map(monday::plusDays), key = { it.toString() }) { day ->
                val items = agendaItems(store.tasks, store.habits, store.trackers, day)
                PlannerCard(modifier = Modifier.fillMaxWidth(), selected = day == date, onClick = { onDateChange(day); mode = "День" }) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))), style = MaterialTheme.typography.titleMedium)
                        Text(if (items.isEmpty()) "Свободно" else "${items.count { it.completed }} из ${items.size} выполнено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        items.sortedBy { it.startMinutes ?: Int.MAX_VALUE }.take(3).forEach { Text(it.title, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        } else {
            item { PlannerMonth(date, store.today, count = { agendaItems(store.tasks, store.habits, store.trackers, it).size }) { onDateChange(it); mode = "День" } }
        }
    }
    if (selected != null) AgendaEntryDialog(store, selected) { selectedKey = null }
    if (add) TaskEditorDialog(store = store, initialDate = date, initialStartMinutes = addTime, onDismiss = { add = false }, onSaved = { it?.let(onDateChange) })
}

@Composable
private fun AgendaCompactRow(store: PlannerStore, item: AgendaItem, visibleDate: LocalDate, onOpen: () -> Unit) {
    val task = store.tasks.firstOrNull { it.id == item.id && item.kind == AgendaKind.TASK }
    if (task != null) { PlannerTaskRow(store, task, onOpen); return }
    val tracker = store.trackers.firstOrNull { it.id == item.id && item.kind == AgendaKind.TRACKER }
    val checkable = tracker == null || tracker.type in listOf(TrackerType.CHECK, TrackerType.STREAK)
    PlannerSurface(modifier = Modifier.fillMaxWidth(), color = androidx.compose.ui.graphics.Color.Transparent, onClick = onOpen) {
        Row(Modifier.padding(end = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (checkable) CompletionButton(item.completed, item.title) {
                when (item.kind) {
                    AgendaKind.HABIT -> store.toggleHabitOn(item.id, item.occurrenceDate)
                    AgendaKind.TRACKER -> store.setTrackerValue(item.id, item.occurrenceDate, if (item.completed) null else 1.0)
                    else -> Unit
                }
            } else IconButton(onClick = onOpen) { Icon(Icons.Rounded.Tune, "Изменить показатель") }
            Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                Text(item.title, style = MaterialTheme.typography.bodyMedium)
                Text(item.startMinutes?.let { timeRange(it, item.durationMinutes) } ?: if (item.kind == AgendaKind.HABIT) "Привычка" else "Показатель",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, null, Modifier.size(18.dp))
        }
    }
}

@Composable
internal fun PlannerMonth(selected: LocalDate, today: LocalDate, count: (LocalDate) -> Int = { 0 }, onSelect: (LocalDate) -> Unit) {
    val month = YearMonth.from(selected)
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - 1
    val weeks = (offset + month.lengthOfMonth() + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(selected.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        repeat(weeks) { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(7) { dow ->
                    val number = week * 7 + dow - offset + 1
                    if (number !in 1..month.lengthOfMonth()) Spacer(Modifier.weight(1f)) else {
                        val day = month.atDay(number)
                        PlannerSurface(modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics {
                            this.selected = selected == day
                            contentDescription = day.toString() + if (day == today) ", сегодня" else ""
                        }, selected = day == selected, onClick = { onSelect(day) }) {
                            Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(number.toString(), style = MaterialTheme.typography.bodyMedium)
                                if (count(day) > 0) Text(count(day).toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
