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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.components.*
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(store: PlannerStore, displayName: String,
    onOpenPlan: (LocalDate) -> Unit, onOpenProgress: () -> Unit, onOpenHabits: () -> Unit) {
    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var showCatalog by rememberSaveable { mutableStateOf(false) }
    var showFocus by rememberSaveable { mutableStateOf(false) }
    val task = store.tasks.firstOrNull { it.id == selectedTaskId }
    if (task != null) { TaskDetailScreen(store, task) { selectedTaskId = null }; return }
    val date = store.today
    val tasks = store.tasks.filter { it.dueDate == date.toString() }
        .sortedWith(compareBy({ it.startMinutes ?: Int.MAX_VALUE }, { -it.priority }))
    val habits = store.habits.filter { it.schedule.isScheduled(date) }
    val trackers = store.trackers.filter { it.period.includes(date) }
    val done = tasks.count { it.completed }
    val greeting = when (LocalTime.now().hour) { in 5..11 -> "Доброе утро,"; in 12..17 -> "Добрый день,"; in 18..22 -> "Добрый вечер,"; else -> "Доброй ночи," }
    val seconds = (store.focusState.session.remainingMillis(store.focusClock) + 999) / 1000
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column {
                Text("PrimePlaner", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                Text("by Belov", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Text(greeting, style = MaterialTheme.typography.bodyMedium)
                if (displayName.isNotBlank()) Text(displayName, style = MaterialTheme.typography.headlineMedium)
                Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }
        }
        item { PlannerWeekStrip(date, date, onOpenPlan) }
        item {
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
                PanelHeading("План на сегодня", if (tasks.isEmpty()) "Открыть" else "$done из ${tasks.size}") { onOpenPlan(date) }
                if (tasks.isNotEmpty()) PlannerProgressIndicator({ done.toFloat() / tasks.size }, Modifier.fillMaxWidth().padding(horizontal = 14.dp))
                if (tasks.isEmpty()) Text("День пока свободен. Добавь первое дело.", Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                tasks.take(3).forEachIndexed { index, item ->
                    PlannerTaskRow(store, item, onOpen = { selectedTaskId = item.id })
                    if (index < minOf(tasks.size, 3) - 1) HorizontalDivider(Modifier.padding(horizontal = 14.dp), thickness = .5.dp)
                }
                if (tasks.size > 3) TextButton(onClick = { onOpenPlan(date) }, modifier = Modifier.fillMaxWidth()) { Text("Все дела · ${tasks.size}") }
                InlineAdd { showAdd = true }
            }
        }
        item {
            val habitText = "${habits.count { date.toString() in it.completedDates }} из ${habits.size}"
            val focusText = "%02d:%02d".format(seconds / 60, seconds % 60)
            if (LocalDensity.current.fontScale > 1.3f) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeShortcut("Привычки", habitText, Icons.Rounded.Spa, Modifier.fillMaxWidth(), onOpenHabits)
                HomeShortcut("Фокус", focusText, Icons.Rounded.Timer, Modifier.fillMaxWidth()) { showFocus = true }
                HomeShortcut("Каталог", "Готовые действия", Icons.Rounded.GridView, Modifier.fillMaxWidth()) { showCatalog = true }
            } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeShortcut("Привычки", habitText, Icons.Rounded.Spa, Modifier.weight(1f), onOpenHabits)
                HomeShortcut("Фокус", focusText, Icons.Rounded.Timer, Modifier.weight(1f)) { showFocus = true }
                HomeShortcut("Каталог", "Готовые действия", Icons.Rounded.GridView, Modifier.weight(1f)) { showCatalog = true }
            }
        }
        item {
            PlannerSurface(modifier = Modifier.fillMaxWidth(), onClick = onOpenProgress) {
                Row(Modifier.padding(14.dp).heightIn(min = 28.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.BarChart, null, Modifier.size(20.dp)); Spacer(Modifier.width(12.dp))
                    Text("Прогресс дня", Modifier.weight(1f)); Icon(Icons.Rounded.ChevronRight, null, Modifier.size(18.dp))
                }
            }
        }
        if (trackers.isNotEmpty()) item { Text("Мои действия и показатели", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
        items(trackers, key = { "tracker-${it.id}" }) { TrackerCard(store, it) }
    }
    if (showAdd) TaskEditorDialog(store = store, initialDate = date, initialStartMinutes = null, onDismiss = { showAdd = false })
    if (showCatalog) ActionCatalogDialog(store, onDismiss = { showCatalog = false })
    if (showFocus) AlertDialog(onDismissRequest = { showFocus = false }, title = { Text("Фокус") },
        text = { FocusTimer(store) }, confirmButton = { TextButton(onClick = { showFocus = false }) { Text("Закрыть") } })
}

@Composable
private fun HomeShortcut(title: String, subtitle: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    PlannerSurface(modifier = modifier, onClick = onClick) {
        Column(Modifier.padding(horizontal = 6.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, Modifier.size(22.dp))
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
internal fun FocusTimer(store: PlannerStore) {
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
