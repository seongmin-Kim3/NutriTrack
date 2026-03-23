package com.example.nutritrack.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class OffProductResult(
    val name: String?,
    val caloriesKcal: Int?,   // kcal (대부분 100g 기준)
    val carbsG: Int?,
    val proteinG: Int?,
    val fatG: Int?
)

object OpenFoodFactsClient {

    private val client = OkHttpClient()

    /**
     * OpenFoodFacts v2:
     * https://world.openfoodfacts.org/api/v2/product/{barcode}.json
     */
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

                val productName =
                    product.optString("product_name", null)
                        ?.takeIf { it.isNotBlank() }

                val nutriments = product.optJSONObject("nutriments")

                // OpenFoodFacts는 항목이 제품마다 다를 수 있음
                // energy-kcal_100g / carbohydrates_100g / proteins_100g / fat_100g
                val kcal = nutriments?.optDouble("energy-kcal_100g", Double.NaN)
                    ?.takeIf { !it.isNaN() }?.toInt()

                val carbs = nutriments?.optDouble("carbohydrates_100g", Double.NaN)
                    ?.takeIf { !it.isNaN() }?.toInt()

                val protein = nutriments?.optDouble("proteins_100g", Double.NaN)
                    ?.takeIf { !it.isNaN() }?.toInt()

                val fat = nutriments?.optDouble("fat_100g", Double.NaN)
                    ?.takeIf { !it.isNaN() }?.toInt()

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
