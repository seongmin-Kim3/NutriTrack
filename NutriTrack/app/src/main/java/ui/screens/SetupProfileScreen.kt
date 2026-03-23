package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritrack.data.settings.GoalPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupProfileScreen(
    goalPrefs: GoalPrefs,
    onSetupComplete: () -> Unit
) {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") } // 🌟 목표 체중 변수 추가

    val activityOptions = listOf("거의 안 함", "가벼운 운동 (주 1~3회)", "보통 (주 3~5회)", "격렬한 운동 (매일)")
    var selectedActivity by remember { mutableStateOf(activityOptions[1]) }

    val goalOptions = listOf("다이어트 (체중 감량)", "체중 유지", "벌크업 (체중 증량)")
    var selectedGoal by remember { mutableStateOf(goalOptions[1]) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuon 시작하기") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "환영합니다! 🎉\n목표 체중에 맞는 맞춤형 식단을\n자동으로 계산해 드릴게요.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = height, onValueChange = { height = it }, label = { Text("키 (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = weight, onValueChange = { weight = it }, label = { Text("현재 체중 (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = targetWeight, onValueChange = { targetWeight = it }, label = { Text("목표 체중 (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("평소 운동량은 어느 정도인가요?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Column(Modifier.selectableGroup()) {
                activityOptions.forEach { text ->
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).selectable(
                            selected = (text == selectedActivity), onClick = { selectedActivity = text }, role = Role.RadioButton
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (text == selectedActivity), onClick = null)
                        Text(text = text, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Nuon을 사용하는 목적이 무엇인가요?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Column(Modifier.selectableGroup()) {
                goalOptions.forEach { text ->
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).selectable(
                            selected = (text == selectedGoal), onClick = { selectedGoal = text }, role = Role.RadioButton
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (text == selectedGoal), onClick = null)
                        Text(text = text, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val h = height.toFloatOrNull() ?: 170f
                    val w = weight.toFloatOrNull() ?: 65f
                    val tw = targetWeight.toFloatOrNull() ?: w

                    goalPrefs.saveUserProfile(h, w, tw, selectedActivity, selectedGoal)

                    // 🌟 맞춤형 식단 자동 계산 로직 (기초대사량 -> 활동량 -> 목표 반영 -> 탄단지)
                    val bmr = w * 24f // 간단 기초대사량
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
                    val carbs = (kcal * 0.5 / 4).toInt() // 탄수화물 50%
                    val protein = (kcal * 0.3 / 4).toInt() // 단백질 30%
                    val fat = (kcal * 0.2 / 9).toInt() // 지방 20%

                    goalPrefs.saveGoals(kcal, carbs, protein, fat) // 계산된 목표 자동 저장!
                    onSetupComplete()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = height.isNotBlank() && weight.isNotBlank() && targetWeight.isNotBlank()
            ) {
                Text("내 정보 저장하고 시작하기", fontSize = MaterialTheme.typography.titleMedium.fontSize)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}