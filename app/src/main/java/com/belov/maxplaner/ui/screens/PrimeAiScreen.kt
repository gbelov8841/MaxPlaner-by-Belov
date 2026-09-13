package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ai.*
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Composable
fun PrimeAiScreen(store: PlannerStore, onOpenMemory: () -> Unit, service: PrimeAiService = remember { UnavailablePrimeAiService() }) {
    var text by rememberSaveable { mutableStateOf("") }
    var days by rememberSaveable { mutableIntStateOf(7) }
    var preview by remember { mutableStateOf<PlanPreview?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun request() {
        busy = true; message = null
        val previous = preview
        val input = text
        val context = AiContext(PlanPeriod(store.today, store.today.plusDays(days.toLong() - 1)), ZoneId.systemDefault(), store.tasks.toList())
        scope.launch {
            try {
                when (val result = service.request(AiRequest(UUID.randomUUID().toString(), if (previous == null) AiCapability.CREATE_PLAN else AiCapability.REVISE_PLAN, input, context, previous))) {
                    is AiResult.Plan -> {
                        val p = result.preview
                        require(p.period == context.period)
                        preview = PlanPreviewEngine.create(p.id, p.period, context.tasks, p.changes, p.expiresAtEpochMillis)
                        message = "Изменения ещё не сохранены"
                    }
                    is AiResult.Questions -> message = result.questions.joinToString("\n")
                    is AiResult.Failure -> message = result.userMessage
                    is AiResult.Refusal -> message = result.message
                    else -> message = "Не удалось получить план. Попробуй изменить запрос"
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { message = "Prime AI временно недоступен" }
            finally { busy = false }
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Prime AI", style = MaterialTheme.typography.headlineLarge)
            Text("Опиши цель — проверь предложение — примени план", style = MaterialTheme.typography.bodySmall)
            if (service is UnavailablePrimeAiService) Text("AI пока не подключён. Задачи, питание и ручное планирование работают как обычно.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onOpenMemory) { Text("Мой профиль Prime AI") }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "День", 7 to "Неделя", 30 to "30 дней").forEach { (count, label) ->
                FilterChip(days == count, { days = count; preview = null }, label = { Text(label) }, enabled = !busy)
            }
        } }
        item { OutlinedTextField(text, { text = it.take(8000) }, modifier = Modifier.fillMaxWidth(), minLines = 3,
            enabled = !busy, label = { Text(if (preview == null) "Чего хочешь достичь?" else "Что изменить в предложении?") })
            Button(onClick = ::request, enabled = text.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (busy) "Готовим предложение…" else if (preview == null) "Создать предложение" else "Обновить предложение")
            }
        }
        message?.let { item { Text(it, style = MaterialTheme.typography.bodyMedium) } }
        preview?.let { p ->
            item { Text("Предлагаемые изменения", style = MaterialTheme.typography.titleMedium) }
            items(p.changes) { change -> PlannerCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${change.kind} · ${(change.after ?: change.before)?.title.orEmpty()}", style = MaterialTheme.typography.titleMedium)
                change.before?.let { Text("Было: ${it.title} · ${it.dueDate ?: "Без даты"} · ${it.startMinutes?.let { m -> "%02d:%02d".format(m / 60, m % 60) } ?: "Без времени"} · ${it.durationMinutes} мин", style = MaterialTheme.typography.bodySmall) }
                change.after?.let { Text("Будет: ${it.dueDate ?: "Без даты"} · ${it.startMinutes?.let { m -> "%02d:%02d".format(m / 60, m % 60) } ?: "Без времени"} · ${it.durationMinutes} мин", style = MaterialTheme.typography.bodySmall)
                    if (it.notes != change.before?.notes) Text("Заметки: ${change.before?.notes.orEmpty()} → ${it.notes}", style = MaterialTheme.typography.bodySmall)
                    if (it.category != change.before?.category) Text("Категория: ${change.before?.category ?: "—"} → ${it.category}", style = MaterialTheme.typography.bodySmall)
                    if (it.priority != change.before?.priority) Text("Приоритет: ${change.before?.priority ?: "—"} → ${it.priority}", style = MaterialTheme.typography.bodySmall)
                    if (it.isFocus != change.before?.isFocus) Text("Главное дело: ${change.before?.isFocus ?: false} → ${it.isFocus}", style = MaterialTheme.typography.bodySmall)
                }
            } } }
            item {
                Button(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                    when (val result = store.applyAiPreview(p, PlanApproval(p.id, p.revision))) {
                        ApplyPreviewResult.Applied, ApplyPreviewResult.AlreadyApplied -> { preview = null; message = "План сохранён" }
                        is ApplyPreviewResult.Rejected -> message = result.reason
                        else -> message = "Не удалось сохранить план"
                    }
                }) { Text("Применить план") }
                TextButton(enabled = !busy, onClick = { preview = null; message = null }, modifier = Modifier.fillMaxWidth()) { Text("Отменить предложение") }
            }
        }
    }
}

@Composable
fun PrimeMemoryScreen(db: PrimeLocalStore) {
    var revision by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<ConfirmedMemory?>(null) }
    var adding by remember { mutableStateOf(false) }
    val facts = remember(revision) { db.memory() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Мой профиль", style = MaterialTheme.typography.headlineLarge)
            Text("Только подтверждённые тобой сведения. Сейчас они хранятся на устройстве и не отправляются AI.", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { adding = true }) { Text("Добавить предпочтение") }
        }
        items(facts, key = { it.key }) { fact -> PlannerCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) {
            Text(fact.value)
            Row { TextButton(onClick = { editing = fact }) { Text("Изменить") }
                TextButton(onClick = { db.forgetMemory(fact.key); revision++ }) { Text("Забыть") } }
        } } }
    }
    if (adding || editing != null) {
        val old = editing
        var value by rememberSaveable(old?.key) { mutableStateOf(old?.value.orEmpty()) }
        AlertDialog(onDismissRequest = { adding = false; editing = null }, title = { Text("Что учитывать?") },
            text = { OutlinedTextField(value, { value = it.take(2000) }, label = { Text("Например: тренируюсь дома") }, minLines = 3) },
            confirmButton = { TextButton(enabled = value.isNotBlank(), onClick = {
                db.confirmMemory(ConfirmedMemory(old?.key ?: UUID.randomUUID().toString(), old?.section ?: MemorySection.PREFERENCES, value.trim(), System.currentTimeMillis()))
                revision++; adding = false; editing = null
            }) { Text("Подтвердить") } }, dismissButton = { TextButton(onClick = { adding = false; editing = null }) { Text("Отмена") } })
    }
}
