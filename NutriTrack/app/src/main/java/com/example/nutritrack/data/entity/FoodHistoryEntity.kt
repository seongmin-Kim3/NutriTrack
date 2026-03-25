package com.example.nutritrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_history")
data class FoodHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    val name: String,
    val calories: Int,
    val carbs: Int,
    val protein: Int,
    val fat: Int,

    // ✅ 이 컬럼명이 DAO 쿼리와 반드시 동일해야 합니다.
    val createdAtMillis: Long = System.currentTimeMillis()
)
