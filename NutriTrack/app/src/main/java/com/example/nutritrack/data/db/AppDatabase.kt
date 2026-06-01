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
        FoodHistoryEntity::class,
        com.example.nutritrack.data.entity.ShoppingEntity::class,
        com.example.nutritrack.data.entity.FavoriteRecipeEntity::class
    ],
    version = 3, // 버전 업그레이드
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun foodDao(): FoodDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun favoriteRecipeDao(): FavoriteRecipeDao
}
