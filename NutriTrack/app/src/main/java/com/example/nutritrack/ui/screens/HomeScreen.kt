package com.example.nutritrack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

// -----------------------------------------------------
// 🌟 1. 체중 진행률 게이지
// -----------------------------------------------------
@Composable
fun WeightGoalGauge(
    currentWeight: Float,
    targetWeight: Float,
    startWeight: Float,
    modifier: Modifier = Modifier
) {
    val totalWeightToLose = abs(startWeight - targetWeight)
    val currentLostWeight = abs(startWeight - currentWeight)

    val targetProgress = if (totalWeightToLose == 0f) 0f
    else (currentLostWeight / totalWeightToLose).coerceIn(0f, 1f)

    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) { animationPlayed = true }

    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "weightProgress"
    )

    val gaugeColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val runnerIcon = Icons.Default.Face

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "🏃 목표 체중까지 달리는 중!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.CenterStart) {
                Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) { drawLine(color = trackColor, start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = size.height, cap = StrokeCap.Round) }
                Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) { drawLine(color = gaugeColor, start = Offset(0f, size.height / 2), end = Offset(size.width * progress, size.height / 2), strokeWidth = size.height, cap = StrokeCap.Round) }
                Icon(painter = rememberVectorPainter(image = runnerIcon), contentDescription = "러닝맨", tint = gaugeColor, modifier = Modifier.size(36.dp).offset(x = (modifier.fillMaxWidth().let { 280.dp } * progress) - 18.dp, y = (-14).dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "시작: ${startWeight}kg", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = "현재: ${currentWeight}kg", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = gaugeColor)
                Text(text = "목표: ${targetWeight}kg", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

// -----------------------------------------------------
// 🌟 2. 꺾은선 그래프
// -----------------------------------------------------
@Composable
fun WeightTrendChart(
    weeklyData: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val maxWeight = weeklyData.maxOf { it.second } + 0.5f
    val minWeight = weeklyData.minOf { it.second } - 0.5f
    val range = maxWeight - minWeight

    val lineColor = MaterialTheme.colorScheme.primary
    val gradientBrush = Brush.verticalGradient(colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent))

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "📉 체중 변화 추이", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val xStep = width / (weeklyData.size - 1)

                    val path = Path()
                    val fillPath = Path()
                    val points = mutableListOf<Offset>()

                    weeklyData.forEachIndexed { index, data ->
                        val x = index * xStep
                        val normalizedY = (data.second - minWeight) / range
                        val y = height - (normalizedY * height)
                        val offset = Offset(x, y)
                        points.add(offset)

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.lineTo(0f, height)
                    fillPath.close()

                    drawPath(path = fillPath, brush = gradientBrush)
                    drawPath(path = path, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                    points.forEach { point ->
                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = point)
                        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = point, style = Stroke(width = 2.dp.toPx()))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weeklyData.forEach { data ->
                    Text(text = data.first, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// -----------------------------------------------------
// 🌟 3. 메인 홈 화면 조립 (onWaterTrack 추가됨!)
// -----------------------------------------------------
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
    onFastingTimer: () -> Unit,
    onAiDiagnosis: () -> Unit,
    onWaterTrack: () -> Unit // 🌟 수분 기록 화면으로 이동하는 길 추가 완료!
) {
    val todayMeals by vm.todayMeals.collectAsState()
    val scrollState = rememberScrollState()
    var isFabExpanded by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val currentDate = remember { today.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN)) }
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
        todayRoutine.contains("하체") || todayRoutine.contains("등") || todayRoutine.contains("가슴") || todayRoutine.contains("대근육") -> "💡 대근육 운동($todayRoutine) 데이!\n운동 2시간 전에 복합 탄수화물(고구마, 오트밀 등)을 든든하게 섭취해 에너지를 꽉 채워주세요."
        todayRoutine.contains("팔") || todayRoutine.contains("어깨") || todayRoutine.contains("삼두") || todayRoutine.contains("이두") -> "💡 소근육 위주 운동($todayRoutine) 데이!\n운동 직후 흡수가 빠른 단백질 보충에 신경 써주시면 근성장에 좋습니다."
        todayRoutine.contains("유산소") -> "💡 체지방 태우는 유산소 데이!\n운동 전 가벼운 바나나 한 개 정도가 좋으며, 수분 섭취를 틈틈이 해주세요."
        todayRoutine.contains("휴식") -> "💡 오늘은 휴식일입니다.\n근육이 푹 쉬면서 자랄 수 있도록 충분한 수면과 단백질 위주의 식단을 유지하세요."
        else -> "💡 오늘도 화이팅입니다!\n운동 전후로 영양 섭취를 잊지 마세요."
    }

    val totalKcal = remember(todayMeals) { todayMeals.sumOf { it.calories } }
    val totalCarbs = remember(todayMeals) { todayMeals.sumOf { it.carbs } }
    val totalProtein = remember(todayMeals) { todayMeals.sumOf { it.protein } }
    val totalFat = remember(todayMeals) { todayMeals.sumOf { it.fat } }
    val goalKcal = goalPrefs.getKcalGoal()
    val goalCarbs = goalPrefs.getCarbsGoal()
    val goalProtein = goalPrefs.getProteinGoal()
    val goalFat = goalPrefs.getFatGoal()

    val currentWeight = goalPrefs.getUserWeight()
    val targetWeight = goalPrefs.getTargetWeight()
    val startWeight = goalPrefs.getStartWeight()

    val realisticWeightData = remember(startWeight, currentWeight) {
        val formatter = DateTimeFormatter.ofPattern("M/d")
        val diff = startWeight - currentWeight
        val estimatedDaysPassed = (abs(diff) / 0.5f * 7).toLong().coerceAtLeast(14L)
        val calculatedStartDate = today.minusDays(estimatedDaysPassed)
        val stepDays = estimatedDaysPassed / 6
        val realisticPattern = listOf(0f, 0.15f, 0.35f, 0.30f, 0.65f, 0.85f, 1f)

        List(7) { i ->
            val pointDate = if (i == 0) calculatedStartDate else if (i == 6) today else calculatedStartDate.plusDays(stepDays * i)
            val dateLabel = when (i) {
                0 -> "시작\n${pointDate.format(formatter)}"
                6 -> "오늘\n${pointDate.format(formatter)}"
                else -> pointDate.format(formatter)
            }
            val simulatedWeight = startWeight - (diff * realisticPattern[i])
            dateLabel to (Math.round(simulatedWeight * 10f) / 10f)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuon") }) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(visible = isFabExpanded) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                        // 🌟 버튼을 누르면 수분 기록 화면(onWaterTrack)으로 이동하도록 수정 완료!
                        ExtendedFloatingActionButton(
                            onClick = { isFabExpanded = false; onWaterTrack() },
                            icon = { Text("💧") },
                            text = { Text("수분 기록하기") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(onClick = { isFabExpanded = false; onGoals() }, icon = { Text("⚖️") }, text = { Text("오늘 체중 갱신") }, containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ExtendedFloatingActionButton(onClick = { isFabExpanded = false; onAddMealWithType("간식") }, icon = { Text("🍚") }, text = { Text("빠른 식사 추가") }, containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    }
                }
                FloatingActionButton(onClick = { isFabExpanded = !isFabExpanded }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                    Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, contentDescription = "기록 메뉴 열기")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "오늘: $currentDate", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onHistory, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp)) { Text("기록 보기", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center) }
                FilledTonalButton(onClick = onSavedFoods, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp)) { Text("내 음식", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center) }
                FilledTonalButton(onClick = onWeekly, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp)) { Text("주간 리포트", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center) }
            }

            FilledTonalButton(onClick = onAiDiagnosis, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("🤖 AI 인바디 / 건강검진 분석하기", fontWeight = FontWeight.Bold) }

            WeightGoalGauge(currentWeight = currentWeight, targetWeight = targetWeight, startWeight = startWeight, modifier = Modifier.fillMaxWidth())
            WeightTrendChart(weeklyData = realisticWeightData, modifier = Modifier.fillMaxWidth())

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(text = "🏋️ 오늘의 운동: $todayRoutine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
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

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${categoryCalories} kcal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                                IconButton(onClick = { onAddMealWithType(category) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Add, contentDescription = "$category 추가", tint = MaterialTheme.colorScheme.primary) }
                            }
                        }
                        if (mealsInCategory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            mealsInCategory.forEach { meal ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}