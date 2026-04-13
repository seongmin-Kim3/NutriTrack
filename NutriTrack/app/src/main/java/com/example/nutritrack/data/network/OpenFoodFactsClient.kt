package com.example.nutritrack.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder // 🌟 한글 검색 필수 번역기

data class OffProductResult(
    val name: String?,
    val caloriesKcal: Int?,
    val carbsG: Int?,
    val proteinG: Int?,
    val fatG: Int?
)

object OpenFoodFactsClient {

    private val client = OkHttpClient()

    private fun extractNutrient(nutriments: JSONObject?, vararg keys: String): Int? {
        if (nutriments == null) return null
        for (key in keys) {
            if (nutriments.has(key) && !nutriments.isNull(key)) {
                val value = nutriments.optDouble(key, Double.NaN)
                if (!value.isNaN()) return value.toInt()
            }
        }
        return null
    }

    suspend fun fetchByBarcode(barcode: String): OffProductResult? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://world.openfoodfacts.org/api/v2/product/$barcode.json"
            val req = Request.Builder().url(url).get().build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val root = JSONObject(body)
                val product = root.optJSONObject("product") ?: return@withContext null

                val productName = product.optString("product_name_ko", "")
                    .takeIf { it.isNotBlank() } ?: product.optString("product_name", null)
                    ?.takeIf { it.isNotBlank() }

                val nutriments = product.optJSONObject("nutriments")
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

    suspend fun searchByName(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        runCatching {
            // 🌟 1. 한글 검색어 깨짐 완벽 방지!
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            // 🌟 2. 모두 먹통이 된 원인인 'fields' 옵션 제거!
            // 대신 page_size를 8로 줄여서 불필요한 데이터를 줄이고 속도를 높였습니다.
            val url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$encodedQuery&search_simple=1&action=process&json=1&page_size=8"

            val req = Request.Builder().url(url).get().build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val body = resp.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(body)
                val productsArray = root.optJSONArray("products") ?: return@withContext emptyList()

                val results = mutableListOf<OffProductResult>()
                for (i in 0 until productsArray.length()) {
                    val product = productsArray.optJSONObject(i) ?: continue

                    // 한국어 이름 최우선, 그다음 영어, 그다음 기본 이름
                    val productName = product.optString("product_name_ko", "")
                        .takeIf { it.isNotBlank() }
                        ?: product.optString("product_name", "")
                            .takeIf { it.isNotBlank() }
                        ?: product.optString("product_name_en", "")
                            .takeIf { it.isNotBlank() }
                        ?: continue

                    val nutriments = product.optJSONObject("nutriments")
                    val kcal = extractNutrient(nutriments, "energy-kcal_100g", "energy-kcal_serving", "energy-kcal_value", "energy-kcal") ?: 0
                    val carbs = extractNutrient(nutriments, "carbohydrates_100g", "carbohydrates_serving", "carbohydrates_value", "carbohydrates") ?: 0
                    val protein = extractNutrient(nutriments, "proteins_100g", "proteins_serving", "proteins_value", "proteins") ?: 0
                    val fat = extractNutrient(nutriments, "fat_100g", "fat_serving", "fat_value", "fat") ?: 0

                    results.add(OffProductResult(productName, kcal, carbs, protein, fat))
                }
                results
            }
        }.getOrDefault(emptyList())
    }
}