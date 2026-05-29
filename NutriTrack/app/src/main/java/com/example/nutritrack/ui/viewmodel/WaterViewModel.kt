package com.example.nutritrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nutritrack.data.settings.GoalPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class WaterViewModel(private val goalPrefs: GoalPrefs) : ViewModel() {
    private val today = LocalDate.now().toString()

    private val _waterIntake = MutableStateFlow(goalPrefs.getWaterIntake(today))
    val waterIntake: StateFlow<Int> = _waterIntake.asStateFlow()

    val waterGoal: Int = goalPrefs.getWaterGoal()

    fun addWater(amount: Int) {
        val newAmount = _waterIntake.value + amount
        _waterIntake.value = newAmount
        goalPrefs.saveWaterIntake(today, newAmount)
    }

    fun resetWater() {
        _waterIntake.value = 0
        goalPrefs.saveWaterIntake(today, 0)
    }
}
