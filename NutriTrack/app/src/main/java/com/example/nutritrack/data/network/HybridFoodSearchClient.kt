package com.example.nutritrack.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 🌟 [임시 식약처 모킹(Mocking) 라우터]
 * 가상의 식약처 데이터(한국어 특화)와 OpenFoodFacts(글로벌)를 결합한 하이브리드 엔진
 */
object HybridFoodSearchClient {

    // 1. 가상의 식약처 공공데이터 DB (발표 시연용 고품질 한식 데이터 😋)
    // 💡 나중에 식약처 API 키가 나오면 이 부분만 진짜 API 호출 코드로 싹 갈아끼우면 됩니다!
    private val koreanFoodDatabase = listOf(
        OffProductResult("제육볶음 (1인분)", 550, 25, 30, 35),
        OffProductResult("된장찌개 (1뚝배기)", 140, 15, 12, 4),
        OffProductResult("김치찌개 (1뚝배기)", 160, 12, 15, 6),
        OffProductResult("돼지국밥 (1그릇)", 460, 10, 45, 25),
        OffProductResult("떡볶이 (1인분)", 300, 60, 8, 3),
        OffProductResult("닭가슴살 샐러드", 180, 10, 25, 5),
        OffProductResult("현미밥 (1공기)", 320, 70, 6, 2),
        OffProductResult("삼겹살 (1인분)", 650, 2, 40, 50),
        OffProductResult("비빔밥 (1그릇)", 480, 75, 12, 10)
    )

    // 2. 한국어 데이터 1차 검색 함수
    private suspend fun searchFoodSafetyKorea(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        // 검색어가 포함된 한식 메뉴를 모두 찾습니다.
        koreanFoodDatabase.filter { it.name?.contains(query, ignoreCase = true) == true }
    }

    // 🌟 3. 핵심! 하이브리드 스마트 검색 함수
    suspend fun smartSearchByName(query: String): List<OffProductResult> = withContext(Dispatchers.IO) {
        // [STEP 1] 1차적으로 국내 데이터(식약처 Mock DB)를 먼저 뒤집니다.
        val koreanResults = searchFoodSafetyKorea(query)

        if (koreanResults.isNotEmpty()) {
            // 한국 음식 데이터가 있으면 글로벌 API를 찌르지 않고 즉시 반환! (로딩 0초)
            return@withContext koreanResults
        }

        // [STEP 2] 국내 데이터에 없으면(예: 오레오, 스니커즈 등) 2차로 기존 글로벌 API를 찌릅니다.
        return@withContext OpenFoodFactsClient.searchByName(query)
    }
}