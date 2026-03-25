package com.example.nutritrack.data.settings

import android.content.Context
import java.time.DayOfWeek

class GoalPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("goals", Context.MODE_PRIVATE)

    // ✅ 사용자 기본 신체 정보 및 목적 불러오기
    fun getUserHeight(): Float = prefs.getFloat("height", 170f)
    fun getUserWeight(): Float = prefs.getFloat("weight", 65f)
    fun getTargetWeight(): Float = prefs.getFloat("target_weight", 60f) // 🌟 추가됨: 목표 체중
    fun getActivityLevel(): String = prefs.getString("activity_level", "보통 (주 3~5회)") ?: "보통 (주 3~5회)"
    fun getDietGoal(): String = prefs.getString("diet_goal", "체중 유지") ?: "체중 유지"

    fun isProfileSetup(): Boolean = prefs.getBoolean("profile_setup", false)

    // ✅ 사용자 기본 신체 정보 저장하기 (목표 체중 포함)
    fun saveUserProfile(height: Float, weight: Float, targetWeight: Float, activityLevel: String, dietGoal: String) {
        prefs.edit()
            .putFloat("height", height)
            .putFloat("weight", weight)
            .putFloat("target_weight", targetWeight)
            .putString("activity_level", activityLevel)
            .putString("diet_goal", dietGoal)
            .putBoolean("profile_setup", true)
            .apply()
    }

    // 영양 목표 불러오기/저장하기
    fun getKcalGoal(): Int = prefs.getInt("kcal", 2000)
    fun getCarbsGoal(): Int = prefs.getInt("carbs", 250)
    fun getProteinGoal(): Int = prefs.getInt("protein", 150)
    fun getFatGoal(): Int = prefs.getInt("fat", 60)

    fun saveGoals(kcal: Int, carbs: Int, protein: Int, fat: Int) {
        prefs.edit()
            .putInt("kcal", kcal)
            .putInt("carbs", carbs)
            .putInt("protein", protein)
            .putInt("fat", fat)
            .apply()
    }

    // 운동 루틴 불러오기/저장하기
    fun getRoutineForDay(dayOfWeekName: String): String {
        return prefs.getString("routine_$dayOfWeekName", "휴식") ?: "휴식"
    }

    fun saveWeeklyRoutines(monday: String, tuesday: String, wednesday: String, thursday: String, friday: String, saturday: String, sunday: String) {
        prefs.edit()
            .putString("routine_${DayOfWeek.MONDAY.name}", monday)
            .putString("routine_${DayOfWeek.TUESDAY.name}", tuesday)
            .putString("routine_${DayOfWeek.WEDNESDAY.name}", wednesday)
            .putString("routine_${DayOfWeek.THURSDAY.name}", thursday)
            .putString("routine_${DayOfWeek.FRIDAY.name}", friday)
            .putString("routine_${DayOfWeek.SATURDAY.name}", saturday)
            .putString("routine_${DayOfWeek.SUNDAY.name}", sunday)
            .apply()
    }
}