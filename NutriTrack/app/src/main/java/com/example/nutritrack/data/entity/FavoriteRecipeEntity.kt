package com.example.nutritrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String,      // 아침, 점심, 저녁 등
    val menuName: String,
    val kcal: Int,
    val ingredients: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
