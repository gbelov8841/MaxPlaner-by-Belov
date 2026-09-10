package com.belov.maxplaner.data

enum class ActionCategory(val title: String) {
    HEALTH("Здоровье"),
    NUTRITION("Питание"),
    DEVELOPMENT("Развитие"),
    PRODUCTIVITY("Фокус"),
    HABITS("Привычки"),
    DIGITAL("Цифровой баланс"),
    HOME("Дом"),
    FINANCE("Финансы")
}

data class ActionTemplate(
    val title: String,
    val category: ActionCategory,
    val defaultMinutes: Int = 30,
    val priority: Int = 2
)

object ActionCatalog {
    val templates = listOf(
        ActionTemplate("Выпить воду", ActionCategory.HEALTH, 15),
        ActionTemplate("Пройтись пешком", ActionCategory.HEALTH, 30),
        ActionTemplate("Сделать зарядку", ActionCategory.HEALTH, 15),
        ActionTemplate("Сделать растяжку", ActionCategory.HEALTH, 15),
        ActionTemplate("Тренировка", ActionCategory.HEALTH, 60, 3),
        ActionTemplate("Взвеситься", ActionCategory.HEALTH, 15),
        ActionTemplate("Завтрак", ActionCategory.NUTRITION, 30),
        ActionTemplate("Обед", ActionCategory.NUTRITION, 30),
        ActionTemplate("Ужин", ActionCategory.NUTRITION, 30),
        ActionTemplate("Добрать белок за день", ActionCategory.NUTRITION, 15),
        ActionTemplate("Чтение 20 минут", ActionCategory.DEVELOPMENT, 20),
        ActionTemplate("Дикция 15 минут", ActionCategory.DEVELOPMENT, 15),
        ActionTemplate("Обучение", ActionCategory.DEVELOPMENT, 30),
        ActionTemplate("Медитация", ActionCategory.DEVELOPMENT, 15),
        ActionTemplate("Выбрать 3 главных дела", ActionCategory.PRODUCTIVITY, 15, 3),
        ActionTemplate("Фокус-сессия", ActionCategory.PRODUCTIVITY, 25, 3),
        ActionTemplate("Спланировать завтра", ActionCategory.PRODUCTIVITY, 15),
        ActionTemplate("Не курить сегодня", ActionCategory.HABITS, 15, 3),
        ActionTemplate("Сократить сигареты за день", ActionCategory.HABITS, 15, 3),
        ActionTemplate("Ограничить кофе", ActionCategory.HABITS, 15),
        ActionTemplate("День без алкоголя", ActionCategory.HABITS, 15),
        ActionTemplate("Без соцсетей утром", ActionCategory.DIGITAL, 30),
        ActionTemplate("Без телефона перед сном", ActionCategory.DIGITAL, 30),
        ActionTemplate("Убрать рабочее место", ActionCategory.HOME, 15),
        ActionTemplate("Записать расходы", ActionCategory.FINANCE, 15),
        ActionTemplate("День без импульсивных покупок", ActionCategory.FINANCE, 15)
    )
}
