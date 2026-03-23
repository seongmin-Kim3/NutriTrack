package com.example.nutritrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.entity.MealEntity
import com.example.nutritrack.data.repo.MealRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MealViewModel(
    private val repo: MealRepository
) : ViewModel() {

    private fun startOfTodayMillis(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun endOfTodayMillis(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    val todayMeals: StateFlow<List<MealEntity>> =
        repo.observeMealsBetween(startOfTodayMillis(), endOfTodayMillis())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun insertMeal(entity: MealEntity) {
        viewModelScope.launch { repo.insert(entity) }
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch { repo.deleteById(id) }
    }

    fun getMealsLast7Days(): Flow<List<MealEntity>> {
        val now = System.currentTimeMillis()
        val from = now - 7L * 24L * 60L * 60L * 1000L
        return repo.observeMealsBetween(from, now)
    }

    suspend fun getMealById(id: Long): MealEntity? = repo.getById(id)

    fun updateMeal(entity: MealEntity) {
        viewModelScope.launch { repo.update(entity) }
    }
}
