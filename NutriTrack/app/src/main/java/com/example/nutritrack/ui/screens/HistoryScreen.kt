package com.example.nutritrack.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.NuonApp
import com.example.nutritrack.ui.viewmodel.MealViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// 🌟 API 26 가짜 경고 입막음! (Desugaring이 켜져 있어서 안전합니다)
@android.annotation.SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    mealVm: MealViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val goalPrefs = remember { (context.applicationContext as NuonApp).container.goalPrefs }

    val selectedDate by mealVm.selectedDate.collectAsState()
    val dailyMeals by mealVm.mealsForSelectedDate.collectAsState()

    var currentMonth by remember { mutableStateOf(YearMonth.from(LocalDate.now())) }

    val routine = remember(selectedDate) {
        goalPrefs.getRoutineForDay(selectedDate.dayOfWeek.name)
    }

    val totalKcal = dailyMeals.sumOf { it.calories }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("식단 다이어리") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "뒤로") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CalendarWidget(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onMonthChange = { currentMonth = it },
                onDateSelected = { mealVm.setSelectedDate(it) }
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${totalKcal} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏋️", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text("이날의 운동 계획", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                                Text(routine, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }

                if (dailyMeals.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text("이날 기록된 식단이 없습니다.", color = Color.Gray)
                        }
                    }
                } else {
                    items(dailyMeals, key = { it.id }) { meal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = meal.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text(text = meal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = "탄 ${meal.carbs}g · 단 ${meal.protein}g · 지 ${meal.fat}g", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "${meal.calories} kcal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                                    IconButton(onClick = { mealVm.deleteMeal(meal.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// 🌟 여기도 API 26 가짜 경고 입막음!
@android.annotation.SuppressLint("NewApi")
@Composable
fun CalendarWidget(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, "이전 달")
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.KeyboardArrowRight, "다음 달")
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (day == "일") Color.Red else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalCells = ((daysInMonth + firstDayOfWeek) / 7 + 1) * 7

            Column {
                for (row in 0 until (totalCells / 7)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val dayIndex = (row * 7) + col
                            val dayNumber = dayIndex - firstDayOfWeek + 1

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayNumber)
                                    val isSelected = date == selectedDate
                                    val isToday = date == LocalDate.now()

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { onDateSelected(date) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                col == 0 -> Color.Red
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
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