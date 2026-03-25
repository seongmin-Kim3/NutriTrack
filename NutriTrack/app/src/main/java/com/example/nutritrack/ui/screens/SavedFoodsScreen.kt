package com.example.nutritrack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutritrack.ui.viewmodel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFoodsScreen(
    foodVm: FoodViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val savedFoods by foodVm.templates.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 음식 목록") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "뒤로") }
                }
            )
        }
    ) { padding ->
        if (savedFoods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("아직 저장된 내 음식이 없습니다.\n식사 추가 화면에서 별(⭐) 버튼을 눌러 저장해보세요!", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedFoods) { food ->
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "${food.calories} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }

                            AnimatedVisibility(visible = expanded) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    Divider()
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("탄수화물: ${food.carbs}g", style = MaterialTheme.typography.bodyMedium)
                                        Text("단백질: ${food.protein}g", style = MaterialTheme.typography.bodyMedium)
                                        Text("지방: ${food.fat}g", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 🌟 오른쪽 아래에 [삭제하기] 와 [정보 수정하기] 버튼을 나란히 배치!
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { foodVm.deleteTemplate(food) }
                                        ) {
                                            Text("삭제하기", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(onClick = { onEdit(food.id) }) {
                                            Text("정보 수정하기")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}