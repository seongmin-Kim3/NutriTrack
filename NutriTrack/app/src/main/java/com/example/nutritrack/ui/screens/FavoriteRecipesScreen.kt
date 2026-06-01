package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipesScreen(
    vm: RecipeViewModel,
    onBack: () -> Unit
) {
    val favorites by vm.favorites.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("즐겨찾는 식단", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "저장된 즐겨찾기가 없습니다.\nAI 추천 식단에서 별(⭐)을 눌러보세요!", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favorites) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = when(recipe.mealType) {
                                        "아침" -> "🍳"
                                        "점심" -> "🥗"
                                        "저녁" -> "🍗"
                                        else -> "🍱"
                                    }, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = recipe.mealType, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                                }
                                Text(text = "${recipe.kcal} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            // 🌟 메뉴 이름 크고 진하게
                            val cleanMenu = recipe.menuName.substringBefore("(").trim()
                            Text(text = cleanMenu, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            // 🌟 재료는 작게 표시
                            Text(text = "재료: ${recipe.ingredients}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 18.sp)
                            
                            if (recipe.description.isNotBlank() && recipe.description != recipe.menuName) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = recipe.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray, lineHeight = 22.sp)
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { vm.deleteFavorite(recipe) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
