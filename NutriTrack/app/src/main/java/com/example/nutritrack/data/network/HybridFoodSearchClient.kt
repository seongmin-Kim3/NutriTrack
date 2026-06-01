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

    // 🌟 식품안전나라(foodsafetykorea) 전용 API 엔드포인트 구조
    private const val BASE_URL_HTTPS = "https://openapi.foodsafetykorea.go.kr/api/%s/%s/json/%d/%d/%s=%s"
    private const val BASE_URL_HTTP = "http://openapi.foodsafetykorea.go.kr/api/%s/%s/json/%d/%d/%s=%s"

    /**
     * 바코드를 찍었을 때: 바코드 -> 제품명(I2790) -> 영양성분(I0730/C003) 순으로 조회
     */
    suspend fun fetchByBarcode(barcode: String): OffProductResult? = withContext(Dispatchers.IO) {
        try {
            Log.d("API_TEST", "🚀 바코드 조회 요청: $barcode")

            // [Step 1] 바코드로 제품명 조회 (I2790 서비스 사용)
            val response = callApi("I2790", 1, 1, "BRCD_NO", barcode) ?: return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
            
            // 서버에서 HTML/Script 응답이 오면 무시 (에러 방지)
            if (!response.trim().startsWith("{")) {
                Log.e("API_TEST", "❌ 서버가 JSON 대신 이상한 데이터를 보냄 (인증오류 의심)")
                return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
            }

            val json = JSONObject(response)
            val root = json.optJSONObject("I2790") ?: json.optJSONObject("RESULT")
            val row = root?.optJSONArray("row")?.optJSONObject(0)

            val productName = row?.optString("PRDT_NM")
            if (productName.isNullOrBlank()) {
                Log.e("API_TEST", "❌ 바코드 제품명 없음")
                return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
            }

            Log.d("API_TEST", "✅ 제품명 찾기 성공: $productName")

            // [Step 2] 찾은 제품명으로 영양성분 조회
            return@withContext smartSearchByName(productName).firstOrNull()
        } catch (e: Exception) {
            Log.e("API_TEST", "🚨 바코드 통신 오류: ${e.message}")
            return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
        }
    }

    /**
     * 이름으로 검색할 때: 식품영양성분정보(I0730) 및 가공식품 영양정보(C003) 활용
     */
    suspend fun smartSearchByName(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<OffProductResult>()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            
            // 🌟 1. I0730 서비스 (식품영양성분정보) 검색 - 결과를 100개로 확대
            results.addAll(fetchFromService("I0730", "DESC_KOR", encodedQuery, 100))
            
            // 🌟 2. 가공식품 영양정보(C003)도 추가로 대량 검색
            results.addAll(fetchFromService("C003", "PRDLST_NM", encodedQuery, 100))

        } catch (e: Exception) {
            Log.e("API_TEST", "🚨 검색 에러: ${e.message}")
        }

        // 결과가 여전히 없으면 글로벌 DB(OpenFoodFacts)로 보충
        if (results.isEmpty()) {
            Log.d("API_TEST", "⚠️ 국내 데이터 없음 -> OpenFoodFacts 검색")
            return@withContext OpenFoodFactsClient.searchByName(query)
        }
        
        // 중복 제거 시 '이름'과 '칼로리'가 모두 같은 경우만 제거하여 다양한 종류를 살림
        val distinctResults = results.distinctBy { "${it.name}_${it.caloriesKcal}_${it.servingInfo}" }
        
        Log.d("API_TEST", "✅ 총 ${distinctResults.size}개의 결과 발견")
        return@withContext distinctResults
    }

    private fun fetchFromService(serviceId: String, queryParam: String, encodedQuery: String, limit: Int): List<OffProductResult> {
        val list = mutableListOf<OffProductResult>()
        val response = callApi(serviceId, 1, limit, queryParam, encodedQuery) ?: return emptyList()
        
        if (!response.trim().startsWith("{")) {
            Log.e("API_TEST", "❌ $serviceId 서비스: 서버 응답 형식 오류")
            return emptyList()
        }

        try {
            val json = JSONObject(response)
            val root = json.optJSONObject(serviceId)
            val rows = root?.optJSONArray("row")

            if (rows != null) {
                for (i in 0 until rows.length()) {
                    val item = rows.getJSONObject(i)
                    val name = item.optString("DESC_KOR").takeIf { it.isNotBlank() } ?: item.optString("PRDLST_NM")
                    
                    val kcal = item.optString("NUTR_CONT1").toDoubleOrNull()?.toInt() ?: 0
                    val carbs = item.optString("NUTR_CONT2").toDoubleOrNull()?.toInt() ?: 0
                    val protein = item.optString("NUTR_CONT3").toDoubleOrNull()?.toInt() ?: 0
                    val fat = item.optString("NUTR_CONT4").toDoubleOrNull()?.toInt() ?: 0

                    val servingWt = item.optString("SERVING_WT").takeIf { it.isNotBlank() && it != "null" }
                        ?: item.optString("SERVING_SIZE").takeIf { it.isNotBlank() && it != "null" }
                    val servingUnit = item.optString("SERVING_UNIT").takeIf { it.isNotBlank() && it != "null" } ?: "g"
                    
                    val servingInfo = if (servingWt != null) "$servingWt$servingUnit 기준" else "정보 없음"

                    if (!name.isNullOrBlank()) {
                        list.add(OffProductResult(name, kcal, carbs, protein, fat, servingInfo))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "❌ $serviceId 파싱 에러")
        }
        return list
    }

    private fun callApi(serviceId: String, start: Int, end: Int, param: String, value: String): String? {
        return try {
            val url = String.format(java.util.Locale.US, BASE_URL_HTTPS, API_KEY, serviceId, start, end, param, value)
            URL(url).readText()
        } catch (e: Exception) {
            try {
                val httpUrl = String.format(java.util.Locale.US, BASE_URL_HTTP, API_KEY, serviceId, start, end, param, value)
                URL(httpUrl).readText()
            } catch (_: Exception) {
                null
            }
        }
    }
}
