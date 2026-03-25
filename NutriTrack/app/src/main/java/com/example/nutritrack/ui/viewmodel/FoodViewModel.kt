package com.example.nutritrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.entity.FoodTemplateEntity
import com.example.nutritrack.data.repo.FoodRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FoodViewModel(
    private val repo: FoodRepository
) : ViewModel() {

    val templates: StateFlow<List<FoodTemplateEntity>> =
        repo.observeTemplates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun insertTemplate(entity: FoodTemplateEntity) {
        viewModelScope.launch { repo.insertTemplate(entity) }
    }

    // 🌟 에러 완벽 해결: 사용자님의 창고 명령어인 repo.deleteTemplate(고유번호) 로 정확히 연결했습니다!
    fun deleteTemplate(entity: FoodTemplateEntity) {
        viewModelScope.launch {
            repo.deleteTemplate(entity.id)
        }
    }

    /**
     * ✅ 내 음식(템플릿) 저장 - 엔티티 필수 필드에 맞춘 버전
     */
    fun saveAsTemplate(
        name: String,
        calories: Int,
        carbs: Int,
        protein: Int,
        fat: Int
    ) {
        val now = System.currentTimeMillis()

        val entity = FoodTemplateEntity(
            id = 0L,
            name = name,
            calories = calories,
            carbs = carbs,
            protein = protein,
            fat = fat,
            createdAtMillis = now
        )

        insertTemplate(entity)
    }
}