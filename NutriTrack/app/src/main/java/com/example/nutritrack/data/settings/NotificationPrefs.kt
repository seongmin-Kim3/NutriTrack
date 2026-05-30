package com.example.nutritrack.data.settings

import android.content.Context

class NotificationPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)

    fun isWaterReminderEnabled(): Boolean = prefs.getBoolean("water_enabled", false)
    fun setWaterReminder(enabled: Boolean) {
        prefs.edit().putBoolean("water_enabled", enabled).apply()
    }

    fun isFastingReminderEnabled(): Boolean = prefs.getBoolean("fasting_enabled", false)
    fun setFastingReminder(enabled: Boolean) {
        prefs.edit().putBoolean("fasting_enabled", enabled).apply()
    }

    fun isLogReminderEnabled(): Boolean = prefs.getBoolean("log_enabled", false)
    fun setLogReminder(enabled: Boolean) {
        prefs.edit().putBoolean("log_enabled", enabled).apply()
    }
}
