package com.example.nutritrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- 1. 데이터 모델 정의 ---
data class Recipe(
    val name: String,
    val kcal: Int,
    val time: String,
    val ingredients: List<String>,
    val steps: List<String>
)

data class DietCategory(
    val title: String,
    val description: String,
    val emoji: String,
    val recipes: List<Recipe>
)

// --- 2. 추천 레시피 데이터 (가짜 데이터) ---
val dummyDietCategories = listOf(
    DietCategory(
        title = "건강 밸런스 식단",
        description = "탄단지가 완벽하게 조화된 영양 만점 정석 식단",
        emoji = "🥗",
        recipes = listOf(
            Recipe(
                "닭가슴살 퀴노아 볶음밥", 420, "15분",
                listOf("닭가슴살 100g", "퀴노아밥 130g", "양파 1/4개", "당근 약간", "굴소스 1스푼"),
                listOf("1. 야채와 닭가슴살을 잘게 깍둑썰기 합니다.", "2. 팬에 올리브유를 두르고 닭가슴살을 먼저 볶습니다.", "3. 야채를 넣고 볶다가 퀴노아밥과 굴소스를 넣고 강불에 빠르게 볶아냅니다.")
            ),
            Recipe(
                "두부 버섯 강정", 350, "20분",
                listOf("두부 1모", "표고버섯 2개", "전분가루", "간장 2스푼", "알룰로스 1스푼"),
                listOf("1. 두부의 물기를 빼고 깍둑썰기 하여 전분가루를 묻힙니다.", "2. 에어프라이어에 180도 15분 구워줍니다.", "3. 팬에 양념장을 끓이고 구워진 두부와 버섯을 넣어 버무립니다.")
            )
        )
    ),
    DietCategory(
        title = "단백질 폭발 고기 식단",
        description = "득근을 위한 든든한 육류 위주의 파워 식단",
        emoji = "🥩",
        recipes = listOf(
            Recipe(
                "소고기 부채살 아스파라거스 구이", 550, "15분",
                listOf("소고기 부채살 200g", "아스파라거스 4가닥", "마늘 5알", "소금, 후추, 올리브유"),
                listOf("1. 소고기에 소금, 후추, 올리브유로 마리네이드 합니다.", "2. 팬을 강하게 달구고 고기를 올려 겉면을 시어링합니다.", "3. 불을 줄이고 마늘과 아스파라거스를 함께 구워 육즙을 입힙니다.")
            ),
            Recipe(
                "돼지 안심 간장 덮밥", 480, "20분",
                listOf("돼지고기 안심 150g", "현미밥 130g", "양파 1/2개", "저염 간장 2스푼", "계란 1개"),
                listOf("1. 지방이 적은 안심을 얇게 슬라이스 합니다.", "2. 양파를 볶다가 간장 소스와 고기를 넣고 졸입니다.", "3. 현미밥 위에 얹고 가운데에 수란(또는 노른자)을 올립니다.")
            )
        )
    ),
    DietCategory(
        title = "가벼운 프레시 샐러드",
        description = "가볍고 산뜻하게 한 끼를 채우고 싶은 분들을 위한 식단",
        emoji = "🥑",
        recipes = listOf(
            Recipe(
                "연어 아보카도 포케", 380, "10분",
                listOf("생연어 100g", "아보카도 1/2개", "병아리콩 2스푼", "현미밥 100g", "스리라차 마요"),
                listOf("1. 연어와 아보카도를 한 입 크기로 깍둑썰기 합니다.", "2. 그릇에 현미밥을 깔고 준비된 재료를 예쁘게 둘러 담습니다.", "3. 스리라차 소스와 하프 마요네즈를 섞어 가볍게 뿌려 먹습니다.")
            ),
            Recipe(
                "리코타 치즈 과일 샐러드", 290, "5분",
                listOf("샐러드 채소 한 줌", "리코타 치즈 50g", "방울토마토 5개", "블루베리", "발사믹 드레싱"),
                listOf("1. 샐러드 채소를 깨끗하게 씻어 물기를 제거합니다.", "2. 접시에 채소와 과일을 담고 리코타 치즈를 스쿱으로 떠서 올립니다.", "3. 먹기 직전 발사믹 드레싱을 가볍게 뿌립니다.")
            )
        )
    )
)

// --- 3. 메인 화면 UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(onBack: () -> Unit) {
    // 화면 이동 상태 관리 (카테고리 목록 -> 레시피 목록 -> 레시피 상세)
    var selectedCategory by remember { mutableStateOf<DietCategory?>(null) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            selectedRecipe != null -> "레시피 상세"
                            selectedCategory != null -> selectedCategory!!.title
                            else -> "Nuon 맞춤 식단 추천"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // 뒤로 가기 로직
                        when {
                            selectedRecipe != null -> selectedRecipe = null // 레시피 상세 -> 레시피 목록
                            selectedCategory != null -> selectedCategory = null // 레시피 목록 -> 카테고리 목록
                            else -> onBack() // 앱 뒤로가기
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                // 3단계: 레시피 상세 화면
                selectedRecipe != null -> {
                    RecipeDetailView(recipe = selectedRecipe!!)
                }
                // 2단계: 특정 카테고리의 레시피 목록 화면
                selectedCategory != null -> {
                    RecipeListView(category = selectedCategory!!) { recipe ->
                        selectedRecipe = recipe
                    }
                }
                // 1단계: 테마별 카테고리 선택 화면
                else -> {
                    CategoryListView { category ->
                        selectedCategory = category
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryListView(onCategoryClick: (DietCategory) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(dummyDietCategories) { category ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onCategoryClick(category) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.emoji, style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = category.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = category.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeListView(category: DietCategory, onRecipeClick: (Recipe) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(category.recipes) { recipe ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onRecipeClick(recipe) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = recipe.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "⏱ ${recipe.time}  |  🔥 ${recipe.kcal} kcal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeDetailView(recipe: Recipe) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(scrollState)
    ) {
        Text(text = recipe.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChipLabel("🔥 ${recipe.kcal} kcal")
            ChipLabel("⏱ 소요시간: ${recipe.time}")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("🛒 준비 재료", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        recipe.ingredients.forEach { ingredient ->
            Text(text = "• $ingredient", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("👨‍🍳 조리 순서", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        recipe.steps.forEach { step ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Text(text = step, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ChipLabel(text: String) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}