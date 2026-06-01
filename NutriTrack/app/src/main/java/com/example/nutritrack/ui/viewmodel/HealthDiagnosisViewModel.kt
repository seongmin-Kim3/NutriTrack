package com.example.nutritrack.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthDiagnosisViewModel : ViewModel() {

    private val apiKey = BuildConfig.GEMINI_API_KEY.replace("\\s".toRegex(), "")

    // 🌟 [Gemini 2.0 전용] 최신 2.0 모델을 사용하여 성능을 극대화합니다.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
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
                val prompt = """
                    당신은 최고 수준의 헬스 분석가입니다. 인바디/건강검진 이미지를 분석하여 다음 규칙에 따라 '한국어'로 핵심만 보고하세요.
                    1. 인사말은 생략하고 바로 본론으로 들어갑니다.
                    2. 반드시 다음 구분자를 사용하여 정보를 분리하세요:
                    [분석] (현재 상태 요약, 핵심 수치 강조)
                    [강점] (현재 신체의 긍정적인 부분)
                    [개선] (가장 시급하게 고쳐야 할 부분)
                    [식단] (추천하는 영양소 구성과 식재료)
                    [운동] (추천하는 운동 종류와 빈도)
                    
                    불필요한 설명은 빼고 짧고 강렬한 문장 위주로 작성하세요.
                """.trimIndent()
                val response = generativeModel.generateContent(content { image(bitmap); text(prompt) })
                _uiState.value = DiagnosisUiState.Success(response.text ?: "분석 실패")
            } catch (e: Exception) {
                _uiState.value = DiagnosisUiState.Error("네트워크 상태를 확인해주세요.")
            }
        }
    }

    fun getDailyNutritionAdvice(meals: List<com.example.nutritrack.data.entity.MealEntity>, goals: String) {
        if (meals.isEmpty()) return
        viewModelScope.launch {
            try {
                val summary = meals.joinToString { "${it.name}(${it.calories}kcal)" }
                val prompt = """
                    당신은 다정한 퍼스널 영양사입니다. 
                    오늘의 식단($summary)과 사용자의 상황($goals)을 분석해서, 
                    사용자에게 힘이 되는 따뜻한 조언이나 영양학적 팁을 딱 한 문장(50자 내외)으로 말해주세요.
                    예: "단백질 섭취가 아주 좋아요! 저녁엔 가벼운 산책으로 소화를 도와보는 건 어떨까요?"
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                _dailyAdvice.value = response.text?.trim() ?: "맛있게 드셨나요? 기록을 이어가 보세요!"
            } catch (e: Exception) {
                Log.e("AI_DEBUG", "일일 조언 생성 실패", e)
                _dailyAdvice.value = "기록이 쌓이면 특별한 조언을 해드릴게요!"
            }
        }
    }

    fun getRecipeAndExercisePlan(height: String, weight: Float, targetWeight: Float, dietType: String, exerciseCount: String) {
        _isRecipeLoading.value = true
        viewModelScope.launch {
            try {
                val prompt = """
                    목표: ${targetWeight}kg 달성을 위한 맞춤형 처방.
                    사용자: 키 ${height}cm, 체중 ${weight}kg, 선호식단 $dietType, 빈도 $exerciseCount.
                    
                    반드시 아래 형식으로만 답변하세요:
                    ===식단===
                    [아침]
                    메뉴: (이름)
                    칼로리: (kcal)
                    재료: 재료1, 재료2, 재료3
                    설명: (짧은 설명)
                    
                    [점심]
                    메뉴: (이름)
                    칼로리: (kcal)
                    재료: 재료1, 재료2, 재료3
                    설명: (짧은 설명)
                    
                    [저녁]
                    메뉴: (이름)
                    칼로리: (kcal)
                    재료: 재료1, 재료2, 재료3
                    설명: (짧은 설명)

                    ===운동===
                    - 월/수/금: 운동종류 (세트/횟수)
                    - 화/목: 운동종류 (세트/횟수)
                    - 꿀팁: 한 줄 요약
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                val result = response.text ?: ""
                _recipePlan.value = result.substringAfter("===식단===").substringBefore("===운동===").trim()
                _exercisePlan.value = result.substringAfter("===운동===").trim()
            } catch (e: Exception) {
                _recipePlan.value = "레시피를 준비하지 못했습니다."
            } finally { _isRecipeLoading.value = false }
        }
    }

    fun getAiNutritionGoal(height: Float, weight: Float, targetWeight: Float, activityLevel: String, dietGoal: String, onResult: (Int, Int, Int, Int) -> Unit) {
        viewModelScope.launch {
            try {
                val prompt = """
                    당신은 임상 영양사입니다. 다음 사용자의 정보를 바탕으로 하루 권장 영양 목표를 산출하세요.
                    정보: 키 ${height}cm, 체중 ${weight}kg, 목표체중 ${targetWeight}kg, 활동량 $activityLevel, 다이어트 목적 $dietGoal.
                    
                    결과는 반드시 다음 형식으로만 답변하세요:
                    칼로리: [숫자]
                    탄수화물: [숫자]
                    단백질: [숫자]
                    지방: [숫자]
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: ""
                
                val kcal = text.substringAfter("칼로리:").substringBefore("\n").trim().filter { it.isDigit() }.toIntOrNull() ?: 2000
                val carbs = text.substringAfter("탄수화물:").substringBefore("\n").trim().filter { it.isDigit() }.toIntOrNull() ?: 250
                val protein = text.substringAfter("단백질:").substringBefore("\n").trim().filter { it.isDigit() }.toIntOrNull() ?: 150
                val fat = text.substringAfter("지방:").substringBefore("\n").trim().filter { it.isDigit() }.filter { it.isDigit() }.toIntOrNull() ?: 60
                
                onResult(kcal, carbs, protein, fat)
            } catch (e: Exception) {
                Log.e("AI_DEBUG", "영양 목표 설계 에러", e)
            }
        }
    }

    fun getWeeklyAnalysis(dataSummary: String, goalKcal: Int) {
        _isWeeklyLoading.value = true
        viewModelScope.launch {
            try {
                val prompt = """
                    일주일 식단: $dataSummary, 일일목표: $goalKcal kcal.
                    지난 일주일의 영양 성적표를 작성하세요.
                    반드시 다음 형식으로 답변하세요:
                    [총평] (전반적인 습관 평가)
                    [칭찬] (가장 잘 지킨 영양소나 습관)
                    [주의] (가장 부족하거나 과했던 부분)
                    [액션플랜] (다음 주에 바로 실천할 행동 1가지)
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                _weeklyAnalysis.value = response.text ?: "분석 실패"
            } catch (e: Exception) {
                _weeklyAnalysis.value = "데이터가 부족하여 분석할 수 없습니다."
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
