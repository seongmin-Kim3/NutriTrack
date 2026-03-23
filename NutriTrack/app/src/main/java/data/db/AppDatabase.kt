package com.example.nutritrack.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nutritrack.data.entity.FoodHistoryEntity
import com.example.nutritrack.data.entity.FoodTemplateEntity
import com.example.nutritrack.data.entity.MealEntity

@Database(
    entities = [
        MealEntity::class,
        FoodTemplateEntity::class,
        FoodHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun foodDao(): FoodDao
}
