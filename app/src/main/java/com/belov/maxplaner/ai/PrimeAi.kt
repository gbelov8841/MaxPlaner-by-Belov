package com.belov.maxplaner.ai

import com.belov.maxplaner.data.PlannerTask
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class AiCapability { CREATE_PLAN, REVISE_PLAN, SIMPLIFY_DAY, ESTIMATE_FOOD, SUGGEST_FOOD, REVIEW_PERIOD, CHAT }
data class PlanPeriod(val start: LocalDate, val end: LocalDate) {
    init { require(ChronoUnit.DAYS.between(start, end) in 0..30) }
    operator fun contains(date: LocalDate) = date >= start && date <= end
}
data class AiContext(val period: PlanPeriod, val timezone: ZoneId, val tasks: List<PlannerTask>,
    val confirmedProfile: Map<String, String> = emptyMap())
data class AiRequest(val requestId: String, val capability: AiCapability, val text: String,
    val context: AiContext, val previousPreview: PlanPreview? = null) {
    init { require(requestId.isNotBlank()); require(text.isNotBlank() && text.length <= 8000) }
}
data class AiAllowance(val remaining: Int, val resetsAtEpochMillis: Long) {
    init { require(remaining >= 0 && resetsAtEpochMillis >= 0) }
}
enum class AiError { UNAVAILABLE, OFFLINE, UNAUTHORIZED, RATE_LIMITED, INVALID_RESPONSE }
sealed interface AiResult {
    data class Questions(val questions: List<String>) : AiResult {
        init { require(questions.size in 1..3 && questions.all { it.isNotBlank() }) }
    }
    data class Plan(val preview: PlanPreview) : AiResult
    data class Food(val items: List<FoodItem>, val explanation: String) : AiResult
    data class Insight(val text: String, val suggestedAction: AiCapability? = null) : AiResult
    data class Refusal(val message: String) : AiResult
    data class Failure(val reason: AiError, val retryAfterSeconds: Int? = null) : AiResult {
        val userMessage = "Prime AI временно недоступен"
    }
}
/** Implementation talks only to PrimePlaner backend. It has no authority to apply changes. */
interface PrimeAiService {
    suspend fun request(request: AiRequest): AiResult
}
/** Shipping default until authenticated backend is connected; never fabricates AI advice. */
class UnavailablePrimeAiService : PrimeAiService {
    override suspend fun request(request: AiRequest) = AiResult.Failure(AiError.UNAVAILABLE)
}
