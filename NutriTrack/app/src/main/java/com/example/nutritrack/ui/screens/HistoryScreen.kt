package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutritrack.ui.viewmodel.MealViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    mealVm: MealViewModel,
    onBack: () -> Unit
) {
    val list by mealVm.todayMeals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("오늘 기록") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(list) { m ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(m.name, style = MaterialTheme.typography.titleMedium)
                        Text("${m.calories} kcal · 탄 ${m.carbs}g · 단 ${m.protein}g · 지 ${m.fat}g")
                        Text("식사: ${m.type}")
                    }
                }
            }
        }
    }
}
