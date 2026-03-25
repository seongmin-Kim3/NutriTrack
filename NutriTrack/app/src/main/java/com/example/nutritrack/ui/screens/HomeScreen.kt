package com.example.nutritrack.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutritrack.data.settings.GoalPrefs
import com.example.nutritrack.ui.components.MacroProgressBar
import com.example.nutritrack.ui.viewmodel.MealViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun WeightGoalGauge(
    currentWeight: Float,
    targetWeight: Float,
    startWeight: Float,
    modifier: Modifier = Modifier
) {
    val totalWeightToLose = abs(startWeight - targetWeight)
    val currentLostWeight = abs(startWeight - currentWeight)

    val progress = if (totalWeightToLose == 0f) 0f
    else (currentLostWeight / totalWeightToLose).coerceIn(0f, 1f)

    val gaugeColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val runnerIcon = Icons.Default.Face

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🏃 목표 체중까지 달리는 중!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                    drawLine(
                        color = trackColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round
                    )
                }

                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                    drawLine(
                        color = gaugeColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width * progress, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round
                    )
                }

                Icon(
                    painter = rememberVectorPainter(image = runnerIcon),
                    contentDescription = "러닝맨",
                    tint = gaugeColor,
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (modifier.fillMaxWidth().let { 280.dp } * progress) - 16.dp, y = (-12).dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "시작: ${startWeight}kg", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = "현재: ${currentWeight}kg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = gaugeColor)
                Text(text = "목표: ${targetWeight}kg", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MealViewModel,
    goalPrefs: GoalPrefs,
    onAddMealWithType: (String) -> Unit,
    onHistory: () -> Unit,
    onGoals: () -> Unit,
    onWeekly: () -> Unit,
    onSavedFoods: () -> Unit,
    onRecipeRecommend: () -> Unit,
    onFastingTimer: () -> Unit
) {
    val todayMeals by vm.todayMeals.collectAsState()
    val scrollState = rememberScrollState()

    val today = LocalDate.now()
    val currentDate = remember {
        today.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN))
    }
    val currentDayOfWeek = today.dayOfWeek

    val weeklyRoutine = remember(currentDate) {
        mapOf(
            DayOfWeek.MONDAY to goalPrefs.getRoutineForDay(DayOfWeek.MONDAY.name),
            DayOfWeek.TUESDAY to goalPrefs.getRoutineForDay(DayOfWeek.TUESDAY.name),
            DayOfWeek.WEDNESDAY to goalPrefs.getRoutineForDay(DayOfWeek.WEDNESDAY.name),
            DayOfWeek.THURSDAY to goalPrefs.getRoutineForDay(DayOfWeek.THURSDAY.name),
            DayOfWeek.FRIDAY to goalPrefs.getRoutineForDay(DayOfWeek.FRIDAY.name),
            DayOfWeek.SATURDAY to goalPrefs.getRoutineForDay(DayOfWeek.SATURDAY.name),
            DayOfWeek.SUNDAY to goalPrefs.getRoutineForDay(DayOfWeek.SUNDAY.name)
        )
    }

    val todayRoutine = weeklyRoutine[currentDayOfWeek] ?: "휴식"

    val aiDietTip = when {
        todayRoutine.contains("하체") || todayRoutine.contains("등") || todayRoutine.contains("가슴") || todayRoutine.contains("대근육") ->
            "💡 대근육 운동($todayRoutine) 데이!\n운동 2시간 전에 복합 탄수화물(고구마, 오트밀 등)을 든든하게 섭취해 에너지를 꽉 채워주세요."
        todayRoutine.contains("팔") || todayRoutine.contains("어깨") || todayRoutine.contains("삼두") || todayRoutine.contains("이두") ->
            "💡 소근육 위주 운동($todayRoutine) 데이!\n운동 직후 흡수가 빠른 단백질 보충에 신경 써주시면 근성장에 좋습니다."
        todayRoutine.contains("유산소") ->
            "💡 체지방 태우는 유산소 데이!\n운동 전 가벼운 바나나 한 개 정도가 좋으며, 수분 섭취를 틈틈이 해주세요."
        todayRoutine.contains("휴식") ->
            "💡 오늘은 휴식일입니다.\n근육이 푹 쉬면서 자랄 수 있도록 충분한 수면과 단백질 위주의 식단을 유지하세요."
        else ->
            "💡 오늘도 화이팅입니다!\n운동 전후로 영양 섭취를 잊지 마세요."
    }

    val totalKcal = remember(todayMeals) { todayMeals.sumOf { it.calories } }
    val totalCarbs = remember(todayMeals) { todayMeals.sumOf { it.carbs } }
    val totalProtein = remember(todayMeals) { todayMeals.sumOf { it.protein } }
    val totalFat = remember(todayMeals) { todayMeals.sumOf { it.fat } }

    val goalKcal = goalPrefs.getKcalGoal()
    val goalCarbs = goalPrefs.getCarbsGoal()
    val goalProtein = goalPrefs.getProteinGoal()
    val goalFat = goalPrefs.getFatGoal()

    // 🌟 에러 해결: 에러 났던 코드를 지우고 임시 가짜 데이터를 넣었습니다!
    // 나중에 이 숫자를 바꿔보시면 러닝맨이 왔다갔다 하는 걸 볼 수 있습니다.
    val startWeight = 80f   // 시작 체중
    val currentWeight = 73f // 현재 체중
    val targetWeight = 65f  // 목표 체중

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuon") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "오늘: $currentDate",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(onClick = onHistory, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp)) {
                    Text("기록 보기", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                }
                FilledTonalButton(onClick = onSavedFoods, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp)) {
                    Text("내 음식", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                }
                FilledTonalButton(onClick = onWeekly, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp)) {
                    Text("주간 리포트", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                }
            }

            WeightGoalGauge(
                currentWeight = currentWeight,
                targetWeight = targetWeight,
                startWeight = startWeight,
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏋️ 오늘의 운동: $todayRoutine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = aiDietTip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("오늘 섭취 요약", style = MaterialTheme.typography.titleMedium)

                    MacroProgressBar(title = "칼로리", current = totalKcal, target = goalKcal, unit = "kcal", modifier = Modifier.fillMaxWidth())
                    Divider()
                    Text("탄/단/지 목표 달성률", style = MaterialTheme.typography.titleSmall)
                    MacroProgressBar(title = "탄수화물", current = totalCarbs, target = goalCarbs, unit = "g", modifier = Modifier.fillMaxWidth())
                    MacroProgressBar(title = "단백질", current = totalProtein, target = goalProtein, unit = "g", modifier = Modifier.fillMaxWidth())
                    MacroProgressBar(title = "지방", current = totalFat, target = goalFat, unit = "g", modifier = Modifier.fillMaxWidth())
                }
            }

            val mealCategories = listOf("아침", "점심", "저녁", "간식")

            mealCategories.forEach { category ->
                val mealsInCategory = todayMeals.filter { it.type == category }
                val categoryCalories = mealsInCategory.sumOf { it.calories }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${categoryCalories} kcal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                IconButton(
                                    onClick = { onAddMealWithType(category) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "$category 추가", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        if (mealsInCategory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            mealsInCategory.forEach { meal ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = meal.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "${meal.calories} kcal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        } else {
                            Text("기록된 식사가 없습니다.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onGoals, modifier = Modifier.fillMaxWidth()) { Text("목표 및 루틴 설정") }
            OutlinedButton(onClick = onRecipeRecommend, modifier = Modifier.fillMaxWidth()) { Text("👨‍🍳 맞춤 식단 & 레시피 추천받기") }
            OutlinedButton(onClick = onFastingTimer, modifier = Modifier.fillMaxWidth()) { Text("⏳ 16:8 간헐적 단식 타이머") }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}