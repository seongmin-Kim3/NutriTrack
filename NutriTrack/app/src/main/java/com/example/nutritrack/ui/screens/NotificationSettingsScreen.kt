package com.example.nutritrack.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.data.settings.NotificationPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { NotificationPrefs(context) }
    
    var waterEnabled by remember { mutableStateOf(prefs.isWaterReminderEnabled()) }
    var fastingEnabled by remember { mutableStateOf(prefs.isFastingReminderEnabled()) }
    var logEnabled by remember { mutableStateOf(prefs.isLogReminderEnabled()) }

    // 알림 권한 요청 런처 (안드로이드 13 이상)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // 권한 거부 시 스위치 다시 끔
            waterEnabled = false
            fastingEnabled = false
            logEnabled = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("알림 및 루틴 가이드", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "자동 알림 설정", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            // 💧 수분 알림 카드
            NotificationToggleCard(
                title = "수분 섭취 알림",
                description = "2시간마다 물 마실 시간을 알려드려요.",
                icon = "💧",
                checked = waterEnabled,
                onCheckedChange = { 
                    if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    waterEnabled = it
                    prefs.setWaterReminder(it)
                }
            )

            // ⏳ 단식 종료 알림 카드
            NotificationToggleCard(
                title = "단식 루틴 알림",
                description = "단식 종료 및 시작 시간을 알려드려요.",
                icon = "⏳",
                checked = fastingEnabled,
                onCheckedChange = { 
                    fastingEnabled = it
                    prefs.setFastingReminder(it)
                }
            )

            // 📝 식사 기록 독려 알림 카드
            NotificationToggleCard(
                title = "식사 기록 독려",
                description = "저녁 9시에 오늘의 기록을 챙겨드려요.",
                icon = "📝",
                checked = logEnabled,
                onCheckedChange = { 
                    logEnabled = it
                    prefs.setLogReminder(it)
                }
            )
        }
    }
}

@Composable
fun NotificationToggleCard(
    title: String,
    description: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}
