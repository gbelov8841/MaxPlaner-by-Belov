package com.belov.maxplaner.ai

import com.belov.maxplaner.data.PlannerTask
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.*

class PrimeAiTest {
    private val date = LocalDate.of(2026, 9, 13)
    private val period = PlanPeriod(date, date.plusDays(6))
    private val task = PlannerTask(id = "existing", title = "Чтение", dueDate = date.toString(), startMinutes = 600, durationMinutes = 30)
    private fun preview(changes: List<TaskChange>, baseline: List<PlannerTask> = listOf(task)) =
        PlanPreviewEngine.create("preview", period, baseline, changes, 5000)
    private fun apply(p: PlanPreview, current: List<PlannerTask> = listOf(task), receipts: Set<String> = emptySet(), now: Long = 1) =
        PlanPreviewEngine.prepare(current, p, PlanApproval(p.id, p.revision), receipts, now)

    @Test fun previewDoesNotWriteAndExplicitApprovalMovesPreservingFields() {
        val changed = task.copy(dueDate = date.plusDays(1).toString())
        val p = preview(listOf(TaskChange(task, changed)))
        assertEquals(date.toString(), task.dueDate)
        assertTrue(PlanPreviewEngine.prepare(listOf(task), p, PlanApproval(p.id, "old"), emptySet(), 1) is ApplyPreviewResult.Rejected)
        assertEquals(listOf(changed), (apply(p) as ApplyPreviewResult.Ready).tasks)
    }
    @Test fun editedPreviewCannotReuseApproval() {
        val p = preview(listOf(TaskChange(task, task.copy(title = "Новое"))))
        assertTrue(apply(p.copy(changes = listOf(TaskChange(task, task.copy(title = "Подмена")))) ) is ApplyPreviewResult.Rejected)
    }
    @Test fun staleScheduleAndExpiredPreviewAreRejected() {
        val p = preview(listOf(TaskChange(task, null)))
        assertTrue(apply(p, listOf(task.copy(notes = "Правка вручную"))) is ApplyPreviewResult.Rejected)
        assertTrue(apply(p, now = 5000) is ApplyPreviewResult.Rejected)
    }
    @Test fun receiptMakesRetryIdempotentEvenAfterScheduleChanges() {
        val p = preview(listOf(TaskChange(task, null)))
        val ready = apply(p) as ApplyPreviewResult.Ready
        assertEquals(ApplyPreviewResult.AlreadyApplied, apply(p, ready.tasks, setOf(ready.receipt)))
    }
    @Test fun invalidBatchNeverPartiallyApplies() {
        val p = preview(listOf(TaskChange(task, null), TaskChange(null, task.copy(id = "bad", startMinutes = -1))))
        assertTrue(apply(p) is ApplyPreviewResult.Rejected)
        assertEquals("Чтение", task.title)
    }
    @Test fun protectedHistoryAndDuplicateRowsAreRejected() {
        val done = task.copy(completed = true)
        assertTrue(apply(preview(listOf(TaskChange(done, null)), listOf(done)), listOf(done)) is ApplyPreviewResult.Rejected)
        assertTrue(apply(preview(listOf(TaskChange(task, null), TaskChange(task, null)))) is ApplyPreviewResult.Rejected)
    }
    @Test fun overlapIncludingPreviousMidnightIsRejected() {
        val previous = task.copy(id = "night", dueDate = date.minusDays(1).toString(), startMinutes = 1410, durationMinutes = 120)
        val added = task.copy(id = "new", startMinutes = 30)
        val base = listOf(previous)
        assertTrue(apply(preview(listOf(TaskChange(null, added)), base), base) is ApplyPreviewResult.Rejected)
    }
    @Test fun outsidePeriodIsRejected() {
        assertTrue(apply(preview(listOf(TaskChange(task, task.copy(dueDate = date.plusDays(20).toString()))))) is ApplyPreviewResult.Rejected)
    }
    @Test fun plannedFoodDoesNotBecomeConsumedAndMissingTargetIsUnknown() {
        val item = FoodItem("Еда", "100 г", Nutrients(200.0, 10.0, 5.0, 20.0), FoodSource.USER)
        val entries = listOf(FoodEntry("plan", date, FoodState.PLANNED, listOf(item)))
        val day = nutritionDay(date, entries, null)
        assertEquals(200.0, day.planned.kcal, 0.0); assertEquals(0.0, day.consumed.kcal, 0.0); assertNull(day.remaining)
    }
    @Test fun estimatesRequireUncertaintyAndNonFiniteValuesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { FoodItem("Еда", "порция", Nutrients.Zero, FoodSource.AI_ESTIMATE) }
        assertThrows(IllegalArgumentException::class.java) { Nutrients(Double.NaN, 0.0, 0.0, 0.0) }
    }
    @Test fun foodTotalsKeepDatesAndTargetsSeparate() {
        val item = FoodItem("Еда", "порция", Nutrients(200.0, 10.0, 5.0, 20.0), FoodSource.USER)
        val entries = listOf(FoodEntry("a", date, FoodState.CONSUMED, listOf(item)), FoodEntry("b", date.plusDays(1), FoodState.CONSUMED, listOf(item)))
        assertEquals(300.0, nutritionDay(date, entries, NutritionTarget(date, Nutrients(500.0, 50.0, 40.0, 60.0))).remaining!!.kcal, 0.0)
    }
    @Test fun fakeAndUnavailableProvidersHaveNoWriteAuthority() {
        val p = preview(listOf(TaskChange(task, null)))
        val fake = object : PrimeAiService { override suspend fun request(request: AiRequest): AiResult = AiResult.Plan(p) }
        val request = AiRequest("request", AiCapability.SIMPLIFY_DAY, "Упрости день", AiContext(period, ZoneId.of("UTC"), listOf(task)))
        assertEquals(AiResult.Plan(p), immediate { fake.request(request) })
        assertEquals(AiError.UNAVAILABLE, (immediate { UnavailablePrimeAiService().request(request) } as AiResult.Failure).reason)
        assertEquals(date.toString(), task.dueDate)
    }
    private fun <T> immediate(block: suspend () -> T): T {
        var result: Result<T>? = null
        block.startCoroutine(object : Continuation<T> { override val context = EmptyCoroutineContext
            override fun resumeWith(value: Result<T>) { result = value } })
        return requireNotNull(result).getOrThrow()
    }
}
