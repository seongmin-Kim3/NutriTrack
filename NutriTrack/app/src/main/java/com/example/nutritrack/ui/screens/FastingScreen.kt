package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
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
import com.example.nutritrack.data.settings.FastingPrefs
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastingScreen(
    fastingPrefs: FastingPrefs,
    onBack: () -> Unit
) {
    // 상태 관리
    var isFasting by remember { mutableStateOf(fastingPrefs.isFasting()) }
    var startTime by remember { mutableStateOf(fastingPrefs.getStartTime()) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // 1초마다 시간 업데이트하는 코루틴 마법
    LaunchedEffect(isFasting) {
        while (isFasting) {
            currentTime = System.currentTimeMillis()
            delay(1000L) // 1초 대기
        }
    }

    // 시간 계산 로직
    val targetFastingTimeMs = 16 * 60 * 60 * 1000L // 16시간을 밀리초로 변환
    val elapsedMs = if (isFasting) currentTime - startTime else 0L
    val remainingMs = maxOf(0L, targetFastingTimeMs - elapsedMs)

    // 진행률 계산 (0.0 ~ 1.0)
    val progress = if (isFasting) (elapsedMs.toFloat() / targetFastingTimeMs.toFloat()).coerceIn(0f, 1f) else 0f

    // 00:00:00 포맷팅 함수
    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("간헐적 단식 타이머 (16:8)") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "뒤로가기") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 🌟 원형 프로그레스 바 영역
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // 배경 원 (회색)
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 16.dp
                )
                // 차오르는 원 (파란색)
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 16.dp
                )

                // 중앙 텍스트
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isFasting) "단식 진행 중 🔥" else "준비 완료 🍽️",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatTime(elapsedMs),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isFasting) {
                        Text(text = "남은 시간: ${formatTime(remainingMs)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 시작/종료 버튼
            Button(
                onClick = {
                    if (isFasting) {
                        // 단식 종료
                        fastingPrefs.setFasting(false)
                        isFasting = false
                    } else {
                        // 단식 시작
                        val now = System.currentTimeMillis()
                        fastingPrefs.setStartTime(now)
                        fastingPrefs.setFasting(true)
                        startTime = now
                        isFasting = true
                        currentTime = now
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFasting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isFasting) "단식 종료하기" else "16시간 단식 시작하기",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}