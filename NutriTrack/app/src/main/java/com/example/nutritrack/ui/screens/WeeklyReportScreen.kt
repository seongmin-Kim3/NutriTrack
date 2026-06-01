package com.example.nutritrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
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
import com.example.nutritrack.ui.viewmodel.HealthDiagnosisViewModel
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

@Composable
fun WeeklyAiCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: String, color: Color) {
    val cleanContent = content.replace("[총평]", "").replace("[칭찬]", "").replace("[주의]", "").replace("[액션플랜]", "").trim()
    if (cleanContent.isBlank() || cleanContent.length < 5) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = cleanContent, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = Color.DarkGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReportScreen(
    mealVm: MealViewModel,
    aiVm: HealthDiagnosisViewModel,
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
        val byDate = meals.groupBy { m -> Instant.ofEpochMilli(m.createdAtMillis).atZone(zone).toLocalDate() }
        last7Dates.map { d ->
            val list = byDate[d].orEmpty()
            DaySummary(d, list.sumOf { it.calories }, list.sumOf { it.carbs }, list.sumOf { it.protein }, list.sumOf { it.fat })
        }
    }

    val totalKcal = remember(daySummaries) { daySummaries.sumOf { it.kcal } }
    val avgKcal = totalKcal / 7
    val goalKcal = goalPrefs.getKcalGoal()

    val aiWeeklyAnalysis by aiVm.weeklyAnalysis.collectAsState()
    val isAnalyzing by aiVm.isWeeklyLoading.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("주간 리포트", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 🌟 1. AI 정밀 분석 헤더
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "AI가 분석한 나의 주간 영양", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Button(
                                onClick = {
                                    val dataSummary = daySummaries.joinToString("\n") { "${it.date}: ${it.kcal}kcal" }
                                    aiVm.getWeeklyAnalysis(dataSummary, goalKcal)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("✨ 분석 리포트 생성", color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (aiWeeklyAnalysis.length > 10 && !isAnalyzing) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        WeeklyAiCard("이번 주 총평", Icons.Default.Analytics, aiWeeklyAnalysis.substringAfter("[총평]").substringBefore("[칭찬]"), Color(0xFF1976D2))
                        WeeklyAiCard("칭찬합니다!", Icons.Default.CheckCircle, aiWeeklyAnalysis.substringAfter("[칭찬]").substringBefore("[주의]"), Color(0xFF388E3C))
                        WeeklyAiCard("주의하세요", Icons.Default.Error, aiWeeklyAnalysis.substringAfter("[주의]").substringBefore("[액션플랜]"), Color(0xFFE64A19))
                        WeeklyAiCard("다음 주 미션", Icons.Default.Lightbulb, aiWeeklyAnalysis.substringAfter("[액션플랜]"), Color(0xFFFBC02D))
                    }
                }
            }

            // 🌟 2. 평균 수치 카드
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "일 평균 섭취량", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Text(text = "$avgKcal kcal", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        LinearProgressIndicator(
                            progress = { (avgKcal.toFloat() / goalKcal.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 8.dp),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }

            item { Text(text = "날짜별 기록", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

            items(daySummaries.reversed()) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = summary.date.format(dateFmt), fontWeight = FontWeight.Bold)
                            Text(text = "탄 ${summary.carbs} 단 ${summary.protein} 지 ${summary.fat}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(text = "${summary.kcal} kcal", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
