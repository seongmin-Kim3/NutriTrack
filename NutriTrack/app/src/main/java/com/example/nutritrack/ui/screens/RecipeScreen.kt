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
import com.example.nutritrack.ui.viewmodel.RecipeViewModel
import com.example.nutritrack.ui.viewmodel.ShoppingViewModel

@Composable
fun SmartItem(
    text: String, 
    onAddShopping: (String) -> Unit,
    onSaveFavorite: (String, String, String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val urlPattern = Patterns.WEB_URL.toRegex()
    
    val lines = text.split("\n").filter { it.isNotBlank() }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        lines.forEach { line ->
            val match = urlPattern.find(line)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // 1. 헤더 파싱
                    val title = if (line.contains("[") && line.contains("]")) {
                        line.substringAfter("[").substringBefore("]")
                    } else if (line.startsWith("- ")) {
                        line.substringAfter("- ").substringBefore(":").trim()
                    } else "추천 정보"

                    // 본문 텍스트 정리
                    var bodyText = line.replace("[$title]", "").replace("- $title:", "").trim()
                    if (bodyText.startsWith("- ")) bodyText = bodyText.substring(2)
                    
                    val cleanBodyText = if (match != null) bodyText.replace(match.value, "").replace("()", "").trim() else bodyText

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when {
                                title.contains("아침") -> "🍳"
                                title.contains("점심") -> "🥗"
                                title.contains("저녁") -> "🍗"
                                else -> "💪"
                            }
                            Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = CircleShape) {
                                Text(text = icon, modifier = Modifier.padding(8.dp), fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            
                            // 🌟 즐겨찾기 별 버튼
                            if (title.contains("아침") || title.contains("점심") || title.contains("저녁")) {
                                IconButton(onClick = { onSaveFavorite(title, cleanBodyText, cleanBodyText) }) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                                }
                                
                                // 🌟 우측에 칼로리 표시 (파싱 시도)
                                val kcalMatch = Regex("(\\d+)\\s*kcal").find(cleanBodyText)
                                kcalMatch?.let {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(text = "${it.groupValues[1]} kcal", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = cleanBodyText, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)

                    // 2. 유튜브 버튼
                    if (match != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { uriHandler.openUri(match.value) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E5F5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF7B1FA2))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("영상 가이드 보기 (YouTube)", color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold)
                        }
                    }

                    // 3. 재료 버튼화 (식단인 경우)
                    if (title.contains("아침") || title.contains("점심") || title.contains("저녁")) {
                        val ingredientsPart = cleanBodyText.substringAfter("재료:").trim()
                        if (ingredientsPart != cleanBodyText) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFF5F5F5))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("🛒 필요한 재료 (눌러서 장바구니 추가)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            @OptIn(ExperimentalLayoutApi::class)
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ingredientsPart.split(",").forEach { ingredient ->
                                    val name = ingredient.trim().substringBefore("(").trim()
                                    if (name.isNotBlank()) {
                                        AssistChip(
                                            onClick = { onAddShopping(name) },
                                            label = { Text(name) },
                                            leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    aiVm: HealthDiagnosisViewModel,
    shoppingVm: ShoppingViewModel,
    recipeVm: RecipeViewModel, // 🌟 즐겨찾기 뷰모델 추가
    onBack: () -> Unit,
    onGoToShoppingList: () -> Unit,
    onGoToSettings: () -> Unit,
    onGoToFavorites: () -> Unit, // 🌟 즐겨찾기 화면 이동
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
    var hasGeneratedOnce by remember { mutableStateOf(recipePlan.isNotBlank()) }

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
        Crossfade(targetState = currentStep, label = "step") { step ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                when (step) {
                    0 -> {
                        Text(text = "현재 프로필 정보를 기반으로\n최적의 플랜을 설계합니다", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        
                        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "확인된 나의 정보", fontWeight = FontWeight.Bold, color = Color.Gray)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("키 / 체중", fontWeight = FontWeight.Medium)
                                    Text("${userHeight}cm / ${userWeight}kg")
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("목표", fontWeight = FontWeight.Medium)
                                    Text("$dietGoal (${targetWeight}kg)")
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF5F5F5))
                                TextButton(onClick = onGoToSettings, modifier = Modifier.align(Alignment.End)) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("내 정보 수정하기")
                                }
                            }
                        }

                        Button(
                            onClick = { 
                                if (!hasGeneratedOnce) {
                                    aiVm.getRecipeAndExercisePlan(userHeight, userWeight, targetWeight, dietGoal, goalPrefs.getActivityLevel())
                                    hasGeneratedOnce = true
                                } else {
                                    currentStep = 1
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasGeneratedOnce) Color(0xFF673AB7) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("AI가 플랜을 짜고 있습니다...")
                            } else {
                                Text(
                                    text = if (hasGeneratedOnce) "나만의 플랜 확인하기 ✨" else "나만의 플랜 생성하기 ✨", 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        
                        // 🌟 맞춤식단 즐겨찾기 버튼 복구
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
                            Text("🍽️ 추천 식단", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onGoToShoppingList) { Text("🛒 장바구니 확인") }
                        }
                        
                        SmartItem(
                            text = recipePlan, 
                            onAddShopping = { shoppingVm.addItem(it) },
                            onSaveFavorite = { type, name, ingr ->
                                recipeVm.saveFavorite(type, name, 0, ingr, name)
                            }
                        )

                        Button(onClick = { currentStep = 2 }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("다음: 운동 루틴 확인 ➡️", fontWeight = FontWeight.Bold) }
                    }
                    2 -> {
                        Text("🏃 추천 운동", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        SmartItem(
                            text = exercisePlan, 
                            onAddShopping = {},
                            onSaveFavorite = { _, _, _ -> }
                        )
                        OutlinedButton(onClick = { currentStep = 0; hasGeneratedOnce = false }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("플랜 새로 만들기") }
                    }
                }
            }
        }
    }
}
