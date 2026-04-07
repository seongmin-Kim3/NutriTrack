package com.example.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.nutritrack.ui.nav.AppNav
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🌟 파이어베이스를 확인해서 현재 로그인된 유저가 있는지 검사합니다.
        val currentUser = FirebaseAuth.getInstance().currentUser

        // 로그인되어 있으면 "home", 아니면 "login"을 첫 화면으로 지정합니다.
        val startDestination = if (currentUser != null) "home" else "login"

        setContent {
            NutriTrackTheme {
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