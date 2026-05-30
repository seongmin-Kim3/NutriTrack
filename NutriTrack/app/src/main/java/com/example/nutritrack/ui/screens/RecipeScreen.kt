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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import kotlinx.coroutines.launch

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
fun RecipeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val goalPrefs = remember { (context.applicationContext as NuonApp).container.goalPrefs }
    var height by remember { mutableStateOf("") }
    var dietType by remember { mutableStateOf("") }
    var exerciseCount by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(0) }
    var dietPlan by remember { mutableStateOf("") }
    var exercisePlan by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AQ.Ab8RN6KnAjxbfom7JWWxtU_aSeIcul6AhzZnuHZjXa1TJ-IC7A".trim(),
            requestOptions = RequestOptions(apiVersion = "v1")
        )
    }

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
        Crossfade(targetState = currentStep, label = "", modifier = Modifier.padding(padding)) { step ->
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
                                isLoading = true
                                val prompt = "전문 트레이너로서 키 ${height}cm 사용자의 $dietType 식단과 $exerciseCount 운동 루틴을 분석해줘. ===식단=== (내용) ===운동=== (내용)"
                                scope.launch {
                                    try {
                                        val result = generativeModel.generateContent(prompt).text ?: ""
                                        dietPlan = result.substringAfter("===식단===").substringBefore("===운동===").trim()
                                        exercisePlan = result.substringAfter("===운동===").trim()
                                        currentStep = 1
                                    } catch (e: Exception) { dietPlan = "에러 발생: ${e.message}" }
                                    finally { isLoading = false }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("AI 분석 시작하기 ✨", fontWeight = FontWeight.Bold)
                        }
                    }
                    1 -> {
                        Text("🥗 추천 식단", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        SmartYoutubeItem(dietPlan)
                        Button(onClick = { currentStep = 2 }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("운동 계획 확인하기 ➡️") }
                    }
                    2 -> {
                        Text("💪 추천 운동", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        SmartYoutubeItem(exercisePlan)
                        OutlinedButton(onClick = { currentStep = 0 }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("다시 입력하기") }
                    }
                }
            }
        }
    }
}
