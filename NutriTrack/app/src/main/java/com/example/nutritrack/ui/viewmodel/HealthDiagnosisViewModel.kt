package com.example.nutritrack.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthDiagnosisViewModel : ViewModel() {

    // 🚨 공백이나 줄바꿈을 완벽히 제거하기 위해 replace 사용
    private val apiKey = "".replace("\\s".toRegex(), "")

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        requestOptions = RequestOptions(apiVersion = "v1")
    )

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Initial)
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    private val _dailyAdvice = MutableStateFlow<String>("식단을 입력하면 AI 조언을 받을 수 있습니다.")
    val dailyAdvice: StateFlow<String> = _dailyAdvice.asStateFlow()

    fun analyzeHealthData(bitmap: Bitmap) {
        _uiState.value = DiagnosisUiState.Loading
        Log.d("AI_DEBUG", "🚀 건강 데이터 분석 시작 (이미지 포함)")

        viewModelScope.launch {
            try {
                // ... (생략된 프롬프트 내용 유지) ...
                val prompt = """
                    당신은 전문적인 퍼스널 트레이너이자 임상 영양사입니다.
                    첨부된 이미지는 사용자의 인바디(체성분 분석) 또는 건강검진 결과지입니다.
                    
                    다음 양식에 맞춰서 사용자가 이해하기 쉽게 분석 결과를 한국어로 작성해 주세요:
                    1. 📊 현재 상태 요약 (핵심 수치 중심)
                    2. 💡 강점 및 개선이 필요한 점
                    3. 🥗 맞춤 식단 가이드 (탄/단/지 비율 및 추천 식재료)
                    4. 🏃‍♂️ 추천 운동 루틴
                    
                    전문 용어는 쉽게 풀어서 설명해 주세요.
                """.trimIndent()

                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )

                val resultText = response.text ?: "분석 결과를 가져올 수 없습니다."
                Log.d("AI_DEBUG", "✅ AI 분석 성공: $resultText")
                _uiState.value = DiagnosisUiState.Success(resultText)
            } catch (e: Exception) {
                Log.e("AI_DEBUG", "❌ AI 분석 중 에러 발생: ${e.message}", e)
                val userFriendlyMessage = when {
                    e.message?.contains("API_KEY_INVALID") == true -> "API 키가 유효하지 않습니다. Google AI Studio에서 키를 확인해주세요."
                    e.message?.contains("QUOTA_EXCEEDED") == true -> "API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
                    else -> e.localizedMessage ?: "알 수 없는 오류가 발생했습니다."
                }
                _uiState.value = DiagnosisUiState.Error(userFriendlyMessage)
            }
        }
    }

    fun getDailyNutritionAdvice(meals: List<com.example.nutritrack.data.entity.MealEntity>, goals: String) {
        if (meals.isEmpty()) return
        Log.d("AI_DEBUG", "🚀 일일 영양 조언 요청 중...")

        viewModelScope.launch {
            try {
                val mealSummary = meals.joinToString(", ") { "${it.name}(${it.calories}kcal)" }
                val prompt = """
                    사용자의 오늘 식단: $mealSummary
                    사용자의 목표/상황: $goals
                    
                    위 식단을 바탕으로 영양학적 조언을 딱 한 문장(50자 내외)으로 친절하게 한국어로 해주세요. 
                    예: "오늘은 단백질이 부족하니 저녁에는 두부를 곁들여 보세요!"
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val advice = response.text?.trim() ?: "맛있게 드셨나요? 꾸준한 기록이 중요합니다!"
                Log.d("AI_DEBUG", "✅ 일일 조언 성공: $advice")
                _dailyAdvice.value = advice
            } catch (e: Exception) {
                Log.e("AI_DEBUG", "❌ 일일 조언 에러 발생", e)
                // 🌟 에러의 클래스 이름과 메시지를 모두 화면에 표시합니다.
                _dailyAdvice.value = "에러: ${e.javaClass.simpleName} - ${e.message}"
            }
        }
    }
}

sealed class DiagnosisUiState {
    object Initial : DiagnosisUiState()
    object Loading : DiagnosisUiState()
    data class Success(val result: String) : DiagnosisUiState()
    data class Error(val message: String) : DiagnosisUiState()
}