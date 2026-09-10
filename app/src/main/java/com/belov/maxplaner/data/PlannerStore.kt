package com.belov.maxplaner.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class PlannerTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dueDate: String? = LocalDate.now().toString(),
    val priority: Int = 2,
    val completed: Boolean = false,
    val isFocus: Boolean = false,
    val startMinutes: Int? = null,
    val durationMinutes: Int = 60
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
    var focusMinutes by mutableIntStateOf(prefs.getInt("focus_minutes", 0))
        private set

    init {
        load()
        if (tasks.isEmpty() && habits.isEmpty() && !prefs.getBoolean("seeded", false)) {
            tasks += PlannerTask(
                title = "Сформулировать 3 главных результата дня",
                priority = 3,
                isFocus = true,
                startMinutes = 9 * 60,
                durationMinutes = 45
            )
            tasks += PlannerTask(
                title = "Разобрать входящие задачи",
                priority = 2,
                startMinutes = 11 * 60,
                durationMinutes = 30
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
        durationMinutes: Int = 60
    ) {
        if (title.isBlank()) return
        tasks += PlannerTask(
            title = title.trim(),
            dueDate = dueDate,
            priority = priority,
            isFocus = focus,
            startMinutes = startMinutes,
            durationMinutes = durationMinutes.coerceIn(15, 12 * 60)
        )
        persist()
    }

    fun toggleTask(id: String) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index >= 0) tasks[index] = tasks[index].copy(completed = !tasks[index].completed)
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

    fun addFocusMinutes(minutes: Int) {
        focusMinutes += minutes.coerceAtLeast(0)
        prefs.edit().putInt("focus_minutes", focusMinutes).apply()
    }

    fun streak(habit: Habit): Int {
        var date = LocalDate.now()
        var streak = 0
        while (habit.completedDates.contains(date.toString())) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    private fun load() {
        runCatching {
            val taskArray = JSONArray(prefs.getString("tasks", "[]"))
            repeat(taskArray.length()) { i ->
                val o = taskArray.getJSONObject(i)
                tasks += PlannerTask(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    dueDate = o.optString("dueDate").takeIf { it.isNotBlank() },
                    priority = o.optInt("priority", 2),
                    completed = o.optBoolean("completed", false),
                    isFocus = o.optBoolean("isFocus", false),
                    startMinutes = if (o.has("startMinutes") && !o.isNull("startMinutes")) o.optInt("startMinutes") else null,
                    durationMinutes = o.optInt("durationMinutes", 60).coerceAtLeast(15)
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
