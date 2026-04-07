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
// 🌟 1. MainActivity에서 'home'으로 갈지 'login'으로 갈지 결정한 값을 받아옵니다.
fun AppNav(startDestination: String = "login") {
    val navController = rememberNavController()
    val context = LocalContext.current

    val app = context.applicationContext as NuonApp
    val container = app.container

    // 기존 뷰모델 및 저장소들
    val goalPrefs = container.goalPrefs
    val fastingPrefs = FastingPrefs(context)
    val mealVm: MealViewModel = viewModel(factory = container.mealViewModelFactory)
    val foodVm: FoodViewModel = viewModel(factory = container.foodViewModelFactory)

    // 🌟 2. 파이어베이스 회원가입/로그인을 담당할 사령관(ViewModel) 준비!
    val authRepository = AuthRepository()
    val authViewModel = AuthViewModel(authRepository)

    NavHost(
        navController = navController,
        startDestination = startDestination // 🌟 3. 고정된 값이 아닌, 받아온 시작점으로 앱을 켭니다.
    ) {

        // --- [인증 및 초기 설정 구간] ---

        composable("login") {
            LoginScreen(
                authVm = authViewModel, // 로그인 화면에 뷰모델 연결
                onLoginSuccess = {
                    // 로그인 성공 시 프로필 설정 여부에 따라 홈으로 갈지 설정으로 갈지 결정!
                    val nextScreen = if (goalPrefs.isProfileSetup()) "home" else "setupProfile"
                    navController.navigate(nextScreen) {
                        popUpTo("login") { inclusive = true } // 로그인 화면은 뒤로 가기 못하게 파괴
                    }
                },
                onNavigateToSignUp = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignUpScreen(
                authVm = authViewModel, // 회원가입 화면에 뷰모델 연결
                onBack = { navController.popBackStack() },
                onSignupSuccess = { navController.popBackStack() } // 가입 성공하면 다시 로그인 화면으로!
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