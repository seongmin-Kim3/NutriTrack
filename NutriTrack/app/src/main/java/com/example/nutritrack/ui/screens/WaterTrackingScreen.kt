package com.example.nutritrack.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackingScreen(onBack: () -> Unit) {
    // 🌟 하루 목표량 (2000ml)과 현재 마신 물의 양 상태 저장
    var currentWater by remember { mutableIntStateOf(0) }
    val dailyGoal = 2000

    // 🌟 물이 차오르는 부드러운 애니메이션
    val progress by animateFloatAsState(
        targetValue = (currentWater.toFloat() / dailyGoal).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "waterProgress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("수분 기록") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 💧 1. 물방울 원형 프로그레스 바 영역
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // 회색 배경 테두리
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 20.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // 시원한 파란색 실제 물 게이지
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF29B6F6), // Water Blue Color
                    strokeWidth = 20.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // 가운데 텍스트
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💧", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$currentWater ml",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF29B6F6)
                    )
                    Text(
                        text = "/ $dailyGoal ml",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // 💧 2. 물 추가 버튼 3총사
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { currentWater += 100 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81D4FA)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) { Text("+ 100ml", color = Color.Black, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { currentWater += 250 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) { Text("+ 250ml", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { currentWater += 500 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) { Text("+ 500ml", fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 실수로 잘못 눌렀을 때를 대비한 초기화 버튼
            TextButton(onClick = { currentWater = 0 }) {
                Text("기록 초기화", color = Color.Gray)
            }
        }
    }
}