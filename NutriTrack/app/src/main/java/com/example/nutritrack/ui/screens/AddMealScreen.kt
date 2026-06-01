package com.example.nutritrack.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.example.nutritrack.data.network.HybridFoodSearchClient
import com.example.nutritrack.data.network.OffProductResult
import com.example.nutritrack.ui.viewmodel.FoodViewModel
import com.example.nutritrack.ui.viewmodel.MealViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    mealVm: MealViewModel,
    mealType: String,
    foodVm: FoodViewModel,
    scannedName: String? = null,
    scannedKcal: String? = null,
    scannedCarbs: String? = null,
    scannedProtein: String? = null,
    scannedFat: String? = null,
    onBack: () -> Unit,
    onOpenBarcode: () -> Unit
) {
    val context = LocalContext.current
    val savedFoods by foodVm.templates.collectAsState(initial = emptyList())
    // 🌟 이미 저장된 이 날의 식사 목록 가져오기
    val allMeals by mealVm.mealsForSelectedDate.collectAsState()
    val existingMeals = remember(allMeals, mealType) {
        allMeals.filter { it.type == mealType }
    }

    var foodName by remember(scannedName) { mutableStateOf(scannedName ?: "") }
    var kcal by remember(scannedKcal) { mutableStateOf(scannedKcal ?: "") }
    var carbs by remember(scannedCarbs) { mutableStateOf(scannedCarbs ?: "") }
    var protein by remember(scannedProtein) { mutableStateOf(scannedProtein ?: "") }
    var fat by remember(scannedFat) { mutableStateOf(scannedFat ?: "") }

    var showSuggestions by remember { mutableStateOf(false) }
    var showSavedOnly by remember { mutableStateOf(false) }
    var apiSearchResults by remember { mutableStateOf<List<OffProductResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // 끼니별 테마 색상 설정 (메인 화면과 통일)
    val themeColor = when(mealType) {
        "아침" -> Color(0xFFE8F5E9)
        "점심" -> Color(0xFFE3F2FD)
        "저녁" -> Color(0xFFFFF3E0)
        else -> Color(0xFFFCE4EC)
    }
    val contentColor = when(mealType) {
        "아침" -> Color(0xFF2E7D32)
        "점심" -> Color(0xFF1565C0)
        "저녁" -> Color(0xFFE65100)
        else -> Color(0xFFC2185B)
    }

    val filteredSavedFoods = remember(foodName, savedFoods) {
        if (foodName.isBlank()) savedFoods
        else savedFoods.filter { it.name.contains(foodName, ignoreCase = true) }
    }

    LaunchedEffect(foodName, showSavedOnly) {
        if (showSavedOnly || foodName.isBlank()) {
            apiSearchResults = emptyList()
            return@LaunchedEffect
        }
        delay(500)
        isSearching = true
        apiSearchResults = HybridFoodSearchClient.smartSearchByName(foodName)
        isSearching = false
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "$mealType 기록", fontWeight = FontWeight.Bold, color = contentColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = contentColor) }
                },
                actions = {
                    IconButton(onClick = onOpenBarcode) { Text("📷", fontSize = 20.sp, color = contentColor) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = themeColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 🌟 1. 검색 카드 (메인 화면 스타일, 직접 입력 지원)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { 
                                foodName = it
                                showSuggestions = true 
                                showSavedOnly = false 
                            },
                            placeholder = { Text("음식 검색...") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = contentColor) },
                            trailingIcon = { 
                                if(foodName.isNotBlank()) {
                                    IconButton(onClick = { foodName = ""; showSuggestions = false }) { 
                                        Icon(Icons.Default.Close, null) 
                                    } 
                                } 
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = contentColor,
                                unfocusedBorderColor = Color(0xFFEEEEEE)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showSavedOnly = !showSavedOnly; showSuggestions = true },
                            modifier = Modifier.size(52.dp).background(if (showSavedOnly) themeColor else Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Default.Star, null, tint = if (showSavedOnly) contentColor else Color.Gray)
                        }
                    }

                    AnimatedVisibility(visible = showSuggestions && (foodName.isNotBlank() || showSavedOnly)) {
                        Card(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isSearching && !showSavedOnly) {
                                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = contentColor) }
                            } else {
                                LazyColumn {
                                    val list = if (showSavedOnly) filteredSavedFoods.map { OffProductResult(it.name, it.calories, it.carbs, it.protein, it.fat) } else apiSearchResults
                                    items(list) { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                foodName = item.name ?: ""; kcal = (item.caloriesKcal ?: 0).toString()
                                                carbs = (item.carbsG ?: 0).toString(); protein = (item.proteinG ?: 0).toString(); fat = (item.fatG ?: 0).toString()
                                                showSuggestions = false
                                            }.padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = item.name ?: "이름 없음", fontWeight = FontWeight.Medium)
                                            Text(text = "${item.caloriesKcal} kcal", color = contentColor, fontWeight = FontWeight.Bold)
                                        }
                                        HorizontalDivider(color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🌟 2. 이미 기록된 메뉴 리스트 (삭제 기능 포함)
            if (existingMeals.isNotEmpty()) {
                Text(text = "이미 기록된 $mealType", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    existingMeals.forEach { meal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = meal.name, fontWeight = FontWeight.Bold, color = contentColor)
                                    Text(text = "${meal.calories} kcal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                IconButton(onClick = { mealVm.deleteMeal(meal) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 🌟 3. 입력 중인 메뉴 요약
            if (foodName.isNotBlank()) {
                Text(text = "새로 입력 중인 메뉴", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = foodName, fontWeight = FontWeight.ExtraBold, color = contentColor, modifier = Modifier.weight(1f))
                        Text(text = "${kcal.ifBlank { "0" }} kcal", fontWeight = FontWeight.Bold)
                        
                        // 🌟 삭제 버튼 부활!
                        IconButton(onClick = { 
                            foodName = ""; kcal = ""; carbs = ""; protein = ""; fat = "" 
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "삭제", tint = Color.Gray)
                        }
                    }
                }
            }

            // 🌟 3. 상세 영양 정보 카드 (메인 화면 스타일)
            Text(text = "영양 성분 상세", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = kcal, 
                        onValueChange = { kcal = it }, 
                        label = { Text("총 에너지 (kcal)") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = contentColor,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("탄수(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = contentColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
                        OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("단백(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = contentColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
                        OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("지방(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = contentColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { 
                        foodVm.saveAsTemplate(foodName, kcal.toIntOrNull() ?: 0, carbs.toIntOrNull() ?: 0, protein.toIntOrNull() ?: 0, fat.toIntOrNull() ?: 0)
                        Toast.makeText(context, "자주 먹는 음식에 저장되었습니다!", Toast.LENGTH_SHORT).show() 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = foodName.isNotBlank() && kcal.isNotBlank(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
                ) { Text("⭐ 자주 먹는 음식으로 등록", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = {
                        val newMeal = MealEntity(type = mealType, name = foodName, calories = kcal.toIntOrNull() ?: 0, carbs = carbs.toIntOrNull() ?: 0, protein = protein.toIntOrNull() ?: 0, fat = fat.toIntOrNull() ?: 0, createdAtMillis = System.currentTimeMillis())
                        mealVm.insertMeal(newMeal)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = foodName.isNotBlank() && kcal.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = contentColor)
                ) { Text("기록 완료하기", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
