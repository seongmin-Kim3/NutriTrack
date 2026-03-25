package com.example.nutritrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_templates")
data class FoodTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    val name: String,

    // ✅ “기준 값(원본값)”
    // 100g 기준이면 100g 기준 값, 아니면 1회 섭취 기준 값
    val calories: Int,
    val carbs: Int,
    val protein: Int,
    val fat: Int,

    // ✅ 4-2 추가
    // true = 100g 기준 값이다
    val isPer100g: Boolean = false,

    // ✅ 4-2 추가
    // 추천 기본 섭취량(g). 0이면 미사용
    val defaultGrams: Int = 0,

    val createdAtMillis: Long = System.currentTimeMillis()
)
