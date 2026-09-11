package com.belov.maxplaner.ui.screens

import com.belov.maxplaner.data.categoryLabel

import androidx.compose.runtime.saveable.rememberSaveable
import com.belov.maxplaner.ui.components.ActionSetupDialog
import com.belov.maxplaner.ui.components.TrackerCard
import com.belov.maxplaner.data.TrackerType
import com.belov.maxplaner.ui.components.TaskEditorDialog
import com.belov.maxplaner.ui.components.CompletionButton
import androidx.compose.ui.text.style.TextDecoration

import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.components.PlannerProgressIndicator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.FocusMode
import com.belov.maxplaner.data.FOCUS_DURATION_MILLIS
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(store: PlannerStore) {
    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var showCatalog by rememberSaveable { mutableStateOf(false) }
    val selectedTask = store.tasks.firstOrNull { it.id == selectedTaskId }
    if (selectedTask != null) {
        TaskDetailScreen(store, selectedTask) { selectedTaskId = null }
        return
    }
    val today = store.today.toString()
    val tasks = store.tasks.filter { it.dueDate == today }
        .sortedWith(compareBy({ it.completed }, { !it.isFocus }, { -it.priority }, { it.startMinutes ?: Int.MAX_VALUE }))
    val habits = store.habits.filter { it.schedule.isScheduled(store.today) }
    val trackers = store.trackers.filter { it.period.includes(store.today) }
    val plannedActions = trackers.filter { it.type == TrackerType.CHECK || it.type == TrackerType.STREAK }
    val metrics = trackers.filter { it.type != TrackerType.CHECK && it.type != TrackerType.STREAK }
    val done = plannedActions.count { it.isComplete(store.today) } + tasks.count { it.completed } + habits.count { today in it.completedDates }
    val total = tasks.size + habits.size + plannedActions.size
    val progress = if (total == 0) 0f else done.toFloat() / total
    val keyTasks = tasks.filter { !it.completed }.take(2)
    val keyTaskIds = keyTasks.map { it.id }.toSet()
    val remainingTasks = tasks.filterNot { it.id in keyTaskIds }
    val greeting = when (java.time.LocalTime.now().hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }
    val tokens = LocalStyleTokens.current
    LazyColumn(
        state = rememberLazyListState(), modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = tokens.screenPadding, end = tokens.screenPadding, top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.sectionSpacing)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("PrimePlaner", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFFE8C56A))
                        Text("by Belov", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF13283A)) {
                        Icon(Icons.Rounded.NotificationsNone, contentDescription = "Уведомления", tint = Color(0xFF9EB5C6), modifier = Modifier.padding(10.dp).size(22.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(greeting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(
                    store.today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            WeekStrip(store.today)
        }
        item {
            PlannerCard(hero = true, colors = CardDefaults.cardColors(containerColor = Color(0xFF102437))) {
                Row(
                    Modifier.fillMaxWidth().padding(tokens.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Сегодня", style = MaterialTheme.typography.labelLarge, color = Color(0xFF8CA7BC))
                        Text(
                            if (total == 0) "Собери свой день" else if (done == total) "План выполнен" else "Держим ритм",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (total == 0) "Добавь первое действие" else "$done из $total выполнено",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (total > 0) {
                            PlannerProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { if (total == 0) 0f else progress },
                            modifier = Modifier.size(72.dp),
                            strokeWidth = 6.dp,
                            color = Color(0xFFE8C56A),
                            trackColor = Color(0xFF24384A)
                        )
                        Text(
                            if (total == 0) "0%" else "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE8C56A)
                        )
                    }
                }
            }
        }
        if (keyTasks.isNotEmpty()) {
            item { SectionTitle("Главное на сегодня") }
            items(keyTasks, key = { "key-${it.id}" }) { task ->
                TodayTaskRow(store, task, true) { selectedTaskId = task.id }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Быстрое добавление")
                val stackQuickAdd = LocalDensity.current.fontScale > 1.2f
                if (stackQuickAdd) Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showCatalog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF13283A)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF5CA8FF).copy(alpha = .14f)) {
                                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFF78B7FF), modifier = Modifier.padding(10.dp).size(24.dp))
                            }
                            Text("Готовое", fontWeight = FontWeight.SemiBold)
                            Text("Из каталога", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showAdd = true },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF182535)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFE8C56A).copy(alpha = .12f)) {
                                Icon(Icons.Rounded.Add, null, tint = Color(0xFFE8C56A), modifier = Modifier.padding(10.dp).size(24.dp))
                            }
                            Text("Создать своё", fontWeight = FontWeight.SemiBold)
                            Text("Своя цель", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAddCard("Готовое", "Из каталога", Icons.Rounded.AutoAwesome) { showCatalog = true }
                    QuickAddCard("Создать своё", "Своя цель", Icons.Rounded.Add) { showAdd = true }
                }
            }
        }
        item { PrimeCategoryGrid() }
        if (total > 0) {
            item { SectionTitle("Сегодня по плану") }
            items(remainingTasks, key = { "task-${it.id}" }) { task ->
                TodayTaskRow(store, task, false) { selectedTaskId = task.id }
            }
            items(habits, key = { "habit-${it.id}" }) { habit ->
                PlannerCard(modifier = Modifier.fillMaxWidth(), shape = tokens.compactShape) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompletionButton(today in habit.completedDates, habit.title) { store.toggleHabitToday(habit.id) }
                        Column(Modifier.weight(1f)) {
                            Text(habit.title, style = MaterialTheme.typography.titleMedium)
                            Text((habit.startMinutes?.let { com.belov.maxplaner.data.timeRange(it, habit.durationMinutes) + " · " } ?: "Привычка · ") + "серия ${store.streak(habit)}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        items(plannedActions, key = { "action-${it.id}" }) { TrackerCard(store, it) }
        item { SectionTitle("Фокус") }
        item { FocusTimer(store) }
        if (metrics.isNotEmpty()) {
            item { SectionTitle("Мои показатели") }
            items(metrics, key = { "metric-${it.id}" }) { TrackerCard(store, it) }
        }
        if (total > 0 || store.focusMinutes > 0) {
            item {
                PlannerCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Итог дня", style = MaterialTheme.typography.titleMedium)
                        if (total > 0) Text("Выполнено $done из $total")
                        val streak = habits.maxOfOrNull { store.streak(it) } ?: 0
                        if (streak > 0) Text("Самая длинная текущая серия: $streak")
                        if (store.focusMinutes > 0) Text("Фокус за всё время: ${store.focusMinutes} мин", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (showAdd) ActionSetupDialog(store, onDismiss = { showAdd = false })
    if (showCatalog) ActionCatalogDialog(store, onDismiss = { showCatalog = false })
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TodayTaskRow(store: PlannerStore, task: com.belov.maxplaner.data.PlannerTask, prominent: Boolean, onOpen: () -> Unit) {
    PlannerCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen), shape = LocalStyleTokens.current.compactShape,
        colors = CardDefaults.cardColors(containerColor = if (prominent) Color(0xFF162432) else Color(0xFF101C29))) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null)
                Text(task.startMinutes?.let { "%02d:%02d · %d мин".format(it / 60, it % 60, task.durationMinutes) } ?: categoryLabel(task.category),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, "Открыть детали", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FocusTimer(store: PlannerStore) {
    val session = store.focusState.session
    val remaining = session.remainingMillis(store.focusClock)
    val seconds = (remaining + 999) / 1000
    val running = session.mode == FocusMode.Running
    val status = when (session.mode) {
        FocusMode.Idle -> "25 минут на главное"
        FocusMode.Running -> "Время фокуса"
        FocusMode.Paused -> "На паузе"
        FocusMode.Completed -> "Готово · +25 минут фокуса"
    }
    PlannerCard(shape = LocalStyleTokens.current.cardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timer, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%02d:%02d".format(seconds / 60, seconds % 60), style = MaterialTheme.typography.headlineMedium)
                }
            }
            PlannerProgressIndicator(
                progress = { 1f - remaining.toFloat() / FOCUS_DURATION_MILLIS },
                modifier = Modifier.fillMaxWidth()
            )
            FilledTonalButton(
                onClick = { if (running) store.pauseFocus() else store.startFocus() },
                modifier = Modifier.fillMaxWidth(),
                shape = LocalStyleTokens.current.pillShape
            ) {
                Text(when (session.mode) {
                    FocusMode.Running -> "Пауза"
                    FocusMode.Paused -> "Продолжить"
                    FocusMode.Completed -> "Ещё 25 минут"
                    FocusMode.Idle -> "Начать фокус"
                })
            }
        }
    }
}

@Composable
private fun WeekStrip(today: java.time.LocalDate) {
    val start = today.minusDays(3)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        repeat(7) { offset ->
            val date = start.plusDays(offset.toLong())
            val selected = date == today
            PlannerSurface(
                shape = LocalStyleTokens.current.pillShape,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .16f) else MaterialTheme.colorScheme.surface,
                selected = selected,
                modifier = Modifier.width(44.dp)
            ) {
                Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("ru")).take(2).uppercase(), style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Text(date.dayOfMonth.toString(), fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun PrimeCategoryGrid() {
    val categories = listOf(
        Triple("Здоровье", Icons.Rounded.Favorite, Color(0xFF52D98B)),
        Triple("Спорт", Icons.Rounded.FitnessCenter, Color(0xFF5CA8FF)),
        Triple("Развитие", Icons.Rounded.Psychology, Color(0xFFB98AFF)),
        Triple("Работа", Icons.Rounded.Work, Color(0xFFFFB45C)),
        Triple("Общение", Icons.Rounded.Groups, Color(0xFFFF7FA8)),
        Triple("Финансы", Icons.Rounded.AccountBalanceWallet, Color(0xFFE8C56A))
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Категории")
        categories.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (title, icon, tint) ->
                    Surface(
                        modifier = Modifier.weight(1f).heightIn(min = 108.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF101C29),
                        tonalElevation = 1.dp
                    ) {
                        Column(Modifier.padding(vertical = 16.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(14.dp), color = tint.copy(alpha = .14f)) {
                                Icon(icon, null, tint = tint, modifier = Modifier.padding(10.dp).size(26.dp))
                            }
                            Text(title, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
