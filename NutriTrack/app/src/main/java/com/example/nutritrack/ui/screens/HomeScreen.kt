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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.data.settings.GoalPrefs
import com.example.nutritrack.ui.viewmodel.MealViewModel
import kotlinx.coroutines.launch
import java.time.Instant
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        // onClick = onClick  // removed to prevent crash if not handled
    ) {
        Row(
            modifier = Modifier.clickable { onClick() }.padding(16.dp),
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
            Text(text = "$calories kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
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
    onWaterTrack: () -> Unit,
    onNotificationSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val selectedDate by vm.selectedDate.collectAsState()
    val todayMeals by vm.mealsForSelectedDate.collectAsState()
    val dailyAiAdvice by aiVm.dailyAdvice.collectAsState()
    var isAiAdviceLoading by remember { mutableStateOf(false) }
    
    val waterIntake by waterVm.waterIntake.collectAsState()
    val waterGoal = waterVm.waterGoal
    val scrollState = rememberScrollState()
    var isFabExpanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val userNickname = goalPrefs.getUserNickname()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            vm.setSelectedDate(date)
                        }
                        showDatePicker = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) { Text("선택 완료", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }, modifier = Modifier.padding(bottom = 8.dp)) { 
                    Text("취소", color = Color.Gray) 
                }
            },
            shape = RoundedCornerShape(28.dp),
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    headlineContentColor = Color.Black,
                    titleContentColor = Color.Gray,
                    selectedDayContainerColor = Color(0xFF673AB7),
                    selectedDayContentColor = Color.White,
                    todayContentColor = Color(0xFF673AB7),
                    todayDateBorderColor = Color(0xFF673AB7),
                    dayContentColor = Color.DarkGray,
                    weekdayContentColor = Color.Gray
                )
            )
        }
    }

    LaunchedEffect(todayMeals, selectedDate) {
        if (todayMeals.isNotEmpty()) {
            isAiAdviceLoading = true
            val routine = goalPrefs.getRoutineForDay(selectedDate.dayOfWeek.name)
            aiVm.getDailyNutritionAdvice(todayMeals, "오늘 운동 루틴: $routine")
            isAiAdviceLoading = false
        }
    }

    val dateLabel = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("yy년 M월 d일", Locale.KOREAN))
    }
    
    val totalKcal = remember(todayMeals) { todayMeals.sumOf { it.calories } }
    val goalKcal = goalPrefs.getKcalGoal()
    val remainingKcal = (goalKcal - totalKcal).coerceAtLeast(0)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                Spacer(Modifier.height(24.dp))
                // 🌟 NutriTrack 대신 닉네임님 메뉴로 변경!
                Text(
                    "${userNickname}님의 메뉴",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF5F5F5))
                Spacer(Modifier.height(16.dp))
                
                NavigationDrawerItem(
                    label = { Text("맞춤 식단 & 레시피", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onRecipeRecommend() },
                    icon = { Icon(Icons.Default.Restaurant, null) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFE8F5E9), unselectedIconColor = Color(0xFF2E7D32), unselectedTextColor = Color(0xFF2E7D32))
                )
                NavigationDrawerItem(
                    label = { Text("AI 건강 데이터 분석", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onAiDiagnosis() },
                    icon = { Icon(Icons.Default.Analytics, null) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFE3F2FD), unselectedIconColor = Color(0xFF1565C0), unselectedTextColor = Color(0xFF1565C0))
                )
                NavigationDrawerItem(
                    label = { Text("주간 리포트 보기", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onWeekly() },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFF3E5F5), unselectedIconColor = Color(0xFF7B1FA2), unselectedTextColor = Color(0xFF7B1FA2))
                )
                NavigationDrawerItem(
                    label = { Text("목표 및 프로필 설정", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onGoals() },
                    icon = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFE0F2F1), unselectedIconColor = Color(0xFF00695C), unselectedTextColor = Color(0xFF00695C))
                )
                NavigationDrawerItem(
                    label = { Text("알림 및 루틴 설정", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNotificationSettings() },
                    icon = { Icon(Icons.Default.Notifications, null) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFFFF3E0), unselectedIconColor = Color(0xFFE65100), unselectedTextColor = Color(0xFFE65100))
                )
                NavigationDrawerItem(
                    label = { Text("내 음식 관리", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onSavedFoods() },
                    icon = { Icon(Icons.Default.Star, null) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFFCE4EC), unselectedIconColor = Color(0xFFC2185B), unselectedTextColor = Color(0xFFC2185B))
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { Text("로그아웃", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                            onLogout()
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    modifier = Modifier.padding(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFF5F5F5), unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
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
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "메뉴", modifier = Modifier.size(28.dp))
                    }
                    Text(text = dateLabel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    IconButton(onClick = { showDatePicker = true }, modifier = Modifier.background(Color.White, CircleShape).size(44.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "날짜 선택", tint = MaterialTheme.colorScheme.primary)
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
                        val isGoalMet = totalKcal in (goalKcal - 100)..(goalKcal + 100)
                        val statusText = when {
                            totalKcal == 0 -> "기록을 시작해보세요!"
                            isGoalMet -> "축하합니다! 목표를 달성했어요 🎉"
                            totalKcal > goalKcal + 100 -> "목표 칼로리를 초과했어요 ⚠️"
                            else -> "조금 더 드셔도 괜찮아요 👍"
                        }
                        val statusColor = if (isGoalMet) Color(0xFF66BB6A) else if (totalKcal > goalKcal + 100) Color(0xFFEF5350) else Color.Gray

                        Surface(color = statusColor.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.padding(bottom = 16.dp)) {
                            Text(text = statusText, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = totalKcal.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(text = "섭취", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                                CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = Color(0xFFF0F0F0), strokeWidth = 10.dp, strokeCap = StrokeCap.Round)
                                CircularProgressIndicator(progress = { (totalKcal.toFloat() / goalKcal.coerceAtLeast(1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary, strokeWidth = 10.dp, strokeCap = StrokeCap.Round)
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

                        val dayOfWeekKr = when(selectedDate.dayOfWeek) {
                            java.time.DayOfWeek.MONDAY -> "월요일"
                            java.time.DayOfWeek.TUESDAY -> "화요일"
                            java.time.DayOfWeek.WEDNESDAY -> "수요일"
                            java.time.DayOfWeek.THURSDAY -> "목요일"
                            java.time.DayOfWeek.FRIDAY -> "금요일"
                            java.time.DayOfWeek.SATURDAY -> "토요일"
                            java.time.DayOfWeek.SUNDAY -> "일요일"
                        }
                        val todayRoutine = goalPrefs.getRoutineForDay(selectedDate.dayOfWeek.name)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // 🌟 요일 루틴 옆에 운동 표시
                                    Text(text = "$dayOfWeekKr 루틴", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (todayRoutine.isBlank() || todayRoutine == "휴식") "휴식 🧘" else todayRoutine,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // AI 조언 카드
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isAiAdviceLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "AI가 식단을 분석 중입니다...", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1B5E20))
                        } else {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (todayMeals.isEmpty()) "식단을 입력하면 AI 조언을 받을 수 있습니다." else dailyAiAdvice, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1B5E20), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "식사 기록", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onHistory) { Text(text = "전체보기", style = MaterialTheme.typography.labelLarge) }
                }
                
                val categories = listOf("아침" to "🍳", "점심" to "🍱", "저녁" to "🥗", "간식" to "🍎")
                categories.forEach { (name, emoji) ->
                    val meals = todayMeals.filter { it.type == name }
                    MealCategoryCard(title = name, icon = emoji, calories = meals.sumOf { it.calories }, meals = meals, onClick = { onAddMealWithType(name) })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f).clickable { onWaterTrack() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "💧 수분", fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$waterIntake / $waterGoal ml", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Card(modifier = Modifier.weight(1f).clickable { onFastingTimer() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "⏳ 단식", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "타이머 확인", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
