package com.example.nutritrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritrack.data.settings.GoalPrefs
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingScreen(
    goalPrefs: GoalPrefs,
    onBack: () -> Unit
) {
    // 1. 프로필 상태 불러오기
    var height by remember { mutableStateOf(goalPrefs.getUserHeight().toString()) }
    var weight by remember { mutableStateOf(goalPrefs.getUserWeight().toString()) }
    var targetWeight by remember { mutableStateOf(goalPrefs.getTargetWeight().toString()) }

    val activityOptions = listOf("거의 안 함", "가벼운 운동 (주 1~3회)", "보통 (주 3~5회)", "격렬한 운동 (매일)")
    var selectedActivity by remember { mutableStateOf(goalPrefs.getActivityLevel()) }

    val goalOptions = listOf("다이어트 (체중 감량)", "체중 유지", "벌크업 (체중 증량)")
    var selectedGoal by remember { mutableStateOf(goalPrefs.getDietGoal()) }

    // 2. 루틴 상태 불러오기
    var mon by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.MONDAY.name)) }
    var tue by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.TUESDAY.name)) }
    var wed by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.WEDNESDAY.name)) }
    var thu by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.THURSDAY.name)) }
    var fri by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.FRIDAY.name)) }
    var sat by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.SATURDAY.name)) }
    var sun by remember { mutableStateOf(goalPrefs.getRoutineForDay(DayOfWeek.SUNDAY.name)) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("목표 및 프로필 설정") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 자동 계산된 결과 표시 영역 ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("현재 자동 설정된 하루 목표량", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${goalPrefs.getKcalGoal()} kcal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("탄수화물 ${goalPrefs.getCarbsGoal()}g | 단백질 ${goalPrefs.getProteinGoal()}g | 지방 ${goalPrefs.getFatGoal()}g", style = MaterialTheme.typography.bodyMedium)
                Text("* 아래에서 체중이나 목적을 변경하고 저장하면 새로 계산됩니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top=8.dp))
            }

            Divider()

            // --- 프로필 수정 섹션 ---
            Text("신체 정보 업데이트", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("현재 체중(kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = targetWeight, onValueChange = { targetWeight = it }, label = { Text("목표 체중(kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
            }

            Text("운동량 및 목적", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Column(Modifier.selectableGroup()) {
                activityOptions.forEach { text ->
                    Row(Modifier.fillMaxWidth().height(40.dp).selectable(selected = (text == selectedActivity), onClick = { selectedActivity = text }, role = Role.RadioButton), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (text == selectedActivity), onClick = null)
                        Text(text = text, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(Modifier.selectableGroup()) {
                goalOptions.forEach { text ->
                    Row(Modifier.fillMaxWidth().height(40.dp).selectable(selected = (text == selectedGoal), onClick = { selectedGoal = text }, role = Role.RadioButton), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (text == selectedGoal), onClick = null)
                        Text(text = text, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- 주간 운동 루틴 섹션 ---
            Text("주간 운동 루틴", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(mon, { mon = it }, label = { Text("월요일") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(tue, { tue = it }, label = { Text("화요일") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(wed, { wed = it }, label = { Text("수요일") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(thu, { thu = it }, label = { Text("목요일") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(fri, { fri = it }, label = { Text("금요일") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sat, { sat = it }, label = { Text("토요일") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sun, { sun = it }, label = { Text("일요일") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(8.dp))

            // --- 통합 저장 버튼 ---
            Button(
                onClick = {
                    val h = height.toFloatOrNull() ?: 170f
                    val w = weight.toFloatOrNull() ?: 65f
                    val tw = targetWeight.toFloatOrNull() ?: w

                    // 1. 프로필 정보 저장
                    goalPrefs.saveUserProfile(h, w, tw, selectedActivity, selectedGoal)

                    // 2. 바뀐 체중으로 목표 재계산 및 저장
                    val bmr = w * 24f
                    val activityMultiplier = when(selectedActivity) {
                        "거의 안 함" -> 1.2f
                        "가벼운 운동 (주 1~3회)" -> 1.375f
                        "보통 (주 3~5회)" -> 1.55f
                        else -> 1.725f
                    }
                    val tdee = bmr * activityMultiplier
                    val targetKcal = when(selectedGoal) {
                        "다이어트 (체중 감량)" -> tdee - 500
                        "벌크업 (체중 증량)" -> tdee + 500
                        else -> tdee
                    }
                    val kcal = targetKcal.toInt()
                    val carbs = (kcal * 0.5 / 4).toInt()
                    val protein = (kcal * 0.3 / 4).toInt()
                    val fat = (kcal * 0.2 / 9).toInt()

                    goalPrefs.saveGoals(kcal, carbs, protein, fat)

                    // 3. 루틴 저장
                    goalPrefs.saveWeeklyRoutines(mon, tue, wed, thu, fri, sat, sun)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("설정 저장 및 자동 계산", fontSize = MaterialTheme.typography.titleMedium.fontSize) }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}