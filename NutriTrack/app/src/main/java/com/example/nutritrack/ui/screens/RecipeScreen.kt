package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.NuonApp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    // 🌟 기존에 프로필에서 설정한 현재 체중과 목표 체중을 자동으로 불러옵니다!
    val goalPrefs = remember { (context.applicationContext as NuonApp).container.goalPrefs }
    val currentWeight = goalPrefs.getUserWeight()
    val targetWeight = goalPrefs.getTargetWeight()

    // 사용자 추가 입력 데이터
    var height by remember { mutableStateOf("") }
    var dietType by remember { mutableStateOf("") }
    var exerciseCount by remember { mutableStateOf("") }

    // AI 결과 상태 관리
    var aiResponse by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 🌟 제미나이 AI 모델 세팅 (gemini-1.5-flash 모델이 속도가 가장 빠릅니다!)
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            // 🚨 주의: 테스트용으로 여기에 직접 넣지만, 나중에는 보안을 위해 숨기는 것이 좋습니다.
            apiKey = "AIzaSyBY2ZOk2rPqbQUm9EkhT8mHY4pf_N4xWbg"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✨ AI 맞춤 트레이너") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "뒤로") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 사용자 정보 입력 폼
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("나의 신체 정보 및 목표", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = "${currentWeight}kg",
                            onValueChange = {},
                            label = { Text("현재 체중 (자동)") },
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "${targetWeight}kg",
                            onValueChange = {},
                            label = { Text("목표 체중 (자동)") },
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("키 (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dietType,
                        onValueChange = { dietType = it },
                        label = { Text("원하는 식단 (예: 고단백, 키토제닉, 일반식)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = exerciseCount,
                        onValueChange = { exerciseCount = it },
                        label = { Text("일주일 운동 목표 (예: 주 3회, 매일)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // 2. AI 추천 받기 버튼
            Button(
                onClick = {
                    if (height.isBlank() || dietType.isBlank() || exerciseCount.isBlank()) {
                        return@Button
                    }

                    isLoading = true
                    aiResponse = ""

                    // 🌟 AI에게 보낼 프롬프트(명령어) 조립!
                    val prompt = """
                        너는 10년 차 전문 헬스 트레이너이자 영양사야. 다음 내 정보를 바탕으로 맞춤형 플랜을 짜줘.
                        - 키: ${height}cm
                        - 현재 체중: ${currentWeight}kg
                        - 목표 체중: ${targetWeight}kg
                        - 선호하는 식단 종류: $dietType
                        - 목표 운동 빈도: $exerciseCount
                        
                        위 정보를 바탕으로 다음 3가지를 추천해줘:
                        1. 나에게 맞는 1일 추천 식단 (아침, 점심, 저녁 구체적인 메뉴)
                        2. 일주일 운동 루틴 및 추천 운동 방법
                        3. 다이어트와 건강을 위한 따뜻한 조언 한마디
                        
                        보기 좋게 마크다운과 이모지를 사용해서 친절한 말투로 작성해 줘.
                    """.trimIndent()

                    // 제미나이에게 질문 전송
                    scope.launch {
                        try {
                            val response = generativeModel.generateContent(prompt)
                            aiResponse = response.text ?: "응답을 받지 못했습니다."
                        } catch (e: Exception) {
                            aiResponse = "AI와 연결 중 오류가 발생했습니다: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && height.isNotBlank() && dietType.isNotBlank() && exerciseCount.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI가 식단을 짜는 중...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("✨ 맞춤 플랜 추천받기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 3. AI 답변 결과 화면
            if (aiResponse.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🤖 AI 트레이너의 맞춤 솔루션", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                        Text(
                            text = aiResponse,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}