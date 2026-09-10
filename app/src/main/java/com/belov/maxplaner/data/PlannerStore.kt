package com.belov.maxplaner.data

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
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
    val completedDates: Set<String> = emptySet(),
    val schedule: HabitSchedule = HabitSchedule.Daily,
    val startMinutes: Int? = null,
    val durationMinutes: Int = 30
)

class PlannerStore(context: Context) {
    private val prefs = context.getSharedPreferences("maxplaner_store", Context.MODE_PRIVATE)
    val tasks = mutableStateListOf<PlannerTask>()
    val habits = mutableStateListOf<Habit>()
    val trackers = mutableStateListOf<Tracker>()
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
        loadTrackers()
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

    fun addTracker(tracker: Tracker) {
        if (tracker.title.isBlank() || tracker.period.weekdays.isEmpty()) return
        trackers += tracker.copy(title = tracker.title.trim())
        persistTrackers()
    }

    fun setTrackerValue(id: String, date: LocalDate, value: Double?) {
        val index = trackers.indexOfFirst { it.id == id }
        if (index < 0) return
        val tracker = trackers[index]
        if (!tracker.period.includes(date) || (value != null && !tracker.accepts(value))) return
        val values = tracker.values.toMutableMap()
        if (value == null) values.remove(date.toString()) else values[date.toString()] = value
        trackers[index] = tracker.copy(values = values)
        persistTrackers()
    }

    fun updateTrackerTime(id: String, startMinutes: Int?, durationMinutes: Int) {
        val index = trackers.indexOfFirst { it.id == id }
        if (index < 0 || startMinutes != null && startMinutes !in 0..1439) return
        trackers[index] = trackers[index].copy(startMinutes = startMinutes, durationMinutes = durationMinutes.coerceIn(15, 720))
        persistTrackers()
    }

    fun updateHabitTime(id: String, startMinutes: Int?, durationMinutes: Int) {
        val index = habits.indexOfFirst { it.id == id }
        if (index < 0 || startMinutes != null && startMinutes !in 0..1439) return
        habits[index] = habits[index].copy(startMinutes = startMinutes, durationMinutes = durationMinutes.coerceIn(15, 720))
        persist()
    }

    fun deleteTracker(id: String) {
        trackers.removeAll { it.id == id }
        persistTrackers()
    }

    private fun loadTrackers() {
        val array = runCatching { JSONArray(prefs.getString("trackers_v1", "[]")) }.getOrNull() ?: return
        repeat(array.length()) { i ->
            // One malformed record must not prevent loading other records or legacy data.
            runCatching {
                val o = array.getJSONObject(i)
                val values = o.optJSONObject("values") ?: JSONObject()
                val weekdays = o.getJSONArray("weekdays")
                val tracker = Tracker(
                    id = o.getString("id"), title = o.getString("title"), category = o.optString("category", "Личное"),
                    type = TrackerType.valueOf(o.getString("type")), unit = o.optString("unit", ""),
                    target = if (o.isNull("target")) null else o.getDouble("target"),
                    initialTarget = if (o.isNull("initialTarget")) null else o.getDouble("initialTarget"),
                    weeklyStep = o.optDouble("weeklyStep", 0.0),
                    startMinutes = if (o.has("startMinutes") && !o.isNull("startMinutes")) o.optInt("startMinutes").takeIf { it in 0..1439 } else null,
                    durationMinutes = o.optInt("durationMinutes", 30).coerceIn(15, 720),
                    direction = GoalDirection.valueOf(o.optString("direction", "AT_LEAST")),
                    period = ActionPeriod(
                        start = LocalDate.parse(o.getString("start")),
                        end = o.optString("end").takeIf { it.isNotBlank() }?.let(LocalDate::parse),
                        weekdays = buildSet { repeat(weekdays.length()) { add(DayOfWeek.valueOf(weekdays.getString(it))) } }
                    ),
                    values = values.keys().asSequence().mapNotNull { key ->
                        val number = values.optDouble(key, Double.NaN)
                        if (number.isFinite() && number >= 0) key to number else null
                    }.toMap()
                )
                trackers += tracker
            }
        }
    }

