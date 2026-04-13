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

    val authRepository = AuthRepository()
    val authViewModel = AuthViewModel(authRepository)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // --- [인증 및 초기 설정 구간] ---

        composable("login") {
            LoginScreen(
                authVm = authViewModel,
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
                authVm = authViewModel,
                onBack = { navController.popBackStack() },
                onSignupSuccess = { navController.popBackStack() }
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

        // --- [메인 기능 구간] ---

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
                onFastingTimer = { navController.navigate("fasting") }
            )
        }

        composable("recipe") {
            RecipeScreen(onBack = { navController.popBackStack() })
        }

        composable("fasting") {
            FastingScreen(
                fastingPrefs = fastingPrefs,
                onBack = { navController.popBackStack() }
            )
        }

        // 🌟 수정된 부분 1: 식단 추가 화면 (데이터 받을 준비 완료)
        composable(
            route = "add/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { entry ->
            val type = entry.arguments?.getString("type") ?: "점심"

            // 💡 바코드 화면에서 돌아올 때 주머니(savedStateHandle)에 담아온 데이터를 꺼냅니다!
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

        // 🌟 수정된 부분 2: 바코드 스캔 화면 (진짜 데이터 보내기!)
        composable("barcode") {
            BarcodeScanScreen(
                // 💡 드디어 가짜가 아닌 진짜 통신병이 물어온 6개의 데이터를 받습니다 (fat 포함)
                onFound = { code, name, kcal, carbs, protein, fat ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("sName", name)
                        set("sKcal", kcal.toString())
                        set("sCarbs", carbs.toString())
                        set("sProtein", protein.toString())
                        set("sFat", fat.toString()) // 🌟 진짜 지방 데이터도 주머니에 쏙!
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
    }
}