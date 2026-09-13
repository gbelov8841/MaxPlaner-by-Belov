package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ai.*
import com.belov.maxplaner.ui.components.*
import java.time.LocalDate
import java.util.UUID

internal fun amount(value: Double) = if (value % 1.0 == 0.0) value.toLong().toString() else "%.1f".format(value)
internal fun macroText(n: Nutrients) = "Б ${amount(n.protein)} г · Ж ${amount(n.fat)} г · У ${amount(n.carbs)} г"

@Composable
fun NutritionSummary(day: NutritionDay, onOpen: () -> Unit, onAdd: () -> Unit) {
    PlannerCard(Modifier.fillMaxWidth()) {
        PanelHeading("Питание сегодня", "Открыть", onOpen)
        Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${amount(day.consumed.kcal)} / ${day.target?.let { amount(it.kcal) } ?: "—"} ккал", style = MaterialTheme.typography.titleMedium)
            Text(macroText(day.consumed), style = MaterialTheme.typography.bodySmall)
            if (day.target == null) Text("Цель пока не задана", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else if (day.target.kcal > 0) PlannerProgressIndicator({ (day.consumed.kcal / day.target.kcal).toFloat().coerceIn(0f, 1f) }, Modifier.fillMaxWidth())
        }
        InlineAdd("Добавить еду", onAdd)
    }
}

@Composable
fun NutritionScreen(db: PrimeLocalStore, today: LocalDate, addRequested: Boolean = false, onAddRequestConsumed: () -> Unit = {}, onChanged: () -> Unit) {
    var dateText by rememberSaveable { mutableStateOf(today.toString()) }
    val date = LocalDate.parse(dateText)
    var revision by remember { mutableIntStateOf(0) }
    var adding by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(addRequested) { if (addRequested) { dateText = today.toString(); adding = true; onAddRequestConsumed() } }
    var targets by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<FoodEntry?>(null) }
    var deleting by remember { mutableStateOf<FoodEntry?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    fun changed() { revision++; onChanged() }
    val entries = remember(revision, date) { db.entries(date) }
    val day = remember(revision, date) { db.day(date) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Питание", style = MaterialTheme.typography.headlineLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { dateText = date.minusDays(1).toString() }) { Text("Раньше") }
                TextButton(onClick = { dateText = today.toString() }) { Text(date.toString()) }
                TextButton(onClick = { dateText = date.plusDays(1).toString() }) { Text("Позже") }
            }
        }
        item { PlannerCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Факт · ${amount(day.consumed.kcal)} ккал", style = MaterialTheme.typography.titleLarge)
            Text(macroText(day.consumed), style = MaterialTheme.typography.bodySmall)
            Text("Цель · ${day.target?.let { amount(it.kcal) + " ккал" } ?: "не задана"}")
            day.target?.let { Text(macroText(it), style = MaterialTheme.typography.bodySmall) }
            Text("Запланировано еды · ${amount(day.planned.kcal)} ккал", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { targets = true }) { Text("Изменить цель на день") }
        } } }
        item { Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) { Text("Добавить еду вручную") }
            Text("Укажи значения для своей порции с упаковки или из известного тебе источника. AI-распознавание пока не подключено.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (entries.isEmpty()) item { Text("Записей на этот день пока нет") }
        items(entries, key = { it.id }) { entry ->
            PlannerCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(entry.items.joinToString { it.name }, style = MaterialTheme.typography.titleMedium)
                Text(if (entry.state == FoodState.CONSUMED) "Съедено" else "Запланировано", style = MaterialTheme.typography.labelSmall)
                Text("${amount(entry.total.kcal)} ккал · ${macroText(entry.total)}", style = MaterialTheme.typography.bodySmall)
                entry.items.forEach { item -> Text(item.portion + when (item.source) {
                    FoodSource.PACKAGE_LABEL -> " · с упаковки"
                    FoodSource.USER -> " · введено вручную"
                    FoodSource.AI_ESTIMATE -> " · приблизительно: ${item.uncertainty}"
                }, style = MaterialTheme.typography.labelSmall) }
                Row {
                    if (entry.items.size == 1) TextButton(onClick = { selected = entry }) { Text("Изменить") }
                    if (entry.state == FoodState.PLANNED && entries.none { it.plannedEntryId == entry.id }) TextButton(onClick = {
                        if (db.confirmFood(entry.copy(id = UUID.randomUUID().toString(), state = FoodState.CONSUMED, plannedEntryId = entry.id))) changed()
                        else message = "Не удалось добавить запись"
                    }) { Text("Съедено") }
                    TextButton(onClick = { deleting = entry }) { Text("Удалить") }
                }
            } }
        }
        message?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
    }
    if (adding || selected != null) FoodEditor(date, selected, onDismiss = { adding = false; selected = null }) { entry ->
        val old = selected
        val saved = if (old == null) db.confirmFood(entry) else db.editFood(old, entry)
        if (saved) { changed(); adding = false; selected = null } else message = "Запись изменилась. Открой её заново"
    }
    if (targets) NutrientEditor("Цель на ${date}", day.target, { targets = false }) { n ->
        db.setTarget(NutritionTarget(date, n)); changed(); targets = false
    }
    deleting?.let { entry -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Удалить запись?") },
        text = { Text("Будет удалена только эта запись. Другие приёмы пищи сохранятся.") },
        confirmButton = { TextButton(onClick = { db.deleteFood(entry.id); changed(); deleting = null }) { Text("Удалить") } },
        dismissButton = { TextButton(onClick = { deleting = null }) { Text("Отмена") } }) }
}

