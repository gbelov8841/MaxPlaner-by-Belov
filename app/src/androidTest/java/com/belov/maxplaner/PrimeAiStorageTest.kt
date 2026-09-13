package com.belov.maxplaner

import androidx.test.platform.app.InstrumentationRegistry
import com.belov.maxplaner.ai.*
import com.belov.maxplaner.data.PlannerStore
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class PrimeAiStorageTest {
    @Test fun nutritionAndMemorySurviveRestartWithoutDuplicateIntake() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "prime-test-${UUID.randomUUID()}.db"
        val date = LocalDate.now()
        val item = FoodItem("Проверка", "100 г", Nutrients(100.0, 10.0, 2.0, 12.0), FoodSource.PACKAGE_LABEL)
        val entry = FoodEntry("entry", date, FoodState.PLANNED, listOf(item))
        try {
            PrimeLocalStore(context, name).use { db ->
                assertTrue(db.confirmFood(entry)); assertFalse(db.confirmFood(entry))
                db.setTarget(NutritionTarget(date, Nutrients(300.0, 30.0, 20.0, 40.0)))
                db.confirmMemory(ConfirmedMemory("location", MemorySection.PREFERENCES, "Дома", 1000))
                assertEquals(0.0, db.day(date).consumed.kcal, 0.0)
                val consumed = entry.copy(id = "actual", state = FoodState.CONSUMED, plannedEntryId = entry.id)
                assertTrue(db.confirmFood(consumed))
                assertFalse(db.confirmFood(consumed.copy(id = "duplicate-actual")))
                assertTrue(db.editFood(consumed, consumed.copy(items = listOf(item.copy(portion = "уточнено")))))
                assertFalse(db.editFood(consumed, consumed.copy(items = listOf(item, item))))
            }
            PrimeLocalStore(context, name).use { db ->
                assertEquals(2, db.entries(date).size); assertEquals(100.0, db.day(date).planned.kcal, 0.0); assertEquals(100.0, db.day(date).consumed.kcal, 0.0)
                assertEquals(200.0, db.day(date).remaining!!.kcal, 0.0)
                assertNull(db.target(date.plusDays(1)))
                assertEquals("Дома", db.memory().single().value)
                db.forgetMemory("location"); assertTrue(db.memory().isEmpty())
                assertTrue(db.deleteFood(entry.id)); assertEquals(100.0, db.day(date).consumed.kcal, 0.0)
                assertTrue(db.deleteFood("actual")); assertTrue(db.entries(date).isEmpty())
            }
        } finally { context.deleteDatabase(name) }
    }
    @Test fun taskPreviewCommitsOnceAndKeepsExistingPlannerData() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val store = PlannerStore(instrumentation.targetContext)
            val before = store.tasks.toList()
            val habits = store.habits.toList()
            val history = store.completionHistory.toList()
            val task = com.belov.maxplaner.data.PlannerTask(id = UUID.randomUUID().toString(), title = "AI integration test", dueDate = LocalDate.now().toString(), startMinutes = null)
            val p = PlanPreviewEngine.create(UUID.randomUUID().toString(), PlanPeriod(LocalDate.now(), LocalDate.now()), before, listOf(TaskChange(null, task)), System.currentTimeMillis() + 60000)
            try {
                assertEquals(ApplyPreviewResult.Applied, store.applyAiPreview(p, PlanApproval(p.id, p.revision)))
                val reloaded = PlannerStore(instrumentation.targetContext)
                assertEquals(before + task, reloaded.tasks.toList())
                assertEquals(ApplyPreviewResult.AlreadyApplied, reloaded.applyAiPreview(p, PlanApproval(p.id, p.revision)))
                assertEquals(habits, reloaded.habits.toList()); assertEquals(history, reloaded.completionHistory.toList())
            } finally { store.deleteTask(task.id) }
        }
    }
}
