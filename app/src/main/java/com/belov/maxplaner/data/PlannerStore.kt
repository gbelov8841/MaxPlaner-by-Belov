package com.belov.maxplaner.data

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false
)

data class PlannerTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dueDate: String? = LocalDate.now().toString(),
    val priority: Int = 2,
    val completed: Boolean = false,
    val isFocus: Boolean = false,
    val startMinutes: Int? = null,
    val durationMinutes: Int = 60,
    val notes: String = "",
    val category: String = "Личное",
    val recurrence: String = "Не повторять",
    val checklist: List<ChecklistItem> = emptyList()
)

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completedDates: Set<String> = emptySet()
)

class PlannerStore(context: Context) {
    private val prefs = context.getSharedPreferences("maxplaner_store", Context.MODE_PRIVATE)
    val tasks = mutableStateListOf<PlannerTask>()
    val habits = mutableStateListOf<Habit>()
    private val bootId = runCatching { Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1) }.getOrDefault(-1)
    var focusClock by mutableStateOf(readFocusClock())
        private set
    var focusState by mutableStateOf(loadFocus())
        private set
    val focusMinutes: Int get() = focusState.totalMinutes
    var today by mutableStateOf(LocalDate.now())
        private set

    init {
        refreshFocus()
        load()
        if (tasks.isEmpty() && habits.isEmpty() && !prefs.getBoolean("seeded", false)) {
            tasks += PlannerTask(
                title = "Сформулировать 3 главных результата дня",
                priority = 3,
                isFocus = true,
                startMinutes = 9 * 60,
                durationMinutes = 45,
                category = "Работа",
                notes = "Зафиксировать конкретные результаты, которые действительно двигают день вперёд.",
                checklist = listOf(
                    ChecklistItem(title = "Определить главный результат"),
                    ChecklistItem(title = "Выбрать ещё 2 важных результата"),
                    ChecklistItem(title = "Забронировать время в календаре")
                )
            )
            tasks += PlannerTask(
                title = "Разобрать входящие задачи",
                priority = 2,
                startMinutes = 11 * 60,
                durationMinutes = 30,
                category = "Работа"
            )
            habits += Habit(title = "Вода")
            habits += Habit(title = "Чтение 20 минут")
            persist()
            prefs.edit().putBoolean("seeded", true).apply()
        }
    }

    fun addTask(
        title: String,
        dueDate: String? = LocalDate.now().toString(),
        priority: Int = 2,
        focus: Boolean = false,
        startMinutes: Int? = null,
        durationMinutes: Int = 60,
        notes: String = "",
        category: String = "Личное",
        recurrence: String = "Не повторять",
        checklist: List<ChecklistItem> = emptyList()
    ) {
        if (title.isBlank()) return
        tasks += PlannerTask(
            title = title.trim(),
            dueDate = dueDate,
            priority = priority,
            isFocus = focus,
            startMinutes = startMinutes,
            durationMinutes = durationMinutes.coerceIn(15, 12 * 60),
            notes = notes.trim(),
            category = category.ifBlank { "Личное" },
            recurrence = recurrence.ifBlank { "Не повторять" },
            checklist = checklist
        )
        persist()
    }

    fun updateTask(updated: PlannerTask) {
        val index = tasks.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            tasks[index] = updated
            persist()
        }
    }

    fun toggleTask(id: String) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index >= 0) tasks[index] = tasks[index].copy(completed = !tasks[index].completed)
        persist()
    }

    fun toggleChecklistItem(taskId: String, itemId: String) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index < 0) return
        val task = tasks[index]
        tasks[index] = task.copy(
            checklist = task.checklist.map { item ->
                if (item.id == itemId) item.copy(completed = !item.completed) else item
            }
        )
        persist()
    }

    fun addChecklistItem(taskId: String, title: String) {
        if (title.isBlank()) return
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index < 0) return
        val task = tasks[index]
        tasks[index] = task.copy(checklist = task.checklist + ChecklistItem(title = title.trim()))
        persist()
    }

    fun deleteTask(id: String) {
        tasks.removeAll { it.id == id }
        persist()
    }

    fun addHabit(title: String) {
        if (title.isBlank()) return
        habits += Habit(title = title.trim())
        persist()
    }

    fun toggleHabitToday(id: String) {
        val index = habits.indexOfFirst { it.id == id }
        if (index < 0) return
        val today = LocalDate.now().toString()
        val dates = habits[index].completedDates.toMutableSet()
        if (!dates.add(today)) dates.remove(today)
        habits[index] = habits[index].copy(completedDates = dates)
        persist()
    }

    fun deleteHabit(id: String) {
        habits.removeAll { it.id == id }
        persist()
    }

    fun refreshFocus() {
        focusClock = readFocusClock()
        today = LocalDate.now()
        saveFocus(focusState.refresh(focusClock))
    }

    fun startFocus() {
        focusClock = readFocusClock()
        saveFocus(focusState.start(focusClock))
    }

    fun pauseFocus() {
        focusClock = readFocusClock()
        saveFocus(focusState.pause(focusClock))
    }

    private fun readFocusClock() = FocusClock(System.currentTimeMillis(), SystemClock.elapsedRealtime(), bootId)

    private fun loadFocus(): FocusState = runCatching {
        val saved = JSONObject(prefs.getString("focus_session", "{}") ?: "{}")
        FocusState(
            session = FocusSession(
                mode = FocusMode.valueOf(saved.optString("mode", FocusMode.Idle.name)),
                pausedMillis = saved.optLong("pausedMillis", FOCUS_DURATION_MILLIS).coerceIn(0L, FOCUS_DURATION_MILLIS),
                deadlineWallMillis = saved.optLong("deadlineWallMillis", 0L),
                deadlineElapsedMillis = saved.optLong("deadlineElapsedMillis", 0L),
                bootId = saved.optInt("bootId", -1)
            ),
            totalMinutes = prefs.getInt("focus_minutes", 0)
        )
    }.getOrElse { FocusState(totalMinutes = prefs.getInt("focus_minutes", 0)) }

    private fun saveFocus(next: FocusState) {
        if (next == focusState) return
        focusState = next
        val session = next.session
        val saved = JSONObject().apply {
            put("mode", session.mode.name)
            put("pausedMillis", session.pausedMillis)
            put("deadlineWallMillis", session.deadlineWallMillis)
            put("deadlineElapsedMillis", session.deadlineElapsedMillis)
            put("bootId", session.bootId)
        }
        prefs.edit().putString("focus_session", saved.toString())
            .putInt("focus_minutes", next.totalMinutes).apply()
    }

    fun streak(habit: Habit): Int = activeHabitStreak(habit.completedDates)

    private fun load() {
        runCatching {
            val taskArray = JSONArray(prefs.getString("tasks", "[]"))
            repeat(taskArray.length()) { i ->
                val o = taskArray.getJSONObject(i)
                val checklistJson = o.optJSONArray("checklist") ?: JSONArray()
                val checklist = buildList {
                    repeat(checklistJson.length()) { j ->
                        val item = checklistJson.getJSONObject(j)
                        add(ChecklistItem(
                            id = item.optString("id", UUID.randomUUID().toString()),
                            title = item.optString("title", ""),
                            completed = item.optBoolean("completed", false)
                        ))
                    }
                }
                tasks += PlannerTask(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    dueDate = o.optString("dueDate").takeIf { it.isNotBlank() },
                    priority = o.optInt("priority", 2),
                    completed = o.optBoolean("completed", false),
                    isFocus = o.optBoolean("isFocus", false),
                    startMinutes = if (o.has("startMinutes") && !o.isNull("startMinutes")) o.optInt("startMinutes") else null,
                    durationMinutes = o.optInt("durationMinutes", 60).coerceAtLeast(15),
                    notes = o.optString("notes", ""),
                    category = o.optString("category", "Личное").ifBlank { "Личное" },
                    recurrence = o.optString("recurrence", "Не повторять").ifBlank { "Не повторять" },
                    checklist = checklist
                )
            }
            val habitArray = JSONArray(prefs.getString("habits", "[]"))
            repeat(habitArray.length()) { i ->
                val o = habitArray.getJSONObject(i)
                val datesJson = o.optJSONArray("dates") ?: JSONArray()
                val dates = buildSet { repeat(datesJson.length()) { add(datesJson.getString(it)) } }
                habits += Habit(o.getString("id"), o.getString("title"), dates)
            }
        }
    }

    private fun persist() {
        val taskArray = JSONArray().apply {
            tasks.forEach { task ->
                put(JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("dueDate", task.dueDate ?: "")
                    put("priority", task.priority)
                    put("completed", task.completed)
                    put("isFocus", task.isFocus)
                    if (task.startMinutes == null) put("startMinutes", JSONObject.NULL) else put("startMinutes", task.startMinutes)
                    put("durationMinutes", task.durationMinutes)
                    put("notes", task.notes)
                    put("category", task.category)
                    put("recurrence", task.recurrence)
                    put("checklist", JSONArray().apply {
                        task.checklist.forEach { item ->
                            put(JSONObject().apply {
                                put("id", item.id)
                                put("title", item.title)
                                put("completed", item.completed)
                            })
                        }
                    })
                })
            }
        }
        val habitArray = JSONArray().apply {
            habits.forEach { habit ->
                put(JSONObject().apply {
                    put("id", habit.id)
                    put("title", habit.title)
                    put("dates", JSONArray(habit.completedDates.toList()))
                })
            }
        }
        prefs.edit().putString("tasks", taskArray.toString()).putString("habits", habitArray.toString()).apply()
    }
}
