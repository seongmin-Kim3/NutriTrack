package com.example.nutritrack.data.settings

import android.content.Context

class FastingPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("fasting_prefs", Context.MODE_PRIVATE)

    // 단식 중인지 여부 저장
    fun isFasting(): Boolean = prefs.getBoolean("is_fasting", false)
    fun setFasting(isFasting: Boolean) {
        prefs.edit().putBoolean("is_fasting", isFasting).apply()
    }

    // 단식 시작 시간(밀리초) 저장
    fun getStartTime(): Long = prefs.getLong("start_time", 0L)
    fun setStartTime(timeMillis: Long) {
        prefs.edit().putLong("start_time", timeMillis).apply()
    }
}