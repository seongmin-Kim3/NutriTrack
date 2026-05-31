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
import com.example.nutritrack.data.repo.AuthRepository
import com.example.nutritrack.data.settings.FastingPrefs
import com.example.nutritrack.ui.screens.*
import com.example.nutritrack.ui.viewmodel.AuthViewModel
import com.example.nutritrack.ui.viewmodel.FoodViewModel
import com.example.nutritrack.ui.viewmodel.MealViewModel
import com.example.nutritrack.ui.viewmodel.HealthDiagnosisViewModel
import com.example.nutritrack.ui.viewmodel.WaterViewModel
import com.example.nutritrack.ui.viewmodel.ShoppingViewModel

@Composable
fun AppNav(startDestination: String = "login") {
    val navController = rememberNavController()
    val context = LocalContext.current

    val app = context.applicationContext as NuonApp
    val container = app.container

    val goalPrefs = container.goalPrefs
    val fastingPrefs = FastingPrefs(context)
    val mealVm: MealViewModel = viewModel(factory = container.mealViewModelFactory)
    val foodVm: FoodViewModel = viewModel(factory = container.foodViewModelFactory)
    val aiDiagnosisVm: HealthDiagnosisViewModel = viewModel() // AI 뷰모델
    val waterVm: WaterViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return WaterViewModel(goalPrefs) as T
        }
    })
    val shoppingVm: ShoppingViewModel = viewModel(factory = container.shoppingViewModelFactory)

    val authRepository = AuthRepository()
    val authViewModel = AuthViewModel(authRepository)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                authVm = authViewModel,
                onLoginSuccess = {
                    val nextScreen = if (goalPrefs.isProfileSetup()) "home" else "setupProfile"
                    navController.navigate(nextScreen) { popUpTo("login") { inclusive = true } }
                },
                onNavigateToSignUp = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignUpScreen(
                authVm = authViewModel,
                onBack = { navController.popBackStack() },
                onSignupSuccess = { navController.popBackStack() }
            )
        }

        composable("setupProfile") {
            SetupProfileScreen(
                goalPrefs = goalPrefs,
                onSetupComplete = {
                    navController.navigate("home") { popUpTo("setupProfile") { inclusive = true } }
                }
            )
        }

        composable("home") {
            HomeScreen(
                vm = mealVm,
                aiVm = aiDiagnosisVm,
                waterVm = waterVm,
                goalPrefs = goalPrefs,
                onAddMealWithType = { type -> navController.navigate("add/$type") },
                onHistory = { navController.navigate("history") },
                onGoals = { navController.navigate("goals") },
                onWeekly = { navController.navigate("weekly") },
                onSavedFoods = { navController.navigate("savedFoods") },
                onRecipeRecommend = { navController.navigate("recipe") },
                onFastingTimer = { navController.navigate("fasting") },
                onAiDiagnosis = { navController.navigate("health_diagnosis") }, // 👈 이 줄 추가
                onWaterTrack = { navController.navigate("water_tracking") },
                onNotificationSettings = { navController.navigate("notificationSettings") }
            )
        }

        composable("notificationSettings") {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable("recipe") {
            RecipeScreen(
                shoppingVm = shoppingVm,
                onBack = { navController.popBackStack() },
                onGoToShoppingList = { navController.navigate("shoppingList") }
            )
        }

        composable("shoppingList") {
            ShoppingListScreen(vm = shoppingVm, onBack = { navController.popBackStack() })
        }

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
            val savedStateHandle = entry.savedStateHandle
            val sName = savedStateHandle.get<String>("sName")
            val sKcal = savedStateHandle.get<String>("sKcal")
            val sCarbs = savedStateHandle.get<String>("sCarbs")
            val sProtein = savedStateHandle.get<String>("sProtein")
            val sFat = savedStateHandle.get<String>("sFat")

            AddMealScreen(
                mealVm = mealVm, mealType = type, foodVm = foodVm,
                scannedName = sName, scannedKcal = sKcal, scannedCarbs = sCarbs, scannedProtein = sProtein, scannedFat = sFat,
                onBack = { navController.popBackStack() },
                onOpenBarcode = { navController.navigate("barcode") }
            )
        }

        composable("barcode") {
            BarcodeScanScreen(
                onFound = { code, name, kcal, carbs, protein, fat ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("sName", name)
                        set("sKcal", kcal.toString())
                        set("sCarbs", carbs.toString())
                        set("sProtein", protein.toString())
                        set("sFat", fat.toString())
                    }
                    navController.popBackStack()
                },
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

        // 🌟 새로 추가된 AI 진단 화면 연결
        composable("aiDiagnosis") {
            HealthDiagnosisScreen(viewModel = aiDiagnosisVm, onBack = { navController.popBackStack() })
        }

        composable("water") {
            WaterTrackingScreen(vm = waterVm, onBack = { navController.popBackStack() })
        }
    }
}