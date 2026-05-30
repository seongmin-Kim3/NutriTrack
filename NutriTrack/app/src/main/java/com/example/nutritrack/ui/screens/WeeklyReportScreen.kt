package com.example.nutritrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.data.settings.GoalPrefs
import com.example.nutritrack.ui.viewmodel.MealViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class DaySummary(
    val date: LocalDate,
    val kcal: Int,
    val carbs: Int,
    val protein: Int,
    val fat: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReportScreen(
    mealVm: MealViewModel,
    goalPrefs: GoalPrefs,
    onBack: () -> Unit
) {
    val meals by mealVm.getMealsLast7Days().collectAsState(initial = emptyList())
    val zone = remember { ZoneId.systemDefault() }
    val dateFmt = remember { DateTimeFormatter.ofPattern("M/d (E)") }

    val last7Dates = remember {
        val today = LocalDate.now()
        (0..6).map { today.minusDays((6 - it).toLong()) }
    }

    val daySummaries = remember(meals) {
        val byDate = meals.groupBy { m ->
            Instant.ofEpochMilli(m.createdAtMillis).atZone(zone).toLocalDate()
        }
        last7Dates.map { d ->
            val list = byDate[d].orEmpty()
            DaySummary(d, list.sumOf { it.calories }, list.sumOf { it.carbs }, list.sumOf { it.protein }, list.sumOf { it.fat })
        }
    }

    val totalKcal = remember(daySummaries) { daySummaries.sumOf { it.kcal } }
    val avgKcal = totalKcal / 7
    val goalKcal = goalPrefs.getKcalGoal()

    // 🌟 AI 주간 분석 상태
    var aiWeeklyAnalysis by remember { mutableStateOf("아래 버튼을 눌러 AI 주간 분석을 시작하세요!") }
    var isAnalyzing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // AI 모델 설정
    val aiViewModel: com.example.nutritrack.ui.viewmodel.HealthDiagnosisViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val generativeModel = remember {
        com.google.ai.client.generativeai.GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AQ.Ab8RN6KnAjxbfom7JWWxtU_aSeIcul6AhzZnuHZjXa1TJ-IC7A".trim(),
            requestOptions = com.google.ai.client.generativeai.type.RequestOptions(apiVersion = "v1")
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("주간 리포트", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 🌟 1. AI 주간 정밀 분석 섹션 (새로 추가!)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)) // 연한 보라색 AI 테마
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF9C27B0))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "AI 주간 정밀 분석", fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isAnalyzing) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                            Text(text = "AI가 지난 일주일을 꼼꼼히 살피고 있습니다...", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text(text = aiWeeklyAnalysis, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    isAnalyzing = true
                                    val dataSummary = daySummaries.joinToString("\n") { 
                                        "${it.date}: ${it.kcal}kcal (탄${it.carbs} 단${it.protein} 지${it.fat})"
                                    }
                                    val prompt = """
                                        사용자의 일주일 식단 요약:
                                        $dataSummary
                                        
                                        사용자의 하루 목표 칼로리: $goalKcal kcal
                                        
                                        위 데이터를 바탕으로 이번 주의 영양 성적표를 작성해줘. 
                                        1. 잘한 점 2. 아쉬운 점 3. 다음 주를 위한 핵심 팁 하나를 한국어로 친절하게 알려줘.
                                    """.trimIndent()
                                    
                                    scope.launch {
                                        try {
                                            val response = generativeModel.generateContent(prompt)
                                            aiWeeklyAnalysis = response.text ?: "분석 내용을 가져오지 못했습니다."
                                        } catch (e: Exception) {
                                            aiWeeklyAnalysis = "분석 중 오류 발생: ${e.localizedMessage}"
                                        } finally { isAnalyzing = false }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                            ) {
                                Text("✨ 주간 리포트 생성하기")
                            }
                        }
                    }
                }
            }

            // 🌟 2. 주간 요약 대시보드 카드
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "지난 7일 평균", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = avgKcal.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                            Text(text = " kcal / 일", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
                        }
                        LinearProgressIndicator(
                            progress = { (avgKcal.toFloat() / goalKcal).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.White,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text(text = "목표 ${goalKcal}kcal 대비 ${if(avgKcal > goalKcal) "초과" else "적정"} 수준입니다.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Text(text = "날짜별 상세 기록", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // 🌟 2. 일별 리스트 카드
            items(daySummaries.reversed()) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = summary.date.format(dateFmt), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(text = "탄 ${summary.carbs}g · 단 ${summary.protein}g · 지 ${summary.fat}g", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(text = "${summary.kcal} kcal", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
