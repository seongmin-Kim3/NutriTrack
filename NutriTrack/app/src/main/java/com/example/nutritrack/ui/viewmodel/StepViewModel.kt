package com.example.nutritrack.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class StepViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs = application.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned

    init {
        checkAndResetDailySteps()
        startTracking()
    }

    private fun startTracking() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun checkAndResetDailySteps() {
        val lastDate = prefs.getString("last_date", "")
        val today = LocalDate.now().toString()

        if (lastDate != today) {
            // 날짜가 바뀌었으면 오늘 기록을 0으로 리셋하기 위해 현재 센서값을 기준점으로 저장
            prefs.edit().apply {
                putString("last_date", today)
                putInt("base_steps", -1) // 다음 센서 이벤트에서 현재값을 base로 잡음
                putInt("saved_steps", 0)
                apply()
            }
            _steps.value = 0
        } else {
            _steps.value = prefs.getInt("saved_steps", 0)
        }
        updateCalories(_steps.value)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0].toInt()
            var baseSteps = prefs.getInt("base_steps", -1)

            if (baseSteps == -1) {
                baseSteps = totalStepsSinceBoot
                prefs.edit().putInt("base_steps", baseSteps).apply()
            }

            val todaySteps = (totalStepsSinceBoot - baseSteps).coerceAtLeast(0)
            _steps.value = todaySteps
            updateCalories(todaySteps)
            
            // 앱이 꺼져있을 때를 대비해 현재 걸음수 저장
            prefs.edit().putInt("saved_steps", todaySteps).apply()
        }
    }

    private fun updateCalories(steps: Int) {
        // 평균적으로 30걸음당 1kcal 소모 (몸무게/속도에 따라 다르지만 일반적인 기준)
        _caloriesBurned.value = (steps * 0.04).toInt()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}
