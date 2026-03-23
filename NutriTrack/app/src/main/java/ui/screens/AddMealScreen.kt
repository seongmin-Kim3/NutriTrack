package com.example.nutritrack.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.data.entity.MealEntity
import com.example.nutritrack.ui.viewmodel.FoodViewModel
import com.example.nutritrack.ui.viewmodel.MealViewModel

data class FoodInfo(val brand: String, val name: String, val kcal: Int, val carbs: Int, val protein: Int, val fat: Int)

val foodDatabase = listOf(
    FoodInfo("스타벅스", "아이스 아메리카노", 10, 2, 0, 0),
    FoodInfo("스타벅스", "카페라떼", 110, 10, 6, 5),
    FoodInfo("스타벅스", "바닐라 크림 콜드브루", 125, 11, 3, 8),
    FoodInfo("맥도날드", "빅맥", 583, 46, 27, 33),
    FoodInfo("맥도날드", "상하이 버거", 464, 48, 20, 20),
    FoodInfo("서브웨이", "로스트 치킨 (위트)", 320, 45, 23, 5),
    FoodInfo("서브웨이", "에그마요 (화이트)", 480, 44, 16, 26),
    FoodInfo("일반", "닭가슴살 100g", 109, 0, 23, 2),
    FoodInfo("일반", "현미밥 1공기", 320, 71, 7, 1),
    FoodInfo("맘스터치", "싸이버거", 511, 60, 22, 21),
    FoodInfo("교촌치킨", "허니콤보 (1조각)", 150, 12, 10, 7),
    FoodInfo("일반", "연어 샐러드", 250, 10, 20, 15)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    mealVm: MealViewModel,
    mealType: String,
    foodVm: FoodViewModel,
    onBack: () -> Unit,
    onOpenBarcode: () -> Unit
) {
    val context = LocalContext.current

    // 🌟 중복 검사를 위해 현재 '내 음식'에 저장된 리스트를 실시간으로 불러옵니다.
    val savedFoods by foodVm.templates.collectAsState(initial = emptyList())

    var foodName by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    var showSuggestions by remember { mutableStateOf(false) }

    val filteredFoods = remember(foodName) {
        if (foodName.isBlank()) emptyList()
        else foodDatabase.filter {
            it.name.contains(foodName, ignoreCase = true) ||
                    it.brand.contains(foodName, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$mealType 추가") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "뒤로") }
                },
                actions = {
                    IconButton(onClick = onOpenBarcode) {
                        Text("📷", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = foodName,
                onValueChange = {
                    foodName = it
                    showSuggestions = true
                },
                label = { Text("음식 또는 브랜드 검색 (예: 스타벅스)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            AnimatedVisibility(visible = showSuggestions && filteredFoods.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LazyColumn {
                        items(filteredFoods) { food ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        foodName = if (food.brand != "일반") "[${food.brand}] ${food.name}" else food.name
                                        kcal = food.kcal.toString()
                                        carbs = food.carbs.toString()
                                        protein = food.protein.toString()
                                        fat = food.fat.toString()
                                        showSuggestions = false
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = food.name, fontWeight = FontWeight.Bold)
                                    Text(text = food.brand, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text(text = "${food.kcal} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Divider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = kcal, onValueChange = { kcal = it }, label = { Text("칼로리(kcal)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("탄수화물(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("단백질(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("지방(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    // 🌟 중복 검사 로직 추가! (이미 저장된 이름과 똑같은지 확인)
                    val isDuplicate = savedFoods.any { it.name == foodName }

                    if (isDuplicate) {
                        // 중복이면 에러 알림 띄우고 저장 취소
                        Toast.makeText(context, "이미 내 음식에 저장된 메뉴입니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        // 중복이 아니면 정상적으로 저장
                        val safeKcal = kcal.toIntOrNull() ?: 0
                        val safeCarbs = carbs.toIntOrNull() ?: 0
                        val safeProtein = protein.toIntOrNull() ?: 0
                        val safeFat = fat.toIntOrNull() ?: 0

                        foodVm.saveAsTemplate(
                            name = foodName,
                            calories = safeKcal,
                            carbs = safeCarbs,
                            protein = safeProtein,
                            fat = safeFat
                        )
                        Toast.makeText(context, "내 음식에 저장되었습니다!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = foodName.isNotBlank() && kcal.isNotBlank()
            ) {
                Text("⭐ 자주 먹는 '내 음식'으로 저장", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val safeKcal = kcal.toIntOrNull() ?: 0
                    val safeCarbs = carbs.toIntOrNull() ?: 0
                    val safeProtein = protein.toIntOrNull() ?: 0
                    val safeFat = fat.toIntOrNull() ?: 0

                    val newMeal = MealEntity(
                        type = mealType,
                        name = foodName,
                        calories = safeKcal,
                        carbs = safeCarbs,
                        protein = safeProtein,
                        fat = safeFat,
                        createdAtMillis = System.currentTimeMillis()
                    )

                    mealVm.insertMeal(newMeal)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = foodName.isNotBlank() && kcal.isNotBlank()
            ) {
                Text("식사 기록 완료", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}