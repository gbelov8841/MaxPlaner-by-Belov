package com.belov.maxplaner.ui.screens

import com.belov.maxplaner.ui.icons.PrimeIcons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.size
import com.belov.maxplaner.data.categoryLabel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ui.components.ActionSetupDialog
import com.belov.maxplaner.ui.components.TrackerCard
import com.belov.maxplaner.data.ActionTemplate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import com.belov.maxplaner.data.ActionCatalog
import com.belov.maxplaner.data.ActionCategory
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.TrackerType
import com.belov.maxplaner.ui.components.CompletionButton
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.TaskEditorDialog
import com.belov.maxplaner.ui.theme.LocalStyleTokens

@Composable
fun TasksV2Screen(store: PlannerStore) {
    var showCustom by rememberSaveable { mutableStateOf(false) }
    var showCatalog by rememberSaveable { mutableStateOf(false) }

    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedTask = store.tasks.firstOrNull { it.id == selectedTaskId }
    if (selectedTask != null) {
        TaskDetailScreen(store, selectedTask) { selectedTaskId = null }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Мои дела", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
                Text("Выбери готовое действие или добавь своё", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { showCatalog = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                    Text("Выбрать готовое", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = { showCustom = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                    Text("Создать своё", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        if (store.tasks.isEmpty() && store.trackers.isEmpty()) {
            item {
                PlannerCard {
                    Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding)) {
                        Text("Добавь первое дело", style = MaterialTheme.typography.titleMedium)
                        Text("Готовые варианты уже собраны по понятным категориям.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (store.trackers.isNotEmpty()) {
            item { Text("Мои действия и показатели", style = MaterialTheme.typography.titleLarge) }
            items(store.trackers, key = { "tracker-${it.id}" }) { tracker -> TrackerCard(store, tracker) }
            if (store.tasks.isNotEmpty()) item { Text("Дела", style = MaterialTheme.typography.titleLarge) }
        }
        val sorted = store.tasks.sortedWith(compareBy({ it.completed }, { -it.priority }))
        items(sorted, key = { it.id }) { task ->
            PlannerCard(modifier = Modifier.fillMaxWidth(), onClick = { selectedTaskId = task.id }) {
                Row(
                    Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
                    Column(Modifier.weight(1f).padding(vertical = if (LocalDensity.current.fontScale > 1.2f) 4.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(if (LocalDensity.current.fontScale > 1.2f) 6.dp else 3.dp)) {
                        Text(
                            task.title,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else null
                        )
                        Text(
                            task.startMinutes?.let { com.belov.maxplaner.data.timeRange(it, task.durationMinutes) } ?: categoryLabel(task.category),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { store.deleteTask(task.id) }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Удалить")
                    }
                }
            }
        }
    }

    if (showCustom) ActionSetupDialog(store, onDismiss = { showCustom = false })
    if (showCatalog) ActionCatalogDialog(store, onDismiss = { showCatalog = false })
}

@Composable
internal fun ActionCatalogDialog(store: PlannerStore, onDismiss: () -> Unit, initialCategory: ActionCategory? = null) {
    var category by rememberSaveable(initialCategory) { mutableStateOf(initialCategory) }
    var selectedTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var custom by rememberSaveable { mutableStateOf(false) }
    var showCategories by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val selected = ActionCatalog.templates.firstOrNull { it.title == selectedTitle }
    if (selected != null || custom) {
        ActionSetupDialog(store, onDismiss = onDismiss, template = selected, category = category?.title ?: "Личное")
        return
    }
    val visible = if (query.isNotBlank()) ActionCatalog.templates.filter { it.title.contains(query.trim(), ignoreCase = true) }
        else category?.let { c -> ActionCatalog.templates.filter { it.category == c } } ?: ActionCatalog.popular
    val tokens = LocalStyleTokens.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = tokens.heroShape,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (tokens.floatingGlass) .90f else 1f),
        tonalElevation = tokens.heroElevation,
        title = { Text(category?.title?.let(::categoryLabel) ?: "Популярное") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(query, { query = it }, label = { Text("Найти действие") }, singleLine = true, shape = tokens.compactShape, modifier = Modifier.fillMaxWidth()) }
                item {
                    TextButton(onClick = { showCategories = !showCategories }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (showCategories) "Скрыть категории" else "Все категории · 11") }
                    AnimatedVisibility(
                        visible = showCategories,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryRow(PrimeIcons.Progress, "Популярное", "Часто выбирают", category == null) { category = null; query = ""; showCategories = false }
                            ActionCategory.entries.forEach { c ->
                                val meta = categoryMeta(c)
                                CategoryRow(meta.first, c.title.substringAfter(" "), meta.second, category == c) { category = c; query = ""; showCategories = false }
                            }
                        }
                    }
                }
                if (category != null) item {
                    val meta = categoryMeta(category!!)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = tokens.cardShape,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .72f)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(meta.first, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(category!!.title.substringAfter(" "), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                Text(meta.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${visible.size} готовых вариантов", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                item { OutlinedButton(onClick = { custom = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = LocalStyleTokens.current.pillShape) { Icon(Icons.Rounded.Edit, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Создать своё", maxLines = 1) } }
                if (visible.isEmpty()) item { Text("Ничего не найдено. Можно создать своё действие.") }
                items(visible, key = { it.title }) { action ->
                    PlannerCard(modifier = Modifier.fillMaxWidth(), onClick = { selectedTitle = action.title }) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Icon(categoryMeta(action.category).first, null, modifier = Modifier.padding(10.dp).size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(action.title, fontWeight = FontWeight.SemiBold, maxLines = 2)
                                Text(actionTypeLabel(action), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Открыть", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Закрыть") } }
    )
}

@Composable
private fun CategoryRow(icon: ImageVector, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.015f else 1f, tween(180), label = "categoryScale")
    com.belov.maxplaner.ui.components.PlannerSurface(
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.clickable(onClick = onClick),
        shape = LocalStyleTokens.current.compactShape,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .13f) else MaterialTheme.colorScheme.surfaceVariant,
        selected = selected
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Открыть раздел", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun categoryMeta(category: ActionCategory): Pair<ImageVector, String> = when (category) {
    ActionCategory.HEALTH -> PrimeIcons.Health to "Сон, вода и самочувствие"
    ActionCategory.SPORT -> PrimeIcons.Sport to "Тренировки и движение"
    ActionCategory.HABITS -> PrimeIcons.Shield to "Привычки и ограничения"
    ActionCategory.NUTRITION -> PrimeIcons.Health to "Питание и режим"
    ActionCategory.MENTAL -> PrimeIcons.Health to "Состояние и восстановление"
    ActionCategory.DEVELOPMENT -> PrimeIcons.Book to "Чтение, речь и обучение"
    ActionCategory.PRODUCTIVITY -> PrimeIcons.Progress to "Фокус и важные дела"
    ActionCategory.RELATIONSHIPS -> PrimeIcons.Home to "Близкие и общение"
    ActionCategory.DIGITAL -> PrimeIcons.Shield to "Экран и цифровые привычки"
    ActionCategory.FINANCE -> PrimeIcons.Wallet to "Бюджет и накопления"
    ActionCategory.HOME -> PrimeIcons.Home to "Порядок и бытовые дела"
}

private fun actionTypeLabel(action: ActionTemplate): String = when (action.type) {
    TrackerType.CHECK -> "Отметить выполненным"
    TrackerType.COUNTER -> if (action.unit.isNotBlank()) "Считать · ${action.unit}" else "Считать количество"
    TrackerType.NUMBER -> if (action.unit.isNotBlank()) "Записать · ${action.unit}" else "Записать значение"
    TrackerType.DURATION -> "По времени · ${action.target?.toInt() ?: action.defaultMinutes} мин"
    TrackerType.STREAK -> "Серия дней"
    TrackerType.SCALE -> "Оценка по шкале"
    TrackerType.REDUCTION_GOAL -> "Постепенно уменьшать"
    TrackerType.INCREASE_GOAL -> "Постепенно увеличивать"
}
