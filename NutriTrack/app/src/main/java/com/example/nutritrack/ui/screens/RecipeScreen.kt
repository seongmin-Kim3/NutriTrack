package com.example.nutritrack.ui.screens

import android.util.Patterns
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SmartYoutubeItem(text: String) {
    val uriHandler = LocalUriHandler.current
    val urlPattern = Patterns.WEB_URL.toRegex()
    val lines = text.split("\n")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        lines.forEach { line ->
            val match = urlPattern.find(line)
            if (match != null) {
                val url = match.value
                val cleanText = line.replace(url, "").replace("()", "").replace("( )", "").trim()

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(url) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Red, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            if (cleanText.isNotBlank()) Text(text = cleanText, fontWeight = FontWeight.Bold)
                            Text(text = "유튜브에서 방법 보기", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else if (line.isNotBlank()) {
                Text(text = line, fontSize = 15.sp, lineHeight = 24.sp, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    aiVm: HealthDiagnosisViewModel, // 🌟 통합된 AI 뷰모델 사용
    shoppingVm: ShoppingViewModel,
    onBack: () -> Unit,
    onGoToShoppingList: () -> Unit
) {
    val context = LocalContext.current
    val goalPrefs = remember { (context.applicationContext as NuonApp).container.goalPrefs }
    
    val recipePlan by aiVm.recipePlan.collectAsState()
    val exercisePlan by aiVm.exercisePlan.collectAsState()
    val isLoading by aiVm.isRecipeLoading.collectAsState()

    var height by remember { mutableStateOf("") }
    var dietType by remember { mutableStateOf("") }
    var exerciseCount by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI 맞춤 추천", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { if (currentStep > 0) currentStep -= 1 else onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Crossfade(targetState = currentStep, label = "stepTransition", modifier = Modifier.padding(padding)) { step ->
            Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                when (step) {
                    0 -> {
                        Text("더 정확한 추천을 위해\n정보를 입력해주세요 ✍️", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("키 (cm)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                                OutlinedTextField(value = dietType, onValueChange = { dietType = it }, label = { Text("선호 식단 (예: 저탄고지)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                                OutlinedTextField(value = exerciseCount, onValueChange = { exerciseCount = it }, label = { Text("운동 빈도 (예: 주 3회)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                        }
                        Button(
                            onClick = {
                                if (height.isBlank() || dietType.isBlank() || exerciseCount.isBlank()) return@Button
                                aiVm.getRecipeAndExercisePlan(
                                    height, 
                                    goalPrefs.getUserWeight(), 
                                    goalPrefs.getTargetWeight(), 
                                    dietType, 
                                    exerciseCount
                                )
                                currentStep = 1
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("AI가 데이터를 분석 중입니다...", fontWeight = FontWeight.Bold)
                            } else {
                                Text("AI 분석 시작하기 ✨", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    1 -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("🥗 추천 식단", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onGoToShoppingList) { Text("🛒 내 장바구니 보기") }
                        }

                        if (recipePlan.isNotBlank()) {
                            SmartYoutubeItem(recipePlan)
                        } else if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }

                        var newItemName by remember { mutableStateOf("") }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newItemName,
                                    onValueChange = { newItemName = it },
                                    label = { Text("필요한 재료 담기", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { if(newItemName.isNotBlank()) { shoppingVm.addItem(newItemName); newItemName = "" } },
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("담기") }
                            }
                        }

                        Button(onClick = { currentStep = 2 }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("운동 계획 확인하기 ➡️") }
                    }
                    2 -> {
                        Text("💪 추천 운동", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (exercisePlan.isNotBlank()) {
                            SmartYoutubeItem(exercisePlan)
                        }
                        OutlinedButton(onClick = { currentStep = 0 }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("다시 입력하기") }
                    }
                }
            }
        }
    }
}
