package com.example.nutritrack.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class OffProductResult(
    val name: String?,
    val caloriesKcal: Int?,
    val carbsG: Int?,
    val proteinG: Int?,
    val fatG: Int?
)

object OpenFoodFactsClient {

    private val client = OkHttpClient()

    // 🌟 융통성 있게 영양 데이터를 싹싹 긁어오는 탐지기 함수 추가!
    private fun extractNutrient(nutriments: JSONObject?, vararg keys: String): Int? {
        if (nutriments == null) return null
        for (key in keys) {
            if (nutriments.has(key) && !nutriments.isNull(key)) {
                val value = nutriments.optDouble(key, Double.NaN)
                if (!value.isNaN()) return value.toInt() // 유효한 숫자를 찾으면 바로 반환!
            }
        }
        return null
    }

    suspend fun fetchByBarcode(barcode: String): OffProductResult? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://world.openfoodfacts.org/api/v2/product/$barcode.json"

            val req = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null

                val root = JSONObject(body)
                val product = root.optJSONObject("product") ?: return@withContext null

                // 🌟 이름 탐색: 한국어 이름(product_name_ko)이 있으면 1순위로, 없으면 영어/기본 이름 사용
                val productName = product.optString("product_name_ko", "")
                    .takeIf { it.isNotBlank() }
                    ?: product.optString("product_name", null)
                        ?.takeIf { it.isNotBlank() }

                val nutriments = product.optJSONObject("nutriments")

                // 🌟 영양 정보 탐색: 100g 기준, 1회 제공량, 기본값 순서대로 싹 다 뒤져서 가져옵니다.
                val kcal = extractNutrient(nutriments, "energy-kcal_100g", "energy-kcal_serving", "energy-kcal_value", "energy-kcal")
                val carbs = extractNutrient(nutriments, "carbohydrates_100g", "carbohydrates_serving", "carbohydrates_value", "carbohydrates")
                val protein = extractNutrient(nutriments, "proteins_100g", "proteins_serving", "proteins_value", "proteins")
                val fat = extractNutrient(nutriments, "fat_100g", "fat_serving", "fat_value", "fat")

                OffProductResult(
                    name = productName,
                    caloriesKcal = kcal,
                    carbsG = carbs,
                    proteinG = protein,
                    fatG = fat
                )
            }
        }.getOrNull()
    }
}