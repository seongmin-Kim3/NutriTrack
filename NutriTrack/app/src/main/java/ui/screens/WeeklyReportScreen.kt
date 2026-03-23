package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val dateFmt = remember { DateTimeFormatter.ofPattern("MM/dd (E)") }

    // ✅ 최근 7일(오늘 포함) LocalDate 리스트
    val last7Dates = remember {
        val today = LocalDate.now()
        (0..6).map { today.minusDays((6 - it).toLong()) } // 오래된 날짜 -> 오늘 순
    }

    // ✅ 날짜별 합계
    val daySummaries = remember(meals) {
        val byDate = meals.groupBy { m ->
            Instant.ofEpochMilli(m.createdAtMillis).atZone(zone).toLocalDate()
        }

        last7Dates.map { d ->
            val list = byDate[d].orEmpty()
            DaySummary(
                date = d,
                kcal = list.sumOf { it.calories },
                carbs = list.sumOf { it.carbs },
                protein = list.sumOf { it.protein },
                fat = list.sumOf { it.fat }
            )
        }
    }

    val totalKcal = remember(daySummaries) { daySummaries.sumOf { it.kcal } }
    val totalCarbs = remember(daySummaries) { daySummaries.sumOf { it.carbs } }
    val totalProtein = remember(daySummaries) { daySummaries.sumOf { it.protein } }
    val totalFat = remember(daySummaries) { daySummaries.sumOf { it.fat } }

    val avgKcal = remember(totalKcal) { totalKcal / 7 }
    val avgCarbs = remember(totalCarbs) { totalCarbs / 7 }
    val avgProtein = remember(totalProtein) { totalProtein / 7 }
    val avgFat = remember(totalFat) { totalFat / 7 }

    val goalKcal = goalPrefs.getKcalGoal()
    val goalCarbs = goalPrefs.getCarbsGoal()
    val goalProtein = goalPrefs.getProteinGoal()
    val goalFat = goalPrefs.getFatGoal()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("주간 리포트") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("최근 7일 요약", style = MaterialTheme.typography.titleMedium)
                    Text("기록 개수: ${meals.size}개")
                    Divider()

                    Text("7일 총합")
                    Text("칼로리: ${totalKcal} kcal")
                    Text("탄수: ${totalCarbs} g")
                    Text("단백: ${totalProtein} g")
                    Text("지방: ${totalFat} g")

                    Divider()

                    Text("일 평균(7일 기준)")
                    Text("칼로리: ${avgKcal} / 목표 ${goalKcal} kcal")
                    Text("탄수: ${avgCarbs} / 목표 ${goalCarbs} g")
                    Text("단백: ${avgProtein} / 목표 ${goalProtein} g")
                    Text("지방: ${avgFat} / 목표 ${goalFat} g")
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("일별 합계", style = MaterialTheme.typography.titleMedium)

                    daySummaries.forEach { d ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(d.date.format(dateFmt), style = MaterialTheme.typography.titleSmall)
                            Text("칼 ${d.kcal} kcal · 탄 ${d.carbs}g · 단 ${d.protein}g · 지 ${d.fat}g")
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
