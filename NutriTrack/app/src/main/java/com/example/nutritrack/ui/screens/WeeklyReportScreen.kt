package com.example.nutritrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
            // 🌟 1. 주간 요약 대시보드 카드
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
