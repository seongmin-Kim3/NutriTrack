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

    // 🌟 공공데이터 포털(data.go.kr) 식품영양성분정보 정식 엔드포인트
    private const val BASE_URL = "https://apis.data.go.kr/1471000/FoodNtrIrdntInfoService1/getFoodNtrItdntList1"

    suspend fun fetchByBarcode(barcode: String): OffProductResult? = withContext(Dispatchers.IO) {
        // 🌟 공공데이터 포털은 바코드 검색을 지원하지 않는 경우가 많으므로
        // 바코드 조회는 글로벌 DB(OpenFoodFacts)를 먼저 활용하도록 복구합니다.
        return@withContext OpenFoodFactsClient.fetchByBarcode(barcode)
    }

    suspend fun smartSearchByName(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<OffProductResult>()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            // 파라미터 구조: 서비스키, 식품명, 페이지번호, 결과수, 타입(JSON)
            val urlString = "$BASE_URL?serviceKey=$API_KEY&desc_kor=$encodedQuery&pageNo=1&numOfRows=20&type=json"
            
            Log.d("API_TEST", "🔍 공공데이터 검색 요청: $urlString")
            val response = URL(urlString).readText()

            val json = JSONObject(response)
            val body = json.optJSONObject("body")
            val items = body?.optJSONArray("items")

            if (items != null) {
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val name = item.optString("DESC_KOR")
                    // 공공데이터 특성상 영양성분이 문자열로 올 수 있어 Double 변환 후 처리
                    val kcal = item.optString("NUTR_CONT1", "0").toDoubleOrNull()?.toInt() ?: 0
                    val carbs = item.optString("NUTR_CONT2", "0").toDoubleOrNull()?.toInt() ?: 0
                    val protein = item.optString("NUTR_CONT3", "0").toDoubleOrNull()?.toInt() ?: 0
                    val fat = item.optString("NUTR_CONT4", "0").toDoubleOrNull()?.toInt() ?: 0

                    if (!name.isNullOrBlank()) {
                        results.add(OffProductResult(name, kcal, carbs, protein, fat))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "공공데이터 검색 에러: ${e.message}")
        }

        // 한국 공공데이터 결과가 없으면 글로벌 DB(OpenFoodFacts)에서 2차 검색
        if (results.isEmpty()) {
            Log.d("API_TEST", "⚠️ 공공데이터 결과 없음, 글로벌 DB로 전환")
            return@withContext OpenFoodFactsClient.searchByName(query)
        }
        return@withContext results
    }
}
