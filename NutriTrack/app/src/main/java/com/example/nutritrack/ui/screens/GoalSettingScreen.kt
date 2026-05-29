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
    var startWeight by remember { mutableStateOf(goalPrefs.getStartWeight().toString()) }
    var weight by remember { mutableStateOf(goalPrefs.getUserWeight().toString()) }
    var targetWeight by remember { mutableStateOf(goalPrefs.getTargetWeight().toString()) }

    val activityOptions = listOf("거의 안 함", "가벼운 운동 (주 1~3회)", "보통 (주 3~5회)", "격렬한 운동 (매일)")
    var selectedActivity by remember { mutableStateOf(goalPrefs.getActivityLevel()) }

    val goalOptions = listOf("다이어트 (체중 감량)", "체중 유지", "벌크업 (체중 증량)")
    var selectedGoal by remember { mutableStateOf(goalPrefs.getDietGoal()) }

    // 🌟 2. 사용자가 직접 입력할 목표 영양소 상태 추가! (기존에 저장된 값을 기본으로 띄움)
    var customKcal by remember { mutableStateOf(goalPrefs.getKcalGoal().toString()) }
    var customCarbs by remember { mutableStateOf(goalPrefs.getCarbsGoal().toString()) }
    var customProtein by remember { mutableStateOf(goalPrefs.getProteinGoal().toString()) }
    var customFat by remember { mutableStateOf(goalPrefs.getFatGoal().toString()) }

    // 🌟 3. 현재 입력된 체중과 목적을 바탕으로 실시간 권장량 계산 (마법의 실시간 동기화)
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

    // 4. 루틴 상태 불러오기
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

            // --- 🌟 1. 직접 설정하는 영양 목표 입력칸 ---
            Text("나만의 맞춤 영양 목표 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = customKcal,
                onValueChange = { customKcal = it },
                label = { Text("목표 칼로리 (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = customCarbs, onValueChange = { customCarbs = it }, label = { Text("탄수화물(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = customProtein, onValueChange = { customProtein = it }, label = { Text("단백질(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = customFat, onValueChange = { customFat = it }, label = { Text("지방(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
            }

            // --- 🌟 2. 권장량 예시 박스 (참고용) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("💡 내 체중과 목적에 맞는 권장량 예시", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${recommendedKcal} kcal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Text("탄수화물 ${recCarbs}g | 단백질 ${recProtein}g | 지방 ${recFat}g", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(12.dp))
                // 센스 만점! 버튼 클릭 한 번으로 위에 있는 직접 입력칸에 권장량이 싹 채워집니다.
                Button(
                    onClick = {
                        customKcal = recommendedKcal.toString()
                        customCarbs = recCarbs.toString()
                        customProtein = recProtein.toString()
                        customFat = recFat.toString()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("예시 권장량으로 채우기")
                }
            }

            Divider()

            // --- 프로필 수정 섹션 ---
            Text("신체 정보 업데이트", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = startWeight, onValueChange = { startWeight = it }, label = { Text("시작(kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("현재(kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = targetWeight, onValueChange = { targetWeight = it }, label = { Text("목표(kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
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
                    val sw = startWeight.toFloatOrNull() ?: w

                    // 1. 프로필 정보 저장
                    goalPrefs.saveUserProfile(h, w, tw, selectedActivity, selectedGoal)
                    goalPrefs.setStartWeight(sw)

                    // 🌟 2. 이제 무조건 자동 계산이 아니라, 사용자가 '직접 입력한 수치'를 저장합니다!
                    // (만약 빈칸으로 뒀다면, 자동으로 계산된 권장량을 대신 넣어주는 안전장치 적용)
                    val finalKcal = customKcal.toIntOrNull() ?: recommendedKcal
                    val finalCarbs = customCarbs.toIntOrNull() ?: recCarbs
                    val finalProtein = customProtein.toIntOrNull() ?: recProtein
                    val finalFat = customFat.toIntOrNull() ?: recFat

                    goalPrefs.saveGoals(finalKcal, finalCarbs, finalProtein, finalFat)

                    // 3. 루틴 저장
                    goalPrefs.saveWeeklyRoutines(mon, tue, wed, thu, fri, sat, sun)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("내 맞춤 설정으로 저장하기", fontSize = MaterialTheme.typography.titleMedium.fontSize) }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}