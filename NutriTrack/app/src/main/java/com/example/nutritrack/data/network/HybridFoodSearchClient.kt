package com.example.nutritrack.data.network

import com.example.nutritrack.BuildConfig
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

object HybridFoodSearchClient {

    private val API_KEY = BuildConfig.FOOD_SAFETY_API_KEY.replace("\\s".toRegex(), "")

    // 🌟 가장 안정적인 HTTP 주소와 파라미터 구조 사용
    private const val BASE_URL = "http://openapi.foodsafetykorea.go.kr/api/%s/%s/json/%d/%d/%s=%s"

    suspend fun fetchByBarcode(barcode: String): OffProductResult? = withContext(Dispatchers.IO) {
        try {
            // 1. 바코드로 제품명 조회 (I2790)
            val url = String.format(BASE_URL, API_KEY, "I2790", 1, 1, "BRCD_NO", barcode)
            val response = URL(url).readText()
            val json = JSONObject(response)
            val row = json.optJSONObject("I2790")?.optJSONArray("row")?.optJSONObject(0)

            val productName = row?.optString("PRDT_NM") ?: return@withContext null

            // 2. 제품명으로 영양성분 조회 (I0730)
            return@withContext smartSearchByName(productName).firstOrNull()
        } catch (e: Exception) {
            Log.e("API_TEST", "바코드 조회 실패: ${e.message}")
            null
        }
    }

    suspend fun smartSearchByName(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<OffProductResult>()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            // 🌟 I0730 서비스에서 1번부터 50번까지 결과를 가져옵니다.
            val url = String.format(BASE_URL, API_KEY, "I0730", 1, 50, "DESC_KOR", encodedQuery)

            Log.d("API_TEST", "🔍 검색 요청: $url")
            val response = URL(url).readText()

            // API 키가 활성화되지 않았거나 에러인 경우 체크
            if (response.contains("INFO-32") || response.contains("INFO-200")) {
                Log.e("API_TEST", "❌ API 키 승인 대기 중이거나 유효하지 않음: $response")
                return@withContext OpenFoodFactsClient.searchByName(query)
            }

            val json = JSONObject(response)
            val root = json.optJSONObject("I0730")
            val rows = root?.optJSONArray("row")

            if (rows != null) {
                for (i in 0 until rows.length()) {
                    val item = rows.getJSONObject(i)
                    val name = item.optString("DESC_KOR")
                    val kcal = item.optString("NUTR_CONT1").toDoubleOrNull()?.toInt() ?: 0
                    val carbs = item.optString("NUTR_CONT2").toDoubleOrNull()?.toInt() ?: 0
                    val protein = item.optString("NUTR_CONT3").toDoubleOrNull()?.toInt() ?: 0
                    val fat = item.optString("NUTR_CONT4").toDoubleOrNull()?.toInt() ?: 0

                    if (!name.isNullOrBlank()) {
                        results.add(OffProductResult(name, kcal, carbs, protein, fat))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "검색 에러: ${e.message}")
        }

        // 식약처 데이터가 없으면 전세계 DB(OpenFoodFacts) 호출
        if (results.isEmpty()) {
            return@withContext OpenFoodFactsClient.searchByName(query)
        }
        return@withContext results
    }
}