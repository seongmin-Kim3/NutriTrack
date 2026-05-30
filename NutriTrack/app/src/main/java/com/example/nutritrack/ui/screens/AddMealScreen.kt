package com.example.nutritrack.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                title = { Text(text = "$mealType 기록", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로") }
                },
                actions = {
                    IconButton(onClick = onOpenBarcode) { Text("📷", fontSize = 20.sp) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 🌟 검색 및 즐겨찾기 섹션
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it; showSuggestions = true; showSavedOnly = false },
                            placeholder = { Text("음식 검색...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFEEEEEE))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showSavedOnly = !showSavedOnly; showSuggestions = true },
                            modifier = Modifier.size(52.dp).background(if (showSavedOnly) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (showSavedOnly) MaterialTheme.colorScheme.primary else Color.Gray)
                        }
                    }

                    AnimatedVisibility(visible = showSuggestions && (foodName.isNotBlank() || showSavedOnly)) {
                        Card(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            if (isSearching && !showSavedOnly) {
                                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                            } else {
                                LazyColumn {
                                    val list = if (showSavedOnly) filteredSavedFoods.map { OffProductResult(it.name, it.calories, it.carbs, it.protein, it.fat) } else apiSearchResults
                                    items(list) { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                foodName = item.name ?: ""; kcal = (item.caloriesKcal ?: 0).toString()
                                                carbs = (item.carbsG ?: 0).toString(); protein = (item.proteinG ?: 0).toString(); fat = (item.fatG ?: 0).toString()
                                                showSuggestions = false
                                            }.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = item.name ?: "이름 없음", fontWeight = FontWeight.Medium)
                                            Text(text = "${item.caloriesKcal} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🌟 영양 성분 입력 섹션
            Text(text = "상세 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = kcal, onValueChange = { kcal = it }, label = { Text("에너지 (kcal)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("탄수(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("단백(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("지방(g)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { foodVm.saveAsTemplate(foodName, kcal.toIntOrNull() ?: 0, carbs.toIntOrNull() ?: 0, protein.toIntOrNull() ?: 0, fat.toIntOrNull() ?: 0); Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = foodName.isNotBlank() && kcal.isNotBlank()
                ) { Text("⭐ 내 음식으로 저장", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = {
                        val newMeal = MealEntity(type = mealType, name = foodName, calories = kcal.toIntOrNull() ?: 0, carbs = carbs.toIntOrNull() ?: 0, protein = protein.toIntOrNull() ?: 0, fat = fat.toIntOrNull() ?: 0, createdAtMillis = System.currentTimeMillis())
                        mealVm.insertMeal(newMeal); onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = foodName.isNotBlank() && kcal.isNotBlank()
                ) { Text("기록 완료", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