    private fun persistTrackers() {
        val array = JSONArray()
        trackers.forEach { tracker ->
            array.put(JSONObject().apply {
                put("id", tracker.id); put("title", tracker.title); put("category", tracker.category)
                put("startMinutes", tracker.startMinutes ?: JSONObject.NULL); put("durationMinutes", tracker.durationMinutes)
                put("type", tracker.type.name); put("unit", tracker.unit)
                put("target", tracker.target ?: JSONObject.NULL)
                put("initialTarget", tracker.initialTarget ?: JSONObject.NULL); put("weeklyStep", tracker.weeklyStep)
                put("direction", tracker.direction.name); put("start", tracker.period.start.toString())
                put("end", tracker.period.end?.toString() ?: "")
                put("weekdays", JSONArray(tracker.period.weekdays.map { it.name }))
                put("values", JSONObject(tracker.values))
            })
        }
        prefs.edit().putString("trackers_v1", array.toString()).apply()
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

    fun addHabit(title: String, schedule: HabitSchedule = HabitSchedule.Daily, startMinutes: Int? = null, durationMinutes: Int = 30) {
        if (title.isBlank()) return
        habits += Habit(title = title.trim(), schedule = schedule.normalized(), startMinutes = startMinutes, durationMinutes = durationMinutes.coerceIn(15, 720))
        persist()
    }

    fun updateHabitSchedule(id: String, schedule: HabitSchedule) {
        val index = habits.indexOfFirst { it.id == id }
        if (index < 0) return
        habits[index] = habits[index].copy(schedule = schedule.normalized())
        persist()
    }

    fun toggleHabitToday(id: String) = toggleHabitOn(id, LocalDate.now())

    fun toggleHabitOn(id: String, date: LocalDate) {
        val index = habits.indexOfFirst { it.id == id }
        if (index < 0) return
        if (!habits[index].schedule.isScheduled(date)) return
        val dateKey = date.toString()
        val dates = habits[index].completedDates.toMutableSet()
        if (!dates.add(dateKey)) dates.remove(dateKey)
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

    fun streak(habit: Habit): Int = scheduledHabitStreak(habit.completedDates, habit.schedule)

    fun habitCompletionRate(habit: Habit, days: Int): Int =
        scheduledCompletionRate(habit.completedDates, habit.schedule, days)

    fun isHabitScheduledToday(habit: Habit): Boolean = habit.schedule.isScheduled(LocalDate.now())

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
                habits += Habit(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    completedDates = dates,
                    startMinutes = if (o.has("startMinutes") && !o.isNull("startMinutes")) o.optInt("startMinutes").takeIf { it in 0..1439 } else null,
                    durationMinutes = o.optInt("durationMinutes", 30).coerceIn(15, 720),
                    schedule = readHabitSchedule(o)
                )
            }
        }
    }

    private fun readHabitSchedule(o: JSONObject): HabitSchedule {
        val type = runCatching {
            HabitScheduleType.valueOf(o.optString("scheduleType", HabitScheduleType.DAILY.name))
        }.getOrDefault(HabitScheduleType.DAILY)
        val weekdaysJson = o.optJSONArray("scheduleWeekdays") ?: JSONArray()
        val weekdays = buildSet {
            repeat(weekdaysJson.length()) { index ->
                runCatching { DayOfWeek.valueOf(weekdaysJson.getString(index)) }.getOrNull()?.let(::add)
            }
        }
        return HabitSchedule(type = type, weekdays = weekdays).normalized()
    }

    private fun HabitSchedule.normalized(): HabitSchedule = when (type) {
        HabitScheduleType.DAILY -> HabitSchedule.Daily
        HabitScheduleType.WEEKDAYS -> copy(weekdays = weekdays.toSet())
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
                    put("startMinutes", habit.startMinutes ?: JSONObject.NULL)
                    put("durationMinutes", habit.durationMinutes)
                    put("scheduleType", habit.schedule.type.name)
                    put("scheduleWeekdays", JSONArray(habit.schedule.weekdays.map { it.name }))
                })
            }
        }
        prefs.edit().putString("tasks", taskArray.toString()).putString("habits", habitArray.toString()).apply()
    }
}
