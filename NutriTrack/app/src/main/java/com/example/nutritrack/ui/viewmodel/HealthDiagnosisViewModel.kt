package com.example.nutritrack.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthDiagnosisViewModel : ViewModel() {

    private val apiKey = BuildConfig.GEMINI_API_KEY.replace("\\s".toRegex(), "")

    // 🌟 [핵심 수정] 404 에러를 해결하기 위해 v1beta 버전으로 고정합니다.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        requestOptions = RequestOptions(apiVersion = "v1beta")
    )

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Initial)
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    private val _dailyAdvice = MutableStateFlow<String>("식단을 입력하면 AI 조언을 받을 수 있습니다.")
    val dailyAdvice: StateFlow<String> = _dailyAdvice.asStateFlow()

    // 레시피 추천 상태
    private val _recipePlan = MutableStateFlow<String>("")
    val recipePlan: StateFlow<String> = _recipePlan.asStateFlow()

    private val _exercisePlan = MutableStateFlow<String>("")
    val exercisePlan: StateFlow<String> = _exercisePlan.asStateFlow()

    private val _isRecipeLoading = MutableStateFlow(false)
    val isRecipeLoading: StateFlow<Boolean> = _isRecipeLoading.asStateFlow()

    // 주간 분석 상태
    private val _weeklyAnalysis = MutableStateFlow<String>("아래 버튼을 눌러 AI 주간 분석을 시작하세요!")
    val weeklyAnalysis: StateFlow<String> = _weeklyAnalysis.asStateFlow()

    private val _isWeeklyLoading = MutableStateFlow(false)
    val isWeeklyLoading: StateFlow<Boolean> = _isWeeklyLoading.asStateFlow()

    fun analyzeHealthData(bitmap: Bitmap) {
        _uiState.value = DiagnosisUiState.Loading
        viewModelScope.launch {
            try {
                val prompt = "인바디/건강검진 이미지를 분석해서 1.상태요약 2.강점/개선점 3.식단 4.운동 루틴을 한국어로 설명해줘."
                val response = generativeModel.generateContent(content { image(bitmap); text(prompt) })
                _uiState.value = DiagnosisUiState.Success(response.text ?: "결과를 가져올 수 없습니다.")
            } catch (e: Exception) {
                _uiState.value = DiagnosisUiState.Error("분석 에러: ${e.localizedMessage}")
            }
        }
    }

    fun getDailyNutritionAdvice(meals: List<com.example.nutritrack.data.entity.MealEntity>, goals: String) {
        if (meals.isEmpty()) return
        viewModelScope.launch {
            try {
                val summary = meals.joinToString { "${it.name}(${it.calories}kcal)" }
                val prompt = "오늘 식단: $summary, 상황: $goals. 이 식단을 바탕으로 영양 조언을 한 문장으로 해줘."
                val response = generativeModel.generateContent(prompt)
                _dailyAdvice.value = response.text?.trim() ?: "기록이 쌓이면 더 정확한 조언이 가능합니다."
            } catch (e: Exception) {
                _dailyAdvice.value = "조언 생성 중 오류가 발생했습니다."
            }
        }
    }

    fun getRecipeAndExercisePlan(height: String, weight: Float, targetWeight: Float, dietType: String, exerciseCount: String) {
        _isRecipeLoading.value = true
        viewModelScope.launch {
            try {
                val prompt = "키${height}, 체중${weight}, 목표${targetWeight}, 식단${dietType}, 빈도${exerciseCount} 분석해서 ===식단=== 내용 ===운동=== 내용으로 추천해줘."
                val response = generativeModel.generateContent(prompt)
                val result = response.text ?: ""
                _recipePlan.value = result.substringAfter("===식단===").substringBefore("===운동===").trim()
                _exercisePlan.value = result.substringAfter("===운동===").trim()
            } catch (e: Exception) {
                _recipePlan.value = "레시피를 가져오지 못했습니다."
            } finally { _isRecipeLoading.value = false }
        }
    }

    fun getWeeklyAnalysis(dataSummary: String, goalKcal: Int) {
        _isWeeklyLoading.value = true
        viewModelScope.launch {
            try {
                val prompt = "일주일 데이터: $dataSummary, 목표: $goalKcal. 영양 성적표(잘한점, 아쉬운점, 팁)를 작성해줘."
                val response = generativeModel.generateContent(prompt)
                _weeklyAnalysis.value = response.text ?: "분석 실패"
            } catch (e: Exception) {
                _weeklyAnalysis.value = "주간 분석 에러 발생"
            } finally { _isWeeklyLoading.value = false }
        }
    }
}

sealed class DiagnosisUiState {
    object Initial : DiagnosisUiState()
    object Loading : DiagnosisUiState()
    data class Success(val result: String) : DiagnosisUiState()
    data class Error(val message: String) : DiagnosisUiState()
}
