package com.example.nutritrack.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nutritrack.NuonApp
import com.example.nutritrack.data.settings.FastingPrefs
import com.example.nutritrack.ui.screens.*
import com.example.nutritrack.ui.viewmodel.FoodViewModel
import com.example.nutritrack.ui.viewmodel.MealViewModel

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val app = context.applicationContext as NuonApp
    val container = app.container

    val goalPrefs = container.goalPrefs
    val fastingPrefs = FastingPrefs(context) // 🌟 타이머 저장소 추가됨!
    val mealVm: MealViewModel = viewModel(factory = container.mealViewModelFactory)
    val foodVm: FoodViewModel = viewModel(factory = container.foodViewModelFactory)

    val startDest = "login"

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    val nextScreen = if (goalPrefs.isProfileSetup()) "home" else "setupProfile"
                    navController.navigate(nextScreen) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("setupProfile") {
            SetupProfileScreen(
                goalPrefs = goalPrefs,
                onSetupComplete = {
                    navController.navigate("home") {
                        popUpTo("setupProfile") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                vm = mealVm,
                goalPrefs = goalPrefs,
                onAddMealWithType = { type -> navController.navigate("add/$type") },
                onHistory = { navController.navigate("history") },
                onGoals = { navController.navigate("goals") },
                onWeekly = { navController.navigate("weekly") },
                onSavedFoods = { navController.navigate("savedFoods") },
                onRecipeRecommend = { navController.navigate("recipe") },
                onFastingTimer = { navController.navigate("fasting") } // 🌟 타이머 화면으로 가는 길 뚫림!
            )
        }

        composable("recipe") {
            RecipeScreen(onBack = { navController.popBackStack() })
        }

        // 🌟 새로 추가된 타이머 화면!
        composable("fasting") {
            FastingScreen(
                fastingPrefs = fastingPrefs,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "add/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { entry ->
            val type = entry.arguments?.getString("type") ?: "점심"
            AddMealScreen(
                mealVm = mealVm, mealType = type, foodVm = foodVm,
                onBack = { navController.popBackStack() },
                onOpenBarcode = { navController.navigate("barcode") }
            )
        }

        composable("barcode") {
            BarcodeScanScreen(
                onFound = { _, _, _, _, _ -> navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(mealVm = mealVm, onBack = { navController.popBackStack() })
        }

        composable("goals") {
            GoalSettingScreen(goalPrefs = goalPrefs, onBack = { navController.popBackStack() })
        }

        composable("savedFoods") {
            SavedFoodsScreen(
                foodVm = foodVm,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate("editFoodTemplate/$id") }
            )
        }

        composable(
            route = "editFoodTemplate/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            EditFoodTemplateScreen(id = id, onBack = { navController.popBackStack() })
        }

        composable("weekly") {
            WeeklyReportScreen(mealVm = mealVm, goalPrefs = goalPrefs, onBack = { navController.popBackStack() })
        }
    }
}