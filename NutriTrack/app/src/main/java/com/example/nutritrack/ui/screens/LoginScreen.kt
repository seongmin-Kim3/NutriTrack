package com.example.nutritrack.ui.screens

import android.widget.Toast
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
import com.example.nutritrack.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authVm: AuthViewModel, // 🌟 1. 파이어베이스 사령관님 입장!
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by authVm.isLoading.collectAsState() // 🌟 로딩 상태 관찰

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 에러 메시지 텍스트와 스위치
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 기존의 예쁜 Nuon 로고 디자인 유지!
            Text("Nuon", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 48.dp))

            OutlinedTextField(value = email, onValueChange = { email = it; showError = false }, label = { Text("이메일") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it; showError = false }, label = { Text("비밀번호") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)

            // 🌟 서버에서 알려주는 진짜 에러 메시지 출력
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "이메일과 비밀번호를 모두 입력해주세요."
                        showError = true
                        return@Button
                    }

                    // 🌟 2. 로컬 메모장이 아닌, 파이어베이스 사령관에게 진짜 로그인 지시!
                    authVm.login(
                        email = email,
                        password = password,
                        onSuccess = {
                            showError = false
                            Toast.makeText(context, "로그인 환영합니다!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess() // 정답이면 문을 열어줍니다!
                        },
                        onError = { errorMsg ->
                            errorMessage = errorMsg // 틀리면 파이어베이스가 준 경고창을 켭니다!
                            showError = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading, // 로딩 중일 땐 버튼 여러번 못 누르게 막기
                shape = MaterialTheme.shapes.large
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("로그인", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("처음이신가요?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onNavigateToSignUp) { Text("회원가입 하기", fontWeight = FontWeight.Bold) }
            }
        }
    }
}