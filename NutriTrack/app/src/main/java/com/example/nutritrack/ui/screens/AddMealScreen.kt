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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.nutritrack.data.network.OffProductResult
import com.example.nutritrack.data.network.OpenFoodFactsClient
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

    var foodName by remember(scannedName) { mutableStateOf(scannedName ?: "") }
    var kcal by remember(scannedKcal) { mutableStateOf(scannedKcal ?: "") }
    var carbs by remember(scannedCarbs) { mutableStateOf(scannedCarbs ?: "") }
    var protein by remember(scannedProtein) { mutableStateOf(scannedProtein ?: "") }
    var fat by remember(scannedFat) { mutableStateOf(scannedFat ?: "") }

    var showSuggestions by remember { mutableStateOf(false) }
    var showSavedOnly by remember { mutableStateOf(false) }

    var apiSearchResults by remember { mutableStateOf<List<OffProductResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val filteredSavedFoods = remember(foodName, savedFoods) {
        if (foodName.isBlank()) savedFoods
        else savedFoods.filter { it.name.contains(foodName, ignoreCase = true) }
    }

    // 마법의 디바운스(Debounce) 로직
    LaunchedEffect(foodName, showSavedOnly) {
        if (showSavedOnly) return@LaunchedEffect
        if (foodName.isBlank()) {
            apiSearchResults = emptyList()
            return@LaunchedEffect
        }

        delay(500)
        isSearching = true
        apiSearchResults = OpenFoodFactsClient.searchByName(foodName)
        isSearching = false
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = {
                        foodName = it
                        showSuggestions = true
                        showSavedOnly = false
                    },
                    label = { Text(if (showSavedOnly) "내 음식 리스트" else "음식 또는 브랜드 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        showSavedOnly = !showSavedOnly
                        showSuggestions = true
                    },
                    modifier = Modifier.background(
                        if (showSavedOnly) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "내 음식",
                        tint = if (showSavedOnly) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }

            // 🌟 수정된 부분: 결과가 0개라도 박스가 꺼지지 않고 유지됩니다!
            AnimatedVisibility(visible = showSuggestions && (foodName.isNotBlank() || showSavedOnly)) {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (isSearching && !showSavedOnly) {
                        // ⏳ 1. 검색 중일 때 (빙글빙글 로딩)
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    } else if (!showSavedOnly && apiSearchResults.isEmpty()) {
                        // 🌟 2. 한국어 등으로 검색해서 서버에 결과가 없을 때 (안내 문구 띄우기)
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🤔", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("검색 결과가 없습니다.", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("영어로 검색하거나, 아래에 직접 입력해주세요!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    } else {
                        // 3. 정상적으로 결과가 나왔을 때 리스트 보여주기
                        LazyColumn {
                            if (showSavedOnly) {
                                // ⭐ 내 음식 리스트
                                items(filteredSavedFoods) { savedItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                foodName = savedItem.name
                                                kcal = savedItem.calories.toString()
                                                carbs = savedItem.carbs.toString()
                                                protein = savedItem.protein.toString()
                                                fat = savedItem.fat.toString()
                                                showSuggestions = false
                                                showSavedOnly = false
                                            }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = savedItem.name, fontWeight = FontWeight.Bold)
                                            Text(text = "탄 ${savedItem.carbs} 단 ${savedItem.protein} 지 ${savedItem.fat}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "${savedItem.calories} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                                            Icon(Icons.Default.Star, contentDescription = "저장됨", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                                }
                            } else {
                                // 🌐 오픈 API 검색 결과 리스트
                                items(apiSearchResults) { apiItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                foodName = apiItem.name ?: "이름 없음"
                                                kcal = (apiItem.caloriesKcal ?: 0).toString()
                                                carbs = (apiItem.carbsG ?: 0).toString()
                                                protein = (apiItem.proteinG ?: 0).toString()
                                                fat = (apiItem.fatG ?: 0).toString()
                                                showSuggestions = false
                                            }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = apiItem.name ?: "이름 없음", fontWeight = FontWeight.Bold)
                                            Text(text = "탄 ${apiItem.carbsG ?: 0} 단 ${apiItem.proteinG ?: 0} 지 ${apiItem.fatG ?: 0}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Text(text = "${apiItem.caloriesKcal ?: 0} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                                }
                            }
                        }
                    }
                }
            }

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
                    val isDuplicate = savedFoods.any { it.name == foodName }
                    if (isDuplicate) {
                        Toast.makeText(context, "이미 내 음식에 저장된 메뉴입니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        foodVm.saveAsTemplate(foodName, kcal.toIntOrNull() ?: 0, carbs.toIntOrNull() ?: 0, protein.toIntOrNull() ?: 0, fat.toIntOrNull() ?: 0)
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
                    val newMeal = MealEntity(
                        type = mealType,
                        name = foodName,
                        calories = kcal.toIntOrNull() ?: 0,
                        carbs = carbs.toIntOrNull() ?: 0,
                        protein = protein.toIntOrNull() ?: 0,
                        fat = fat.toIntOrNull() ?: 0,
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