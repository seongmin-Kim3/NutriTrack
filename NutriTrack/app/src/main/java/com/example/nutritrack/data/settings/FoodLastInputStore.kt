package com.example.nutritrack.data.settings

import android.content.Context

data class FoodLastInput(
    val mode: String,          // "GRAMS" or "SERVING"
    val grams: String,         // last grams
    val servingCount: String   // last serving count
)

class FoodLastInputStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("food_last_input_store", Context.MODE_PRIVATE)

    fun load(foodName: String): FoodLastInput? {
        val key = key(foodName)
        val raw = prefs.getString(key, null) ?: return null

        // raw format: mode|grams|servingCount
        val parts = raw.split("|")
        if (parts.size != 3) return null

        return FoodLastInput(
            mode = parts[0],
            grams = parts[1],
            servingCount = parts[2]
        )
    }

    fun save(foodName: String, mode: String, grams: String, servingCount: String) {
        val key = key(foodName)
        val raw = "${mode}|${grams}|${servingCount}"
        prefs.edit().putString(key, raw).apply()
    }

    fun clear(foodName: String) {
        prefs.edit().remove(key(foodName)).apply()
    }

    private fun key(foodName: String): String {
        return "food_last_${foodName.trim().lowercase()}"
    }
}
