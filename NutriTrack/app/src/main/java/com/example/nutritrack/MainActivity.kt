package com.example.nutritrack

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.nutritrack.ui.nav.AppNav
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val startDestination = if (currentUser != null) "home" else "login"

        setContent {
            NutriTrackTheme {
                // 🌟 만보기 권한 요청 (Android 10 이상)
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                    // 🌟 알림 채널 초기화 (한 번만 실행되도록 위치 이동)
                    com.example.nutritrack.ui.components.NotificationHelper.createNotificationChannel(this@MainActivity)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 미리 잘 만들어두신 AppNav(네비게이션 매니저)를 호출하고 시작점 전달!
                    AppNav(startDestination = startDestination)
                }
            }
        }
    }
}