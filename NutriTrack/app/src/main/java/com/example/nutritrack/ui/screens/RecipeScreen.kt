package com.example.nutritrack.ui.screens

import android.util.Patterns
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow // 기본 재생 아이콘 사용!
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
import kotlinx.coroutines.launch

// 🌟 [핵심 기능] 텍스트는 그대로 살리고, 링크만 쏙 빼서 예쁜 버튼으로!
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
                // 🌟 링크와 괄호만 싹 지우고, 상세한 설명 텍스트는 그대로 남깁니다!
                val cleanText = line.replace(url, "").replace("()", "").replace("( )", "").trim()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. 상세한 운동/식단 설명 텍스트 보여주기
                    if (cleanText.isNotBlank()) {
                        Text(
                            text = cleanText,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // 2. 그 바로 밑에 유튜브 버튼 예쁘게 달아주기
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri(url) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow, // 에러 안 나는 기본 아이콘!
                                contentDescription = "영상 보기",
                                tint = Color(0xFFFF0000), // 유튜브 레드!
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "유튜브에서 자세한 방법 보기",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else if (line.isNotBlank()) {
                // 링크가 없는 일반 설명 텍스트
                Text(
                    text = line,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val goalPrefs = remember { (context.applicationContext as NuonApp).container.goalPrefs }
    val currentWeight = goalPrefs.getUserWeight()
    val targetWeight = goalPrefs.getTargetWeight()

    var height by remember { mutableStateOf("") }
    var dietType by remember { mutableStateOf("") }
    var exerciseCount by remember { mutableStateOf("") }

    var currentStep by remember { mutableStateOf(0) }
    var dietPlan by remember { mutableStateOf("") }
    var exercisePlan by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyA3L5oooCJpfLPgjlqnoyH4rmrPr72ALMc"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentStep == 0) "✨ AI 맞춤 추천" else "📊 분석 결과") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) currentStep -= 1 else onBack()
                    }) { Icon(Icons.Default.ArrowBack, "뒤로") }
                }
            )
        }
    ) { padding ->
        Crossfade(targetState = currentStep, label = "page_transition", modifier = Modifier.padding(padding)) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (step) {
                    0 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("신체 정보 확인", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = "${currentWeight}kg", onValueChange = {}, label = { Text("현재 체중") }, readOnly = true, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = "${targetWeight}kg", onValueChange = {}, label = { Text("목표 체중") }, readOnly = true, modifier = Modifier.weight(1f))
                                }
                                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("키 (cm)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = dietType, onValueChange = { dietType = it }, label = { Text("선호 식단 (예: 일반식)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = exerciseCount, onValueChange = { exerciseCount = it }, label = { Text("운동 빈도 (예: 주 3회)") }, modifier = Modifier.fillMaxWidth())
                            }
                        }

                        Button(
                            onClick = {
                                if (height.isBlank() || dietType.isBlank() || exerciseCount.isBlank()) return@Button
                                isLoading = true

                                // 🌟 AI에게 "설명을 아주 길게 쓰고 그 줄 끝에 링크를 달아라!" 라고 명령을 강화했습니다.
                                val prompt = """
                                    너는 전문 트레이너야. 내 정보(키:${height}cm, 체중:${currentWeight}kg, 목표:${targetWeight}kg, 식단:$dietType, 빈도:$exerciseCount)를 분석해줘.
                                    
                                    반드시 아래 형식을 지켜서 두 부분으로만 나눠!
                                    ===식단===
                                    (식단 추천)
                                    ===운동===
                                    (운동 추천)
                                    🚨 중요: 운동을 추천할 때는 '스쿼트: 3세트 15회, 하체를 강화합니다. (유튜브링크)' 처럼 운동 이름, 세트수, 횟수, 효과를 아주 상세히 글로 적고 그 줄 맨 끝에만 괄호치고 풀(Full) URL 링크를 넣어줘!
                                """.trimIndent()

                                scope.launch {
                                    try {
                                        val result = generativeModel.generateContent(prompt).text ?: ""
                                        dietPlan = result.substringAfter("===식단===").substringBefore("===운동===").trim()
                                        exercisePlan = result.substringAfter("===운동===").trim()
                                        currentStep = 1
                                    } catch (e: Exception) {
                                        dietPlan = "분석 중 오류가 발생했습니다."
                                    } finally { isLoading = false }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            else Text("AI 분석 시작하기 ✨")
                        }
                    }
                    1 -> {
                        Text("🍽️ 추천 식단 가이드", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        SmartYoutubeItem(text = dietPlan)
                        Button(onClick = { currentStep = 2 }, modifier = Modifier.fillMaxWidth()) {
                            Text("다음: 운동 루틴 확인 ➡️")
                        }
                    }
                    2 -> {
                        Text("💪 일주일 운동 계획", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        SmartYoutubeItem(text = exercisePlan)
                        Button(onClick = { currentStep = 0 }, modifier = Modifier.fillMaxWidth()) {
                            Text("처음으로 돌아가기 🔄")
                        }
                    }
                }
            }
        }
    }
}