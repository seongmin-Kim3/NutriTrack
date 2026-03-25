package com.example.nutritrack.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    // 💡 내부 저장소를 불러오는 코드
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 💡 에러 메시지를 띄울지 말지 결정하는 스위치
    var showError by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nuon", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 48.dp))

            OutlinedTextField(value = email, onValueChange = { email = it; showError = false }, label = { Text("이메일") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it; showError = false }, label = { Text("비밀번호") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)

            // 🌟 에러 발생 시 띄워줄 빨간 경고 문구
            if (showError) {
                Text(
                    text = "가입되지 않은 이메일이거나 비밀번호가 틀렸습니다.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // 1. 폰에 저장된 진짜 이메일과 비밀번호를 꺼내옵니다.
                    val savedEmail = authPrefs.getString("email", "")
                    val savedPass = authPrefs.getString("password", "")

                    // 2. 입력한 정보와 저장된 정보가 완벽하게 일치하는지 검사합니다!
                    if (email == savedEmail && password == savedPass && savedEmail!!.isNotBlank()) {
                        showError = false
                        onLoginSuccess() // 정답이면 문을 열어줍니다!
                    } else {
                        showError = true // 틀리면 빨간 경고창을 켭니다!
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp), enabled = email.isNotBlank() && password.isNotBlank(), shape = MaterialTheme.shapes.large
            ) { Text("로그인", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("처음이신가요?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onNavigateToSignUp) { Text("회원가입 하기", fontWeight = FontWeight.Bold) }
            }
        }
    }
}