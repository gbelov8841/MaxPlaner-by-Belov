package com.belov.maxplaner.ai

import com.belov.maxplaner.data.PlannerTask
import java.security.MessageDigest
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.time.LocalDate

data class TaskChange(val before: PlannerTask?, val after: PlannerTask?) {
    val id: String get() = (before ?: after)!!.id
    val kind: String get() = when {
        before == null -> "Добавить"
        after == null -> "Удалить"
        before.dueDate != after.dueDate || before.startMinutes != after.startMinutes -> "Перенести"
        else -> "Изменить"
    }
}
data class PlanPreview(val id: String, val revision: String, val period: PlanPeriod,
    val baseline: List<PlannerTask>, val changes: List<TaskChange>, val expiresAtEpochMillis: Long)
data class PlanApproval(val previewId: String, val revision: String)
sealed interface ApplyPreviewResult {
    data object Applied : ApplyPreviewResult
    data object AlreadyApplied : ApplyPreviewResult
    data class Rejected(val reason: String) : ApplyPreviewResult
    data class Ready(val tasks: List<PlannerTask>, val receipt: String) : ApplyPreviewResult
}

/** Pure reducer: cannot mutate the planner and rejects the entire batch on any invalid row. */
object PlanPreviewEngine {
    fun create(id: String, period: PlanPeriod, baseline: List<PlannerTask>, changes: List<TaskChange>, expiresAtEpochMillis: Long): PlanPreview {
        val draft = PlanPreview(id, "", period, baseline.toList(), changes.toList(), expiresAtEpochMillis)
        return draft.copy(revision = fingerprint(draft))
    }

    private fun fingerprint(p: PlanPreview): String {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            fun text(value: String?) { val data = value?.toByteArray(); out.writeInt(data?.size ?: -1); if (data != null) out.write(data) }
            fun task(t: PlannerTask?) {
                out.writeBoolean(t != null)
                if (t == null) return
                text(t.id); text(t.title); text(t.dueDate); out.writeInt(t.priority)
                out.writeBoolean(t.completed); out.writeBoolean(t.isFocus)
                out.writeInt(t.startMinutes ?: -1); out.writeInt(t.durationMinutes)
                text(t.notes); text(t.category); text(t.recurrence); out.writeInt(t.checklist.size)
                t.checklist.forEach { text(it.id); text(it.title); out.writeBoolean(it.completed) }
            }
            text(p.id); text(p.period.start.toString()); text(p.period.end.toString()); out.writeLong(p.expiresAtEpochMillis)
            out.writeInt(p.baseline.size); p.baseline.forEach(::task)
            out.writeInt(p.changes.size); p.changes.forEach { task(it.before); task(it.after) }
        }
        return MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun prepare(current: List<PlannerTask>, preview: PlanPreview, approval: PlanApproval,
        appliedReceipts: Set<String>, nowMillis: Long): ApplyPreviewResult {
        fun reject(reason: String) = ApplyPreviewResult.Rejected(reason)
        if (preview.id.isBlank() || preview.revision.isBlank() || approval != PlanApproval(preview.id, preview.revision))
            return reject("Подтверди текущую версию плана")
        if (preview.revision != fingerprint(preview)) return reject("Предложение изменилось. Подтверди его заново")
        val receipt = receipt(preview)
        if (receipt in appliedReceipts) return ApplyPreviewResult.AlreadyApplied
        if (nowMillis >= preview.expiresAtEpochMillis) return reject("Предложение устарело. Создай новое")
        if (current != preview.baseline) return reject("Расписание изменилось. Обнови предложение")
        if (preview.changes.size !in 1..200 || current.map { it.id }.distinct().size != current.size)
            return reject("Некорректный набор изменений")
        val changedIds = mutableSetOf<String>()
        val next = current.associateBy { it.id }.toMutableMap()
        for (change in preview.changes) {
            val before = change.before
            val after = change.after
            if (before == null && after == null) return reject("Пустое изменение")
            if (!changedIds.add(change.id)) return reject("Повторяющееся изменение")
            if (next[change.id] != before) return reject("Исходная задача не совпадает")
            if (before?.completed == true || after?.completed == true) return reject("История выполнения защищена")
            if (before != null && runCatching { LocalDate.parse(before.dueDate) in preview.period }.getOrDefault(false).not())
                return reject("Задача вне выбранного периода")
            if (after != null) {
                if (before != null && before.id != after.id) return reject("Идентификатор задачи изменён")
                if (!valid(after, preview.period)) return reject("Некорректная задача")
                if (before == null && (after.checklist.any { it.completed } || after.recurrence != "Не повторять"))
                    return reject("Неподдерживаемое выполнение или повторение")
                if (before != null && (before.checklist != after.checklist || before.recurrence != after.recurrence))
                    return reject("Список шагов и повторения требуют отдельного подтверждения")
                next[change.id] = after
            } else next.remove(change.id)
        }
        // Never silently resolve collisions. Preview UI must revise the proposal first.
        val scheduled = next.values.filter { !it.completed && it.startMinutes != null }
        for (a in scheduled.indices) for (b in a + 1 until scheduled.size) {
            val x = scheduled[a]; val y = scheduled[b]
            if (x.id !in changedIds && y.id !in changedIds) continue
            val xd = runCatching { LocalDate.parse(x.dueDate).toEpochDay() }.getOrNull() ?: continue
            val yd = runCatching { LocalDate.parse(y.dueDate).toEpochDay() }.getOrNull() ?: continue
            val xs = xd * 1440 + x.startMinutes!!; val ys = yd * 1440 + y.startMinutes!!
            if (xs < ys + y.durationMinutes && ys < xs + x.durationMinutes)
                return reject("Есть пересечение времени. Измени предложение")
        }
        return ApplyPreviewResult.Ready(next.values.toList(), receipt)
    }

    private fun valid(task: PlannerTask, period: PlanPeriod): Boolean =
        task.id.isNotBlank() && task.title.isNotBlank() && task.title.length <= 300 &&
            task.notes.length <= 10000 && task.priority in 1..3 && task.durationMinutes in 15..720 &&
            (task.startMinutes == null || task.startMinutes in 0..1439) &&
            runCatching { LocalDate.parse(task.dueDate) in period }.getOrDefault(false)

    /** Opaque bounded preference key; approval still binds the full preview revision. */
    private fun receipt(preview: PlanPreview): String {
        val bytes = "${preview.id.length}:${preview.id}${preview.revision.length}:${preview.revision}".toByteArray()
        return MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
