package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
    var isFasting by remember { mutableStateOf(fastingPrefs.isFasting()) }
    var startTime by remember { mutableStateOf(fastingPrefs.getStartTime()) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isFasting) {
        while (isFasting) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val targetFastingTimeMs = 16 * 60 * 60 * 1000L
    val elapsedMs = if (isFasting) currentTime - startTime else 0L
    val remainingMs = maxOf(0L, targetFastingTimeMs - elapsedMs)
    val progress = if (isFasting) (elapsedMs.toFloat() / targetFastingTimeMs.toFloat()).coerceIn(0f, 1f) else 0f

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("단식 타이머", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 🌟 럭셔리 서클 타이머
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFEEEEEE),
                    strokeWidth = 20.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isFasting) Color(0xFFFFA726) else MaterialTheme.colorScheme.primary,
                    strokeWidth = 20.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isFasting) "단식 중 🔥" else "단식 준비", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                    Text(text = formatTime(elapsedMs), fontSize = 54.sp, fontWeight = FontWeight.Black)
                    if (isFasting) {
                        Text(text = "남은 시간: ${formatTime(remainingMs)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFFA726))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 단식 팁", fontWeight = FontWeight.Bold)
                    Text("단식 중에는 물, 블랙 커피, 차(tea)를 충분히 섭취하는 것이 도움이 됩니다.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Button(
                onClick = {
                    if (isFasting) { fastingPrefs.setFasting(false); isFasting = false }
                    else {
                        val now = System.currentTimeMillis()
                        fastingPrefs.setStartTime(now); fastingPrefs.setFasting(true)
                        startTime = now; isFasting = true; currentTime = now
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isFasting) Color(0xFFEF5350) else MaterialTheme.colorScheme.primary)
            ) {
                Text(text = if (isFasting) "단식 종료하기" else "16시간 단식 시작", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
