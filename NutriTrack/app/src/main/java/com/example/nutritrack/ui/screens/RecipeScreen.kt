package com.example.nutritrack.ui.screens

import android.util.Patterns
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.NuonApp
import com.example.nutritrack.ui.viewmodel.HealthDiagnosisViewModel
import com.example.nutritrack.ui.viewmodel.ShoppingViewModel

@Composable
fun RecipeCard(
    title: String,
    menu: String,
    kcal: String,
    ingredients: String,
    description: String,
    onAddIngredients: (String) -> Unit,
    onFavoriteClick: () -> Unit // 🌟 즐겨찾기 클릭 이벤트 추가
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = when(title) {
                                "아침" -> "🍳"
                                "점심" -> "🥗"
                                "저녁" -> "🍗"
                                else -> "🍱"
                            },
                            modifier = Modifier.padding(8.dp),
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    // 🌟 즐겨찾기(별) 버튼 추가
                    IconButton(onClick = onFavoriteClick) {
                        Icon(Icons.Default.Star, contentDescription = "즐겨찾기", tint = Color(0xFFFFD700))
                    }
                }
                Text(text = "$kcal kcal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = menu, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, lineHeight = 22.sp)
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(text = "🛒 필요한 재료 (눌러서 장바구니 추가)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            
            @OptIn(ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ingredients.split(",").forEach { ingredient ->
                    val name = ingredient.trim()
                    if (name.isNotBlank()) {
                        FilterChip(
                            selected = false,
                            onClick = { onAddIngredients(name) },
                            label = { Text(name) },
                            leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(containerColor = Color(0xFFF8F9FA))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(content: String) {
    val uriHandler = LocalUriHandler.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = content.trim(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { 
                    val query = content.trim().replace(" ", "+")
                    uriHandler.openUri("https://www.youtube.com/results?search_query=$query+운동법") 
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E5F5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF7B1FA2))
                Spacer(modifier = Modifier.width(8.dp))
                Text("운동 알아보러 가기 (YouTube)", color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    aiVm: HealthDiagnosisViewModel,
    shoppingVm: ShoppingViewModel,
    recipeVm: com.example.nutritrack.ui.viewmodel.RecipeViewModel, // 🌟 즐겨찾기용 뷰모델 추가
    onBack: () -> Unit,
    onGoToShoppingList: () -> Unit,
    onGoToSettings: () -> Unit,
    onGoToFavorites: () -> Unit, // 🌟 즐겨찾기 화면 이동 콜백 추가
    initialStep: Int = 0
) {
    val context = LocalContext.current
    val goalPrefs = remember { (context.applicationContext as NuonApp).container.goalPrefs }
    val recipePlan by aiVm.recipePlan.collectAsState()
    val exercisePlan by aiVm.exercisePlan.collectAsState()
    val isLoading by aiVm.isRecipeLoading.collectAsState()

    val userHeight = goalPrefs.getUserHeight().toString()
    val userWeight = goalPrefs.getUserWeight()
    val targetWeight = goalPrefs.getTargetWeight()
    val dietGoal = goalPrefs.getDietGoal()

    var currentStep by remember { mutableIntStateOf(initialStep) }
    var hasPlanGenerated by remember { mutableStateOf(recipePlan.isNotBlank()) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI 맞춤 플랜", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { if (currentStep > 0) currentStep -= 1 else onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Crossfade(targetState = currentStep, label = "") { step ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                when (step) {
                    0 -> {
                        Text(text = "현재 프로필 정보를 기반으로\n최적의 플랜을 설계합니다", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        
                        Card(
                            shape = RoundedCornerShape(24.dp), 
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "나의 신체 정보", fontWeight = FontWeight.Bold, color = Color.Gray)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("키 / 현재 체중", fontWeight = FontWeight.Medium)
                                    Text("${userHeight}cm / ${userWeight}kg", fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("목표 체중", fontWeight = FontWeight.Medium)
                                    Text("${targetWeight}kg", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("식단 목표", fontWeight = FontWeight.Medium)
                                    Text(dietGoal, fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(color = Color(0xFFF5F5F5))
                                TextButton(onClick = onGoToSettings, modifier = Modifier.align(Alignment.End)) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("내 정보 수정하기")
                                }
                            }
                        }

                        Button(
                            onClick = { 
                                if (!hasPlanGenerated) {
                                    aiVm.getRecipeAndExercisePlan(userHeight, userWeight, targetWeight, dietGoal, goalPrefs.getActivityLevel())
                                    hasPlanGenerated = true
                                } else {
                                    currentStep = 1
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasPlanGenerated) Color(0xFF673AB7) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("데이터를 분석 중입니다...")
                            } else {
                                Text(
                                    text = if (hasPlanGenerated) "나만의 플랜 확인하기 ✨" else "나만의 플랜 생성하기 ✨", 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        // 🌟 [추가] 맞춤식단 즐겨찾기 버튼
                        OutlinedButton(
                            onClick = onGoToFavorites,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF673AB7).copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("맞춤식단 즐겨찾기", color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                        }
                    }
                    1 -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("🍽️ 추천 식단", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            TextButton(onClick = onGoToShoppingList) { 
                                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("장바구니 확인") 
                            }
                        }
                        
                        if (recipePlan.isNotBlank()) {
                            val sections = recipePlan.split("[")
                            sections.forEach { section ->
                                if (section.contains("]")) {
                                    val title = section.substringBefore("]").trim()
                                    val menu = section.substringAfter("메뉴:").substringBefore("칼로리:").trim()
                                    val kcal = section.substringAfter("칼로리:").substringBefore("재료:").trim().filter { it.isDigit() }
                                    val ingredients = section.substringAfter("재료:").substringBefore("설명:").trim()
                                    val description = section.substringAfter("설명:").substringBefore("\n\n").trim()
                                    
                                    RecipeCard(
                                        title = title,
                                        menu = menu,
                                        kcal = kcal,
                                        ingredients = ingredients,
                                        description = description,
                                        onAddIngredients = { shoppingVm.addItem(it) },
                                        onFavoriteClick = {
                                            recipeVm.saveFavorite(title, menu, kcal.toIntOrNull() ?: 0, ingredients, description)
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { currentStep = 2 }, 
                            modifier = Modifier.fillMaxWidth().height(56.dp), 
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("운동 루틴 확인하기 ➡️", fontWeight = FontWeight.Bold) }
                    }
                    2 -> {
                        Text("🏃 추천 운동", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        if (exercisePlan.isNotBlank()) {
                            exercisePlan.split("\n- ").forEach { line ->
                                if (line.isNotBlank() && line.length > 5) {
                                    val cleanLine = line.replace("-", "").trim()
                                    ExerciseItem(cleanLine)
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = { currentStep = 0; hasPlanGenerated = false }, 
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) { Text("플랜 새로 만들기", color = Color.Gray) }
                    }
                }
            }
        }
    }
}
