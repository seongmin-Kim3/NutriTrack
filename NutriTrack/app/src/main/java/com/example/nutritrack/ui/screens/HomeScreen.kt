package com.example.nutritrack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.data.settings.GoalPrefs
import com.example.nutritrack.ui.viewmodel.MealViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MacroMiniItem(label: String, current: Int, goal: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp)) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = color.copy(alpha = 0.2f),
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = { (current.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
            Text(text = label.take(1), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "${current}g", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(text = "/ ${goal}g", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealCategoryCard(
    title: String,
    icon: String,
    calories: Int,
    meals: List<com.example.nutritrack.data.entity.MealEntity>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (meals.isEmpty()) {
                    Text(text = "기록된 식사 없음", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    Text(text = meals.joinToString(", ") { it.name }, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                }
            }
            Text(text = "${calories} kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MealViewModel,
    aiVm: com.example.nutritrack.ui.viewmodel.HealthDiagnosisViewModel,
    waterVm: com.example.nutritrack.ui.viewmodel.WaterViewModel,
    goalPrefs: GoalPrefs,
    onAddMealWithType: (String) -> Unit,
    onHistory: () -> Unit,
    onGoals: () -> Unit,
    onWeekly: () -> Unit,
    onSavedFoods: () -> Unit,
    onRecipeRecommend: () -> Unit,
    onFastingTimer: () -> Unit,
    onAiDiagnosis: () -> Unit,
    onWaterTrack: () -> Unit
) {
    val selectedDate by vm.selectedDate.collectAsState()
    val todayMeals by vm.mealsForSelectedDate.collectAsState()
    
    val dailyAiAdvice by aiVm.dailyAdvice.collectAsState()
    val waterIntake by waterVm.waterIntake.collectAsState()
    val waterGoal = waterVm.waterGoal
    val scrollState = rememberScrollState()
    var isFabExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        vm.setSelectedDate(date)
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val dateLabel = remember(selectedDate) {
        val today = LocalDate.now()
        when (selectedDate) {
            today -> "오늘"
            today.minusDays(1) -> "어제"
            else -> selectedDate.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN))
        }
    }
    
    val totalKcal = remember(todayMeals) { todayMeals.sumOf { it.calories } }
    val goalKcal = goalPrefs.getKcalGoal()
    val remainingKcal = (goalKcal - totalKcal).coerceAtLeast(0)

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(visible = isFabExpanded) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                        ExtendedFloatingActionButton(onClick = { isFabExpanded = false; onWaterTrack() }, icon = { Text("💧") }, text = { Text("수분") })
                        ExtendedFloatingActionButton(onClick = { isFabExpanded = false; onGoals() }, icon = { Text("⚖️") }, text = { Text("체중") })
                    }
                }
                FloatingActionButton(onClick = { isFabExpanded = !isFabExpanded }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) {
                    Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            
            // 상단 인사말 및 날짜 선택
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "안녕하세요!", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showDatePicker = true }
                    ) {
                        Text(text = dateLabel, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onSavedFoods,
                        modifier = Modifier.background(Color.White, CircleShape).size(44.dp)
                    ) { 
                        Icon(Icons.Default.Star, contentDescription = "내 음식", tint = Color(0xFFFFD700)) 
                    }
                    IconButton(
                        onClick = onWeekly,
                        modifier = Modifier.background(Color.White, CircleShape).size(44.dp)
                    ) { 
                        Icon(Icons.Default.DateRange, contentDescription = "주간 리포트", tint = MaterialTheme.colorScheme.primary) 
                    }
                }
            }

            // 1. 칼로리 메인 대시보드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // 🌟 목표 달성 상태 칩 추가
                    val isGoalMet = totalKcal in (goalKcal - 100)..(goalKcal + 100)
                    val statusText = when {
                        totalKcal == 0 -> "기록을 시작해보세요!"
                        isGoalMet -> "축하합니다! 목표를 달성했어요 🎉"
                        totalKcal > goalKcal + 100 -> "목표 칼로리를 초과했어요 ⚠️"
                        else -> "조금 더 드셔도 괜찮아요 👍"
                    }
                    val statusColor = if (isGoalMet) Color(0xFF66BB6A) else if (totalKcal > goalKcal + 100) Color(0xFFEF5350) else Color.Gray

                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = totalKcal.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = "섭취", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                            CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = Color(0xFFF0F0F0), strokeWidth = 10.dp, strokeCap = StrokeCap.Round)
                            CircularProgressIndicator(progress = { (totalKcal.toFloat() / goalKcal).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary, strokeWidth = 10.dp, strokeCap = StrokeCap.Round)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = remainingKcal.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                                Text(text = "남음", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = goalKcal.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = "목표", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFFF5F5F5))
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        MacroMiniItem("탄수화물", todayMeals.sumOf { it.carbs }, goalPrefs.getCarbsGoal(), Color(0xFFFFA726))
                        MacroMiniItem("단백질", todayMeals.sumOf { it.protein }, goalPrefs.getProteinGoal(), Color(0xFF66BB6A))
                        MacroMiniItem("지방", todayMeals.sumOf { it.fat }, goalPrefs.getFatGoal(), Color(0xFFEF5350))
                    }
                }
            }

            // AI 조언
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = dailyAiAdvice, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1B5E20))
                }
            }

            // AI 레시피 추천 카드
            OutlinedCard(
                onClick = onRecipeRecommend,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👨‍🍳", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "맞춤 식단 & 레시피", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(text = "AI가 제안하는 오늘 최고의 메뉴", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                }
            }

            // 2. 식사 카테고리
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "식사 기록", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onHistory) {
                    Text(text = "전체보기", style = MaterialTheme.typography.labelLarge)
                }
            }
            
            val categories = listOf("아침" to "🍳", "점심" to "🍱", "저녁" to "🥗", "간식" to "🍎")
            categories.forEach { (name, emoji) ->
                val meals = todayMeals.filter { it.type == name }
                MealCategoryCard(
                    title = name,
                    icon = emoji,
                    calories = meals.sumOf { it.calories },
                    meals = meals,
                    onClick = { onAddMealWithType(name) }
                )
            }

            // 3. 수분 및 단식
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f).clickable { onWaterTrack() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "💧 수분", fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$waterIntake / $waterGoal ml", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).clickable { onFastingTimer() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "⏳ 단식", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "타이머 확인", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // AI 진단 버튼
            Button(
                onClick = onAiDiagnosis,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI 인바디 분석기 실행", fontWeight = FontWeight.Bold)
            }

            // 목표 설정 버튼
            OutlinedButton(
                onClick = onGoals,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("목표 및 프로필 설정 수정", style = MaterialTheme.typography.labelLarge)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
