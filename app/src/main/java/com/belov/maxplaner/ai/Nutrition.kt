package com.belov.maxplaner.ai

import java.time.LocalDate

enum class FoodSource { USER, PACKAGE_LABEL, AI_ESTIMATE }
enum class FoodState { PLANNED, CONSUMED }
data class Nutrients(val kcal: Double, val protein: Double, val fat: Double, val carbs: Double) {
    init { require(listOf(kcal, protein, fat, carbs).all { it.isFinite() && it >= 0 && it <= 100000 }) }
    operator fun plus(other: Nutrients) = Nutrients(kcal + other.kcal, protein + other.protein, fat + other.fat, carbs + other.carbs)
    companion object { val Zero = Nutrients(0.0, 0.0, 0.0, 0.0) }
}
data class FoodItem(val name: String, val portion: String, val nutrients: Nutrients,
    val source: FoodSource, val uncertainty: String? = null) {
    init {
        require(name.isNotBlank() && name.length <= 300 && portion.isNotBlank() && portion.length <= 300)
        require(source != FoodSource.AI_ESTIMATE || !uncertainty.isNullOrBlank())
    }
}
data class FoodEntry(val id: String, val date: LocalDate, val state: FoodState, val items: List<FoodItem>, val plannedEntryId: String? = null) {
    init { require(id.isNotBlank() && items.size in 1..100); require(plannedEntryId == null || (state == FoodState.CONSUMED && plannedEntryId.isNotBlank() && plannedEntryId != id)) }
    val total get() = items.fold(Nutrients.Zero) { total, item -> total + item.nutrients }
}
data class NutritionTarget(val date: LocalDate, val nutrients: Nutrients)
data class NutritionDay(val date: LocalDate, val target: Nutrients?, val planned: Nutrients, val consumed: Nutrients) {
    val remaining: Nutrients? get() = target?.let { t -> Nutrients(
        (t.kcal - consumed.kcal).coerceAtLeast(0.0), (t.protein - consumed.protein).coerceAtLeast(0.0),
        (t.fat - consumed.fat).coerceAtLeast(0.0), (t.carbs - consumed.carbs).coerceAtLeast(0.0)) }
}
fun nutritionDay(date: LocalDate, entries: List<FoodEntry>, target: NutritionTarget?): NutritionDay {
    require(target == null || target.date == date)
    fun sum(state: FoodState) = entries.filter { it.date == date && it.state == state }
        .fold(Nutrients.Zero) { total, entry -> total + entry.total }
    return NutritionDay(date, target?.nutrients, sum(FoodState.PLANNED), sum(FoodState.CONSUMED))
}
enum class MemorySection { GOALS, PREFERENCES, LIMITATIONS, SCHEDULE, NUTRITION }
/** Constructed only by a user-confirmation/editor flow, never automatically from model output. */
data class ConfirmedMemory(val key: String, val section: MemorySection, val value: String, val confirmedAtEpochMillis: Long) {
    init { require(key.isNotBlank() && key.length <= 100 && value.isNotBlank() && value.length <= 2000 && confirmedAtEpochMillis > 0) }
}
