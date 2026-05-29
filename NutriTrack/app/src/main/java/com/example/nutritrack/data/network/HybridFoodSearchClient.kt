package com.example.nutritrack.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object HybridFoodSearchClient {

    // 🚨 여기에 발급받으신 인증키를 다시 붙여넣어 주세요!
    private const val API_KEY = "571e40c0bb0356067625120ba9e467e3fb2a3695d6ab5ab96f2c34c97a9d1ee2"

    private const val BASE_URL = "https://apis.data.go.kr/1471000/FoodNtrIrdntInfoService1/getFoodNtrItdntList1"

    private suspend fun searchFoodSafetyKorea(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<OffProductResult>()

        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "$BASE_URL?serviceKey=$API_KEY&desc_kor=$encodedQuery&pageNo=1&numOfRows=10&type=json"
            val url = URL(urlString)

            Log.d("API_TEST", "🚀 서버에 요청 보내는 중... 검색어: $query")
            Log.d("API_TEST", "🔗 요청 주소: $urlString")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000 // 대기 시간 조금 더 늘림
            connection.readTimeout = 8000

            val responseCode = connection.responseCode
            Log.d("API_TEST", "📥 서버 응답 코드: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 성공했을 때
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                // 만약 응답에 에러 메시지가 포함되어 있다면 (공공데이터 포털 특성)
                if (responseText.contains("<returnReasonCode>") || responseText.contains("INVALID_REQUEST_PARAMETER_ERROR")) {
                    Log.e("API_TEST", "❌ 서버 응답은 OK지만 내부 에러 발생: $responseText")
                    return@withContext emptyList()
                }
                
                Log.d("API_TEST", "✅ 정상 응답 데이터 추출 중...")
                val jsonObject = JSONObject(responseText)
                val body = jsonObject.optJSONObject("body")
                val items = body?.optJSONArray("items")

                if (items != null) {
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val name = item.optString("DESC_KOR", "이름 없음")
                        val kcal = item.optString("NUTR_CONT1", "0").toDoubleOrNull()?.toInt() ?: 0
                        val carbs = item.optString("NUTR_CONT2", "0").toDoubleOrNull()?.toInt() ?: 0
                        val protein = item.optString("NUTR_CONT3", "0").toDoubleOrNull()?.toInt() ?: 0
                        val fat = item.optString("NUTR_CONT4", "0").toDoubleOrNull()?.toInt() ?: 0

                        results.add(OffProductResult(name, kcal, carbs, protein, fat))
                    }
                }
            } else {
                // 에러 코드가 날아왔을 때 (401, 500 등)
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("API_TEST", "❌ 서버 에러 발생! 응답 내용: $errorText")
            }
        } catch (e: Exception) {
            // 인터넷 연결 실패 등 예외가 발생했을 때
            Log.e("API_TEST", "🚨 통신 중 예외(Exception) 발생: ${e.message}", e)
        }

        return@withContext results
    }

    suspend fun smartSearchByName(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        val koreanResults = searchFoodSafetyKorea(query)
        if (koreanResults.isNotEmpty()) {
            return@withContext koreanResults
        }
        return@withContext OpenFoodFactsClient.searchByName(query)
    }
}