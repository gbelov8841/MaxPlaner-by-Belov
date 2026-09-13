package com.belov.maxplaner

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.belov.maxplaner.ai.*
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.screens.PrimeAiScreen
import com.belov.maxplaner.ui.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class PrimeAiPreviewUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun fakeProposalOnlyWritesAfterApply() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = PlannerStore(context)
        val before = store.tasks.toList()
        val id = UUID.randomUUID().toString()
        val fake = object : PrimeAiService {
            override suspend fun request(request: AiRequest): AiResult {
                val task = PlannerTask(id = id, title = "Тестовая прогулка", dueDate = request.context.period.start.toString(), startMinutes = null)
                return AiResult.Plan(PlanPreviewEngine.create("ui-$id", request.context.period, request.context.tasks,
                    listOf(TaskChange(null, task)), System.currentTimeMillis() + 60000))
            }
        }
        try {
            ui.setContent { MaxPlanerTheme(AppearanceStore(context)) { PrimeAiScreen(store, {}, fake) } }
            ui.onNodeWithText("Чего хочешь достичь?").performTextInput("Хочу гулять")
            ui.onNodeWithText("Создать предложение").performClick()
            ui.onNodeWithText("Изменения ещё не сохранены").assertExists()
            assertEquals(before, PlannerStore(context).tasks.toList())
            ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Применить план"))
            ui.onNodeWithText("Применить план").performClick()
            ui.onNodeWithText("План сохранён").assertExists()
            assertEquals(1, PlannerStore(context).tasks.count { it.id == id })
        } finally { ui.runOnIdle { store.deleteTask(id) } }
    }
}
