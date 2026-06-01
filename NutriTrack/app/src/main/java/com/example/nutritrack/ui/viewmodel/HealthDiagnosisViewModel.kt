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

    // 🌟 [Gemini 2.5 전용] 최신 모델을 사용합니다.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
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
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                _dailyAdvice.value = response.text?.trim() ?: "맛있게 드셨나요? 기록을 이어가 보세요!"
            } catch (e: Exception) {
                _dailyAdvice.value = "기록이 쌓이면 특별한 조언을 해드릴게요!"
            }
        }
    }

    fun getRecipeAndExercisePlan(height: String, weight: Float, targetWeight: Float, dietType: String, exerciseCount: String) {
        _isRecipeLoading.value = true
        _recipePlan.value = "" // 🌟 로딩 시작 시 기존 플랜 초기화
        _exercisePlan.value = ""
        
        viewModelScope.launch {
            try {
                val prompt = """
                    당신은 최고 실력의 영양사 및 트레이너입니다.
                    목표: ${targetWeight}kg 달성, 사용자: 키 ${height}cm, 체중 ${weight}kg, 선호식단 $dietType, 빈도 $exerciseCount.
                    
                    반드시 아래 형식을 '완벽하게' 지켜서 답변하세요. 다른 미사여구는 절대 하지 마세요.
                    
                    ===식단===
                    [아침] 메뉴명 (000 kcal) - 재료: 재료1, 재료2, 재료3
                    [점심] 메뉴명 (000 kcal) - 재료: 재료1, 재료2, 재료3
                    [저녁] 메뉴명 (000 kcal) - 재료: 재료1, 재료2, 재료3
                    
                    ===운동===
                    - 운동명 1: 상세내용 (유튜브 가이드: https://www.youtube.com/results?search_query=운동명1)
                    - 운동명 2: 상세내용 (유튜브 가이드: https://www.youtube.com/results?search_query=운동명2)
                    
                    🚨 중요: 
                    1. 식단은 반드시 [끼니] 메뉴명 (칼로리) - 재료: 재료들 형식을 한 줄에 하나씩 작성하세요.
                    2. 운동은 앞에 '-'를 붙여서 작성하세요.
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                val result = response.text ?: ""
                _recipePlan.value = result.substringAfter("===식단===").substringBefore("===운동===").trim()
                _exercisePlan.value = result.substringAfter("===운동===").trim()
            } catch (e: Exception) {
                Log.e("AI_DEBUG", "Plan generation error", e)
                _recipePlan.value = "에러가 발생했습니다. 잠시 후 다시 시도해주세요."
            } finally {
                _isRecipeLoading.value = false
            }
        }
    }

    fun getAiNutritionGoal(height: Float, weight: Float, targetWeight: Float, activityLevel: String, dietGoal: String, onResult: (Int, Int, Int, Int) -> Unit) {
        viewModelScope.launch {
            try {
                // 🌟 [AI 프롬프트 고도화] 모든 변수를 활용하여 과학적인 수치를 요청합니다.
                val prompt = """
                    당신은 임상 영양사입니다. 다음 사용자의 정보를 바탕으로 과학적이고 정밀한 하루 권장 영양 목표를 산출하세요.
                    
                    사용자 정보:
                    - 키: ${height}cm
                    - 현재 체중: ${weight}kg
                    - 목표 체중: ${targetWeight}kg
                    - 평소 활동량: $activityLevel
                    - 다이어트 목적: $dietGoal
                    
                    위 정보를 모두 참고하여, 목표 달성을 위한 최적의 '하루 섭취량'을 계산하세요.
                    반드시 아래 형식으로만 답변하고, 다른 미사여구는 생략하세요.
                    
                    칼로리: [숫자]
                    탄수화물: [숫자]
                    단백질: [숫자]
                    지방: [숫자]
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: ""
                
                // 🌟 [수치 오류 완벽 해결] 정규식을 사용하여 각 라벨 뒤에 오는 '첫 번째 숫자'만 정확히 가져옵니다.
                fun extractNum(source: String, label: String): Int? {
                    val line = source.lines().find { it.contains(label) } ?: return null
                    // 라벨 이후의 텍스트에서 숫자만 찾음
                    val afterLabel = line.substringAfter(label)
                    return Regex("\\d+").find(afterLabel)?.value?.toIntOrNull()
                }
                
                val kcal = extractNum(text, "칼로리:") ?: 2100
                val carbs = extractNum(text, "탄수화물:") ?: 260
                val protein = extractNum(text, "단백질:") ?: 150
                val fat = extractNum(text, "지방:") ?: 65
                
                onResult(kcal, carbs, protein, fat)
            } catch (e: Exception) {
                Log.e("AI_DEBUG", "영양 목표 설계 에러", e)
                // 실패 시 기본값이라도 반환
                onResult(2100, 260, 150, 65)
            }
        }
    }

    fun getWeeklyAnalysis(dataSummary: String, goalKcal: Int) {
        _isWeeklyLoading.value = true
        viewModelScope.launch {
            try {
                val prompt = """
                    일주일 식단 데이터: $dataSummary
                    일일 목표 칼로리: $goalKcal kcal
                    
                    위 데이터를 바탕으로 이번 주의 영양 성적표를 '핵심만 아주 짧게' 작성하세요. 
                    인사말, 기간 설명, 표 형식의 데이터 나열은 '절대' 하지 마세요.
                    
                    반드시 아래 형식을 지키고, 각 항목당 '딱 한 문장'으로만 답변하세요:
                    [총평] 이번 주 전반적인 식습관에 대한 한 줄 평
                    [칭찬] 가장 훌륭했던 점 한 줄
                    [주의] 가장 개선이 필요한 점 한 줄
                    [액션플랜] 다음 주를 위한 핵심 미션 한 줄
                """.trimIndent()
                val response = generativeModel.generateContent(prompt)
                _weeklyAnalysis.value = response.text ?: "분석 내용을 가져오지 못했습니다."
            } catch (e: Exception) {
                _weeklyAnalysis.value = "지난 일주일의 기록을 분석하고 있습니다. 잠시만 기다려주세요!"
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
