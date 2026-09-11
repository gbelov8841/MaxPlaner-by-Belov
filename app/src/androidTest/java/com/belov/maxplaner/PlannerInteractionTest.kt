package com.belov.maxplaner

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.data.HabitSchedule
import com.belov.maxplaner.ui.theme.ThemePacks
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlannerInteractionTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    private fun screenshot(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "screenshots/$name.png")
        file.parentFile!!.mkdirs()
        instrumentation.uiAutomation.takeScreenshot().also { image ->
            file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }; image.recycle()
        }
    }

    @Test fun allThemesNavigateAndCaptureRealScreens() {
        ThemePacks.forEach { pack ->
            ui.onNodeWithText("Ещё").performClick()
            ui.onNodeWithText("Оформление").performClick()
            ui.onNodeWithText(pack.name).performScrollTo().performClick()
            ui.onNodeWithText("Главная").performClick()
            ui.onNodeWithText("План на сегодня").assertIsDisplayed()
            screenshot("${pack.id}-home")
            ui.onNodeWithText("План").performClick()
            ui.onNodeWithText("План дня").assertIsDisplayed()
            screenshot("${pack.id}-day")
            ui.onNodeWithText("Неделя").performClick()
            screenshot("${pack.id}-week")
            ui.onNodeWithText("Месяц").performClick()
            screenshot("${pack.id}-month")
            ui.onNodeWithText("День").performClick()
        }
    }

    @Test fun dateFromHomeOpensTheSelectedDay() {
        val today = LocalDate.now()
        val other = if (today.dayOfWeek.value == 7) today.minusDays(1) else today.plusDays(1)
        ui.onNodeWithText("Главная").performClick()
        val label = other.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru")))
        ui.onNodeWithContentDescription(label).performClick()
        ui.onNodeWithText(other.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru")))).assertIsDisplayed()
    }

    @Test fun taskCompletionAndHabitRenameSurviveStoreReload() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ui.runOnIdle {
            val store = PlannerStore(context)
            store.addTask("Persistence test", LocalDate.now().toString())
            val task = store.tasks.last()
            store.toggleTask(task.id)
            store.updateTask(store.tasks.first { it.id == task.id }.copy(notes = "keep completion"))
            val loaded = PlannerStore(context)
            assertTrue(loaded.tasks.first { it.id == task.id }.completed)
            assertEquals(1, loaded.completionHistory.count { it.taskId == task.id })
            loaded.toggleTask(task.id)
            assertTrue(PlannerStore(context).completionHistory.none { it.taskId == task.id })
            loaded.deleteTask(task.id)
            loaded.addHabit("Rename test", HabitSchedule.Daily)
            val habit = loaded.habits.last()
            loaded.toggleHabitOn(habit.id, LocalDate.now())
            loaded.updateHabit(habit.id, "Renamed", HabitSchedule.Daily, 600, 30)
            val persisted = PlannerStore(context).habits.first { it.id == habit.id }
            assertEquals("Renamed", persisted.title)
            assertTrue(LocalDate.now().toString() in persisted.completedDates)
            loaded.deleteHabit(habit.id)
        }
    }
}
