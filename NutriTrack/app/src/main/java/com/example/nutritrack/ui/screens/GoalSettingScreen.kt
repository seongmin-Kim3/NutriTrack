package com.example.nutritrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.data.settings.GoalPrefs
import com.example.nutritrack.ui.viewmodel.HealthDiagnosisViewModel
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingScreen(
    goalPrefs: GoalPrefs,
    aiVm: HealthDiagnosisViewModel,
    onBack: () -> Unit
) {
    val userNickname = goalPrefs.getUserNickname()

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

    var isAiCalculating by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("프로필 및 목표 설정", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 🌟 1. 기본 프로필 정보 (이름님 신체정보로 명칭 변경)
            Text(text = "${userNickname}님의 신체 정보", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "기본 정보", fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedTextField(
                        value = height, 
                        onValueChange = { height = it }, 
                        label = { Text("키(cm)") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = startWeight, onValueChange = { startWeight = it }, label = { Text("시작(kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("현재(kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                    OutlinedTextField(value = targetWeight, onValueChange = { targetWeight = it }, label = { Text("목표(kg)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            }

            // 🌟 2. 활동량 및 목적
            Text(text = "라이프스타일 및 목적", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(12.dp).selectableGroup()) {
                    Text(text = "평소 활동량", style = MaterialTheme.typography.labelLarge, color = Color.Gray, modifier = Modifier.padding(16.dp))
                    activityOptions.forEach { text ->
                        Row(Modifier.fillMaxWidth().height(48.dp).selectable(selected = (text == selectedActivity), onClick = { selectedActivity = text }, role = Role.RadioButton).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = (text == selectedActivity), onClick = null)
                            Text(text = text, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF5F5F5))
                    Text(text = "다이어트 목적", style = MaterialTheme.typography.labelLarge, color = Color.Gray, modifier = Modifier.padding(16.dp))
                    goalOptions.forEach { text ->
                        Row(Modifier.fillMaxWidth().height(48.dp).selectable(selected = (text == selectedGoal), onClick = { selectedGoal = text }, role = Role.RadioButton).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = (text == selectedGoal), onClick = null)
                            Text(text = text, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }

            // 🌟 3. AI 맞춤 목표 설계
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "AI 맞춤 목표 설계", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "입력하신 정보를 바탕으로\n최적의 영양 밸런스를 계산합니다.", 
                        color = Color.White.copy(alpha = 0.8f), 
                        fontSize = 13.sp, 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (isAiCalculating) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Button(
                            onClick = {
                                isAiCalculating = true
                                aiVm.getAiNutritionGoal(
                                    height.toFloatOrNull() ?: 170f,
                                    weight.toFloatOrNull() ?: 65f,
                                    targetWeight.toFloatOrNull() ?: 60f,
                                    selectedActivity,
                                    selectedGoal
                                ) { k, c, p, f ->
                                    customKcal = k.toString()
                                    customCarbs = c.toString()
                                    customProtein = p.toString()
                                    customFat = f.toString()
                                    isAiCalculating = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("✨ AI 전문가에게 설계받기", color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 🌟 4. 상세 영양 목표
            Text(text = "상세 영양 목표", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    OutlinedTextField(
                        value = customKcal, 
                        onValueChange = { customKcal = it }, 
                        label = { Text("에너지 (kcal)") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = customCarbs, onValueChange = { customCarbs = it }, label = { Text("탄수(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = customProtein, onValueChange = { customProtein = it }, label = { Text("단백(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = customFat, onValueChange = { customFat = it }, label = { Text("지방(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                }
            }

            Button(
                onClick = {
                    goalPrefs.saveUserProfile(height.toFloatOrNull()?:170f, weight.toFloatOrNull()?:65f, targetWeight.toFloatOrNull()?:60f, selectedActivity, selectedGoal)
                    goalPrefs.setStartWeight(startWeight.toFloatOrNull()?:65f)
                    goalPrefs.saveGoals(customKcal.toIntOrNull()?:2000, customCarbs.toIntOrNull()?:250, customProtein.toIntOrNull()?:150, customFat.toIntOrNull()?:60)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(text = "모든 설정 저장하기", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