@Composable
private fun FoodEditor(date: LocalDate, original: FoodEntry?, onDismiss: () -> Unit, onSave: (FoodEntry) -> Unit) {
    val first = original?.items?.singleOrNull()
    var name by rememberSaveable { mutableStateOf(first?.name.orEmpty()) }
    var portion by rememberSaveable { mutableStateOf(first?.portion.orEmpty()) }
    var label by rememberSaveable { mutableStateOf(first?.source == FoodSource.PACKAGE_LABEL) }
    val id = rememberSaveable { original?.id ?: UUID.randomUUID().toString() }
    NutrientEditor("Проверь продукты", first?.nutrients, onDismiss, extra = {
        OutlinedTextField(name, { name = it.take(300) }, label = { Text("Название еды") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(portion, { portion = it.take(300) }, label = { Text("Порция, например 180 г") }, modifier = Modifier.fillMaxWidth())
        FilterChip(label, { label = !label }, label = { Text("Данные с упаковки") })
        Text("Калории и БЖУ ниже — для всей указанной порции, не на 100 г.", style = MaterialTheme.typography.bodySmall)
    }, valid = name.isNotBlank() && portion.isNotBlank(), button = if (original == null) "Добавить в день" else "Сохранить") { n ->
        val item = FoodItem(name.trim(), portion.trim(), n, if (label) FoodSource.PACKAGE_LABEL else FoodSource.USER)
        onSave(original?.copy(items = listOf(item)) ?: FoodEntry(id, date, FoodState.CONSUMED, listOf(item)))
    }
}

@Composable
private fun NutrientEditor(title: String, initial: Nutrients?, onDismiss: () -> Unit,
    extra: @Composable () -> Unit = {}, valid: Boolean = true, button: String = "Сохранить", onSave: (Nutrients) -> Unit) {
    var kcal by rememberSaveable { mutableStateOf(initial?.let { amount(it.kcal) }.orEmpty()) }
    var protein by rememberSaveable { mutableStateOf(initial?.let { amount(it.protein) }.orEmpty()) }
    var fat by rememberSaveable { mutableStateOf(initial?.let { amount(it.fat) }.orEmpty()) }
    var carbs by rememberSaveable { mutableStateOf(initial?.let { amount(it.carbs) }.orEmpty()) }
    fun number(s: String) = s.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.0..100000.0 }
    val values = listOf(kcal, protein, fat, carbs).map(::number)
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            extra()
            listOf(Triple("Калории, ккал", kcal, { v: String -> kcal = v }), Triple("Белки, г", protein, { v: String -> protein = v }),
                Triple("Жиры, г", fat, { v: String -> fat = v }), Triple("Углеводы, г", carbs, { v: String -> carbs = v })).forEach { (label, value, update) ->
                OutlinedTextField(value, { update(it.take(12)) }, label = { Text(label) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
                    isError = value.isNotBlank() && number(value) == null)
            }
        }
    }, confirmButton = { TextButton(enabled = valid && values.all { it != null }, onClick = {
        onSave(Nutrients(values[0]!!, values[1]!!, values[2]!!, values[3]!!))
    }) { Text(button) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}
