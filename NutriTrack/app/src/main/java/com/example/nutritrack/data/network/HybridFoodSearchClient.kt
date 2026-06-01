package com.example.nutritrack.data.network

import com.example.nutritrack.BuildConfig
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

object HybridFoodSearchClient {

    private val API_KEY = BuildConfig.FOOD_SAFETY_API_KEY.trim()

    // ✅ 공공데이터포털 API (apis.data.go.kr)
    private const val BASE_URL = "https://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02/getFoodNtrCpntDbInq02"

    // ✅ 유통바코드는 식품안전나라 API 그대로 사용
    private const val BARCODE_URL = "https://openapi.foodsafetykorea.go.kr/api/%s/I2790/json/1/1/BRCD_NO/%s"

    /**
     * 바코드 조회: 바코드 → 제품명 → 영양성분 순서
     */
    suspend fun fetchByBarcode(barcode: String): OffProductResult? = withContext(Dispatchers.IO) {
        try {
            Log.d("API_TEST", "🚀 바코드 조회 요청: $barcode")

            val url = String.format(BARCODE_URL, API_KEY, barcode)
            val response = try { URL(url).readText() } catch (e: Exception) { null }
                ?: return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)

            if (!response.trim().startsWith("{")) {
                Log.e("API_TEST", "❌ 바코드 응답 형식 오류")
                return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
            }

            val json = JSONObject(response)
            val resultCode = json.optJSONObject("I2790")
                ?.optJSONObject("RESULT")?.optString("CODE") ?: ""
            if (resultCode == "INFO-200") {
                Log.e("API_TEST", "❌ 바코드 결과 없음 → OpenFoodFacts")
                return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
            }

            val row = json.optJSONObject("I2790")?.optJSONArray("row")?.optJSONObject(0)
            val productName = row?.optString("PRDLST_NM")
            if (productName.isNullOrBlank()) {
                return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
            }

            Log.d("API_TEST", "✅ 바코드 제품명: $productName")
            return@withContext smartSearchByName(productName).firstOrNull()

        } catch (e: Exception) {
            Log.e("API_TEST", "🚨 바코드 오류: ${e.message}")
            return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
        }
    }

    /**
     * 이름으로 검색: 공공데이터포털 식품영양성분DB 사용
     */
    suspend fun smartSearchByName(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<OffProductResult>()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$BASE_URL?serviceKey=$API_KEY&FOOD_NM_KR=$encodedQuery&numOfRows=30&pageNo=1&type=json"
            Log.d("API_TEST", "📡 요청 URL: $url")

            val response = URL(url).readText()

            if (!response.trim().startsWith("{")) {
                Log.e("API_TEST", "❌ 응답 형식 오류")
                return@withContext OpenFoodFactsClient.searchByName(query)
            }

            val json = JSONObject(response)
            val resultCode = json.optJSONObject("header")?.optString("resultCode") ?: ""
            if (resultCode != "00") {
                Log.e("API_TEST", "❌ API 오류 코드: $resultCode")
                return@withContext OpenFoodFactsClient.searchByName(query)
            }

            val items = json.optJSONObject("body")?.optJSONArray("items")
            if (items == null || items.length() == 0) {
                Log.d("API_TEST", "⚠️ 결과 없음 → OpenFoodFacts")
                return@withContext OpenFoodFactsClient.searchByName(query)
            }

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)

                val name = item.optString("FOOD_NM_KR").takeIf { it.isNotBlank() } ?: continue

                // 영양성분 파싱 (AMT_NUM1~4: 에너지, 탄수화물, 단백질, 지방)
                val kcal    = item.optString("AMT_NUM1").toDoubleOrNull()?.toInt() ?: 0
                val carbs   = item.optString("AMT_NUM2").toDoubleOrNull()?.toInt() ?: 0
                val protein = item.optString("AMT_NUM3").toDoubleOrNull()?.toInt() ?: 0
                val fat     = item.optString("AMT_NUM4").toDoubleOrNull()?.toInt() ?: 0

                val servingSize = item.optString("SERVING_SIZE").takeIf { it.isNotBlank() && it != "null" }
                val servingInfo = if (servingSize != null) "${servingSize}g 기준" else "100g 기준"

                results.add(OffProductResult(name, kcal, carbs, protein, fat, servingInfo))
            }

            Log.d("API_TEST", "✅ 총 ${results.size}개 결과")

        } catch (e: Exception) {
            Log.e("API_TEST", "🚨 검색 오류: ${e.message}")
            return@withContext OpenFoodFactsClient.searchByName(query)
        }

        if (results.isEmpty()) {
            Log.d("API_TEST", "⚠️ 국내 데이터 없음 → OpenFoodFacts")
            return@withContext OpenFoodFactsClient.searchByName(query)
        }

        return@withContext results.distinctBy { "${it.name}_${it.caloriesKcal}" }
    }
}