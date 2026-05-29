package com.example.nutritrack.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthDiagnosisViewModel : ViewModel() {

    // 🚨 여기에 구글 AI 스튜디오에서 발급받은 Gemini API 키를 넣으세요
    private val apiKey = "AIzaSyATEuxW_RjsPR7JvraXXCtY3Eg1H9c73Zw"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Initial)
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    fun analyzeHealthData(bitmap: Bitmap) {
        _uiState.value = DiagnosisUiState.Loading

        viewModelScope.launch {
            try {
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

                _uiState.value = DiagnosisUiState.Success(response.text ?: "분석 결과를 가져올 수 없습니다.")
            } catch (e: Exception) {
                _uiState.value = DiagnosisUiState.Error(e.localizedMessage ?: "알 수 없는 오류가 발생했습니다.")
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