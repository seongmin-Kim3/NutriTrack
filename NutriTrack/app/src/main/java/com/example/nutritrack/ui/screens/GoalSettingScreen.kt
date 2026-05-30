package com.example.nutritrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.data.settings.GoalPrefs
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingScreen(
    goalPrefs: GoalPrefs,
    onBack: () -> Unit
) {
    var height by remember { mutableStateOf(goalPrefs.getUserHeight().toString()) }
    var weight by remember { mutableStateOf(goalPrefs.getUserWeight().toString()) }
    var targetWeight by remember { mutableStateOf(goalPrefs.getTargetWeight().toString()) }
    var startWeight by remember { mutableStateOf(goalPrefs.getStartWeight().toString()) }

    val activityOptions = listOf("거의 안 함", "가벼운 운동 (주 1~3회)", "보통 (주 3~5회)", "격렬한 운동 (매일)")
    var selectedActivity by remember { mutableStateOf(goalPrefs.getActivityLevel()) }

    val goalOptions = listOf("다이어트 (체중 감량)", "체중 유지", "벌크업 (체중 증량)")
    var selectedGoal by remember { mutableStateOf(goalPrefs.getDietGoal()) }

    var customKcal by remember { mutableStateOf(goalPrefs.getKcalGoal().toString()) }
    var customCarbs by remember { mutableStateOf(goalPrefs.getCarbsGoal().toString()) }
    var customProtein by remember { mutableStateOf(goalPrefs.getProteinGoal().toString()) }
    var customFat by remember { mutableStateOf(goalPrefs.getFatGoal().toString()) }

    val recommendedKcal = remember(weight, selectedActivity, selectedGoal) {
        val w = weight.toFloatOrNull() ?: 65f
        val bmr = w * 24f
        val activityMultiplier = when(selectedActivity) {
            "거의 안 함" -> 1.2f
            "가벼운 운동 (주 1~3회)" -> 1.375f
            "보통 (주 3~5회)" -> 1.55f
            else -> 1.725f
        }
        val tdee = bmr * activityMultiplier
        when(selectedGoal) {
            "다이어트 (체중 감량)" -> tdee - 500f
            "벌크업 (체중 증량)" -> tdee + 500f
            else -> tdee
        }.toInt()
    }
    val recCarbs = (recommendedKcal * 0.5 / 4).toInt()
    val recProtein = (recommendedKcal * 0.3 / 4).toInt()
    val recFat = (recommendedKcal * 0.2 / 9).toInt()

    var mon by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.MONDAY.name)) }
    var tue by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.TUESDAY.name)) }
    var wed by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.WEDNESDAY.name)) }
    var thu by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.THURSDAY.name)) }
    var fri by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.FRIDAY.name)) }
    var sat by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.SATURDAY.name)) }
    var sun by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.SUNDAY.name)) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("목표 및 프로필", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 🌟 1. 영양 목표 설정 카드
            Text(text = "영양 목표", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = customKcal, onValueChange = { customKcal = it }, label = { Text("하루 칼로리 (kcal)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = customCarbs, onValueChange = { customCarbs = it }, label = { Text("탄수(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = customProtein, onValueChange = { customProtein = it }, label = { Text("단백(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = customFat, onValueChange = { customFat = it }, label = { Text("지방(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "AI 권장: ${recommendedKcal}kcal (탄 $recCarbs 단 $recProtein 지 $recFat)", style = MaterialTheme.typography.labelSmall)
                            TextButton(onClick = { customKcal = recommendedKcal.toString(); customCarbs = recCarbs.toString(); customProtein = recProtein.toString(); customFat = recFat.toString() }) {
                                Text("권장량 자동 채우기 ✨", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 🌟 2. 신체 정보 카드
            Text(text = "신체 정보", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("키(cm)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("현재(kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = startWeight, onValueChange = { startWeight = it }, label = { Text("시작(kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = targetWeight, onValueChange = { targetWeight = it }, label = { Text("목표(kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                }
            }

            // 🌟 3. 운동 루틴 카드
            Text(text = "운동 루틴", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val days = listOf("월" to mon, "화" to tue, "수" to wed, "목" to thu, "금" to fri, "토" to sat, "일" to sun)
                    val setters = listOf({v:String -> mon=v}, {v:String -> tue=v}, {v:String -> wed=v}, {v:String -> thu=v}, {v:String -> fri=v}, {v:String -> sat=v}, {v:String -> sun=v})
                    
                    days.forEachIndexed { i, day ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(text = day.first, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                            OutlinedTextField(value = day.second, onValueChange = setters[i], modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), placeholder = { Text("예: 하체 운동", fontSize = 12.sp) }, singleLine = true)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val h = height.toFloatOrNull() ?: 170f
                    val w = weight.toFloatOrNull() ?: 65f
                    val tw = targetWeight.toFloatOrNull() ?: w
                    val sw = startWeight.toFloatOrNull() ?: w
                    goalPrefs.saveUserProfile(h, w, tw, selectedActivity, selectedGoal)
                    goalPrefs.setStartWeight(sw)
                    goalPrefs.saveGoals(customKcal.toIntOrNull() ?: recommendedKcal, customCarbs.toIntOrNull() ?: recCarbs, customProtein.toIntOrNull() ?: recProtein, customFat.toIntOrNull() ?: recFat)
                    goalPrefs.saveWeeklyRoutines(mon, tue, wed, thu, fri, sat, sun)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "설정 저장하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
