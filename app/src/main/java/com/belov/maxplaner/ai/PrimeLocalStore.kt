package com.belov.maxplaner.ai

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Local-only nutrition and confirmed memory; never sends data to an AI provider. */
class PrimeLocalStore(context: Context, name: String = "prime_local_v1.db") : SQLiteOpenHelper(context, name, null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE food (id TEXT PRIMARY KEY, day TEXT NOT NULL, state TEXT NOT NULL, items TEXT NOT NULL)")
        db.execSQL("CREATE INDEX food_day ON food(day)")
        db.execSQL("CREATE TABLE nutrition_target (day TEXT PRIMARY KEY, nutrients TEXT NOT NULL)")
        db.execSQL("CREATE TABLE confirmed_memory (key TEXT PRIMARY KEY, section TEXT NOT NULL, value TEXT NOT NULL, confirmed_at INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("Explicit non-destructive migration required: $oldVersion -> $newVersion")
    }

    /** Caller passes user-reviewed items. Duplicate confirmation cannot duplicate intake. */
    fun confirmFood(entry: FoodEntry): Boolean = writableDatabase.insertWithOnConflict("food", null,
        foodValues(entry), SQLiteDatabase.CONFLICT_IGNORE) != -1L

    /** Optimistic comparison prevents an old edit sheet overwriting a newer correction. */
    fun editFood(before: FoodEntry, after: FoodEntry): Boolean {
        require(before.id == after.id)
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val matches = entries(before.date).firstOrNull { it.id == before.id } == before
            if (matches) db.update("food", foodValues(after), "id=?", arrayOf(before.id))
            db.setTransactionSuccessful()
            matches
        } finally { db.endTransaction() }
    }
    fun deleteFood(id: String): Boolean = writableDatabase.delete("food", "id=?", arrayOf(id)) == 1
    fun entries(date: LocalDate): List<FoodEntry> = readableDatabase.query("food", null, "day=?",
        arrayOf(date.toString()), null, null, "id").use { c -> buildList {
        while (c.moveToNext()) {
            val array = JSONArray(c.getString(c.getColumnIndexOrThrow("items")))
            val items = (0 until array.length()).map { i -> array.getJSONObject(i).let { o ->
                FoodItem(o.getString("name"), o.getString("portion"), readNutrients(o.getJSONObject("nutrients")),
                    FoodSource.valueOf(o.getString("source")), if (o.isNull("uncertainty")) null else o.getString("uncertainty"))
            } }
            add(FoodEntry(c.getString(c.getColumnIndexOrThrow("id")), date,
                FoodState.valueOf(c.getString(c.getColumnIndexOrThrow("state"))), items))
        }
    } }
    fun setTarget(target: NutritionTarget) {
        val row = ContentValues().apply { put("day", target.date.toString()); put("nutrients", nutrientsJson(target.nutrients).toString()) }
        check(writableDatabase.insertWithOnConflict("nutrition_target", null, row, SQLiteDatabase.CONFLICT_REPLACE) != -1L)
    }
    fun clearTarget(date: LocalDate) { writableDatabase.delete("nutrition_target", "day=?", arrayOf(date.toString())) }
    fun target(date: LocalDate): NutritionTarget? = readableDatabase.query("nutrition_target", arrayOf("nutrients"),
        "day=?", arrayOf(date.toString()), null, null, null).use { c ->
        if (c.moveToFirst()) NutritionTarget(date, readNutrients(JSONObject(c.getString(0)))) else null
    }
    fun day(date: LocalDate) = nutritionDay(date, entries(date), target(date))

    fun confirmMemory(fact: ConfirmedMemory) {
        val row = ContentValues().apply { put("key", fact.key); put("section", fact.section.name)
            put("value", fact.value); put("confirmed_at", fact.confirmedAtEpochMillis) }
        check(writableDatabase.insertWithOnConflict("confirmed_memory", null, row, SQLiteDatabase.CONFLICT_REPLACE) != -1L)
    }
    fun forgetMemory(key: String) { writableDatabase.delete("confirmed_memory", "key=?", arrayOf(key)) }
    fun memory(): List<ConfirmedMemory> = readableDatabase.query("confirmed_memory", null, null, null, null, null, "key").use { c -> buildList {
        while (c.moveToNext()) add(ConfirmedMemory(c.getString(c.getColumnIndexOrThrow("key")),
            MemorySection.valueOf(c.getString(c.getColumnIndexOrThrow("section"))), c.getString(c.getColumnIndexOrThrow("value")),
            c.getLong(c.getColumnIndexOrThrow("confirmed_at"))))
    } }
    private fun foodValues(entry: FoodEntry) = ContentValues().apply {
        put("id", entry.id); put("day", entry.date.toString()); put("state", entry.state.name)
        put("items", JSONArray().apply { entry.items.forEach { item -> put(JSONObject().apply {
            put("name", item.name); put("portion", item.portion); put("source", item.source.name)
            put("uncertainty", item.uncertainty ?: JSONObject.NULL); put("nutrients", nutrientsJson(item.nutrients))
        }) } }.toString())
    }
    private fun nutrientsJson(n: Nutrients) = JSONObject().apply {
        put("kcal", n.kcal); put("protein", n.protein); put("fat", n.fat); put("carbs", n.carbs)
    }
    private fun readNutrients(o: JSONObject) = Nutrients(o.getDouble("kcal"), o.getDouble("protein"), o.getDouble("fat"), o.getDouble("carbs"))
}
