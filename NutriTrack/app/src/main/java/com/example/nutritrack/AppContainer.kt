package com.example.nutritrack

import android.content.Context
import androidx.room.Room
import com.example.nutritrack.data.db.AppDatabase
import com.example.nutritrack.data.repo.FoodRepository
import com.example.nutritrack.data.repo.MealRepository
import com.example.nutritrack.data.settings.GoalPrefs
import com.example.nutritrack.ui.viewmodel.FoodViewModelFactory
import com.example.nutritrack.ui.viewmodel.MealViewModelFactory

class AppContainer(context: Context) {

    private val db: AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "nutritrack.db"
        ).fallbackToDestructiveMigration()
            .build()

    private val mealRepo = MealRepository(db.mealDao())
    private val foodRepo = FoodRepository(db.foodDao())

    val goalPrefs: GoalPrefs = GoalPrefs(context.applicationContext)






    val mealViewModelFactory: MealViewModelFactory = MealViewModelFactory(mealRepo)
    val foodViewModelFactory: FoodViewModelFactory = FoodViewModelFactory(foodRepo)
}
